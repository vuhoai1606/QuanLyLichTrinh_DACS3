package com.bfy.schedule_app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class BfyDimens(
    val spacing2: Dp = 2.dp,
    val spacing4: Dp = 4.dp,
    val spacing8: Dp = 8.dp,
    val spacing12: Dp = 12.dp,
    val spacing16: Dp = 16.dp,
    val spacing20: Dp = 20.dp,
    val spacing24: Dp = 24.dp,
    val spacing32: Dp = 32.dp,
    val iconSmall: Dp = 18.dp,
    val iconMedium: Dp = 24.dp,
    val cardElevation: Dp = 1.dp,
    val cornerSmall: Dp = 10.dp,
    val cornerMedium: Dp = 16.dp,
    val cornerLarge: Dp = 24.dp
)

val LocalBfyDimens = staticCompositionLocalOf { BfyDimens() }
