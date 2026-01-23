package ru.agimate.android.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ru.agimate.android.data.local.database.dao.ActionStateDao
import ru.agimate.android.data.local.database.dao.TriggerEventDao
import ru.agimate.android.data.local.database.dao.TriggerStateDao
import ru.agimate.android.data.local.database.entity.ActionStateEntity
import ru.agimate.android.data.local.database.entity.TriggerEventEntity
import ru.agimate.android.data.local.database.entity.TriggerStateEntity

@Database(
    entities = [
        TriggerStateEntity::class,
        TriggerEventEntity::class,
        ActionStateEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun triggerStateDao(): TriggerStateDao
    abstract fun triggerEventDao(): TriggerEventDao
    abstract fun actionStateDao(): ActionStateDao

    companion object {
        const val DATABASE_NAME = "agimate_triggers.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS action_states (
                        actionType TEXT PRIMARY KEY NOT NULL,
                        enabled INTEGER NOT NULL,
                        lastModified INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }
    }
}
