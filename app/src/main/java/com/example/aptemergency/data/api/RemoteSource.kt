package com.example.aptemergency.data.api


import com.example.aptemergency.data.model.ApiResponse
import com.example.aptemergency.data.model.AptResponse
import com.example.aptemergency.data.model.Request
import com.google.gson.JsonObject
import retrofit2.Response

interface RemoteSource {
    suspend fun sendEmergency(request: Request) : AptResponse<ApiResponse>
}