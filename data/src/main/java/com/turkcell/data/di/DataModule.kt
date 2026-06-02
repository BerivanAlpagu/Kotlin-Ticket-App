package com.turkcell.data.di

import com.turkcell.core.domain.auth.AuthRepository
import com.turkcell.core.domain.checkin.CheckinRepository
import com.turkcell.core.domain.event.EventRepository
import com.turkcell.data.local.TokenStore
import com.turkcell.data.network.AuthInterceptor
import com.turkcell.data.remote.AuthApi
import com.turkcell.data.remote.CheckinApi
import com.turkcell.data.repository.AuthRepositoryImpl
import com.turkcell.data.network.TokenAuthenticator
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import org.koin.core.qualifier.named
import com.turkcell.core.domain.purchase.PurchaseRepository
import com.turkcell.core.domain.ticket.TicketRepository
import com.turkcell.data.remote.EventApi
import com.turkcell.data.remote.MeApi
import com.turkcell.data.remote.PurchaseApi
import com.turkcell.data.repository.CheckinRepositoryImpl
import com.turkcell.data.repository.EventRepositoryImpl
import com.turkcell.data.repository.PurchaseRepositoryImpl
import com.turkcell.data.repository.TicketRepositoryImpl

private const val BASE_URL = "https://tickets-api.halitkalayci.com/"

private val REFRESH_CLIENT = named("refresh_client")
private val REFRESH_RETROFIT = named("refresh_retrofit")
private val REFRESH_API = named("refresh_api")

val dataModule = module {
    // Scope (Kapsam)
    // 3 temel seçenek

    // Yaşam döngüsündeki bağımlılığın davranış biçimi

    // Single (Singleton) -> Uygulama yaşam döngüsü boyunca tek örnek.
    // Projede ihtiyaç duyulan her dependency için (data katmanı özelinde)
// tanımlama burada yapılır.
    single {
        Json {
            ignoreUnknownKeys = true // Cevapta var olan ama classta olmayan alanları ignore et.
            explicitNulls = false
            isLenient = true
        }
    }

    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    single {
        TokenStore(context = get())
    }

    single { AuthInterceptor(tokenStore = get()) }

    single {
        TokenAuthenticator(
            tokenStore = get(),
            refreshApiProvider = { get(REFRESH_API) }
        )
    }

    // Refresh Stack
    single(REFRESH_CLIENT) {
        OkHttpClient.Builder().addInterceptor(get<HttpLoggingInterceptor>()).build()
    }

    single(REFRESH_RETROFIT)
    {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get(REFRESH_CLIENT))
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
    }

    single(REFRESH_API)
    {
        get<Retrofit>(REFRESH_RETROFIT).create(AuthApi::class.java)
    }
    // Refresh Stack

    // HTTP isteklerini yönetmek..
    single {
        OkHttpClient.Builder()
            .addInterceptor(get<AuthInterceptor>())
            .authenticator(get<TokenAuthenticator>())
            .addInterceptor(get<HttpLoggingInterceptor>())            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get<OkHttpClient>())
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
    }

    single {
        get<Retrofit>().create(AuthApi::class.java)
    }

    single {
        get<Retrofit>().create(EventApi::class.java)
    }
    single<AuthRepository> {
        AuthRepositoryImpl(
            authApi = get(),
            tokenStore= get()
        )
    }

    // factory -> Her çağırıldığı noktada yeni instance üretir. Her fonksiyon için birer örnek

    // scoped -> Class -> tüm fonksiyonlarına 1 örnek
    single<EventRepository> {
        EventRepositoryImpl(
            eventApi = get()
        )
    }

    single {
        get<Retrofit>().create(PurchaseApi::class.java)
    }

    single<PurchaseRepository> {
        PurchaseRepositoryImpl(
            purchaseApi = get(),
            meApi = get()
        )
    }

    single {
        get<Retrofit>().create(MeApi::class.java)
    }

    single<TicketRepository> {
        TicketRepositoryImpl(
            meApi = get()
        )
    }

    single {
        get<Retrofit>().create(CheckinApi::class.java)
    }

    single<CheckinRepository> {
        CheckinRepositoryImpl(
            checkinApi = get()
        )
    }
}
