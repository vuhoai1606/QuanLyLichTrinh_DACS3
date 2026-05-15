package com.bfy.schedule_app.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class LeaderboardEntryDto(
    val rank: Int,
    val id: String,
    val full_name: String,
    val avatar_url: String? = null,
    val total_exp: Int,
    val current_rank: String
)
