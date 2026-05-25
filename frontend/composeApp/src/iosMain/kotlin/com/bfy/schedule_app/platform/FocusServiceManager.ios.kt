package com.bfy.schedule_app.platform

import androidx.compose.runtime.Composable

actual object FocusServiceManager {
    actual fun startFocusService(context: Any, targetMinutes: Int, timeLeftSeconds: Int) {
        // No-op on iOS
    }

    actual fun stopFocusService(context: Any) {
        // No-op on iOS
    }
}

@Composable
actual fun ShowToast(message: String) {
    // No-op on iOS
}

@Composable
actual fun rememberPlatformContext(): Any {
    return Unit
}
