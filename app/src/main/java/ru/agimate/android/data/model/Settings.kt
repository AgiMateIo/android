package ru.agimate.android.data.model

data class Settings(
    val apiKey: String = "",
    val serverUrl: String = "https://api.agimate.io",
    val deviceId: String = "",
    val debugLogging: Boolean = false,
    val appVersion: String = "1.0"
)
