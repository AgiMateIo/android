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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import ru.agimate.android.MainActivity
import ru.agimate.android.R
import ru.agimate.android.data.model.TriggerType
import ru.agimate.android.domain.repository.ITriggerStateRepository
import ru.agimate.android.domain.usecase.SendTriggerUseCase
import ru.agimate.android.service.triggers.base.ITriggerHandler
import ru.agimate.android.service.triggers.handlers.BatteryLowTriggerHandler
import ru.agimate.android.service.triggers.handlers.IncomingCallTriggerHandler
import ru.agimate.android.service.triggers.handlers.ShakeTriggerHandler
import ru.agimate.android.service.triggers.handlers.WiFiTriggerHandler
import ru.agimate.android.util.Logger

class TriggerMonitorService : Service() {

    private val triggerStateRepo: ITriggerStateRepository by inject()
    private val sendTriggerUseCase: SendTriggerUseCase by inject()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val triggerHandlers = mutableMapOf<TriggerType, ITriggerHandler>()

    override fun onCreate() {
        super.onCreate()
        Logger.i("TriggerMonitorService created")

        initializeHandlers()
        startForeground(NOTIFICATION_ID, createNotification())
        observeTriggerStates()
    }

    private fun initializeHandlers() {
        triggerHandlers[TriggerType.INCOMING_CALL] = IncomingCallTriggerHandler(this, sendTriggerUseCase)
        triggerHandlers[TriggerType.BATTERY_LOW] = BatteryLowTriggerHandler(this, sendTriggerUseCase)
        triggerHandlers[TriggerType.WIFI] = WiFiTriggerHandler(this, sendTriggerUseCase)
        triggerHandlers[TriggerType.SHAKE] = ShakeTriggerHandler(this, sendTriggerUseCase)
    }

    private fun observeTriggerStates() {
        serviceScope.launch {
            triggerStateRepo.getAllStates().collectLatest { states ->
                updateHandlers(states)
            }
        }
    }

    private fun updateHandlers(states: Map<TriggerType, Boolean>) {
        TriggerType.entries.forEach { type ->
            val handler = triggerHandlers[type]
            val isEnabled = states[type] ?: false

            if (isEnabled) {
                handler?.register()
            } else {
                handler?.unregister()
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
            .setContentTitle("Agimate Triggers")
            .setContentText("Monitoring active triggers")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Trigger Monitor Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notification for trigger monitoring service"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.i("TriggerMonitorService started")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Logger.i("TriggerMonitorService destroyed")
        triggerHandlers.values.forEach { it.unregister() }
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "trigger_service_channel"
    }
}
