package ru.agimate.android.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import java.time.Instant

@Serializable
data class TriggerRequest(
    val id: String,
    val type: String,
    val name: String,
    val source: String,
    val deviceId: String,
    val userId: String? = null,
    @Serializable(with = InstantSerializer::class)
    val occurredAt: Instant,
    val data: JsonObject
)
