package ru.agimate.android.di

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ru.agimate.android.data.local.database.AppDatabase
import ru.agimate.android.data.local.datastore.SettingsDataStore
import ru.agimate.android.data.local.repository.ActionStateRepository
import ru.agimate.android.data.local.repository.SettingsRepository
import ru.agimate.android.data.local.repository.TriggerEventRepository
import ru.agimate.android.data.local.repository.TriggerStateRepository
import ru.agimate.android.domain.repository.IActionStateRepository
import ru.agimate.android.domain.repository.ISettingsRepository
import ru.agimate.android.domain.repository.ITriggerEventRepository
import ru.agimate.android.domain.repository.ITriggerStateRepository

val dataModule = module {
    // DataStore
    single { SettingsDataStore(androidContext()) }

    // Room Database
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .addMigrations(AppDatabase.MIGRATION_1_2)
        .build()
    }

    // DAOs
    single { get<AppDatabase>().triggerStateDao() }
    single { get<AppDatabase>().triggerEventDao() }
    single { get<AppDatabase>().actionStateDao() }

    // Repositories
    single<ISettingsRepository> { SettingsRepository(get()) }
    single<ITriggerStateRepository> { TriggerStateRepository(get()) }
    single<ITriggerEventRepository> { TriggerEventRepository(get()) }
    single<IActionStateRepository> { ActionStateRepository(get()) }
}
