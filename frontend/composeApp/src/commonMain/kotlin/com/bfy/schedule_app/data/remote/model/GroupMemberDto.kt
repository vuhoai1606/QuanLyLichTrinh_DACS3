package com.bfy.schedule_app.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class GroupMemberDto(
    val id: String,
    val email: String? = null,
    val full_name: String,
    val avatar_url: String? = null,
    val bio: String? = null,
    val role: String = "MEMBER",
    val joined_at: String
)
