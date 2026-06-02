package com.turkcell.core.domain.purchase

interface PurchaseRepository {
    suspend fun getMyPurchases(): Result<List<Purchase>>
    suspend fun createPurchase(items: List<CreatePurchaseItemRequest>): Result<Purchase>
    suspend fun pay(purchaseId: String): Result<Purchase>
    suspend fun getPurchase(purchaseId: String): Result<Purchase>
}

