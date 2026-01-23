package ru.agimate.android.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trigger_events")
data class TriggerEventEntity(
    @PrimaryKey
    val id: String,
    val triggerName: String,
    val occurredAt: Long,
    val sentAt: Long?,
    val status: String,
    val errorMessage: String?,
    val dataJson: String?
)
