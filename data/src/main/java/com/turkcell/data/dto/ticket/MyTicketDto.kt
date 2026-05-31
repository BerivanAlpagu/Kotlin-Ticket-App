package com.turkcell.data.dto.ticket

import kotlinx.serialization.Serializable

@Serializable
data class MyTicketDto(
    val id: String,
    val qrCode: String,
    val status: String,
    val usedAt: String? = null,
    val ticketType: MyTicketTypeDto
)

@Serializable
data class MyTicketTypeDto(
    val id: String,
    val name: String,
    val priceCents: Long,
    val event: MyTicketEventDto
)

@Serializable
data class MyTicketEventDto(
    val id: String,
    val name: String,
    val place: String? = null,
    val startsAt: String
)
