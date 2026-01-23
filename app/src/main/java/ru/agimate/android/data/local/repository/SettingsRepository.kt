package ru.agimate.android.data.local.repository

import kotlinx.coroutines.flow.Flow
import ru.agimate.android.data.local.datastore.SettingsDataStore
import ru.agimate.android.data.model.Settings
import ru.agimate.android.domain.repository.ISettingsRepository

class SettingsRepository(
    private val settingsDataStore: SettingsDataStore
) : ISettingsRepository {

    override val settingsFlow: Flow<Settings> = settingsDataStore.settingsFlow

    override suspend fun saveApiKey(key: String) {
        settingsDataStore.saveApiKey(key)
    }

    override suspend fun saveServerUrl(url: String) {
        settingsDataStore.saveServerUrl(url)
    }

    override suspend fun saveDeviceId(deviceId: String) {
        settingsDataStore.saveDeviceId(deviceId)
    }

    override suspend fun saveDebugLogging(enabled: Boolean) {
        settingsDataStore.saveDebugLogging(enabled)
    }
}
