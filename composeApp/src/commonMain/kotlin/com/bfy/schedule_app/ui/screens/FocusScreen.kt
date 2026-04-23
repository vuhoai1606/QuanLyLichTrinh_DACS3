package com.bfy.schedule_app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.bfy.schedule_app.ui.components.BfyButton
import com.bfy.schedule_app.ui.components.BfyButtonStyle
import com.bfy.schedule_app.ui.state.FocusUiState
import com.bfy.schedule_app.ui.theme.BfyTheme
import org.jetbrains.compose.resources.stringResource
import schedule_app.composeapp.generated.resources.Res
import schedule_app.composeapp.generated.resources.focus_ready
import schedule_app.composeapp.generated.resources.focus_running
import schedule_app.composeapp.generated.resources.give_up
import schedule_app.composeapp.generated.resources.permission_warning
import schedule_app.composeapp.generated.resources.start_focus

@Composable
fun FocusScreen(
    state: FocusUiState,
    onStart: () -> Unit,
    onGiveUp: () -> Unit
) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val progressColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(BfyTheme.dimens.spacing24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(BfyTheme.dimens.spacing16)
    ) {
        Text(
            text = if (state.isRunning) stringResource(Res.string.focus_running) else stringResource(Res.string.focus_ready),
            style = MaterialTheme.typography.headlineMedium
        )

        Canvas(modifier = Modifier.size(BfyTheme.dimens.spacing32 * 8)) {
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 22f, cap = StrokeCap.Round),
                topLeft = Offset(14f, 14f),
                size = Size(size.width - 28f, size.height - 28f)
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 252f,
                useCenter = false,
                style = Stroke(width = 22f, cap = StrokeCap.Round),
                topLeft = Offset(14f, 14f),
                size = Size(size.width - 28f, size.height - 28f)
            )
        }

        Text(
            text = state.minuteText,
            style = MaterialTheme.typography.displayLarge
        )

        if (!state.isRunning) {
            BfyButton(text = stringResource(Res.string.start_focus), onClick = onStart)
        } else {
            BfyButton(
                text = stringResource(Res.string.give_up),
                onClick = onGiveUp,
                style = BfyButtonStyle.DANGER
            )
        }

        Text(
            text = stringResource(Res.string.permission_warning),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
