package com.turkcell.ticketpass.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.core.domain.event.Event
import com.turkcell.core.domain.event.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventDetailUiState(
    val isLoading: Boolean = false,
    val event: Event? = null,
    val error: String? = null,
    val selectedTickets: Map<String, Int> = emptyMap()
) {
    val totalCents: Long get() = event?.ticketTypes?.sumOf { ticketType ->
        (selectedTickets[ticketType.id] ?: 0).toLong() * ticketType.priceCents
    } ?: 0L
}

class EventDetailViewModel(
    private val eventRepository: EventRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventId: String = checkNotNull(savedStateHandle["eventId"])

    private val _state = MutableStateFlow(EventDetailUiState())
    val state: StateFlow<EventDetailUiState> = _state.asStateFlow()

    init {
        loadEvent()
    }

    fun loadEvent() {
        if (_state.value.isLoading) return

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            eventRepository.getEventById(eventId).fold(
                onSuccess = { event ->
                    _state.update { it.copy(event = event, isLoading = false, error = null) }
                },
                onFailure = { e ->
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Etkinlik yüklenemedi.") }
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
}
