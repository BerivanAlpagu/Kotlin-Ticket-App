package com.turkcell.data.repository

import com.turkcell.core.domain.auth.AuthRepository
import com.turkcell.core.domain.auth.AuthSession
import com.turkcell.core.domain.auth.AuthState
import com.turkcell.core.domain.auth.User
import com.turkcell.core.domain.auth.UserRole
import com.turkcell.data.dto.auth.CredentialsDto
import com.turkcell.data.dto.auth.RefreshRequestDto
import com.turkcell.data.local.TokenStore
import com.turkcell.data.remote.AuthApi
import com.turkcell.data.util.runCatchingApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val tokenStore: TokenStore
) : AuthRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val isLoggedIn: Flow<Boolean> = tokenStore.accessToken.map { it != null }

    override val authState: StateFlow<AuthState> = combine(
        tokenStore.accessToken,
        tokenStore.userRole
    ) { token, role ->
        if (token == null) {
            AuthState.Unauthenticated
        } else {
            AuthState.Authenticated(
                user = User(
                    id = "",
                    email = "",
                    role = UserRole.fromApi(role)
                )
            )
        }
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = AuthState.Loading
    )

    override suspend fun login(
        email: String,
        password: String
    ): Result<AuthSession> = runCatchingApi {
        authApi.login(CredentialsDto(email = email, password = password))
    }.onSuccess {
        tokenStore.save(it.accessToken, it.refreshToken, it.user.role)

    }.map { dto ->
        AuthSession(
            user = User(dto.user.id, dto.user.email, UserRole.fromApi(dto.user.role)),
            accessToken = dto.accessToken,
            refreshToken = dto.refreshToken
        )
    }

    override suspend fun register(
        email: String,
        password: String
    ): Result<AuthSession> = runCatchingApi {
        authApi.register(CredentialsDto(email = email, password = password))
    }.onSuccess {
        tokenStore.save(it.accessToken, it.refreshToken, it.user.role)
    }.map { dto ->
        AuthSession(
            user = User(dto.user.id, dto.user.email, UserRole.fromApi(dto.user.role)),
            accessToken = dto.accessToken,
            refreshToken = dto.refreshToken
        )
    }

    override suspend fun logout(): Result<Unit> {
        val refresh = tokenStore.refreshTokenBlocking()
        return runCatchingApi {
            if (refresh != null) authApi.logout(RefreshRequestDto(refresh))
        }.also {
            tokenStore.clear()
        }.map {}
    }
}