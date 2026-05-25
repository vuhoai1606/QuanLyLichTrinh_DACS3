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

class FocusForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "focus_channel_v4"
        const val NOTIFICATION_ID = 1001
        const val RESULT_NOTIFICATION_ID = 1002
        const val COUNTDOWN_NOTIFICATION_ID = 1003
        const val ACTION_GIVE_UP = "com.bfy.schedule_app.GIVE_UP_FOCUS"
        const val ACTION_CONTINUE = "com.bfy.schedule_app.CONTINUE_FOCUS"
    }

    private var countDownTimer: CountDownTimer? = null
    private var targetMinutes: Int = 0
    private var timeLeftSeconds: Int = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        targetMinutes = intent?.getIntExtra("targetMinutes", 25) ?: 25
        timeLeftSeconds = intent?.getIntExtra("timeLeftSeconds", 0) ?: 0

        // Start foreground with a persistent silent notification
        val silentNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("BFY Schedule - Chế độ tập trung")
            .setContentText("Hệ thống đang giám sát phiên tập trung chạy nền.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setSilent(true)
            .build()
        startForeground(NOTIFICATION_ID, silentNotification)

        // Immediately trigger the floating heads-up countdown notification (10s) as a SEPARATE notification
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(COUNTDOWN_NOTIFICATION_ID, buildFloatingNotification(10))

        // Start 10-second countdown
        countDownTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000).toInt()
                val notification = buildFloatingNotification(secondsLeft)
                nm.notify(COUNTDOWN_NOTIFICATION_ID, notification)
            }

            override fun onFinish() {
                // Cancel countdown notification
                nm.cancel(COUNTDOWN_NOTIFICATION_ID)
                // Timeout - auto give up
                performGiveUp()
            }
        }.start()

        return START_NOT_STICKY
    }

    private fun buildFloatingNotification(secondsLeft: Int): android.app.Notification {
        // Continue action: bring app back to foreground
        val continueIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            action = ACTION_CONTINUE
        }
        val continuePendingIntent = PendingIntent.getActivity(
            this, 0, continueIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Give Up action: broadcast to receiver
        val giveUpIntent = Intent(this, FocusActionReceiver::class.java).apply {
            action = ACTION_GIVE_UP
            putExtra("targetMinutes", targetMinutes)
        }
        val giveUpPendingIntent = PendingIntent.getBroadcast(
            this, 1, giveUpIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Bạn đang trong phiên tập trung!")
            .setContentText("Quay lại trong $secondsLeft giây...")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(false) // Allow user to swipe dismiss if they want
            .setPriority(NotificationCompat.PRIORITY_MAX) // Keep it priority MAX so it stays floated
            .addAction(
                android.R.drawable.ic_media_play,
                "Continue",
                continuePendingIntent
            )
            .addAction(
                android.R.drawable.ic_delete,
                "Give Up",
                giveUpPendingIntent
            )

        // Refresh heads-up notification at 10s and 5s
        if (secondsLeft == 10 || secondsLeft == 5) {
            val fullScreenIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                action = ACTION_CONTINUE
            }
            val fullScreenPendingIntent = PendingIntent.getActivity(
                this, 0, fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.setFullScreenIntent(fullScreenPendingIntent, true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setOnlyAlertOnce(false)
        } else {
            builder.setOnlyAlertOnce(true)
        }

        return builder.build()
    }

    private fun performGiveUp() {
        // Send broadcast to handle the give-up logic
        val giveUpIntent = Intent(this, FocusActionReceiver::class.java).apply {
            action = ACTION_GIVE_UP
            putExtra("targetMinutes", targetMinutes)
        }
        sendBroadcast(giveUpIntent)

        cleanup()
    }

    private fun cleanup() {
        countDownTimer?.cancel()
        countDownTimer = null
        val nm = getSystemService(NotificationManager::class.java)
        nm.cancel(COUNTDOWN_NOTIFICATION_ID)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        countDownTimer = null
        val nm = getSystemService(NotificationManager::class.java)
        nm.cancel(COUNTDOWN_NOTIFICATION_ID)
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Focus Mode",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for Focus Mode"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
                
                val audioAttributes = android.media.AudioAttributes.Builder()
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, audioAttributes)
            }
            val mgr = getSystemService(NotificationManager::class.java)
            mgr.createNotificationChannel(channel)
        }
    }
}
