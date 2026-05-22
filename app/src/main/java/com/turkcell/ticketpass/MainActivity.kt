package com.turkcell.ticketpass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.turkcell.core.domain.auth.AuthRepository
import com.turkcell.core.ui.theme.TicketAppTheme
import com.turkcell.ticketpass.navigation.AppNavHost
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    // Koin'den AuthRepository'yi alıp "by inject()" diyerek Koin'e "bunu sen ver" dedim
    private val authRepository: AuthRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TicketAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Text("App is ready")
                }
                AppNavHost()
            }
        }
    }
}

