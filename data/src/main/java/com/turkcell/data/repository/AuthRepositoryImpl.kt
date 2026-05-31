package com.turkcell.data.repository

import com.turkcell.core.domain.auth.AuthRepository
import com.turkcell.core.domain.auth.AuthSession
import com.turkcell.core.domain.auth.User
import com.turkcell.core.domain.auth.UserRole
import com.turkcell.data.dto.auth.CredentialsDto
import com.turkcell.data.dto.auth.RefreshRequestDto
import com.turkcell.data.local.TokenStore
import com.turkcell.data.remote.AuthApi
import com.turkcell.data.util.runCatchingApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val tokenStore: TokenStore
) : AuthRepository {
    override val isLoggedIn: Flow<Boolean> = tokenStore.accessToken.map{ it != null }

    override suspend fun login(
        email: String,
        password: String
    ): Result<AuthSession> = runCatchingApi {
        authApi.login(CredentialsDto(email=email, password=password))
    }.onSuccess {
        tokenStore.save(it.accessToken, it.refreshToken)
    }
        .map {
                tokenPairDto -> AuthSession(
            user = User(
                tokenPairDto.user.id, tokenPairDto.user.email, UserRole.fromApi(tokenPairDto.user.role),
            ),
            accessToken = tokenPairDto.accessToken,
            refreshToken = tokenPairDto.refreshToken)
        }

    /// backend -> (TokenPairDto) accessToken
    /// backend -> (TokenPairDto) jwt
    /// backend -> (TokenPairDto) accessToken -> (AuthSession) accessToken -> Tüm Uygulama
    /// backend -> (TokenPairDto) jwt -> (AuthSession) accessToken -> Tüm Uygulama

    override suspend fun register(
        email: String,
        password: String
    ): Result<AuthSession> = runCatchingApi {
        authApi.register(CredentialsDto(email=email, password=password))
    }.onSuccess {
        tokenStore.save(it.accessToken, it.refreshToken)
    }
        .map {
                i -> AuthSession(
            user = User(
                i.user.id, i.user.email, UserRole.fromApi(i.user.role),
            ),
            accessToken = i.accessToken,
            refreshToken = i.refreshToken)
        }


    override suspend fun logout(): Result<Unit> {
        val refresh = tokenStore.refreshTokenBlocking()
        return runCatchingApi {
            if (refresh != null) {
                authApi.logout(RefreshRequestDto(refresh))
            }
        }.also {
            tokenStore.clear()
        }.map {}
    }
}