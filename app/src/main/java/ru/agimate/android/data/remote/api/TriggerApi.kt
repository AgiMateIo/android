package ru.agimate.android.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import ru.agimate.android.data.remote.dto.CentrifugoTokenRequest
import ru.agimate.android.data.remote.dto.CentrifugoTokenResponse
import ru.agimate.android.data.remote.dto.LinkDeviceRequest
import ru.agimate.android.data.remote.dto.TriggerRequest

interface TriggerApi {
    @POST("device/trigger/new")
    suspend fun sendTrigger(
        @Body request: TriggerRequest
    ): Response<Unit>

    @POST("device/centrifugo/token")
    suspend fun getCentrifugoToken(
        @Body request: CentrifugoTokenRequest
    ): Response<CentrifugoTokenResponse>

    @POST("device/registration/link")
    suspend fun linkDevice(
        @Body request: LinkDeviceRequest
    ): Response<Unit>
}
