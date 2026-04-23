package com.bfy.schedule_app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun BFYTheme(
    // Mặc định sẽ đọc xem điện thoại đang ở chế độ Sáng hay Tối
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Logic chọn bộ màu
    val colorScheme = if (darkTheme) {
        DarkColors
    } else {
        LightColors
    }

    // Bọc toàn bộ ứng dụng bằng MaterialTheme chuẩn
    MaterialTheme(
        colorScheme = colorScheme,
        typography = BfyTypography,
        shapes = BfyShapes,
        content = content
    )
}