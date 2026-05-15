package com.bfy.schedule_app

import androidx.compose.runtime.Composable

class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

@Composable
actual fun BackHandlerWrapper(enabled: Boolean, onBack: () -> Unit) {
    // No-op for JVM/Desktop
}