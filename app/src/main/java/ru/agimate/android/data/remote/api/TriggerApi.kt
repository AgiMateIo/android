package ru.agimate.android.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import ru.agimate.android.data.remote.dto.TriggerRequest

interface TriggerApi {
    @POST("mobile-api/device/trigger/new")
    suspend fun sendTrigger(
        @Body request: TriggerRequest
    ): Response<Unit>
}
