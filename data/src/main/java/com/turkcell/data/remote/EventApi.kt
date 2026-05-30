package com.turkcell.data.remote

import com.turkcell.data.dto.event.EventDto
import retrofit2.http.GET
import retrofit2.http.Path

interface EventApi {
    @GET("/events")
    suspend fun getEvents(): List<EventDto>

    @GET("/events/{id}")
    suspend fun getEventById(@Path("id") id: String): EventDto
}