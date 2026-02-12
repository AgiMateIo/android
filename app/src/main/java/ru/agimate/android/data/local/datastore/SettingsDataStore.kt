package ru.agimate.android.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.agimate.android.data.model.Settings

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    val settingsFlow: Flow<Settings> = context.dataStore.data.map { preferences ->
        Settings(
            apiKey = preferences[PreferencesKeys.API_KEY] ?: "",
            serverUrl = preferences[PreferencesKeys.SERVER_URL] ?: "https://api.agimate.io",
            deviceId = preferences[PreferencesKeys.DEVICE_ID] ?: "",
            debugLogging = preferences[PreferencesKeys.DEBUG_LOGGING] ?: false
        )
    }

    suspend fun saveApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.API_KEY] = key
        }
    }

    suspend fun saveServerUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SERVER_URL] = url
        }
    }

    suspend fun saveDeviceId(deviceId: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEVICE_ID] = deviceId
        }
    }

    suspend fun saveDebugLogging(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEBUG_LOGGING] = enabled
        }
    }
}
