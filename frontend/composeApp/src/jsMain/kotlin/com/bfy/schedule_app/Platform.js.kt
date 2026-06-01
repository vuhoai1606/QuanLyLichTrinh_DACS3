package com.bfy.schedule_app

import androidx.compose.runtime.Composable

class JsPlatform : Platform {
    override val name: String = "Web with Kotlin/JS"
}

actual fun getPlatform(): Platform = JsPlatform()

@Composable
actual fun BackHandlerWrapper(enabled: Boolean, onBack: () -> Unit) {
    // No-op for JS
}

@Composable
actual fun rememberImagePicker(onImagePicked: (String) -> Unit): () -> Unit {
    return {}
}

@Composable
actual fun rememberBitmapFromUrlOrBase64(source: String?): androidx.compose.ui.graphics.ImageBitmap? {
    return null
}