package ru.agimate.android.data.local.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.agimate.android.data.local.database.dao.ActionStateDao
import ru.agimate.android.data.local.database.entity.ActionStateEntity
import ru.agimate.android.data.model.ActionType
import ru.agimate.android.domain.repository.IActionStateRepository

class ActionStateRepository(
    private val actionStateDao: ActionStateDao
) : IActionStateRepository {

    override fun getAllStates(): Flow<Map<ActionType, Boolean>> {
        return actionStateDao.getAllStates().map { entities ->
            val statesMap = entities.associate { entity ->
                ActionType.valueOf(entity.actionType) to entity.enabled
            }
            // Return all ActionTypes with default value false if not in DB
            ActionType.entries.associateWith { type ->
                statesMap[type] ?: false
            }
        }
    }

    override suspend fun isEnabled(actionType: ActionType): Boolean {
        val state = actionStateDao.getState(actionType.name)
        return state?.enabled ?: false
    }

    override suspend fun setEnabled(actionType: ActionType, enabled: Boolean) {
        val existing = actionStateDao.getState(actionType.name)
        if (existing != null) {
            actionStateDao.updateEnabled(
                actionType = actionType.name,
                enabled = enabled,
                lastModified = System.currentTimeMillis()
            )
        } else {
            actionStateDao.insert(
                ActionStateEntity(
                    actionType = actionType.name,
                    enabled = enabled,
                    lastModified = System.currentTimeMillis()
                )
            )
        }
    }

    override suspend fun hasAnyEnabled(): Boolean {
        return actionStateDao.countEnabled() > 0
    }
}
