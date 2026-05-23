package com.turkcell.data.dto.event

import kotlinx.serialization.Serializable

@Serializable
data class EventDto(
    val id: String,
    val name: String,
    val description: String,
    val place: String?,
    val startsAt: String,
    val endsAt: String?,
    val ticketTypes: List<TicketTypeDto> = emptyList()
)