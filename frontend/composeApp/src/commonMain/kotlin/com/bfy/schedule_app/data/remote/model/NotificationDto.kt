package com.bfy.schedule_app.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class NotificationDto(
    val id: String,
    val user_id: String,
    val title: String,
    val message: String,
    val type: String,
    val ia_read: Boolean,
    val created_at: String
)
