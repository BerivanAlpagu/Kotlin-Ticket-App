package com.turkcell.core.domain.purchase

data class Purchase(
    val id: String,
    val status: PurchaseStatus,
    val totalCents: Long,
    val createdAt: String,
    val paidAt: String?,
    val items: List<PurchaseItem>,
    val tickets: List<Ticket> = emptyList()
)
