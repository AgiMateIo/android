package ru.agimate.android.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.agimate.android.data.model.EventStatus
import ru.agimate.android.data.model.TriggerEvent

interface ITriggerEventRepository {
    fun getRecentEvents(): Flow<List<TriggerEvent>>
    suspend fun insert(event: TriggerEvent)
    suspend fun updateStatus(id: String, status: EventStatus, errorMessage: String? = null)
    suspend fun clearHistory()
}
