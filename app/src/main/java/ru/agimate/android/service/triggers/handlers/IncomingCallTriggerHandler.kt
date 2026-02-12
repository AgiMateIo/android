package ru.agimate.android.service.triggers.handlers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.telephony.TelephonyManager
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import ru.agimate.android.data.model.TriggerType
import ru.agimate.android.domain.usecase.SendTriggerUseCase
import ru.agimate.android.service.triggers.base.BaseTriggerHandler
import ru.agimate.android.util.Logger

class IncomingCallTriggerHandler(
    context: Context,
    sendTriggerUseCase: SendTriggerUseCase
) : BaseTriggerHandler(context, sendTriggerUseCase) {

    override val triggerType = TriggerType.INCOMING_CALL

    private var isRegistered = false

    private val phoneStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

            if (state == TelephonyManager.EXTRA_STATE_RINGING) {
                val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) ?: "unknown"
                Logger.i("Incoming call detected: $phoneNumber")

                val data = buildJsonObject {
                    put("phoneNumber", phoneNumber)
                    put("timestamp", System.currentTimeMillis())
                }

                sendEvent(
                    name = "android.trigger.call.incoming",
                    data = data
                )
            }
        }
    }

    override fun register() {
        if (!isRegistered) {
            val filter = IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
            context.registerReceiver(phoneStateReceiver, filter)
            isRegistered = true
            Logger.i("IncomingCallTriggerHandler registered")
        }
    }

    override fun unregister() {
        if (isRegistered) {
            try {
                context.unregisterReceiver(phoneStateReceiver)
            } catch (e: Exception) {
                Logger.e("Error unregistering phone receiver", e)
            }
            isRegistered = false
            Logger.i("IncomingCallTriggerHandler unregistered")
        }
    }
}
