package com.bfy.schedule_app.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class BadgeDto(
    val id: String,
    val name: String,
    val description: String,
    val icon_url: String? = null,
    val type: String,
    val unlocked_at: String? = null
)
