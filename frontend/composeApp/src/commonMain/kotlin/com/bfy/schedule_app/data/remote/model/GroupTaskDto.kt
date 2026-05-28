package com.bfy.schedule_app.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class GroupTaskDto(
    val id: String,
    val group_id: String,
    val title: String,
    val description: String? = null,
    val type: String? = null,
    val status: String, // "TODO", "IN_PROGRESS", "DONE", "OVERDUE"
    val priority: String? = null,
    val deadline: String? = null,
    val start_time: String? = null,
    val end_time: String? = null,
    val creator_id: String? = null,
    val assignees: List<String> = emptyList(),
    val created_at: String? = null
)
