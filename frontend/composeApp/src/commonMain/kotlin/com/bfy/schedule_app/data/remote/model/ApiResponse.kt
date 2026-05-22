package com.bfy.schedule_app.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val status: Int? = null,
    val success: Boolean? = null,
    val message: String? = null,
    val data: T? = null
)

@Serializable
data class AuthResponseData(
    val user: UserDto,
    val token: String,
    val refreshToken: String? = null
)
