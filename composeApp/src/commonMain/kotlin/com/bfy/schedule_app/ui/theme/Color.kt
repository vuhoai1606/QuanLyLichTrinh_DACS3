package com.bfy.schedule_app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// 1. Định nghĩa các hằng số màu (Hex Code)
val PrimaryBlue = Color(0xFF1E88E5)
val OnPrimaryWhite = Color(0xFFFFFFFF)
val SecondaryTeal = Color(0xFF26A69A)
val ErrorRed = Color(0xFFE53935)

// Màu cho Light Theme
val BackgroundLight = Color(0xFFF5F7FA)
val SurfaceLight = Color(0xFFFFFFFF)
val TextPrimaryLight = Color(0xFF212121)
val TextSecondaryLight = Color(0xFF757575)

// Màu cho Dark Theme
val BackgroundDark = Color(0xFF121212)
val SurfaceDark = Color(0xFF1E1E1E)
val TextPrimaryDark = Color(0xFFE0E0E0)
val TextSecondaryDark = Color(0xFFAAAAAA)

// 2. Map vào Material 3 Light Color Scheme
val LightColors = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = OnPrimaryWhite,
    secondary = SecondaryTeal,
    background = BackgroundLight,
    surface = SurfaceLight,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    error = ErrorRed
)

// 3. Map vào Material 3 Dark Color Scheme
val DarkColors = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = OnPrimaryWhite, // Giữ nguyên chữ trắng trên nền nút xanh
    secondary = SecondaryTeal,
    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    error = ErrorRed
)