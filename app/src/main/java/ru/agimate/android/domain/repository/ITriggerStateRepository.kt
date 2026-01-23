package ru.agimate.android.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.agimate.android.data.model.TriggerType

interface ITriggerStateRepository {
    fun getAllStates(): Flow<Map<TriggerType, Boolean>>
    suspend fun isEnabled(triggerType: TriggerType): Boolean
    suspend fun setEnabled(triggerType: TriggerType, enabled: Boolean)
    suspend fun hasAnyEnabled(): Boolean
}
