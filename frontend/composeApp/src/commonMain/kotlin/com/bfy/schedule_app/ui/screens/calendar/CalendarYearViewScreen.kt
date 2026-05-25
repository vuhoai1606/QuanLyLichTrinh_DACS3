package com.bfy.schedule_app.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bfy.schedule_app.ui.theme.*
import com.bfy.schedule_app.ui.viewmodel.CalendarViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState


import androidx.compose.foundation.clickable
import kotlinx.datetime.*

@Composable
fun CalendarYearViewScreen(
    viewModel: CalendarViewModel = viewModel { CalendarViewModel() },
    onNavigateToMonth: (LocalDate) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedDate = uiState.selectedDate

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        val months = listOf(
            "January", "February", "March", "April",
            "May", "June", "July", "August",
            "September", "October", "November", "December"
        )
        val chunkedMonths = months.chunked(2)
        chunkedMonths.forEach { rowMonths ->
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowMonths.forEach { month ->
                        SmallMonthGrid(
                            monthName = month, 
                            selectedDate = selectedDate,
                            schedules = uiState.schedules,
                            onDayClick = onNavigateToMonth,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    repeat(2 - rowMonths.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SmallMonthGrid(
    monthName: String, 
    selectedDate: LocalDate, 
    schedules: List<com.bfy.schedule_app.data.remote.model.ScheduleDto>,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val months = listOf(
        "January", "February", "March", "April",
        "May", "June", "July", "August",
        "September", "October", "November", "December"
    )
    val monthNumber = months.indexOf(monthName) + 1
    
    val year = selectedDate.year
    val daysInMonth = when (monthNumber) {
        2 -> if (year % 4 == 0) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
    
    val firstDayOfMonth = try {
        LocalDate(year, monthNumber, 1)
    } catch (e: Exception) {
        LocalDate(year, 1, 1)
    }
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.isoDayNumber // 1 (Mon) to 7 (Sun)
    val offset = firstDayOfWeek - 1

    Column(modifier = modifier) {
        Text(
            text = monthName,
            color = PrimaryColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        for (i in 0 until 6) { // 6 rows max to avoid cutoff
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                for (j in 0 until 7) {
                    val dayNum = i * 7 + j - offset + 1
                    if (dayNum in 1..daysInMonth) {
                        val gridDate = LocalDate(year, monthNumber, dayNum)
                        val isHighlighted = gridDate == selectedDate

                        // Check if day has schedules
                        val hasSchedules = schedules.any { schedule ->
                            val itemDate = try {
                                val startStr = schedule.start_time ?: schedule.deadline
                                if (startStr != null) {
                                    Instant.parse(startStr).toLocalDateTime(TimeZone.currentSystemDefault()).date
                                } else {
                                    null
                                }
                            } catch (e: Exception) {
                                null
                            }
                            itemDate == gridDate
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(1.dp)
                                .clip(CircleShape)
                                .background(if (isHighlighted) PrimaryColor else Color.Transparent)
                                .clickable { onDayClick(gridDate) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = dayNum.toString(),
                                    color = if (isHighlighted) TextDark else TextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
                                )
                                if (hasSchedules) {
                                    Box(
                                        modifier = Modifier
                                            .size(3.dp)
                                            .clip(CircleShape)
                                            .background(if (isHighlighted) TextDark else Color(0xFFAD7BFF))
                                    )
                                }
                            }
                        }
                    } else {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}

