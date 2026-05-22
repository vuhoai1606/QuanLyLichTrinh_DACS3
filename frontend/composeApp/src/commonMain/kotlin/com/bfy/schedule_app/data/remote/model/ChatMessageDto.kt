package com.bfy.schedule_app.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageDto(
    val id: String,
    val groupId: String,
    val senderId: String,
    val message: String,
    val type: String, // TEXT, IMAGE, FILE
    val sender: UserDto? = null,
    val created_at: String
)
