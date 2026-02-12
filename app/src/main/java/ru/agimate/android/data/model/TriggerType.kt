package ru.agimate.android.data.model

import androidx.annotation.StringRes
import ru.agimate.android.R

enum class TriggerType(@StringRes val displayNameResId: Int) {
    INCOMING_CALL(R.string.trigger_incoming_call),
    BATTERY_LOW(R.string.trigger_battery_low),
    WIFI(R.string.trigger_wifi),
    SHAKE(R.string.trigger_shake)
}
