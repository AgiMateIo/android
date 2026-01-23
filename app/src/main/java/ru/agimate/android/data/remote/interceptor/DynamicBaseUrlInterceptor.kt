package ru.agimate.android.data.remote.interceptor

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import ru.agimate.android.data.local.datastore.SettingsDataStore

class DynamicBaseUrlInterceptor(
    private val settingsDataStore: SettingsDataStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url

        val serverUrl = runBlocking {
            settingsDataStore.settingsFlow.first().serverUrl
        }.trimEnd('/')

        val newBaseUrl = serverUrl.toHttpUrlOrNull()
            ?: return chain.proceed(originalRequest)

        val newUrl = originalUrl.newBuilder()
            .scheme(newBaseUrl.scheme)
            .host(newBaseUrl.host)
            .port(newBaseUrl.port)
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}