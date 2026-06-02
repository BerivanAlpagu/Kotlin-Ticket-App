package com.turkcell.data.mapper

import com.turkcell.core.domain.checkin.CheckinResult
import com.turkcell.data.dto.checkin.CheckinResultDto

internal fun CheckinResultDto.toDomain(): CheckinResult = CheckinResult(
    ticketId = ticketId,
    ticketType = ticketType,
    eventName = event.name,
    eventVenue = event.place,   // DTO'da place, domain'de venue (API.MD uyumlu)
    eventStartsAt = event.startsAt,
    checkedInAt = checkedInAt
)

