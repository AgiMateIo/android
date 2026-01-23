package ru.agimate.android.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.agimate.android.data.local.database.entity.ActionStateEntity

@Dao
interface ActionStateDao {

    @Query("SELECT * FROM action_states")
    fun getAllStates(): Flow<List<ActionStateEntity>>

    @Query("SELECT * FROM action_states WHERE actionType = :actionType")
    suspend fun getState(actionType: String): ActionStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(state: ActionStateEntity)

    @Query("UPDATE action_states SET enabled = :enabled, lastModified = :lastModified WHERE actionType = :actionType")
    suspend fun updateEnabled(actionType: String, enabled: Boolean, lastModified: Long)

    @Query("SELECT COUNT(*) FROM action_states WHERE enabled = 1")
    suspend fun countEnabled(): Int
}
