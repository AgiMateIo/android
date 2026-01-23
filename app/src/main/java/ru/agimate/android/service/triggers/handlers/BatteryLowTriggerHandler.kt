package ru.agimate.android.service.triggers.handlers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import ru.agimate.android.data.model.TriggerType
import ru.agimate.android.domain.usecase.SendTriggerUseCase
import ru.agimate.android.service.triggers.base.BaseTriggerHandler
import ru.agimate.android.util.Logger

class BatteryLowTriggerHandler(
    context: Context,
    sendTriggerUseCase: SendTriggerUseCase
) : BaseTriggerHandler(context, sendTriggerUseCase) {

    override val triggerType = TriggerType.BATTERY_LOW

    private var isRegistered = false

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_BATTERY_LOW -> {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
                    val batteryPct = if (scale > 0) (level * 100 / scale) else level

                    Logger.i("Battery low detected: $batteryPct%")

                    val data = buildJsonObject {
                        put("batteryLevel", batteryPct)
                        put("threshold", 15)
                        put("timestamp", System.currentTimeMillis())
                    }

                    sendEvent(
                        name = "device.battery.low",
                        data = data
                    )
                }
            }
        }
    }

    override fun register() {
        if (!isRegistered) {
            val filter = IntentFilter(Intent.ACTION_BATTERY_LOW)
            context.registerReceiver(batteryReceiver, filter)
            isRegistered = true
            Logger.i("BatteryLowTriggerHandler registered")
        }
    }

    override fun unregister() {
        if (isRegistered) {
            try {
                context.unregisterReceiver(batteryReceiver)
            } catch (e: Exception) {
                Logger.e("Error unregistering battery receiver", e)
            }
            isRegistered = false
            Logger.i("BatteryLowTriggerHandler unregistered")
        }
    }
}
