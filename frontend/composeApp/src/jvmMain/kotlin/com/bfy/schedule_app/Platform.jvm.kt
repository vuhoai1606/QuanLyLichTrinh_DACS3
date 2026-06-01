package com.bfy.schedule_app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toComposeImageBitmap
import io.ktor.client.request.get
import io.ktor.client.call.body

class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

@Composable
actual fun BackHandlerWrapper(enabled: Boolean, onBack: () -> Unit) {
    // No-op for JVM/Desktop
}

@Composable
actual fun rememberImagePicker(onImagePicked: (String) -> Unit): () -> Unit {
    return {
        try {
            val dialog = java.awt.FileDialog(null as java.awt.Frame?, "Select Avatar Image", java.awt.FileDialog.LOAD)
            dialog.file = "*.jpg;*.jpeg;*.png"
            dialog.isVisible = true
            val fileStr = dialog.file
            val dirStr = dialog.directory
            if (fileStr != null && dirStr != null) {
                val file = java.io.File(dirStr, fileStr)
                if (file.exists()) {
                    val bytes = file.readBytes()
                    val base64 = java.util.Base64.getEncoder().encodeToString(bytes)
                    onImagePicked("data:image/jpeg;base64,$base64")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Composable
actual fun rememberBitmapFromUrlOrBase64(source: String?): androidx.compose.ui.graphics.ImageBitmap? {
    if (source.isNullOrBlank()) return null
    var bitmap by androidx.compose.runtime.remember(source) { androidx.compose.runtime.mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    androidx.compose.runtime.LaunchedEffect(source) {
        try {
            if (source.startsWith("data:image") && source.contains("base64,")) {
                val base64Data = source.substringAfter("base64,")
                val bytes = java.util.Base64.getDecoder().decode(base64Data)
                val bis = java.io.ByteArrayInputStream(bytes)
                val bufferedImage = javax.imageio.ImageIO.read(bis)
                bitmap = bufferedImage?.toComposeImageBitmap()
            } else if (source.startsWith("http")) {
                val response = com.bfy.schedule_app.data.remote.api.ApiClient.client.get(source)
                val bytes: ByteArray = response.body()
                val bis = java.io.ByteArrayInputStream(bytes)
                val bufferedImage = javax.imageio.ImageIO.read(bis)
                bitmap = bufferedImage?.toComposeImageBitmap()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return bitmap
}