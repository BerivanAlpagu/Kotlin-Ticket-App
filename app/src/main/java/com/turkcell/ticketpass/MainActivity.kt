package com.turkcell.ticketpass

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.turkcell.core.domain.AuthRepository
import com.turkcell.core.ui.theme.TicketAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    // Koin'den AuthRepository'yi alıp "by inject()" diyerek Koin'e "bunu sen ver" dedim
    private val authRepository: AuthRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //API isteği atıyorum
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("TicketPass", ">>> Login isteği atılıyor")
        }

        setContent {
            TicketAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Text("App is ready")
                }
            }
        }
    }
}

