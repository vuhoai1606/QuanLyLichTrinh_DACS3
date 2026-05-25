package com.bfy.schedule_app.platform

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.bfy.schedule_app.data.repository.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FocusActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        if (intent.action == FocusForegroundService.ACTION_GIVE_UP) {
            val targetMinutes = intent.getIntExtra("targetMinutes", 25)

            // Set shared state so the UI knows we gave up
            FocusSessionSharedState.isGiveUpTriggered = true

            // Stop the foreground service
            context.stopService(Intent(context, FocusForegroundService::class.java))

            // Call API to record failed session
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val repository = AppRepository()
                    repository.createFocusSession(targetMinutes, "FAILED")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Show result notification
            showResultNotification(context)
        }
    }

    private fun showResultNotification(context: Context) {
        val notification = NotificationCompat.Builder(context, FocusForegroundService.CHANNEL_ID)
            .setContentTitle("Phiên tập trung đã kết thúc")
            .setContentText("Bạn đã từ bỏ phiên tập trung.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(FocusForegroundService.RESULT_NOTIFICATION_ID, notification)
    }
}
