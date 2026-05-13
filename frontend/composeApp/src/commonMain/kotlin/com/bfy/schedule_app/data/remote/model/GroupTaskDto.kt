package com.bfy.schedule_app.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class GroupTaskDto(
    val id: String,
    val group_id: String,
    val title: String,
    val description: String? = null,
    val status: String, // "TODO", "IN_PROGRESS", "DONE", "OVERDUE"
    val deadline: String? = null,
    val assignees: List<String> = emptyList()
)
