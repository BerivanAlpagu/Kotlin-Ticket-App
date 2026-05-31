package com.turkcell.ticketpass.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.core.domain.ticket.MyTicket
import com.turkcell.core.domain.ticket.TicketRepository
import com.turkcell.core.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyTicketsUiState(
    val isLoading: Boolean = false,
    val tickets: List<MyTicket> = emptyList(),
    val error: String? = null
)

class MyTicketsViewModel(
    private val ticketRepository: TicketRepository
) : ViewModel() {
    private val _state = MutableStateFlow(MyTicketsUiState())
    val state: StateFlow<MyTicketsUiState> = _state.asStateFlow()

    init {
        loadTickets()
    }

    fun loadTickets() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            ticketRepository.getMyTickets().fold(
                onSuccess = { list ->
                    _state.update { it.copy(tickets = list, isLoading = false, error = null) }
                },
                onFailure = { e ->
                    _state.update { it.copy(isLoading = false, error = e.toUserMessage()) }
                }
            )
        }
    }
}
