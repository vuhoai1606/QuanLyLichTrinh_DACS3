package com.bfy.schedule_app.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class GroupDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val avatar_url: String? = null,
    val leader_id: String,
    val role: String? = null,
    val created_at: String
)

@Serializable
data class CreateGroupRequest(
    val name: String,
    val description: String?,
    val avatar_url: String?,
    val leader_id: String,
    val members: List<GroupMemberRequest>
)

@Serializable
data class GroupMemberRequest(
    val user_id: String,
    val role: String
)
