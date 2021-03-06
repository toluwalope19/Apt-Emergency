package com.example.aptemergency.utils

import android.content.Context
import com.example.aptemergency.R
import com.example.aptemergency.utils.exceptions.NoNetworkException
import com.example.aptemergency.utils.exceptions.ServerException
import com.google.gson.Gson
import retrofit2.HttpException
import javax.inject.Inject

class ApiError @Inject constructor(private val gson: Gson, private val context: Context) {


    fun extractErrorMessage(throwable: Throwable?): String {

        return when (throwable) {
            is HttpException -> {
                val response = throwable.response()
                val json = response?.errorBody()?.string()

                json?.let {
                    val error = gson.fromJson(json, ErrorResponse::class.java)
                    return@let if (error.message.isNotEmpty()) error.message
                    else context.getString(R.string.unknown_error_message)
                } ?: context.getString(R.string.unknown_error_message)
            }

            is NoNetworkException -> context.getString(R.string.check_connectivity_message)

            is ServerException -> context.getString(R.string.can_not_connect_to_sever_message)

            else -> context.getString(R.string.unknown_error_message)

        }
    }


    data class ErrorResponse(
        val success: String,
        val message: String
    )
}
class ValidationException (message:String)  : Exception(message)