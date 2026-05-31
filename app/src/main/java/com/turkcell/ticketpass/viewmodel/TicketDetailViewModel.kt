package com.turkcell.ticketpass.viewmodel

import androidx.lifecycle.SavedStateHandle
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

data class TicketDetailUiState(
    val isLoading: Boolean = false,
    val ticket: MyTicket? = null,
    val error: String? = null
)

class TicketDetailViewModel(
    private val ticketRepository: TicketRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val ticketId: String = checkNotNull(savedStateHandle["ticketId"])

    private val _state = MutableStateFlow(TicketDetailUiState())
    val state: StateFlow<TicketDetailUiState> = _state.asStateFlow()

    init {
        loadTicket()
    }

    fun loadTicket() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            ticketRepository.getTicket(ticketId).fold(
                onSuccess = { ticket ->
                    _state.update { it.copy(ticket = ticket, isLoading = false, error = null) }
                },
                onFailure = { e ->
                    _state.update { it.copy(isLoading = false, error = e.toUserMessage()) }
                }
            )
        }
    }
}
