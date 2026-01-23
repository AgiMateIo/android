package ru.agimate.android.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.agimate.android.data.local.database.entity.TriggerStateEntity

@Dao
interface TriggerStateDao {

    @Query("SELECT * FROM trigger_states")
    fun getAllStates(): Flow<List<TriggerStateEntity>>

    @Query("SELECT * FROM trigger_states WHERE triggerType = :triggerType")
    suspend fun getState(triggerType: String): TriggerStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(state: TriggerStateEntity)

    @Query("UPDATE trigger_states SET enabled = :enabled, lastModified = :lastModified WHERE triggerType = :triggerType")
    suspend fun updateEnabled(triggerType: String, enabled: Boolean, lastModified: Long)

    @Query("SELECT COUNT(*) FROM trigger_states WHERE enabled = 1")
    suspend fun countEnabled(): Int
}
