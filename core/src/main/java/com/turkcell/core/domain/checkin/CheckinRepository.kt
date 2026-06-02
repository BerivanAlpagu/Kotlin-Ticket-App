package com.turkcell.core.domain.checkin

interface CheckinRepository {
    suspend fun scan(qrCode: String): Result<CheckinResult>
}
