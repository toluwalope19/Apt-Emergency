package com.example.aptemergency.repository

import com.example.aptemergency.data.api.RemoteSource
import com.example.aptemergency.data.model.ApiResponse
import com.example.aptemergency.data.model.AptResponse
import com.example.aptemergency.data.model.Request
import com.google.gson.JsonObject

class EmergencyRepository(
    private val remoteSource: RemoteSource
): Repository {
    override suspend fun sendEmergency(request: Request): AptResponse<ApiResponse> {
       return remoteSource.sendEmergency(request)
    }
}