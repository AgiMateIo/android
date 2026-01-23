package ru.agimate.android.domain.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import ru.agimate.android.data.remote.dto.LinkDeviceRequest
import ru.agimate.android.data.remote.dto.TriggerRequest
import ru.agimate.android.util.Logger
import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit

class TestConnectionUseCase(
    private val json: Json
) {
    suspend operator fun invoke(
        apiKey: String,
        serverUrl: String,
        deviceId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        Logger.d("TestConnection: starting with serverUrl=$serverUrl, deviceId=$deviceId")
        try {
            val triggerRequest = LinkDeviceRequest(
                deviceId = deviceId,
                deviceName = android.os.Build.MODEL,
                deviceOs = "android"
            );

            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()

            val baseUrl = serverUrl.trimEnd('/')
            val requestBody = json.encodeToString(triggerRequest)
                .toRequestBody("application/json".toMediaType())

            val url = "$baseUrl/mobile-api/device/registration/link"
            val httpRequest = Request.Builder()
                .url(url)
                .addHeader("X-Device-Auth-Key", apiKey)
                .post(requestBody)
                .build()

            Logger.d("TestConnection: sending request to $url - $apiKey")
            val response = client.newCall(httpRequest).execute()
            Logger.d("TestConnection: response code=${response.code}, message=${response.message}")

            if (response.isSuccessful) {
                Logger.d("TestConnection: SUCCESS")
                Result.success(Unit)
            } else {
                Logger.e("TestConnection: FAILED - ${response.code}: ${response.message}")
                Result.failure(Exception("Server returned ${response.code}: ${response.message}"))
            }
        } catch (e: Exception) {
            Logger.e("TestConnection: EXCEPTION - ${e.message}", e)
            Result.failure(e)
        }
    }
}
