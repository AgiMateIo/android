package ru.agimate.android.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trigger_states")
data class TriggerStateEntity(
    @PrimaryKey
    val triggerType: String,
    val enabled: Boolean,
    val lastModified: Long
)
