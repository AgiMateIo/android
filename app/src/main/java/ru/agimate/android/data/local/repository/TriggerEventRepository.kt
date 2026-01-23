package ru.agimate.android.data.local.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.agimate.android.data.local.database.dao.TriggerEventDao
import ru.agimate.android.data.local.database.entity.TriggerEventEntity
import ru.agimate.android.data.model.EventStatus
import ru.agimate.android.data.model.TriggerEvent
import ru.agimate.android.domain.repository.ITriggerEventRepository
import java.time.Instant

class TriggerEventRepository(
    private val triggerEventDao: TriggerEventDao
) : ITriggerEventRepository {

    override fun getRecentEvents(): Flow<List<TriggerEvent>> {
        return triggerEventDao.getRecentEvents().map { entities ->
            entities.map { entity -> entity.toTriggerEvent() }
        }
    }

    override suspend fun insert(event: TriggerEvent) {
        triggerEventDao.insert(event.toEntity())
        triggerEventDao.deleteOldEvents()
    }

    override suspend fun updateStatus(id: String, status: EventStatus, errorMessage: String?) {
        triggerEventDao.updateStatus(
            id = id,
            status = status.name,
            sentAt = if (status == EventStatus.SUCCESS) System.currentTimeMillis() else null,
            errorMessage = errorMessage
        )
    }

    override suspend fun clearHistory() {
        triggerEventDao.clearAll()
    }

    private fun TriggerEvent.toEntity(): TriggerEventEntity {
        return TriggerEventEntity(
            id = id,
            triggerName = triggerName,
            occurredAt = occurredAt.toEpochMilli(),
            sentAt = sentAt?.toEpochMilli(),
            status = status.name,
            errorMessage = errorMessage,
            dataJson = null
        )
    }

    private fun TriggerEventEntity.toTriggerEvent(): TriggerEvent {
        return TriggerEvent(
            id = id,
            triggerName = triggerName,
            occurredAt = Instant.ofEpochMilli(occurredAt),
            sentAt = sentAt?.let { Instant.ofEpochMilli(it) },
            status = EventStatus.valueOf(status),
            errorMessage = errorMessage
        )
    }
}
