package ru.agimate.android.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.agimate.android.data.local.database.entity.TriggerEventEntity

@Dao
interface TriggerEventDao {

    @Query("SELECT * FROM trigger_events ORDER BY occurredAt DESC LIMIT 20")
    fun getRecentEvents(): Flow<List<TriggerEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: TriggerEventEntity)

    @Query("DELETE FROM trigger_events WHERE id NOT IN (SELECT id FROM trigger_events ORDER BY occurredAt DESC LIMIT 20)")
    suspend fun deleteOldEvents()

    @Query("UPDATE trigger_events SET status = :status, sentAt = :sentAt, errorMessage = :errorMessage WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, sentAt: Long?, errorMessage: String?)

    @Query("DELETE FROM trigger_events")
    suspend fun clearAll()
}
