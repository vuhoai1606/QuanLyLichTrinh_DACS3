package com.bfy.schedule_app.platform

import androidx.compose.runtime.Composable

actual object FocusServiceManager {
    actual fun startFocusService(context: Any, targetMinutes: Int, timeLeftSeconds: Int) {
        // No-op on JS
    }

    actual fun stopFocusService(context: Any) {
        // No-op on JS
    }
}

@Composable
actual fun ShowToast(message: String) {
    // No-op on JS
}

@Composable
actual fun rememberPlatformContext(): Any {
    return Unit
}
