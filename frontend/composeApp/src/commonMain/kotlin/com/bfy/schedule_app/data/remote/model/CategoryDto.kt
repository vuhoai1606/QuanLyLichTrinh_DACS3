package com.bfy.schedule_app.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val id: String,
    val user_id: String,
    val name: String,
    val hex_color: String
)
