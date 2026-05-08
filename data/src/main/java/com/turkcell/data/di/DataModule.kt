package com.turkcell.data.di

import com.turkcell.core.domain.AuthRepository
import com.turkcell.data.remote.AuthApi
import com.turkcell.data.repository.AuthRepositoryImpl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType

val dataModule = module {

    // 1. Logger — Logcat'te isteği görmek için
    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    // 2. OkHttpClient — İnternetin kendisi
    single {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .build()
    }

    // 3. Retrofit — Sunucuya bağlanan araç
    single {
        val json = Json { ignoreUnknownKeys = true }
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/") // Emülatörden localhost'a bağlanmak için
            .client(get())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    // 4. AuthApi — Sunucudaki login/register endpointleri
    single<AuthApi> {
        get<Retrofit>().create(AuthApi::class.java)
    }

    // 5. AuthRepository — Koin'e "AuthRepository istenince AuthRepositoryImpl ver" de
    single<AuthRepository> {
        AuthRepositoryImpl(get())
    }
}
