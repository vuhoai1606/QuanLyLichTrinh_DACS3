package com.bfy.schedule_app.platform

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat

class AndroidScheduleNotifier(private val context: Context) : ScheduleNotifier {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun scheduleAlarm(id: String, title: String, message: String, triggerAtMillis: Long, isAlarm: Boolean) {
        if (triggerAtMillis <= System.currentTimeMillis()) {
            if (System.currentTimeMillis() - triggerAtMillis > 5 * 60 * 1000) return
        }

        val intent = Intent(context, ScheduleAlarmReceiver::class.java).apply {
            action = "com.bfy.schedule_app.ACTION_SCHEDULE_ALARM"
            putExtra("EXTRA_ID", id)
            putExtra("EXTRA_TITLE", title)
            putExtra("EXTRA_MESSAGE", message)
            putExtra("EXTRA_IS_ALARM", isAlarm)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            e.printStackTrace() // Can happen if exact alarm permission is denied in Android 12+
        }
    }

    override fun cancelAlarm(id: String) {
        val intent = Intent(context, ScheduleAlarmReceiver::class.java).apply {
            action = "com.bfy.schedule_app.ACTION_SCHEDULE_ALARM"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    override fun startCountdown(id: String, title: String, targetMillis: Long) {
        val intent = Intent(context, ScheduleCountdownService::class.java).apply {
            action = ScheduleCountdownService.ACTION_START
            putExtra("EXTRA_ID", id)
            putExtra("EXTRA_TITLE", title)
            putExtra("EXTRA_TARGET_MILLIS", targetMillis)
        }
        ContextCompat.startForegroundService(context, intent)
    }

    override fun stopCountdown(id: String) {
        val intent = Intent(context, ScheduleCountdownService::class.java).apply {
            action = ScheduleCountdownService.ACTION_STOP
            putExtra("EXTRA_ID", id)
        }
        ContextCompat.startForegroundService(context, intent)
    }
}
