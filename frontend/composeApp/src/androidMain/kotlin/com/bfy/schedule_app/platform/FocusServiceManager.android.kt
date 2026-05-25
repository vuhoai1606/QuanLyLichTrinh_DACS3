package com.bfy.schedule_app.platform

import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

actual object FocusServiceManager {
    actual fun startFocusService(context: Any, targetMinutes: Int, timeLeftSeconds: Int) {
        val ctx = context as Context
        val intent = Intent(ctx, FocusForegroundService::class.java).apply {
            putExtra("targetMinutes", targetMinutes)
            putExtra("timeLeftSeconds", timeLeftSeconds)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ctx.startForegroundService(intent)
        } else {
            ctx.startService(intent)
        }
    }

    actual fun stopFocusService(context: Any) {
        val ctx = context as Context
        ctx.stopService(Intent(ctx, FocusForegroundService::class.java))
    }
}

@Composable
actual fun ShowToast(message: String) {
    val context = LocalContext.current
    LaunchedEffect(message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}

@Composable
actual fun rememberPlatformContext(): Any {
    return LocalContext.current
}
