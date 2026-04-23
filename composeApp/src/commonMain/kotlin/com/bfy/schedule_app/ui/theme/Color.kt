package com.bfy.schedule_app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val Blue = Color(0xFF1768D0)
private val Green = Color(0xFF1F9E67)
private val Orange = Color(0xFFF08C00)
private val Red = Color(0xFFD63939)

private val Gray900 = Color(0xFF141A22)
private val Gray800 = Color(0xFF243041)
private val Gray700 = Color(0xFF3A4A61)
private val Gray400 = Color(0xFF9AABBE)
private val Gray200 = Color(0xFFCFD8E4)
private val Gray100 = Color(0xFFE9EEF5)
private val Gray50 = Color(0xFFF4F7FB)
private val White = Color(0xFFFFFFFF)

val LightColors = lightColorScheme(
    primary = Blue,
    onPrimary = White,
    primaryContainer = Color(0xFFDCEBFF),
    onPrimaryContainer = Color(0xFF002D65),
    secondary = Color(0xFF0E7C63),
    onSecondary = White,
    secondaryContainer = Color(0xFFC2F0E5),
    onSecondaryContainer = Color(0xFF00382D),
    background = Gray50,
    onBackground = Gray900,
    surface = White,
    onSurface = Gray900,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray700,
    error = Red,
    onError = White
)

val DarkColors = darkColorScheme(
    primary = Color(0xFFA9CBFF),
    onPrimary = Color(0xFF003062),
    primaryContainer = Color(0xFF00488F),
    onPrimaryContainer = Color(0xFFDCEBFF),
    secondary = Color(0xFF88D7C2),
    onSecondary = Color(0xFF00382D),
    secondaryContainer = Color(0xFF005140),
    onSecondaryContainer = Color(0xFFC2F0E5),
    background = Color(0xFF0F141B),
    onBackground = Color(0xFFE6ECF4),
    surface = Color(0xFF17202A),
    onSurface = Color(0xFFE6ECF4),
    surfaceVariant = Color(0xFF243041),
    onSurfaceVariant = Color(0xFFBCC8D7),
    error = Color(0xFFFFB3B3),
    onError = Color(0xFF680003)
)

@Immutable
data class BfyExtendedColors(
    val success: Color,
    val warning: Color,
    val danger: Color,
    val timelineTodo: Color,
    val timelineTask: Color,
    val timelineEvent: Color,
    val border: Color,
    val textMuted: Color
)

val LightExtendedColors = BfyExtendedColors(
    success = Green,
    warning = Orange,
    danger = Red,
    timelineTodo = Color(0xFF5E60CE),
    timelineTask = Color(0xFF1768D0),
    timelineEvent = Color(0xFFB45309),
    border = Gray200,
    textMuted = Gray700
)

val DarkExtendedColors = BfyExtendedColors(
    success = Color(0xFF4ADE80),
    warning = Color(0xFFFBBF24),
    danger = Color(0xFFF87171),
    timelineTodo = Color(0xFFA5B4FC),
    timelineTask = Color(0xFF93C5FD),
    timelineEvent = Color(0xFFFCD34D),
    border = Gray700,
    textMuted = Gray400
)

val LocalBfyExtendedColors = staticCompositionLocalOf { LightExtendedColors }