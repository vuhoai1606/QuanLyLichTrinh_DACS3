package com.bfy.schedule_app

import androidx.compose.runtime.Composable

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

@Composable
expect fun BackHandlerWrapper(enabled: Boolean = true, onBack: () -> Unit)

@Composable
expect fun rememberImagePicker(onImagePicked: (String) -> Unit): () -> Unit

@Composable
expect fun rememberBitmapFromUrlOrBase64(source: String?): androidx.compose.ui.graphics.ImageBitmap?