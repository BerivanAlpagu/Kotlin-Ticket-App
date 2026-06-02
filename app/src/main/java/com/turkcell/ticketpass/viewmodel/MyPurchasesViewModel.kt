package com.turkcell.ticketpass.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.core.domain.purchase.Purchase
import com.turkcell.core.domain.purchase.PurchaseRepository
import com.turkcell.core.domain.purchase.PurchaseStatus
import com.turkcell.core.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyPurchasesUiState(
    val isLoading: Boolean = false,
    val purchases: List<Purchase> = emptyList(),
    val error: String? = null,
    // Hangi satın alım için ödeme işlemi yapılıyor (process death'e dayanıklı)
    val payingPurchaseId: String? = null,
    val payError: String? = null
)

class MyPurchasesViewModel(
    private val purchaseRepository: PurchaseRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(
        MyPurchasesUiState(
            // Process death sonrası ödeme durumunu geri yükle
            payingPurchaseId = savedStateHandle["payingPurchaseId"]
        )
    )
    val state: StateFlow<MyPurchasesUiState> = _state.asStateFlow()

    init {
        loadPurchases()
    }

    fun loadPurchases() {
        _state.update { it.copy(isLoading = true, error = null, payError = null) }
        viewModelScope.launch {
            purchaseRepository.getMyPurchases().fold(
                onSuccess = { list ->
                    _state.update { it.copy(purchases = list, isLoading = false) }
                    // Eğer ödeme yapılan purchase artık PAID ise state'i temizle
                    val payingId = _state.value.payingPurchaseId
                    if (payingId != null && list.any { it.id == payingId && it.status == PurchaseStatus.PAID }) {
                        clearPayingState()
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(isLoading = false, error = e.toUserMessage()) }
                }
            )
        }
    }

    fun pay(purchaseId: String) {
        // SavedStateHandle'a kaydet — process death olursa ödeme hangi purchase içindi bilinir
        savedStateHandle["payingPurchaseId"] = purchaseId
        _state.update { it.copy(payingPurchaseId = purchaseId, payError = null) }
        viewModelScope.launch {
            purchaseRepository.pay(purchaseId).fold(
                onSuccess = {
                    clearPayingState()
                    loadPurchases() // Listeyi güncelle
                },
                onFailure = { e ->
                    _state.update { it.copy(payingPurchaseId = null, payError = e.toUserMessage()) }
                    savedStateHandle.remove<String>("payingPurchaseId")
                }
            )
        }
    }

    fun clearPayError() {
        _state.update { it.copy(payError = null) }
    }

    private fun clearPayingState() {
        savedStateHandle.remove<String>("payingPurchaseId")
        _state.update { it.copy(payingPurchaseId = null) }
    }
}
