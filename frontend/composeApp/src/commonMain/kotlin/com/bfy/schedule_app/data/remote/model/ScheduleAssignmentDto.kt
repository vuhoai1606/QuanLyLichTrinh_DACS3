package com.bfy.schedule_app.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class ScheduleAssignmentDto(
    val id: String,
    val schedule_id: String,
    val user_id: String,
    val role: String,
    val is_completed: Boolean = false,
    val status: String = "PENDING"
)
