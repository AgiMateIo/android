package ru.agimate.android.data.local.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferencesKeys {
    val API_KEY = stringPreferencesKey("api_key")
    val SERVER_URL = stringPreferencesKey("server_url")
    val DEVICE_ID = stringPreferencesKey("device_id")
    val DEBUG_LOGGING = booleanPreferencesKey("debug_logging")
}
