package ru.agimate.android.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CentrifugoTokenResponse(val response: CentrifugoTokenData)

@Serializable
data class CentrifugoTokenData(
    val connectionToken: String,
    val subscriptionToken: String,
    val channel: String,
    val wsUrl: String? = null
)
