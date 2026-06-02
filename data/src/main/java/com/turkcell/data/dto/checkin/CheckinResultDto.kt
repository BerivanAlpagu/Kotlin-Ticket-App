package com.turkcell.data.dto.checkin

import kotlinx.serialization.Serializable

@Serializable
data class CheckinResultDto(
    val ticketId: String,
    val ticketType: String,
    val event: CheckinEventDto,
    val checkedInAt: String
)

@Serializable
data class CheckinEventDto(
    val id: String,
    val name: String,
    val place: String,      // API'de "venue" değil "place" geliyor
    val startsAt: String
)
