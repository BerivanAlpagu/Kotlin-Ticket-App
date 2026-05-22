package com.turkcell.ticketpass.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.ticketpass.viewmodel.RegisterViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun RegisterScreen(
    // koinViewModel() → Koin'den RegisterViewModel'i çekiyor.
    // Biz "new RegisterViewModel()" demiyoruz, Koin veriyor.
    // AppModule'a viewModelOf(::RegisterViewModel) eklediğimiz için bulabiliyor.
    viewModel: RegisterViewModel = koinViewModel(),

    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isRegistered) {
        if (state.isRegistered)
            onRegisterSuccess()
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Kayıt Ol", style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(24.dp))

            // state.email → ViewModel'deki state'ten geliyor (tek kaynak of truth).
            // onValueChange = viewModel::onEmailChange → her harf yazınca ViewModel'e bildir.
            OutlinedTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("Şifre") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )

            // API'den hata gelince ViewModel state'i günceller → ekran yeniden çizilir → hata görünür, hata yoksa render edilmez
            if (state.errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = state.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(Modifier.height(24.dp))
            Button(
                
                onClick = viewModel::submit,
                // state.canSubmit: email dolu + şifre 8+ karakter + yükleme yok ise true.
                // false ise buton disabled (gri) görünür, basılamaz.
                enabled = state.canSubmit,
                modifier = Modifier.fillMaxWidth(),
            ) {
                
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = LocalContentColor.current,
                    )
                } else {
                    Text("Kayıt Ol")
                }
            }

            Spacer(Modifier.height(24.dp))
            TextButton(onClick = onNavigateToLogin) {
                Text("Zaten hesabın var mı? Giriş yap")
            }
        }
    }
}
