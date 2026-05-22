package com.bfy.schedule_app.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class ScheduleDto(
    val id: String,
    val creator_id: String,
    val title: String,
    val description: String? = null,
    val location: String? = null,
    val type: String, // "TODO", "TASK", "EVENT"
    val priority: String, // "LOW", "MEDIUM", "HIGH"
    val start_time: String? = null,
    val end_time: String? = null,
    val deadline: String? = null,
    val status: String, // "PENDING", "DOING", "DONE"
    val is_all_day: Boolean = false,
    val rrule: String? = null,
    val is_recurring: Boolean = false,
    val recurrence_type: String? = null, // "DAILY", "WEEKLY", "MONTHLY"
    val reminders: List<ReminderDto> = emptyList(),
    val category_name: String? = null,
    val category_color: String? = null,
    val is_countdown_enabled: Boolean = false,
    val group_id: String? = null,
    val assignees: List<String>? = null
)

@Serializable
data class ReminderDto(
    val trigger_type: String, // "WHEN_STARTS", "MIN_5", "MIN_10", etc.
    val is_alarm: Boolean = false
)

@Serializable
data class UpdateScheduleRequest(
    val title: String? = null,
    val description: String? = null,
    val type: String? = null,
    val priority: String? = null,
    val start_time: String? = null,
    val end_time: String? = null,
    val deadline: String? = null,
    val status: String? = null,
    val is_all_day: Boolean? = null,
    val is_recurring: Boolean? = null,
    val recurrence_type: String? = null,
    val category_name: String? = null,
    val is_countdown_enabled: Boolean? = null,
    val reminders: List<ReminderDto>? = null
)
