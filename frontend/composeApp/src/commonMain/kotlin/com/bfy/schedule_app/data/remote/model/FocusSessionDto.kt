package com.bfy.schedule_app.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class FocusSessionDto(
    val id: String,
    val user_id: String,
    val start_time: String,
    val end_time: String? = null,
    val duration_seconds: Int = 0,
    val status: String // "ONGOING", "COMPLETED"
)

@Serializable
data class FocusStatsDto(
    val completed_sessions: Int,
    val total_minutes: Int
)
