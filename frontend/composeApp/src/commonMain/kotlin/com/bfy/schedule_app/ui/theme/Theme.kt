package com.bfy.schedule_app.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import com.bfy.schedule_app.utils.SettingsManager

private val DarkColorPalette = darkColors(
    primary = PrimaryColor,
    background = BackgroundColor,
    surface = SurfaceColor,
    onPrimary = TextDark,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

private val LightColorPalette = lightColors(
    primary = PrimaryColor,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = TextDark,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary
)

@Composable
fun AppTheme(
    darkTheme: Boolean = SettingsManager.isDarkTheme,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette
    MaterialTheme(
        colors = colors,
        content = content
    )
}