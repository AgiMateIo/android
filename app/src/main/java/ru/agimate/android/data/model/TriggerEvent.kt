package ru.agimate.android.data.model

import java.time.Instant

data class TriggerEvent(
    val id: String,
    val triggerName: String,
    val occurredAt: Instant,
    val sentAt: Instant?,
    val status: EventStatus,
    val errorMessage: String? = null
)

enum class EventStatus {
    PENDING,
    SUCCESS,
    FAILED
}
