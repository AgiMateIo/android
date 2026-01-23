package ru.agimate.android.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "action_states")
data class ActionStateEntity(
    @PrimaryKey
    val actionType: String,
    val enabled: Boolean,
    val lastModified: Long
)
