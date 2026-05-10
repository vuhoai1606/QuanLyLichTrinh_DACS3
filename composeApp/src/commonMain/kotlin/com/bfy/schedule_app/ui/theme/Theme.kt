package com.bfy.schedule_app.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
    primary = PrimaryColor,
    background = BackgroundColor,
    surface = SurfaceColor,
    onPrimary = TextDark,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = DarkColorPalette,
        content = content
    )
}