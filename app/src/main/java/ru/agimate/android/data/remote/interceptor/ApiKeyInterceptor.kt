package ru.agimate.android.data.remote.interceptor

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import ru.agimate.android.data.local.datastore.SettingsDataStore

class ApiKeyInterceptor(
    private val settingsDataStore: SettingsDataStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val apiKey = runBlocking {
            settingsDataStore.settingsFlow.first().apiKey
        }

        val request = chain.request().newBuilder()
            .addHeader("X-Device-Auth-Key", apiKey)
            .build()

        return chain.proceed(request)
    }
}
