package com.bfy.schedule_app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val BfyTypography = Typography(
    // Dùng cho Tiêu đề lớn (Ví dụ: Lời chào ở Home, Giờ đếm ngược)
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = 0.sp
    ),
    // Dùng cho Tiêu đề vừa (Ví dụ: Tên Task, Tên Event)
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        letterSpacing = 0.15.sp
    ),
    // Dùng cho Text nội dung bình thường
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.5.sp
    ),
    // Dùng cho Text phụ, ghi chú nhỏ, hoặc chữ dưới Bottom Navigation
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.5.sp
    )
)