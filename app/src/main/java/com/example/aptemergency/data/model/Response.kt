package com.example.aptemergency.data.model

import androidx.annotation.Keep

@Keep
data class AptResponse<out T>(val status: String?, val message: String?, val data: T?)
