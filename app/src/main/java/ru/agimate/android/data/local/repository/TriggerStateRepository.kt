package ru.agimate.android.data.local.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.agimate.android.data.local.database.dao.TriggerStateDao
import ru.agimate.android.data.local.database.entity.TriggerStateEntity
import ru.agimate.android.data.model.TriggerType
import ru.agimate.android.domain.repository.ITriggerStateRepository

class TriggerStateRepository(
    private val triggerStateDao: TriggerStateDao
) : ITriggerStateRepository {

    override fun getAllStates(): Flow<Map<TriggerType, Boolean>> {
        return triggerStateDao.getAllStates().map { entities ->
            entities.associate { entity ->
                TriggerType.valueOf(entity.triggerType) to entity.enabled
            }
        }
    }

    override suspend fun isEnabled(triggerType: TriggerType): Boolean {
        val state = triggerStateDao.getState(triggerType.name)
        return state?.enabled ?: false
    }

    override suspend fun setEnabled(triggerType: TriggerType, enabled: Boolean) {
        val existing = triggerStateDao.getState(triggerType.name)
        if (existing != null) {
            triggerStateDao.updateEnabled(
                triggerType = triggerType.name,
                enabled = enabled,
                lastModified = System.currentTimeMillis()
            )
        } else {
            triggerStateDao.insert(
                TriggerStateEntity(
                    triggerType = triggerType.name,
                    enabled = enabled,
                    lastModified = System.currentTimeMillis()
                )
            )
        }
    }

    override suspend fun hasAnyEnabled(): Boolean {
        return triggerStateDao.countEnabled() > 0
    }
}
