package com.turkcell.core.domain.auth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val isLoggedIn: Flow<Boolean>
    val authState: StateFlow<AuthState>

    suspend fun login(email: String, password: String): Result<AuthSession>
    suspend fun register(email: String, password: String): Result<AuthSession>
    suspend fun logout(): Result<Unit>
}