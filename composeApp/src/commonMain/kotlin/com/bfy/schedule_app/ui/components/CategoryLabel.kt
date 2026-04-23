package com.bfy.schedule_app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CategoryLabel(
    text: String,
    colorHex: String,
    modifier: Modifier = Modifier
) {
    val borderColor = parseColorHex(colorHex)
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = Color.Transparent,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = borderColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

private fun parseColorHex(hex: String): Color {
    val normalized = hex.removePrefix("#")
    val argb = when (normalized.length) {
        6 -> ((0xFF000000 or (normalized.toLongOrNull(16) ?: 0x1768D0))).toInt()
        8 -> (normalized.toLongOrNull(16) ?: 0xFF1768D0).toInt()
        else -> 0xFF1768D0.toInt()
    }
    return Color(argb)
}
