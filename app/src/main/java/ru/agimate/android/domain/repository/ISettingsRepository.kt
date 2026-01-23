package ru.agimate.android.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.agimate.android.data.model.Settings

interface ISettingsRepository {
    val settingsFlow: Flow<Settings>
    suspend fun saveApiKey(key: String)
    suspend fun saveServerUrl(url: String)
    suspend fun saveDeviceId(deviceId: String)
    suspend fun saveDebugLogging(enabled: Boolean)
}
