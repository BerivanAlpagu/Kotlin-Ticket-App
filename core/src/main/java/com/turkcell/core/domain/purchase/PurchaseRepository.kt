package com.turkcell.core.domain.purchase

interface PurchaseRepository {
    suspend fun createPurchase(items: List<CreatePurchaseItemRequest>): Result<Purchase>
    suspend fun pay(purchaseId: String): Result<Purchase>
    suspend fun getPurchase(purchaseId: String): Result<Purchase>
}
