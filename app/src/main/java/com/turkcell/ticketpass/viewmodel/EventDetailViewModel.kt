package com.turkcell.ticketpass.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.core.domain.event.Event
import com.turkcell.core.domain.event.EventRepository
import com.turkcell.core.domain.purchase.CreatePurchaseItemRequest
import com.turkcell.core.domain.purchase.Purchase
import com.turkcell.core.domain.purchase.PurchaseRepository
import com.turkcell.core.util.ApiException
import com.turkcell.core.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventDetailUiState(
    val isLoading: Boolean = false,
    val event: Event? = null,
    val error: String? = null,
    val selectedTickets: Map<String, Int> = emptyMap(),
    val isPurchasing: Boolean = false,
    val completedPurchase: Purchase? = null
) {
    val totalCents: Long get() = event?.ticketTypes?.sumOf { ticketType ->
        (selectedTickets[ticketType.id] ?: 0).toLong() * ticketType.priceCents
    } ?: 0L
}

class EventDetailViewModel(
    private val eventRepository: EventRepository,
    private val purchaseRepository: PurchaseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventId: String = checkNotNull(savedStateHandle["eventId"])

    private val _state = MutableStateFlow(EventDetailUiState())
    val state: StateFlow<EventDetailUiState> = _state.asStateFlow()

    init {
        loadEvent()
    }

    fun loadEvent() {
        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            eventRepository.getEventById(eventId).fold(
                onSuccess = { event ->
                    _state.update { it.copy(event = event, isLoading = false, error = null) }
                },
                onFailure = { e ->
                    _state.update { it.copy(isLoading = false, error = e.toUserMessage()) }
                }
            )
        }
    }

    fun updateTicketQuantity(ticketTypeId: String, delta: Int) {
        val currentEvent = _state.value.event ?: return
        val ticketType = currentEvent.ticketTypes.find { it.id == ticketTypeId } ?: return

        val currentQty = _state.value.selectedTickets[ticketTypeId] ?: 0
        val newQty = (currentQty + delta).coerceIn(0, minOf(20, ticketType.remaining.toInt()))

        val newMap = _state.value.selectedTickets.toMutableMap()
        if (newQty == 0) {
            newMap.remove(ticketTypeId)
        } else {
            newMap[ticketTypeId] = newQty
        }

        _state.update { it.copy(selectedTickets = newMap) }
    }

    fun buyTickets() {
        val selectedTickets = _state.value.selectedTickets
        if (selectedTickets.isEmpty() || _state.value.isPurchasing) return

        _state.update { it.copy(isPurchasing = true, error = null) }

        viewModelScope.launch {
            val items = selectedTickets.map { (ticketTypeId, quantity) ->
                CreatePurchaseItemRequest(ticketTypeId = ticketTypeId, quantity = quantity)
            }

            purchaseRepository.createPurchase(items).fold(
                onSuccess = { purchase ->
                    purchaseRepository.pay(purchase.id).fold(
                        onSuccess = { paidPurchase ->
                            _state.update {
                                it.copy(
                                    isPurchasing = false,
                                    completedPurchase = paidPurchase,
                                    selectedTickets = emptyMap()
                                )
                            }
                        },
                        onFailure = { e ->
                            _state.update { it.copy(isPurchasing = false, error = e.toUserMessage()) }
                            if (e is ApiException && e.code == 409 && e.errorMessage == "capacity_exceeded") {
                                loadEvent()
                            }
                        }
                    )
                },
                onFailure = { e ->
                    _state.update { it.copy(isPurchasing = false, error = e.toUserMessage()) }
                    if (e is ApiException && e.code == 409 && e.errorMessage == "capacity_exceeded") {
                        loadEvent()
                    }
                }
            )
        }
    }

    fun consumeError() = _state.update { it.copy(error = null) }

    fun consumeCompletedPurchase() = _state.update { it.copy(completedPurchase = null) }
}
