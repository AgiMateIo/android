package ru.agimate.android.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ActionTask(
    val type: String,
    val parameters: JsonObject
)
