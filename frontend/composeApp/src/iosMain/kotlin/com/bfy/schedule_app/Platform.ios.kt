package com.bfy.schedule_app

import androidx.compose.runtime.Composable
import platform.UIKit.UIDevice

class IOSPlatform : Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

@Composable
actual fun BackHandlerWrapper(enabled: Boolean, onBack: () -> Unit) {
    // No-op for iOS
}

@Composable
actual fun rememberImagePicker(onImagePicked: (String) -> Unit): () -> Unit {
    return {}
}

@Composable
actual fun rememberBitmapFromUrlOrBase64(source: String?): androidx.compose.ui.graphics.ImageBitmap? {
    return null
}