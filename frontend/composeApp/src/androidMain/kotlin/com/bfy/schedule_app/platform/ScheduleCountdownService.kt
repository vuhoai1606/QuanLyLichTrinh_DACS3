package com.bfy.schedule_app.platform

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.bfy.schedule_app.MainActivity
import java.util.concurrent.ConcurrentHashMap

class ScheduleCountdownService : Service() {

    companion object {
        const val ACTION_START = "com.bfy.schedule_app.START_COUNTDOWN"
        const val ACTION_STOP = "com.bfy.schedule_app.STOP_COUNTDOWN"
        const val CHANNEL_ID = "schedule_countdown_channel"
        const val NOTIFICATION_ID_BASE = 2000
    }

    private val activeTimers = ConcurrentHashMap<String, CountDownTimer>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY
        val id = intent.getStringExtra("EXTRA_ID") ?: return START_NOT_STICKY

        when (action) {
            ACTION_START -> {
                val title = intent.getStringExtra("EXTRA_TITLE") ?: "Task Countdown"
                val targetMillis = intent.getLongExtra("EXTRA_TARGET_MILLIS", 0L)
                startCountdown(id, title, targetMillis, startId)
            }
            ACTION_STOP -> {
                stopCountdown(id)
                if (activeTimers.isEmpty()) {
                    stopSelf()
                }
            }
        }

        return START_STICKY
    }

    private fun startCountdown(id: String, title: String, targetMillis: Long, startId: Int) {
        val millisInFuture = targetMillis - System.currentTimeMillis()
        if (millisInFuture <= 0) return

        val notificationId = NOTIFICATION_ID_BASE + id.hashCode() % 1000

        // If this is the first timer, we must startForeground
        val notification = createNotification(title, formatTime(millisInFuture))
        if (activeTimers.isEmpty()) {
            startForeground(notificationId, notification)
        }

        activeTimers[id]?.cancel()

        val timer = object : CountDownTimer(millisInFuture, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateNotification(notificationId, title, formatTime(millisUntilFinished))
            }

            override fun onFinish() {
                activeTimers.remove(id)
                updateNotification(notificationId, title, "00:00:00 - Time's up!")
                if (activeTimers.isEmpty()) {
                    stopForeground(false)
                    stopSelf()
                }
            }
        }
        activeTimers[id] = timer
        timer.start()
    }

    private fun stopCountdown(id: String) {
        activeTimers[id]?.cancel()
        activeTimers.remove(id)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID_BASE + id.hashCode() % 1000)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Schedule Countdowns",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live countdown for tasks and events"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(title: String, timeText: String): android.app.Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("Time remaining: $timeText")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(notificationId: Int, title: String, timeText: String) {
        val notification = createNotification(title, timeText)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)
    }

    private fun formatTime(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60)) % 24
        val days = millis / (1000 * 60 * 60 * 24)

        return if (days > 0) {
            String.format("%dd %02d:%02d:%02d", days, hours, minutes, seconds)
        } else {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activeTimers.values.forEach { it.cancel() }
        activeTimers.clear()
    }
}
