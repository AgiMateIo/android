package ru.agimate.android.data.model

import android.Manifest
import android.os.Build

data class TriggerDefinition(
    val type: TriggerType,
    val displayName: String,
    val description: String,
    val eventNamePrefix: String,
    val requiredPermissions: List<String> = emptyList()
) {
    companion object {
        val ALL_TRIGGERS = listOf(
            TriggerDefinition(
                type = TriggerType.INCOMING_CALL,
                displayName = "Incoming Call",
                description = "Trigger when receiving a phone call",
                eventNamePrefix = "device.call.incoming",
                requiredPermissions = listOf(Manifest.permission.READ_PHONE_STATE)
            ),
            TriggerDefinition(
                type = TriggerType.BATTERY_LOW,
                displayName = "Battery Low",
                description = "Trigger when battery level is low",
                eventNamePrefix = "device.battery.low"
            ),
            TriggerDefinition(
                type = TriggerType.WIFI,
                displayName = "Wi-Fi Connection",
                description = "Trigger when Wi-Fi connects or disconnects",
                eventNamePrefix = "device.network.wifi",
                requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    listOf(Manifest.permission.ACCESS_FINE_LOCATION)
                } else {
                    listOf(Manifest.permission.ACCESS_COARSE_LOCATION)
                }
            ),
            TriggerDefinition(
                type = TriggerType.SHAKE,
                displayName = "Shake Device",
                description = "Trigger when device is shaken",
                eventNamePrefix = "device.shake"
            )
        )
    }
}
