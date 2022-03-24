package com.example.aptemergency.data.model

data class Request(
    val phoneNumbers: List<String>,
    val image: String,
    val location: Location
)