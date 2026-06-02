package com.turkcell.ticketpass.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.core.domain.checkin.CheckinRepository
import com.turkcell.core.domain.checkin.CheckinResult
import com.turkcell.core.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class CheckinError {
    data object NotFound : CheckinError()       // 404 — Bilet yok / sahte QR
    data object NotAssigned : CheckinError()    // 403 — Bu etkinliğe atanmamışsın
    data object AlreadyUsed : CheckinError()    // 409 — Bilet zaten kullanılmış
    data class Unknown(val message: String) : CheckinError()
}

data class CheckinUiState(
    val isScanning: Boolean = false,
    val lastResult: CheckinResult? = null,
    val error: CheckinError? = null
)

class CheckinViewModel(
    private val checkinRepository: CheckinRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(CheckinUiState())
    val state: StateFlow<CheckinUiState> = _state.asStateFlow()

    // Son başarılı check-in sonucunu process death'e karşı geri yükle
    init {
        val savedTicketId: String? = savedStateHandle["lastCheckinTicketId"]
        val savedEventName: String? = savedStateHandle["lastCheckinEventName"]
        val savedTicketType: String? = savedStateHandle["lastCheckinTicketType"]
        val savedVenue: String? = savedStateHandle["lastCheckinVenue"]
        val savedStartsAt: String? = savedStateHandle["lastCheckinStartsAt"]
        val savedCheckedInAt: String? = savedStateHandle["lastCheckinAt"]

        if (savedTicketId != null && savedEventName != null &&
            savedTicketType != null && savedVenue != null &&
            savedStartsAt != null && savedCheckedInAt != null) {
            _state.update {
                it.copy(
                    lastResult = CheckinResult(
                        ticketId = savedTicketId,
                        ticketType = savedTicketType,
                        eventName = savedEventName,
                        eventVenue = savedVenue,
                        eventStartsAt = savedStartsAt,
                        checkedInAt = savedCheckedInAt
                    )
                )
            }
        }
    }

    fun onQrScanned(qrCode: String) {
        if (_state.value.isScanning) return // çift tıklamayı engelle
        _state.update { it.copy(isScanning = true, error = null) }
        viewModelScope.launch {
            checkinRepository.scan(qrCode).fold(
                onSuccess = { result ->
                    // SavedStateHandle'a kaydet
                    savedStateHandle["lastCheckinTicketId"] = result.ticketId
                    savedStateHandle["lastCheckinEventName"] = result.eventName
                    savedStateHandle["lastCheckinTicketType"] = result.ticketType
                    savedStateHandle["lastCheckinVenue"] = result.eventVenue
                    savedStateHandle["lastCheckinStartsAt"] = result.eventStartsAt
                    savedStateHandle["lastCheckinAt"] = result.checkedInAt
                    _state.update { it.copy(isScanning = false, lastResult = result, error = null) }
                },
                onFailure = { e ->
                    val checkinError = parseError(e)
                    _state.update { it.copy(isScanning = false, error = checkinError) }
                }
            )
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun clearResult() {
        savedStateHandle.remove<String>("lastCheckinTicketId")
        savedStateHandle.remove<String>("lastCheckinEventName")
        savedStateHandle.remove<String>("lastCheckinTicketType")
        savedStateHandle.remove<String>("lastCheckinVenue")
        savedStateHandle.remove<String>("lastCheckinStartsAt")
        savedStateHandle.remove<String>("lastCheckinAt")
        _state.update { it.copy(lastResult = null) }
    }

    private fun parseError(e: Throwable): CheckinError {
        val msg = e.message ?: ""
        return when {
            msg.contains("404") || msg.contains("ticket_not_found") -> CheckinError.NotFound
            msg.contains("403") || msg.contains("not_assigned") -> CheckinError.NotAssigned
            msg.contains("409") || msg.contains("already_used") -> CheckinError.AlreadyUsed
            else -> CheckinError.Unknown(e.toUserMessage())
        }
    }
}
