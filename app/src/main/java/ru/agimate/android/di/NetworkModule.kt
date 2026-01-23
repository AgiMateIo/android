package ru.agimate.android.di

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import ru.agimate.android.data.remote.api.TriggerApi
import ru.agimate.android.data.remote.interceptor.ApiKeyInterceptor
import ru.agimate.android.data.remote.interceptor.DynamicBaseUrlInterceptor
import java.util.concurrent.TimeUnit

val networkModule = module {
    // Interceptors
    single { ApiKeyInterceptor(get()) }
    single { DynamicBaseUrlInterceptor(get()) }

    // OkHttpClient
    single {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(get<DynamicBaseUrlInterceptor>())
            .addInterceptor(get<ApiKeyInterceptor>())
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // JSON
    single {
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }

    // Retrofit (placeholder URL - actual URL set by DynamicBaseUrlInterceptor)
    single {
        Retrofit.Builder()
            .baseUrl("http://placeholder.local/")
            .client(get())
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
    }

    // TriggerApi
    single { get<Retrofit>().create(TriggerApi::class.java) }
}
