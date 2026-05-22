package com.turkcell.domain.event

interface EventRepository {
    suspend fun getEvents(): Result<List<Event>>
}