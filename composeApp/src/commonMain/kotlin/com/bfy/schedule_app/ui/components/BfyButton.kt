package com.bfy.schedule_app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bfy.schedule_app.ui.theme.BfyTheme

enum class BfyButtonStyle {
    PRIMARY,
    SECONDARY,
    DANGER
}

@Composable
fun BfyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: BfyButtonStyle = BfyButtonStyle.PRIMARY
) {
    when (style) {
        BfyButtonStyle.PRIMARY -> {
            Button(
                onClick = onClick,
                modifier = modifier.fillMaxWidth(),
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(text = text)
            }
        }

        BfyButtonStyle.SECONDARY -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier.fillMaxWidth(),
                enabled = enabled,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(text = text)
            }
        }

        BfyButtonStyle.DANGER -> {
            Button(
                onClick = onClick,
                modifier = modifier.fillMaxWidth(),
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BfyTheme.extendedColors.danger,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(text = text)
            }
        }
    }
}
