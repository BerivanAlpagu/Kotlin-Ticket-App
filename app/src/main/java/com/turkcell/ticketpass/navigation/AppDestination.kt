package com.turkcell.ticketpass.navigation

import kotlinx.serialization.Serializable

@Serializable object Login
@Serializable object Register
@Serializable object Home
@Serializable data class EventDetail(val eventId: String)
@Serializable object MyTickets
@Serializable data class TicketDetail(val ticketId: String)
@Serializable object MyPurchases
@Serializable object Checkin