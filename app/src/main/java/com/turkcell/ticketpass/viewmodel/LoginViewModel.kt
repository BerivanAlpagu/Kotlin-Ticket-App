package com.turkcell.ticketpass.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.core.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.turkcell.core.domain.auth.AuthRepository

data class LoginUiState(val email: String = "",
                        val password: String = "",
                        val isLoading: Boolean = false,
                        val errorMessage: String? = null,
                        val isLoggedIn: Boolean = false
) {
    val canSubmit: Boolean get() = email.isNotBlank() && password.length >= 8 && !isLoading
}

class LoginViewModel(
    private val authRepository: AuthRepository //bağımlılık
) : ViewModel() {
    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    // Kullanıcı email alanına yazınca çağrılır, errorMessage sıfırlanır.
    fun onEmailChange(value: String) = _state.update { it.copy(email = value, errorMessage = null) }

    // Kullanıcı şifre alanına yazınca çağrılır.
    fun onPasswordChange(value: String) =
        _state.update { it.copy(password = value, errorMessage = null) }

    fun consumeError() = _state.update { it.copy(errorMessage = null) }

    fun submit() {
        val current = _state.value
        if (!current.canSubmit) return // Guard clause - butona çift basmayı önler

        _state.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            authRepository.login(current.email, current.password)
                .onSuccess { _state.update { it.copy(isLoading = false, isLoggedIn = true) } }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.toUserMessage()
                        )
                    }
                }
        }
    }
}
