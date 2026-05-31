package com.turkcell.data.dto.purchase

import kotlinx.serialization.Serializable

@Serializable
data class PurchaseDto(
    val id: String,
    val status: String,
    val totalCents: Long,
    val createdAt: String,
    val paidAt: String? = null,
    val items: List<PurchaseItemDto> = emptyList(),
    val tickets: List<TicketDto> = emptyList()
)
