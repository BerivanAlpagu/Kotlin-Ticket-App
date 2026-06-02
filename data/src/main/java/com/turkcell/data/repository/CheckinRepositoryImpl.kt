package com.turkcell.data.repository

import com.turkcell.core.domain.checkin.CheckinRepository
import com.turkcell.core.domain.checkin.CheckinResult
import com.turkcell.data.dto.checkin.ScanRequestDto
import com.turkcell.data.mapper.toDomain
import com.turkcell.data.remote.CheckinApi
import com.turkcell.data.util.runCatchingApi

class CheckinRepositoryImpl(
    private val checkinApi: CheckinApi
) : CheckinRepository {

    override suspend fun scan(qrCode: String): Result<CheckinResult> =
        runCatchingApi { checkinApi.scan(ScanRequestDto(qrCode = qrCode)) }
            .map { it.toDomain() }
}
