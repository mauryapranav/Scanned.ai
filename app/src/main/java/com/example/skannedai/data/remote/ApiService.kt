package com.example.skannedai.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

data class ScanRequest(
    val text: String,
    val mode: String,
    val question: String = ""
)

data class ScanResponse(
    val result: String
)

interface ApiService {
    @POST("process")
    suspend fun process(@Body request: ScanRequest): ScanResponse
}
