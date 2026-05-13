package com.bfy.schedule_app.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class GroupDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val leader_id: String,
    val created_at: String
)
