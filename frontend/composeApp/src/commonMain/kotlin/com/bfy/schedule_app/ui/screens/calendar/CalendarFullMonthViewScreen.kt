package com.bfy.schedule_app.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.*
import com.bfy.schedule_app.ui.theme.*
import com.bfy.schedule_app.ui.viewmodel.CalendarViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue


@Composable
fun CalendarFullMonthViewScreen(
    viewModel: CalendarViewModel,
    onItemClick: (com.bfy.schedule_app.data.remote.model.ScheduleDto) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedDate = uiState.selectedDate


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(horizontal = 24.dp)
    ) {
        item { WeekDaysHeader() }
        item { Spacer(modifier = Modifier.height(8.dp)) }
        item { MonthGrid(selectedDate = selectedDate, schedules = uiState.schedules, onDateSelected = { viewModel.onDateSelected(it) }) }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item { AgendaSection(selectedDate, uiState.schedules, onItemClick) }

        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun WeekDaysHeader() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY).forEach { day ->
            Text(com.bfy.schedule_app.utils.Localization.getDayOfWeek(day).uppercase(), color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), style = androidx.compose.ui.text.TextStyle(textAlign = androidx.compose.ui.text.style.TextAlign.Center))
        }
    }
}

@Composable
fun MonthGrid(selectedDate: LocalDate, schedules: List<com.bfy.schedule_app.data.remote.model.ScheduleDto>, onDateSelected: (LocalDate) -> Unit) {
    val firstDayOfMonth = LocalDate(selectedDate.year, selectedDate.month, 1)
    val daysInMonth = when (selectedDate.month) {
        Month.FEBRUARY -> if (selectedDate.year % 4 == 0) 29 else 28
        Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
        else -> 31
    }
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.isoDayNumber // 1 (Mon) to 7 (Sun)
    val offset = firstDayOfWeek - 1

    Column {
        for (i in 0 until 6) { // 6 rows max
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                for (j in 0 until 7) {
                    val dayNum = i * 7 + j - offset + 1
                    if (dayNum in 1..daysInMonth) {
                        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                        val isToday = dayNum == today.dayOfMonth && today.month == selectedDate.month && today.year == selectedDate.year
                        val isSelected = dayNum == selectedDate.dayOfMonth
                        val gridDate = LocalDate(selectedDate.year, selectedDate.month, dayNum)
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) PrimaryColor else if (isToday) PrimaryColor.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { onDateSelected(gridDate) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    dayNum.toString(),
                                    color = if (isSelected) TextDark else if (isToday) PrimaryColor else TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                )
                                // Indicator dots in local timezone
                                val hasSchedules = schedules.any { schedule ->
                                    com.bfy.schedule_app.utils.ScheduleUtils.matchesDate(schedule, gridDate)
                                }
                                if (hasSchedules) {
                                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) TextDark else Color(0xFFAD7BFF)))
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

@Composable
fun AgendaSection(
    selectedDate: LocalDate,
    schedules: List<com.bfy.schedule_app.data.remote.model.ScheduleDto>,
    onItemClick: (com.bfy.schedule_app.data.remote.model.ScheduleDto) -> Unit
) {
    val daySchedules = schedules.filter { schedule ->
        com.bfy.schedule_app.utils.ScheduleUtils.matchesDate(schedule, selectedDate)
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text("${com.bfy.schedule_app.utils.Localization.getMonth(selectedDate.month)} ${selectedDate.dayOfMonth}", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text("${daySchedules.size} ${com.bfy.schedule_app.utils.Localization.get("items")}", color = TextSecondary, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        if (daySchedules.isEmpty()) {
            Text("No events for this day.", color = TextSecondary)
        }

        daySchedules.forEach { schedule ->
            val localTime = try {
                val startStr = schedule.start_time ?: schedule.deadline
                if (startStr != null) {
                    val dt = Instant.parse(startStr).toLocalDateTime(TimeZone.currentSystemDefault())
                    "${dt.hour.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')}"
                } else {
                    "All Day"
                }
            } catch (e: Exception) {
                "All Day"
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(schedule) }
            ) {
                com.bfy.schedule_app.ui.screens.homedashboard.TimelineEventItem(
                    tag = "[${schedule.type.first()}] ${schedule.type}",
                    tagBg = if (schedule.type == "EVENT") Color(0x330F4490) else Color(0x33AD7BFF),
                    tagColor = if (schedule.type == "EVENT") Color(0xFF92B4FF) else Color(0xFFAD7BFF),
                    time = localTime,
                    title = schedule.title,
                    subtitle = schedule.location ?: schedule.description ?: "",
                    borderColor = if (schedule.type == "EVENT") Color(0xFF0F4490) else Color(0xFFAD7BFF)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}


