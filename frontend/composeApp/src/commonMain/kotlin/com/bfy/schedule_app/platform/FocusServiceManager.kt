package com.bfy.schedule_app.platform

import androidx.compose.runtime.Composable

expect object FocusServiceManager {
    fun startFocusService(context: Any, targetMinutes: Int, timeLeftSeconds: Int)
    fun stopFocusService(context: Any)
}

@Composable
expect fun ShowToast(message: String)

@Composable
expect fun rememberPlatformContext(): Any
