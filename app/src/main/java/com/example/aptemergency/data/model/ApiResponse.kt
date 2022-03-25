package com.example.aptemergency.data.model

import androidx.annotation.Keep

@Keep
data class AptResponse<out T>(val status: String?, val message: String?, val data: T?)



data class ApiResponse(
    val status: String,
    val data: DataResponse,
    val message: String
)


data class DataResponse(
    val phoneNumbers: List<String>,
    val image: String,
    val location: Location,
    val id: Long
)