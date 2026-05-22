package com.turkcell.data.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class RefreshRequestDto(val refreshToken: String)

// {"refreshToken":"abc"}