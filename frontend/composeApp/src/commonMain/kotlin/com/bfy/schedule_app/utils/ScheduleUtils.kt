package com.bfy.schedule_app.utils

import com.bfy.schedule_app.data.remote.model.ScheduleDto
import kotlinx.datetime.*

object ScheduleUtils {
    fun matchesDate(schedule: ScheduleDto, date: LocalDate): Boolean {
        val startStr = schedule.start_time ?: schedule.deadline ?: return false
        val startDate = try {
            Instant.parse(startStr).toLocalDateTime(TimeZone.currentSystemDefault()).date
        } catch (e: Exception) {
            return false
        }
        
        if (date < startDate) return false
        if (date == startDate) return true
        if (!schedule.is_recurring && schedule.recurrence_type.isNullOrEmpty()) return false
        
        return when (schedule.recurrence_type?.uppercase()) {
            "DAILY" -> true
            "MON_FRI" -> {
                val day = date.dayOfWeek
                day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY
            }
            "WEEKLY" -> date.dayOfWeek == startDate.dayOfWeek
            "MONTHLY" -> date.dayOfMonth == startDate.dayOfMonth
            "YEARLY" -> date.month == startDate.month && date.dayOfMonth == startDate.dayOfMonth
            else -> false
        }
    }

    fun isOverdue(schedule: ScheduleDto, now: Instant = Clock.System.now()): Boolean {
        if (schedule.status == "DONE") return false
        val deadlineStr = schedule.deadline
        if (deadlineStr != null) {
            try {
                return Instant.parse(deadlineStr) < now
            } catch (e: Exception) {}
        }
        if (schedule.type == "EVENT") {
            val endTimeStr = schedule.end_time
            if (endTimeStr != null) {
                try {
                    return Instant.parse(endTimeStr) < now
                } catch (e: Exception) {}
            }
        }
        return false
    }
}
