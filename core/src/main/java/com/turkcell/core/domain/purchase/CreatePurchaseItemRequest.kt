package com.turkcell.core.domain.purchase

data class CreatePurchaseItemRequest(
    val ticketTypeId: String,
    val quantity: Int
)
