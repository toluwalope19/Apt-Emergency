package com.example.aptemergency.data.api

import com.example.aptemergency.data.model.AptResponse
import com.example.aptemergency.data.model.Request
import com.google.gson.JsonObject
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST
    suspend fun sendEmergency(
        @Body request: Request
    ): AptResponse<JsonObject>
}