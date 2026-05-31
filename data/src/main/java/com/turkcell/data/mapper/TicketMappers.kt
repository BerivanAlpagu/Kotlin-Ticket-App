package com.turkcell.data.mapper

import com.turkcell.core.domain.ticket.MyTicket
import com.turkcell.core.domain.ticket.MyTicketEvent
import com.turkcell.core.domain.ticket.MyTicketType
import com.turkcell.data.dto.ticket.MyTicketDto
import com.turkcell.data.dto.ticket.MyTicketEventDto
import com.turkcell.data.dto.ticket.MyTicketTypeDto

internal fun MyTicketDto.toDomain(): MyTicket = MyTicket(
    id = id,
    qrCode = qrCode,
    status = status,
    usedAt = usedAt,
    ticketType = ticketType.toDomain()
)

internal fun MyTicketTypeDto.toDomain(): MyTicketType = MyTicketType(
    id = id,
    name = name,
    priceCents = priceCents,
    event = event.toDomain()
)

internal fun MyTicketEventDto.toDomain(): MyTicketEvent = MyTicketEvent(
    id = id,
    name = name,
    venue = place.orEmpty(),
    startsAt = startsAt
)
