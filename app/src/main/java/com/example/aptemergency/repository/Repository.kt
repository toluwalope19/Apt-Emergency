package com.example.aptemergency.repository

import com.example.aptemergency.data.model.ApiResponse
import com.example.aptemergency.data.model.AptResponse
import com.example.aptemergency.data.model.Request
import com.google.gson.JsonObject

interface Repository {

   suspend fun sendEmergency(request: Request) : AptResponse<ApiResponse>

}