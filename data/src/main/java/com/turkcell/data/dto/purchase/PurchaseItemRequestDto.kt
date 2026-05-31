package com.turkcell.data.dto.purchase

import kotlinx.serialization.Serializable

@Serializable
data class PurchaseItemRequestDto(
    val ticketTypeId: String,
    val quantity: Int
)
