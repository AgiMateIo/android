package ru.agimate.android.service.triggers.handlers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import ru.agimate.android.data.model.TriggerType
import ru.agimate.android.domain.usecase.SendTriggerUseCase
import ru.agimate.android.service.triggers.base.BaseTriggerHandler
import ru.agimate.android.util.Logger

class WiFiTriggerHandler(
    context: Context,
    sendTriggerUseCase: SendTriggerUseCase
) : BaseTriggerHandler(context, sendTriggerUseCase) {

    override val triggerType = TriggerType.WIFI

    private var isRegistered = false
    private var lastConnectedState: Boolean? = null

    @Suppress("DEPRECATION")
    private val wifiReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            val isConnected = networkInfo?.type == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected

            if (lastConnectedState != isConnected) {
                lastConnectedState = isConnected

                val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val wifiInfo = wifiManager.connectionInfo

                val eventName = if (isConnected) {
                    "android.trigger.wifi.connected"
                } else {
                    "android.trigger.wifi.disconnected"
                }

                Logger.i("WiFi state changed: $eventName")

                val data = buildJsonObject {
                    put("ssid", wifiInfo?.ssid?.replace("\"", "") ?: "unknown")
                    put("bssid", wifiInfo?.bssid ?: "unknown")
                    put("connected", isConnected)
                    put("signalStrength", wifiInfo?.rssi ?: 0)
                    put("timestamp", System.currentTimeMillis())
                }

                sendEvent(
                    name = eventName,
                    data = data
                )
            }
        }
    }

    override fun register() {
        if (!isRegistered) {
            val filter = IntentFilter().apply {
                addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
                addAction(ConnectivityManager.CONNECTIVITY_ACTION)
            }
            context.registerReceiver(wifiReceiver, filter)
            isRegistered = true
            Logger.i("WiFiTriggerHandler registered")
        }
    }

    override fun unregister() {
        if (isRegistered) {
            try {
                context.unregisterReceiver(wifiReceiver)
            } catch (e: Exception) {
                Logger.e("Error unregistering wifi receiver", e)
            }
            isRegistered = false
            lastConnectedState = null
            Logger.i("WiFiTriggerHandler unregistered")
        }
    }
}
