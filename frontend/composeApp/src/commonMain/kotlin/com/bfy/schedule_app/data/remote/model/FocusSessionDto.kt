package com.bfy.schedule_app.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class FocusSessionDto(
    val id: String,
    val user_id: String,
    val duration_minutes: Int,
    val status: String,
    val is_strict_mode: Boolean = false,
    val exp_earned: Int,
    val created_at: String
)
