package com.turkcell.data.repository

import com.turkcell.core.domain.purchase.CreatePurchaseItemRequest
import com.turkcell.core.domain.purchase.Purchase
import com.turkcell.core.domain.purchase.PurchaseRepository
import com.turkcell.data.dto.purchase.CreatePurchaseRequestDto
import com.turkcell.data.dto.purchase.PurchaseItemRequestDto
import com.turkcell.data.mapper.toDomain
import com.turkcell.data.remote.MeApi
import com.turkcell.data.remote.PurchaseApi
import com.turkcell.data.util.runCatchingApi

class PurchaseRepositoryImpl(
    private val purchaseApi: PurchaseApi,
    private val meApi: MeApi
) : PurchaseRepository {

    override suspend fun getMyPurchases(): Result<List<Purchase>> =
        runCatchingApi { meApi.getMyPurchases() }.map { list -> list.map { it.toDomain() } }

    override suspend fun createPurchase(items: List<CreatePurchaseItemRequest>): Result<Purchase> {
        val requestDto = CreatePurchaseRequestDto(
            items = items.map { PurchaseItemRequestDto(ticketTypeId = it.ticketTypeId, quantity = it.quantity) }
        )
        return runCatchingApi { purchaseApi.createPurchase(requestDto) }.map { it.toDomain() }
    }

    override suspend fun pay(purchaseId: String): Result<Purchase> =
        runCatchingApi { purchaseApi.pay(purchaseId) }.map { it.toDomain() }

    override suspend fun getPurchase(purchaseId: String): Result<Purchase> =
        runCatchingApi { purchaseApi.getPurchase(purchaseId) }.map { it.toDomain() }
}

