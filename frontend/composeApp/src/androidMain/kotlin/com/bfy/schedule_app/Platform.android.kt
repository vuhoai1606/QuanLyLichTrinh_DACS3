package com.bfy.schedule_app

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.activity.compose.BackHandler
import androidx.compose.ui.graphics.asImageBitmap
import io.ktor.client.request.get
import io.ktor.client.call.body

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

@Composable
actual fun BackHandlerWrapper(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}

@Composable
actual fun rememberImagePicker(onImagePicked: (String) -> Unit): () -> Unit {
    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val bytes = inputStream.readBytes()
                    inputStream.close()
                    val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                    onImagePicked("data:image/jpeg;base64,$base64")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    return {
        launcher.launch("image/*")
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
                val bytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
                val bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                bitmap = bmp?.asImageBitmap()
            } else if (source.startsWith("http")) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val response = com.bfy.schedule_app.data.remote.api.ApiClient.client.get(source)
                    val bytes: ByteArray = response.body()
                    val bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    bitmap = bmp?.asImageBitmap()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return bitmap
}