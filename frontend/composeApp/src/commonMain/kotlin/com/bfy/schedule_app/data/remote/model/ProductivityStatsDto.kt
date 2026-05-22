package com.bfy.schedule_app.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class ProductivityStatsDto(
    val focusHeatmap: Map<String, Int>,
    val completionRate: Float,
    val dailyTrend: Map<String, Int>,
    val totalFocusMinutes: Int,
    val completedTasksCount: Int
)
