package com.bfy.schedule_app.platform

interface ScheduleNotifier {
    fun scheduleAlarm(id: String, title: String, message: String, triggerAtMillis: Long, isAlarm: Boolean)
    fun cancelAlarm(id: String)
    fun startCountdown(id: String, title: String, targetMillis: Long)
    fun stopCountdown(id: String)
}

object ScheduleNotifierProvider {
    var notifier: ScheduleNotifier? = null
}

fun getReminderOffsetMillis(triggerType: String): Long {
    return when (triggerType) {
        "WHEN_STARTS" -> 0L
        "MIN_5" -> 5 * 60 * 1000L
        "MIN_10" -> 10 * 60 * 1000L
        "MIN_30" -> 30 * 60 * 1000L
        "HOUR_1" -> 60 * 60 * 1000L
        "DAY_1" -> 24 * 60 * 60 * 1000L
        "WEEK_1" -> 7 * 24 * 60 * 60 * 1000L
        else -> {
            // Handle CUSTOM or raw strings like "2 hours before"
            if (triggerType.contains("hours before")) {
                val hours = triggerType.substringBefore(" hours").toLongOrNull() ?: 2L
                hours * 60 * 60 * 1000L
            } else {
                0L
            }
        }
    }
}
