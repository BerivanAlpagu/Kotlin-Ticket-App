package com.turkcell.ticketpass.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.core.domain.auth.AuthRepository
import com.turkcell.core.domain.event.Event
import com.turkcell.core.domain.event.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isEventsLoading: Boolean = false,
    val isEventsRefreshing: Boolean = false,
    val events: List<Event> = emptyList(),
    val eventsError: String? = null
)

class HomeViewModel(
    private val eventRepository: EventRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    init {
        loadEvents()
    }

    fun loadEvents() {
        if (_state.value.isEventsLoading) return

        _state.update { it.copy(isEventsLoading = true, eventsError = null) }

        fetchEvents()
    }

    fun refreshEvents() {
        if (_state.value.isEventsRefreshing) return

        _state.update { it.copy(isEventsRefreshing = true, eventsError = null) }

        fetchEvents()
    }

    private fun fetchEvents() {
        viewModelScope.launch {
            eventRepository.getEvents().fold(
                onSuccess = {
                        list -> _state.update { it.copy(events = list, isEventsLoading = false, eventsError = null)}
                },
                onFailure = {
                        e -> _state.update { it.copy(isEventsLoading = false, eventsError = e.message ?: "Etkinlikler yüklenemedi.") }
                }
            )
        }
    }

}