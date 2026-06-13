package com.bfy.schedule_app.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class ExternalEventDto(
    val title: String,
    val description: String? = null,
    val location: String? = null,
    val start_time: String,
    val end_time: String? = null,
    val is_all_day: Boolean = false,
    val type: String = "EVENT",
    val external_id: String? = null,
    val external_source: String? = null,
    val updated_at: String? = null
)
