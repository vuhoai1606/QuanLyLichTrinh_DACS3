package com.bfy.schedule_app.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class FocusStatsDto(
    val total_minutes: Int = 0,
    val completed_sessions: Int = 0,
    val today_minutes: Int = 0,
    val weekly_minutes: Int = 0,
    val current_streak: Int = 0,
    val total_exp: Int = 0,
    val rank: String = "Beginner"
)
