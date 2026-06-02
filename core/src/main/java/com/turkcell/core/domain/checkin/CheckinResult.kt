package com.turkcell.core.domain.checkin

data class CheckinResult(
    val ticketId: String,
    val ticketType: String,
    val eventName: String,
    val eventVenue: String,
    val eventStartsAt: String,
    val checkedInAt: String
)
