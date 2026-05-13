package com.bfy.schedule_app.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val email: String? = null,
    val full_name: String,
    val avatar_url: String? = null,
    val bio: String? = null,
    val total_exp: Int = 0,
    val current_rank: String = "Rookie",
    val is_active: Boolean = true
)
