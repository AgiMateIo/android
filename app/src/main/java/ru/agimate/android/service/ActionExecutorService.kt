package ru.agimate.android.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import io.github.centrifugal.centrifuge.Client
import io.github.centrifugal.centrifuge.ConnectedEvent
import io.github.centrifugal.centrifuge.ConnectionTokenEvent
import io.github.centrifugal.centrifuge.ConnectionTokenGetter
import io.github.centrifugal.centrifuge.DisconnectedEvent
import io.github.centrifugal.centrifuge.DuplicateSubscriptionException
import io.github.centrifugal.centrifuge.ErrorEvent
import io.github.centrifugal.centrifuge.EventListener
import io.github.centrifugal.centrifuge.Options
import io.github.centrifugal.centrifuge.PublicationEvent
import io.github.centrifugal.centrifuge.SubscribedEvent
import io.github.centrifugal.centrifuge.Subscription
import io.github.centrifugal.centrifuge.SubscriptionEventListener
import io.github.centrifugal.centrifuge.SubscriptionOptions
import io.github.centrifugal.centrifuge.SubscriptionTokenEvent
import io.github.centrifugal.centrifuge.SubscriptionTokenGetter
import io.github.centrifugal.centrifuge.TokenCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import ru.agimate.android.MainActivity
import ru.agimate.android.R
import ru.agimate.android.data.model.ActionTask
import ru.agimate.android.data.model.ActionType
import ru.agimate.android.data.remote.api.TriggerApi
import ru.agimate.android.data.remote.dto.ActionCapability
import ru.agimate.android.data.remote.dto.CentrifugoTokenRequest
import ru.agimate.android.data.remote.dto.LinkDeviceRequest
import ru.agimate.android.data.remote.dto.TriggerCapability
import ru.agimate.android.domain.repository.IActionStateRepository
import ru.agimate.android.domain.repository.ISettingsRepository
import ru.agimate.android.service.actions.base.IActionHandler
import ru.agimate.android.service.actions.handlers.NotificationActionHandler
import ru.agimate.android.service.actions.handlers.TtsActionHandler
import ru.agimate.android.util.Logger

class ActionExecutorService : Service() {

    private val actionStateRepo: IActionStateRepository by inject()
    private val settingsRepo: ISettingsRepository by inject()
    private val triggerApi: TriggerApi by inject()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val actionHandlers = mutableMapOf<ActionType, IActionHandler>()

    private val json = Json { ignoreUnknownKeys = true }

    private var centrifugeClient: Client? = null
    private var centrifugeSubscription: Subscription? = null

    // Centrifugo token state
    private var connectionToken: String? = null
    private var subscriptionToken: String? = null
    private var channel: String? = null

    override fun onCreate() {
        super.onCreate()
        Logger.i("ActionExecutorService created")

        initializeHandlers()
        startForeground(NOTIFICATION_ID, createNotification())
        connectToCentrifugo()
        observeActionStates()
    }

    private fun initializeHandlers() {
        actionHandlers[ActionType.TTS] = TtsActionHandler(this)
        actionHandlers[ActionType.NOTIFICATION] = NotificationActionHandler(this)

        actionHandlers.values.forEach { it.initialize() }
    }

