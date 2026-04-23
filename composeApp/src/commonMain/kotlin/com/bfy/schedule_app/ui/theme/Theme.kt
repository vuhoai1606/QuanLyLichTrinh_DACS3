package com.bfy.schedule_app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun BFYTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColors
    } else {
        LightColors
    }
    val extendedColors = if (darkTheme) {
        DarkExtendedColors
    } else {
        LightExtendedColors
    }

    CompositionLocalProvider(
        LocalBfyExtendedColors provides extendedColors,
        LocalBfyDimens provides BfyDimens()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = BfyTypography,
            shapes = BfyShapes,
            content = content
        )
    }
}

object BfyTheme {
    val dimens: BfyDimens
        @Composable
        @ReadOnlyComposable
        get() = LocalBfyDimens.current

    val extendedColors: BfyExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalBfyExtendedColors.current
}