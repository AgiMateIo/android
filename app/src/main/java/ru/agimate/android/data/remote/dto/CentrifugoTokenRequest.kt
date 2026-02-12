package ru.agimate.android.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CentrifugoTokenRequest(val deviceId: String)
