package com.turkcell.domain.event

class TicketType {
    val id: String,
    val name: String,
    val priceCents: Long,
    val capacity: Long,
    val soldCount: Long,
    val remaining: Long,
}