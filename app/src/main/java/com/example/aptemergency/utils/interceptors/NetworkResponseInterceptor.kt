package com.example.aptemergency.utils.interceptors

import com.example.aptemergency.utils.exceptions.NetworkException
import com.example.aptemergency.utils.exceptions.ServerException
import okhttp3.Interceptor
import okhttp3.Response

class NetworkResponseInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request().newBuilder().build())
        when (response.code) {
            in 400..499 -> throw NetworkException(response.code, response.message)
            in 500..599 -> throw ServerException(response.code, response.message)
        }
        return response
    }
}