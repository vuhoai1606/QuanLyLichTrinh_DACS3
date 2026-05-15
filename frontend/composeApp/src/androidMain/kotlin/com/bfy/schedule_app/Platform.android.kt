package com.bfy.schedule_app

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

@Composable
actual fun BackHandlerWrapper(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}