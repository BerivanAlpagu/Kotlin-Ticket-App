package com.turkcell.data.remote

import com.turkcell.data.dto.checkin.CheckinResultDto
import com.turkcell.data.dto.checkin.ScanRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface CheckinApi {
    @POST("/checkin/scan")
    suspend fun scan(@Body request: ScanRequestDto): CheckinResultDto
}
