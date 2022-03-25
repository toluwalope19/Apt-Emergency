package com.example.aptemergency.data.api

import com.example.aptemergency.data.model.AptResponse
import com.example.aptemergency.data.model.Request
import com.example.aptemergency.data.model.ApiResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("create")
    suspend fun sendEmergency(
        @Body request: Request
    ): AptResponse<ApiResponse>
}