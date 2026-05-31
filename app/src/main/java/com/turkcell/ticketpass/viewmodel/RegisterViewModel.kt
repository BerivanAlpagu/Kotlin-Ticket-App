package com.turkcell.ticketpass.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.core.domain.auth.AuthRepository
import com.turkcell.core.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegistered: Boolean = false
) {
    val canSubmit: Boolean get() = email.matches(emailRegex) && password.length in 8..128 && !isLoading
}


// Login gibi AuthRepository alıyor constructor'dan (Koin inject edecek bunu
class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, errorMessage = null) }

    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, errorMessage = null) }

    fun consumeError() = _state.update { it.copy(errorMessage = null) }

    fun submit() {
        val current = _state.value
        if (!current.canSubmit) return  // Guard clause - butona çift basmayı önler

        _state.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {

            authRepository.register(current.email, current.password)
                .onSuccess {

                    _state.update { it.copy(isLoading = false, isRegistered = true) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = error.toUserMessage(extraCodes = mapOf(409 to "Bu email zaten kayıtlı.")))
                    }
                }
        }
    }
}
