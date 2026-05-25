package com.bfy.schedule_app.ui.screens.focusmode

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bfy.schedule_app.ui.theme.*
import com.bfy.schedule_app.data.remote.model.*
import com.bfy.schedule_app.ui.viewmodel.FocusViewModel
import androidx.compose.foundation.BorderStroke
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.bfy.schedule_app.utils.Localization


@Composable
fun FocusModeScreen(viewModel: FocusViewModel = viewModel { FocusViewModel() }) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    val platformContext = com.bfy.schedule_app.platform.rememberPlatformContext()

    // Detect when app goes to background or returns
    DisposableEffect(lifecycleOwner, uiState.isRunning, uiState.timeLeft, uiState.targetMinutes) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                if (uiState.isRunning) {
                    com.bfy.schedule_app.platform.FocusServiceManager.startFocusService(
                        platformContext,
                        uiState.targetMinutes,
                        uiState.timeLeft
                    )
                }
            } else if (event == Lifecycle.Event.ON_RESUME) {
                com.bfy.schedule_app.platform.FocusServiceManager.stopFocusService(platformContext)
                if (com.bfy.schedule_app.platform.FocusSessionSharedState.isGiveUpTriggered) {
                    com.bfy.schedule_app.platform.FocusSessionSharedState.isGiveUpTriggered = false
                    viewModel.giveUpSession()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var showTimePickerDialog by remember { mutableStateOf(false) }
    var tempMinutes by remember(uiState.targetMinutes) { mutableStateOf(uiState.targetMinutes.toFloat()) }
    
    Box(

        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111316))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Goal / Session Info Section
            Text(
                text = Localization.get("deep_work"),
                color = Color(0xFFE2E2E6),
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.24).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(Color(0xFF282A2D))
                    .border(1.dp, Color(0xFF333538), RoundedCornerShape(percent = 50))
                    .padding(horizontal = 17.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dot icon placeholder
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFF59DBC7), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${Localization.get("today_focus") ?: "Today"}: ${uiState.stats?.today_minutes ?: 0} ${Localization.get("mins") ?: "mins"}",
                    color = Color(0xFFBBCAC5),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Timer Circle Container
            Box(
                modifier = Modifier
                    .size(288.dp),
                contentAlignment = Alignment.Center
            ) {
                // Ambient Glow (Simplified)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x0D59DBC7), CircleShape)
                )

                // The Circle Progress
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            val totalTime = uiState.targetMinutes * 60f
                            val progress = uiState.timeLeft / totalTime
                            drawArc(
                                color = Color(0xFF333538),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round),
                                size = Size(size.width, size.height)
                            )
                            drawArc(
                                color = Color(0xFF59DBC7),
                                startAngle = -90f,
                                sweepAngle = 360f * progress,
                                useCenter = false,
                                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round),
                                size = Size(size.width, size.height)
                            )
                        }
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize(0.9f)
                        .background(Color(0xFF1E2023), CircleShape)
                        .border(1.dp, Color(0xFF333538), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.clickable(enabled = !uiState.isRunning) {
                            showTimePickerDialog = true
                        }
                    ) {
                        val minutes = uiState.timeLeft / 60
                        val seconds = uiState.timeLeft % 60
                        Text(
                            text = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}",
                            color = Color(0xFFE2E2E6),
                            fontSize = 60.sp,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = (-3).sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = (Localization.get("minutes_label") ?: "MINUTES").uppercase(),
                            color = if (!uiState.isRunning) Color(0xFF59DBC7) else Color(0xFFBBCAC5),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.2.sp
                        )
                        if (!uiState.isRunning) {
                            Text(
                                text = "Tap to edit",
                                color = Color(0xFF59DBC7).copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Light
                            )
                        }
                    }
                }

                if (!uiState.isRunning) {
                    // Action Button Positioned Absolute on bottom of the circle
                    Box(
                        modifier = Modifier
                            .width(250.dp)
                            .align(Alignment.BottomCenter)
                            .offset(y = 28.dp) // shift down
                            .shadow(8.dp, RoundedCornerShape(percent = 50))
                            .clip(RoundedCornerShape(percent = 50))
                            .background(Color(0xFF59DBC7))
                            .clickable {
                                if (!uiState.isRunning) {
                                    viewModel.confirmStartFocus()
                                } else {
                                    viewModel.onStartFocusClick()
                                }
                            }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (uiState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (uiState.isRunning) "Pause" else "Start",
                                tint = Color(0xFF003731),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState.isRunning) Localization.get("pause_focus") else Localization.get("start_focus"),
                                color = Color(0xFF003731),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(96.dp))

            // Stats Grid below timer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Completed
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E2023))
                        .border(1.dp, Color(0xFF333538), RoundedCornerShape(12.dp))
                        .padding(17.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = Localization.get("completed"), color = Color(0xFFBBCAC5), fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.6.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(text = "${uiState.stats?.completed_sessions ?: 0}", color = Color(0xFFE2E2E6), fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = Localization.get("sessions"), color = Color(0xFFBBCAC5), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Total Time
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E2023))
                        .border(1.dp, Color(0xFF333538), RoundedCornerShape(12.dp))
                        .padding(17.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = Localization.get("total_time_stat"), color = Color(0xFFBBCAC5), fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.6.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(text = "${uiState.stats?.total_minutes ?: 0}", color = Color(0xFFE2E2E6), fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = Localization.get("mins"), color = Color(0xFFBBCAC5), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }


        }

        if (showTimePickerDialog) {
            var inputString by remember { mutableStateOf(uiState.targetMinutes.toString()) }
            var isError by remember { mutableStateOf(false) }
            val parsedMinutes = inputString.toIntOrNull()
            val isValid = parsedMinutes != null && parsedMinutes in 15..180

            AlertDialog(
                onDismissRequest = { showTimePickerDialog = false },
                title = { Text(text = "Setup Focus Time", fontWeight = FontWeight.Bold) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Enter minutes (15 - 180)",
                            fontSize = 14.sp,
                            color = Color(0xFFBBCAC5)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = inputString,
                            onValueChange = { newValue ->
                                if (newValue.all { it.isDigit() } && newValue.length <= 3) {
                                    inputString = newValue
                                    isError = newValue.toIntOrNull()?.let { it !in 15..180 } ?: true
                                }
                            },
                            singleLine = true,
                            isError = isError,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = if (isError) Color.Red else Color(0xFF59DBC7),
                                unfocusedBorderColor = if (isError) Color.Red else Color.Gray,
                                cursorColor = Color(0xFF59DBC7)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (isError) {
                            Text(
                                text = "Must be between 15 and 180 minutes",
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(15, 25, 45, 60, 90, 120, 180).forEach { mins ->
                                AssistChip(
                                    onClick = { 
                                        inputString = mins.toString()
                                        isError = false
                                    },
                                    label = { Text("$mins") },
                                    colors = AssistChipDefaults.assistChipColors(
                                        labelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (isValid && parsedMinutes != null) {
                                viewModel.setFocusTime(parsedMinutes)
                                showTimePickerDialog = false
                            }
                        },
                        enabled = isValid,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF59DBC7))
                    ) {
                        Text("Save", color = Color(0xFF003731))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePickerDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                containerColor = Color(0xFF1E2023),
                titleContentColor = Color.White,
                textContentColor = Color(0xFFBBCAC5)
            )
        }
    }
}