    private fun connectToCentrifugo() {
        serviceScope.launch {
            try {
                val settings = settingsRepo.settingsFlow.first()
                val deviceId = settings.deviceId
                val serverUrl = settings.serverUrl.trimEnd('/')

                // Step 1: Link device with server (include capabilities)
                Logger.i("Linking device: deviceId=$deviceId")
                val triggers = mapOf(
                    "android.trigger.call.incoming" to TriggerCapability(params = listOf("phoneNumber", "timestamp")),
                    "android.trigger.battery.low" to TriggerCapability(params = listOf("batteryLevel", "threshold", "timestamp")),
                    "android.trigger.wifi.connected" to TriggerCapability(params = listOf("ssid", "bssid", "connected", "signalStrength", "timestamp")),
                    "android.trigger.wifi.disconnected" to TriggerCapability(params = listOf("ssid", "bssid", "connected", "signalStrength", "timestamp")),
                    "android.trigger.shake.detected" to TriggerCapability(params = listOf("acceleration", "x", "y", "z", "timestamp"))
                )
                val actions = mapOf(
                    "android.action.notification.show" to ActionCapability(params = listOf("title", "message")),
                    "android.action.tts.speak" to ActionCapability(params = listOf("text"))
                )
                val linkResponse = triggerApi.linkDevice(
                    LinkDeviceRequest(
                        deviceId = deviceId,
                        deviceName = Build.MODEL,
                        deviceOs = "android",
                        triggers = triggers,
                        actions = actions
                    )
                )
                if (!linkResponse.isSuccessful) {
                    Logger.e("Failed to link device: ${linkResponse.code()} ${linkResponse.message()}")
                    return@launch
                }
                Logger.i("Device linked successfully")

                // Step 2: Fetch Centrifugo tokens
                Logger.i("Fetching Centrifugo tokens...")
                val tokenResponse = triggerApi.getCentrifugoToken(
                    CentrifugoTokenRequest(deviceId = deviceId)
                )
                if (!tokenResponse.isSuccessful || tokenResponse.body() == null) {
                    Logger.e("Failed to fetch Centrifugo tokens: ${tokenResponse.code()} ${tokenResponse.message()}")
                    return@launch
                }
                val tokenData = tokenResponse.body()!!.response
                connectionToken = tokenData.connectionToken
                subscriptionToken = tokenData.subscriptionToken
                channel = tokenData.channel
                Logger.i("Centrifugo tokens received for channel: $channel")

                // Step 3: Derive WebSocket URL
                val wsUrl = if (tokenData.wsUrl != null) {
                    tokenData.wsUrl
                } else {
                    deriveWsUrl(serverUrl)
                }
                Logger.i("Centrifugo WebSocket URL: $wsUrl")

                // Step 4: Create client with token auth
                val options = Options()
                options.token = connectionToken
                options.tokenGetter = object : ConnectionTokenGetter() {
                    override fun getConnectionToken(event: ConnectionTokenEvent, cb: TokenCallback) {
                        serviceScope.launch {
                            try {
                                val refreshResponse = triggerApi.getCentrifugoToken(
                                    CentrifugoTokenRequest(deviceId = deviceId)
                                )
                                if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                                    val data = refreshResponse.body()!!.response
                                    connectionToken = data.connectionToken
                                    subscriptionToken = data.subscriptionToken
                                    cb.Done(null, data.connectionToken)
                                } else {
                                    cb.Done(Exception("Failed to refresh connection token"), null)
                                }
                            } catch (e: Exception) {
                                Logger.e("Error refreshing connection token: ${e.message}")
                                cb.Done(e, null)
                            }
                        }
                    }
                }

                centrifugeClient = Client(
                    wsUrl,
                    options,
                    object : EventListener() {
                        override fun onConnected(client: Client, event: ConnectedEvent) {
                            Logger.i("Connected to Centrifugo with client id: ${event.client}")
                            subscribeToChannel(channel!!, subscriptionToken!!, deviceId)
                        }

                        override fun onDisconnected(client: Client, event: DisconnectedEvent) {
                            Logger.i("Disconnected from Centrifugo: code=${event.code}, reason=${event.reason}")
                        }

                        override fun onError(client: Client, event: ErrorEvent) {
                            Logger.e("Centrifugo client error: ${event.error?.message}")
                        }
                    }
                )

                centrifugeClient?.connect()
                Logger.i("Connecting to Centrifugo...")

            } catch (e: Exception) {
                Logger.e("Failed to connect to Centrifugo: ${e.message}")
            }
        }
    }

    private fun deriveWsUrl(serverUrl: String): String {
        val uri = Uri.parse(serverUrl)
        val host = uri.host ?: ""
        val port = if (uri.port != -1) ":${uri.port}" else ""
        val parts = host.split(".", limit = 2)

        return if (parts.size == 2 && parts[1].contains(".")) {
            // Multi-level domain (e.g. api.agimate.io) -> wss://centrifugo.agimate.io
            "wss://centrifugo.${parts[1]}/connection/websocket"
        } else {
            // Single-level host (e.g. localhost:8080) -> respect original scheme
            val wsScheme = if (uri.scheme == "https") "wss" else "ws"
            "$wsScheme://$host$port/connection/websocket"
        }
    }

    private fun subscribeToChannel(channel: String, subToken: String, deviceId: String) {
        try {
            Logger.i("Subscribing to channel: $channel")

            val subscriptionOptions = SubscriptionOptions()
            subscriptionOptions.token = subToken
            subscriptionOptions.tokenGetter = object : SubscriptionTokenGetter() {
                override fun getSubscriptionToken(event: SubscriptionTokenEvent, cb: TokenCallback) {
                    serviceScope.launch {
                        try {
                            val refreshResponse = triggerApi.getCentrifugoToken(
                                CentrifugoTokenRequest(deviceId = deviceId)
                            )
                            if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                                val data = refreshResponse.body()!!.response
                                subscriptionToken = data.subscriptionToken
                                connectionToken = data.connectionToken
                                cb.Done(null, data.subscriptionToken)
                            } else {
                                cb.Done(Exception("Failed to refresh subscription token"), null)
                            }
                        } catch (e: Exception) {
                            Logger.e("Error refreshing subscription token: ${e.message}")
                            cb.Done(e, null)
                        }
                    }
                }
            }

            centrifugeSubscription = centrifugeClient?.newSubscription(
                channel,
                subscriptionOptions,
                object : SubscriptionEventListener() {
                    override fun onSubscribed(sub: Subscription, event: SubscribedEvent) {
                        Logger.i("Successfully subscribed to channel: ${sub.channel}")
                    }

                    override fun onPublication(sub: Subscription, event: PublicationEvent) {
                        Logger.i("Received publication from channel: ${sub.channel}")
                        handleIncomingAction(event.data)
                    }

                    override fun onError(sub: Subscription, event: io.github.centrifugal.centrifuge.SubscriptionErrorEvent) {
                        Logger.e("Subscription error on channel ${sub.channel}: ${event.error?.message}")
                    }
                }
            )

            centrifugeSubscription?.subscribe()

        } catch (e: DuplicateSubscriptionException) {
            Logger.e("Duplicate subscription: ${e.message}")
        } catch (e: Exception) {
            Logger.e("Failed to subscribe to channel: ${e.message}")
        }
    }

    private fun handleIncomingAction(data: ByteArray) {
        serviceScope.launch {
            try {
                val dataString = String(data, Charsets.UTF_8)
                Logger.i("Received action task: $dataString")

                val task = json.decodeFromString<ActionTask>(dataString)
                val actionType = ActionType.fromActionName(task.type)
                if (actionType == null) {
                    Logger.e("Unknown action type: ${task.type}")
                    return@launch
                }

                // Check if this action type is enabled
                if (!actionStateRepo.isEnabled(actionType)) {
                    Logger.i("Action type ${task.type} is disabled, ignoring")
                    return@launch
                }

                // Execute action
                val handler = actionHandlers[actionType]
                if (handler != null) {
                    handler.execute(task)
                    Logger.i("Action executed successfully: ${task.type}")
                } else {
                    Logger.e("No handler found for action type: ${task.type}")
                }

            } catch (e: Exception) {
                Logger.e("Failed to handle action: ${e.message}")
            }
        }
    }

    private fun observeActionStates() {
        serviceScope.launch {
            actionStateRepo.getAllStates().collectLatest { states ->
                Logger.d("Action states updated: $states")
            }
        }
    }

    private fun createNotification(): Notification {
        createNotificationChannel()

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_actions_title))
            .setContentText(getString(R.string.notification_actions_text))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_actions_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_actions_channel_desc)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.i("ActionExecutorService started")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Logger.i("ActionExecutorService destroyed")

        // Unsubscribe from Centrifugo channel
        try {
            centrifugeSubscription?.unsubscribe()
            centrifugeClient?.disconnect()
            Logger.i("Disconnected from Centrifugo")
        } catch (e: Exception) {
            Logger.e("Error disconnecting from Centrifugo: ${e.message}")
        }

        actionHandlers.values.forEach { it.cleanup() }
        serviceScope.cancel()

        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_ID = 1002
        const val CHANNEL_ID = "action_executor_service_channel"
    }
}
