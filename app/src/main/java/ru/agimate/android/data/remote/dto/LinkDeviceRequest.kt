package ru.agimate.android.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class LinkDeviceRequest(
    val deviceId: String,
    val deviceName: String,
    val deviceOs: String,
    val triggers: Map<String, TriggerCapability>? = null,
    val actions: Map<String, ActionCapability>? = null,
)

@Serializable
data class TriggerCapability(
    val params: List<String> = emptyList()
)

@Serializable
data class ActionCapability(
    val params: List<String> = emptyList()
)
