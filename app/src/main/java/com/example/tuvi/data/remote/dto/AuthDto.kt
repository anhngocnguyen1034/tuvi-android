package com.example.tuvi.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    @SerialName("id_token") val idToken: String,
)

@Serializable
data class LoginResponse(
    val status: String? = null,
    val uid: String? = null,
    val email: String? = null,
    val name: String? = null,
    @SerialName("photo_url") val photoUrl: String? = null,
    val tokens: Int? = null,
)
