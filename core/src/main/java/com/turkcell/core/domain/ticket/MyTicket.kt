package com.turkcell.core.domain.ticket

data class MyTicket(
    val id: String,
    val qrCode: String,
    val status: String,
    val usedAt: String?,
    val ticketType: MyTicketType
)

data class MyTicketType(
    val id: String,
    val name: String,
    val priceCents: Long,
    val event: MyTicketEvent
)

data class MyTicketEvent(
    val id: String,
    val name: String,
    val venue: String,
    val startsAt: String
)
