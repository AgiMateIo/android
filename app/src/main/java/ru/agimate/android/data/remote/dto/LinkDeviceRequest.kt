package ru.agimate.android.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import java.time.Instant

@Serializable
data class LinkDeviceRequest(
    val deviceId: String,
    val deviceName: String,
    val deviceOs: String,
)
