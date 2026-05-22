package com.turkcell.core.domain.auth

data class AuthSession(val user: User, val accessToken: String, val refreshToken: String) {}