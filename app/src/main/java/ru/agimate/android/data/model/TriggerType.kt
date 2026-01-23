package ru.agimate.android.data.model

enum class TriggerType(val displayName: String) {
    INCOMING_CALL("Incoming Call"),
    BATTERY_LOW("Battery Low"),
    WIFI("Wi-Fi Connected/Disconnected"),
    SHAKE("Shake Device")
}
