package ru.agimate.android.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonObject
import ru.agimate.android.data.model.EventStatus
import ru.agimate.android.data.model.TriggerEvent
import ru.agimate.android.data.remote.api.TriggerApi
import ru.agimate.android.data.remote.dto.TriggerRequest
import ru.agimate.android.domain.repository.ISettingsRepository
import ru.agimate.android.domain.repository.ITriggerEventRepository
import java.time.Instant
import java.util.UUID

class SendTriggerUseCase(
    private val triggerApi: TriggerApi,
    private val settingsRepository: ISettingsRepository,
    private val eventRepository: ITriggerEventRepository
) {

    suspend fun execute(
        type: String,
        name: String,
        data: JsonObject
    ): Result<Unit> {
        val settings = settingsRepository.settingsFlow.first()
        val eventId = UUID.randomUUID().toString()

        val event = TriggerEvent(
            id = eventId,
            triggerName = name,
            occurredAt = Instant.now(),
            sentAt = null,
            status = EventStatus.PENDING
        )

        if (settings.debugLogging) {
            eventRepository.insert(event)
        }

        return try {
            val request = TriggerRequest(
                id = eventId,
                type = type,
                name = name,
                source = "android-app",
                deviceId = settings.deviceId,
                userId = null,
                occurredAt = Instant.now(),
                data = data
            )

            val response = triggerApi.sendTrigger(request)

            if (response.isSuccessful) {
                if (settings.debugLogging) {
                    eventRepository.updateStatus(eventId, EventStatus.SUCCESS)
                }
                Result.success(Unit)
            } else {
                val errorMessage = "HTTP ${response.code()}: ${response.message()}"
                if (settings.debugLogging) {
                    eventRepository.updateStatus(eventId, EventStatus.FAILED, errorMessage)
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            if (settings.debugLogging) {
                eventRepository.updateStatus(eventId, EventStatus.FAILED, e.message)
            }
            Result.failure(e)
        }
    }
}
