package ru.agimate.android.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import io.github.centrifugal.centrifuge.Client
import io.github.centrifugal.centrifuge.ConnectedEvent
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
import ru.agimate.android.domain.repository.IActionStateRepository
import ru.agimate.android.domain.repository.ISettingsRepository
import ru.agimate.android.service.actions.base.IActionHandler
import ru.agimate.android.service.actions.handlers.NotificationActionHandler
import ru.agimate.android.service.actions.handlers.TtsActionHandler
import ru.agimate.android.util.Logger

class ActionExecutorService : Service() {

    private val actionStateRepo: IActionStateRepository by inject()
    private val settingsRepo: ISettingsRepository by inject()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val actionHandlers = mutableMapOf<ActionType, IActionHandler>()

    private val json = Json { ignoreUnknownKeys = true }

    private var centrifugeClient: Client? = null
    private var centrifugeSubscription: Subscription? = null

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

                // Convert HTTP URL to WebSocket URL
                val serverUrl = settings.serverUrl
                val wsUrl = serverUrl
                    .replace("http://", "ws://")
                    .replace("https://", "wss://")
                    .trimEnd('/') + "/connection/websocket"

                Logger.i("Centrifugo WebSocket URL: $wsUrl")
                Logger.i("Device ID: $deviceId")

                // Create Centrifuge client with options
                val options = Options()

                centrifugeClient = Client(
                    wsUrl,
                    options,
                    object : EventListener() {
                        override fun onConnected(client: Client, event: ConnectedEvent) {
                            Logger.i("Connected to Centrifugo with client id: ${event.client}")
                            subscribeToChannel(deviceId)
                        }

                        override fun onDisconnected(client: Client, event: DisconnectedEvent) {
                            Logger.i("Disconnected from Centrifugo: code=${event.code}, reason=${event.reason}")
                        }

                        override fun onError(client: Client, event: ErrorEvent) {
                            Logger.e("Centrifugo client error: ${event.error?.message}")
                        }
                    }
                )

                // Connect to Centrifugo
                centrifugeClient?.connect()
                Logger.i("Connecting to Centrifugo...")

            } catch (e: Exception) {
                Logger.e("Failed to connect to Centrifugo: ${e.message}")
            }
        }
    }

    private fun subscribeToChannel(deviceId: String) {
        try {
            val channel = "device:$deviceId:actions"
            Logger.i("Subscribing to channel: $channel")

            val subscriptionOptions = SubscriptionOptions()

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
                val actionType = ActionType.valueOf(task.type)

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
            .setContentTitle("Agimate Actions")
            .setContentText("Ready to receive commands")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Action Executor Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notification for action executor service"
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
