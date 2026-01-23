package ru.agimate.android

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import ru.agimate.android.data.local.datastore.SettingsDataStore
import ru.agimate.android.di.appModule
import ru.agimate.android.di.dataModule
import ru.agimate.android.di.networkModule
import ru.agimate.android.service.TriggerServiceManager
import ru.agimate.android.util.DeviceIdGenerator
import ru.agimate.android.util.Logger

class AgimateApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val triggerServiceManager: TriggerServiceManager by inject()

    override fun onCreate() {
        super.onCreate()

        // Initialize Koin DI
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@AgimateApplication)
            modules(dataModule, networkModule, appModule)
        }

        // Initialize Device ID if not exists
        initializeDeviceId()

        // Restore trigger service if any triggers were enabled
        restoreTriggerService()
    }

    private fun restoreTriggerService() {
        applicationScope.launch {
            Logger.i("Checking for enabled triggers on app start...")
            triggerServiceManager.updateServiceState()
        }
    }

    private fun initializeDeviceId() {
        applicationScope.launch {
            val settingsDataStore = SettingsDataStore(applicationContext)
            val currentSettings = settingsDataStore.settingsFlow.first()

            if (currentSettings.deviceId.isEmpty()) {
                val deviceId = DeviceIdGenerator.generate()
                settingsDataStore.saveDeviceId(deviceId)
            }
        }
    }
}
