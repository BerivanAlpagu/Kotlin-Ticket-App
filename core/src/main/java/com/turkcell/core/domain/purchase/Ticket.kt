package com.turkcell.core.domain.purchase

data class Ticket(
    val id: String,
    val qrCode: String,
    val status: TicketStatus,
    val ticketTypeId: String
)
