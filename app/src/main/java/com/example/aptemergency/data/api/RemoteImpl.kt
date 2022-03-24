package com.example.aptemergency.data.api

import com.example.aptemergency.data.api.RemoteSource
import com.example.aptemergency.data.model.AptResponse
import com.example.aptemergency.data.model.Request
import com.google.gson.JsonObject
import retrofit2.Response
import javax.inject.Inject

class RemoteImpl @Inject constructor(private val apiService: ApiService) :
    RemoteSource {

    override suspend fun sendEmergency(request: Request): AptResponse<JsonObject> {
        return apiService.sendEmergency(request)
    }
}