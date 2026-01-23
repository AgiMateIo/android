package ru.agimate.android.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.agimate.android.data.model.ActionType

interface IActionStateRepository {
    fun getAllStates(): Flow<Map<ActionType, Boolean>>
    suspend fun isEnabled(actionType: ActionType): Boolean
    suspend fun setEnabled(actionType: ActionType, enabled: Boolean)
    suspend fun hasAnyEnabled(): Boolean
}
