package com.turkcell.data.remote

import com.turkcell.data.dto.ticket.MyTicketDto
import retrofit2.http.GET
import retrofit2.http.Path

interface MeApi {
    @GET("/me/tickets")
    suspend fun getMyTickets(): List<MyTicketDto>

    @GET("/me/tickets/{id}")
    suspend fun getTicket(@Path("id") ticketId: String): MyTicketDto
}
