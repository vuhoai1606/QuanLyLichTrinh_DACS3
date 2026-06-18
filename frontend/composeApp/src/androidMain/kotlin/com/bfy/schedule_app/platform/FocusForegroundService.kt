package com.bfy.schedule_app.platform

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.bfy.schedule_app.MainActivity
import com.bfy.schedule_app.data.repository.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FocusForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "focus_channel_v4"
        const val NOTIFICATION_ID = 1001
        const val RESULT_NOTIFICATION_ID = 1002
        const val ACTION_GIVE_UP = "com.bfy.schedule_app.GIVE_UP_FOCUS"
        const val ACTION_CONTINUE = "com.bfy.schedule_app.CONTINUE_FOCUS"
    }

    private var countDownTimer: CountDownTimer? = null
    private var targetMinutes: Int = 0
    private var timeLeftSeconds: Int = 0
    private var shouldTriggerWarning: Boolean = false
    private var wasScreenOn: Boolean = true

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        targetMinutes = intent?.getIntExtra("targetMinutes", 25) ?: 25
        timeLeftSeconds = intent?.getIntExtra("timeLeftSeconds", 0) ?: 0
        shouldTriggerWarning = intent?.getBooleanExtra("shouldTriggerWarning", false) ?: false

        if (shouldTriggerWarning) {
            // Trường hợp 2: Thoát app ra ngoài hoặc đang ở màn hình đa nhiệm (chạy 10s phạt)
            startWarningCountdown()
        } else {
            // Trường hợp 1: Khóa màn hình khi đang ở trong app (không bị phạt)
            // Chạy bộ đếm ngược chính thức tương ứng với số giây còn lại.
            startRegularFocusCountdown()
        }

        return START_NOT_STICKY
    }

    private fun startRegularFocusCountdown() {
        // Start foreground with a persistent notification showing the timer
        val initialNotif = buildRegularFocusNotification(timeLeftSeconds)
        startForeground(NOTIFICATION_ID, initialNotif)

        countDownTimer = object : CountDownTimer(timeLeftSeconds * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftSeconds = (millisUntilFinished / 1000).toInt()
                FocusSessionSharedState.timeLeftSeconds = timeLeftSeconds
                
                val nm = getSystemService(NotificationManager::class.java)
                nm.notify(NOTIFICATION_ID, buildRegularFocusNotification(timeLeftSeconds))
            }

            override fun onFinish() {
                timeLeftSeconds = 0
                FocusSessionSharedState.timeLeftSeconds = -1
                FocusSessionSharedState.isCompletedTriggered = true

                // Gọi API hoàn thành
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val repository = AppRepository()
                        repository.createFocusSession(targetMinutes, "COMPLETED")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // Hiển thị thông báo thành công
                val successNotif = NotificationCompat.Builder(this@FocusForegroundService, CHANNEL_ID)
                    .setContentTitle("Hoàn thành tập trung!")
                    .setContentText("Chúc mừng bạn đã hoàn thành phiên tập trung $targetMinutes phút.")
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .build()

                val nm = getSystemService(NotificationManager::class.java)
                nm.notify(RESULT_NOTIFICATION_ID, successNotif)

                cleanup()
            }
        }.start()
    }

    private fun buildRegularFocusNotification(secondsLeft: Int): android.app.Notification {
        val minutes = secondsLeft / 60
        val seconds = secondsLeft % 60
        val timeString = String.format("%02d:%02d", minutes, seconds)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            action = ACTION_CONTINUE
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Đang tập trung (Khóa màn hình)")
            .setContentText("Thời gian còn lại: $timeString")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun startWarningCountdown() {
        // Khởi chạy Foreground Service bằng thông báo đếm ngược phạt trực tiếp (alert = true)
        val initialNotif = buildFloatingNotification(10, alert = true)
        startForeground(NOTIFICATION_ID, initialNotif)

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wasScreenOn = powerManager.isInteractive
        
        // Nếu màn hình đang tắt ngay tại giây thứ 10 lúc bắt đầu, kích hoạt sáng màn hình ngay lập tức!
        if (!wasScreenOn) {
            wakeUpScreen()
        }

        countDownTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000).toInt()
                
                val powerManagerInside = getSystemService(Context.POWER_SERVICE) as PowerManager
                val isCurrentScreenOff = !powerManagerInside.isInteractive
                
                // Phát hiện sự thay đổi trạng thái từ Màn hình Bật sang Màn hình Tắt (vừa tắt màn hình)
                val screenJustTurnedOff = wasScreenOn && isCurrentScreenOff
                wasScreenOn = !isCurrentScreenOff

                // Alert (phát âm thanh/rung/đẩy heads-up) khi vừa tắt màn hình, hoặc ở giây thứ 10, giây thứ 5
                val shouldAlert = screenJustTurnedOff || secondsLeft == 10 || secondsLeft == 5
                
                val notification = buildFloatingNotification(secondsLeft, alert = shouldAlert)
                val nm = getSystemService(NotificationManager::class.java)
                nm.notify(NOTIFICATION_ID, notification)

                if (shouldAlert && isCurrentScreenOff) {
                    wakeUpScreen()
                }
            }

            override fun onFinish() {
                performGiveUp()
            }
        }.start()
    }

    private fun buildFloatingNotification(secondsLeft: Int, alert: Boolean): android.app.Notification {
        val continueIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            action = ACTION_CONTINUE
        }
        val continuePendingIntent = PendingIntent.getActivity(
            this, 0, continueIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val giveUpIntent = Intent(this, FocusActionReceiver::class.java).apply {
            action = ACTION_GIVE_UP
            putExtra("targetMinutes", targetMinutes)
        }
        val giveUpPendingIntent = PendingIntent.getBroadcast(
            this, 1, giveUpIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val isCurrentScreenOff = !powerManager.isInteractive

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(if (isCurrentScreenOff) "Bạn đã tắt màn hình sau khi thoát app!" else "Bạn đang ngoài phiên tập trung!")
            .setContentText("Quay lại ứng dụng trong $secondsLeft giây...")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true) // Giữ thông báo không bị gạt đi
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Luôn hiển thị nội dung trên màn hình khóa
            .setCategory(NotificationCompat.CATEGORY_ALARM)
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

        // Chỉ alert (phát âm thanh/rung) khi được yêu cầu để tránh nhấp nháy hoặc mất thông báo trên màn hình khóa.
        if (alert) {
            builder.setOnlyAlertOnce(false)
                   .setDefaults(NotificationCompat.DEFAULT_ALL)
        } else {
            builder.setOnlyAlertOnce(true)
        }

        return builder.build()
    }

    private fun wakeUpScreen() {
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLock = pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "BFYSchedule:WakeLock"
            )
            wakeLock.acquire(3000L) // Sáng màn hình trong 3 giây
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun performGiveUp() {
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
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        countDownTimer = null
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
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC // Luôn cho phép hiển thị nội dung trên màn hình khóa
                
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
