package ru.agimate.android.data.model

import android.Manifest
import android.os.Build
import androidx.annotation.StringRes
import ru.agimate.android.R

data class TriggerDefinition(
    val type: TriggerType,
    @StringRes val displayNameResId: Int,
    @StringRes val descriptionResId: Int,
    val eventNamePrefix: String,
    val requiredPermissions: List<String> = emptyList()
) {
    companion object {
        val ALL_TRIGGERS = listOf(
            TriggerDefinition(
                type = TriggerType.INCOMING_CALL,
                displayNameResId = R.string.trigger_incoming_call,
                descriptionResId = R.string.trigger_desc_incoming_call,
                eventNamePrefix = "android.trigger.call.incoming",
                requiredPermissions = listOf(Manifest.permission.READ_PHONE_STATE)
            ),
            TriggerDefinition(
                type = TriggerType.BATTERY_LOW,
                displayNameResId = R.string.trigger_battery_low,
                descriptionResId = R.string.trigger_desc_battery_low,
                eventNamePrefix = "android.trigger.battery.low"
            ),
            TriggerDefinition(
                type = TriggerType.WIFI,
                displayNameResId = R.string.trigger_wifi,
                descriptionResId = R.string.trigger_desc_wifi,
                eventNamePrefix = "android.trigger.wifi",
                requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    listOf(Manifest.permission.ACCESS_FINE_LOCATION)
                } else {
                    listOf(Manifest.permission.ACCESS_COARSE_LOCATION)
                }
            ),
            TriggerDefinition(
                type = TriggerType.SHAKE,
                displayNameResId = R.string.trigger_shake,
                descriptionResId = R.string.trigger_desc_shake,
                eventNamePrefix = "android.trigger.shake"
            )
        )
    }
}
