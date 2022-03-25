package com.example.aptemergency.utils



data class Resource<out T>(val status: Status, val data: T?, val message: String?) {

    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error( error: Throwable, data: T? = null): Resource<T> {
            return Resource(Status.ERROR, data,error.message)
        }

        fun <T> loading(data: T? = null): Resource<T> {
            return Resource(Status.LOADING, data, null)
        }

    }

    enum class Status {
        SUCCESS,
        ERROR,
        LOADING
    }
}


