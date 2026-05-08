package com.turkcell.ticketpass
import android.app.Application
import com.turkcell.data.di.dataModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

// Uygulama başladığında Actvitiylerden önce oluşturulur.
// Singleton (Tek bir instance olarak memoryde kalır)
// Uygulama kapanana kadar yok edilmez..
class TicketAppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@TicketAppApplication) // Uygulama contexti verilir.
            modules(dataModule)

        }
    }
}