package com.bfy.schedule_app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
actual fun GoogleSignInButton(
    text: String,
    modifier: Modifier,
    onTokenReceived: (String?) -> Unit
) {
    Button(
        onClick = { onTokenReceived(null) }, // Not implemented on this platform
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color(0xFF1E1E1E),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(25.dp)
    ) {
        Text(text = " (Unsupported)", fontSize = 16.sp)
    }
}
