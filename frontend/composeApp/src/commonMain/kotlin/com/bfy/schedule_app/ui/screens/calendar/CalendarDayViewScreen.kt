package com.bfy.schedule_app.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import com.bfy.schedule_app.utils.Localization
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


@Composable
fun CalendarDayViewScreen(
    viewModel: CalendarViewModel = viewModel { CalendarViewModel() },
    onItemClick: (com.bfy.schedule_app.data.remote.model.ScheduleDto) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedDate = uiState.selectedDate

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        DayHeader(selectedDate)
        Spacer(modifier = Modifier.height(16.dp))
        DayTimeGrid(selectedDate, uiState.schedules, onItemClick)
    }
}

@Composable
fun DayHeader(date: kotlinx.datetime.LocalDate) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(Localization.getDayOfWeek(date.dayOfWeek), color = PrimaryColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(PrimaryColor),
                contentAlignment = Alignment.Center
            ) {
                Text(date.dayOfMonth.toString(), color = TextDark, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text("${Localization.getMonth(date.month)} ${date.year}", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}


@Composable
fun DayTimeGrid(
    date: kotlinx.datetime.LocalDate,
    schedules: List<com.bfy.schedule_app.data.remote.model.ScheduleDto>,
    onItemClick: (com.bfy.schedule_app.data.remote.model.ScheduleDto) -> Unit
) {
    val daySchedules = schedules.filter { schedule ->
        com.bfy.schedule_app.utils.ScheduleUtils.matchesDate(schedule, date)
    }
    val (allDaySchedules, timedSchedules) = daySchedules.partition { it.is_all_day }

    Column(modifier = Modifier.fillMaxSize()) {
        if (allDaySchedules.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 70.dp, end = 24.dp, bottom = 8.dp)
                    .background(Color(0xFF1E2023))
                    .border(1.dp, Color(0xFF3C4946), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = Localization.get("all_day") ?: "All Day",
                    color = PrimaryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                allDaySchedules.forEach { schedule ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (schedule.type == "EVENT") Color(0x330F4490) else Color(0x33AD7BFF))
                            .clickable { onItemClick(schedule) }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = schedule.title,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize().weight(1f).verticalScroll(rememberScrollState())) {
            Column {
                for (hour in 0..23) {
                    Row(modifier = Modifier.fillMaxWidth().height(80.dp)) {
                        Box(modifier = Modifier.width(70.dp).padding(end = 12.dp), contentAlignment = Alignment.TopEnd) {
                            Text("$hour:00", color = TextSecondary, fontSize = 14.sp)
                        }
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFF282A2D)))
                    }
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
            
            Box(
                modifier = Modifier
                    .padding(start = 70.dp, end = 24.dp)
                    .fillMaxWidth()
            ) {
                timedSchedules.forEach { schedule ->
                    val localStart = try {
                        val startStr = schedule.start_time ?: schedule.deadline
                        if (startStr != null) {
                            Instant.parse(startStr).toLocalDateTime(TimeZone.currentSystemDefault())
                        } else null
                    } catch (e: Exception) {
                        null
                    }

                    if (localStart != null) {
                        val startHour = localStart.hour
                        val startMinute = localStart.minute
                        val topOffset = startHour * 80.0 + (startMinute / 60.0) * 80.0

                        val durationMin = try {
                            val startInst = Instant.parse(schedule.start_time ?: schedule.deadline!!)
                            val endInst = Instant.parse(schedule.end_time ?: schedule.deadline!!)
                            (endInst.toEpochMilliseconds() - startInst.toEpochMilliseconds()) / 60000
                        } catch (e: Exception) {
                            60L
                        }
                        val finalDuration = if (durationMin <= 0) 60L else durationMin
                        val eventHeight = maxOf((finalDuration / 60.0) * 80.0, 60.0)

                        val timeSlotStr = try {
                            val startStr = schedule.start_time ?: schedule.deadline
                            val endStr = schedule.end_time ?: schedule.deadline
                            
                            val startText = if (startStr != null) {
                                val dt = Instant.parse(startStr).toLocalDateTime(TimeZone.currentSystemDefault())
                                "${dt.hour.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')}"
                            } else null
                            
                            val endText = if (endStr != null) {
                                val dt = Instant.parse(endStr).toLocalDateTime(TimeZone.currentSystemDefault())
                                "${dt.hour.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')}"
                            } else null
                            
                            if (startText != null && endText != null && startText != endText) {
                                "$startText - $endText"
                            } else startText ?: (Localization.get("all_day") ?: "All Day")
                        } catch (e: Exception) {
                            Localization.get("all_day") ?: "All Day"
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = topOffset.dp)
                                .height(eventHeight.dp)
                                .clickable { onItemClick(schedule) }
                        ) {
                            com.bfy.schedule_app.ui.screens.homedashboard.TimelineEventItem(
                                tag = "[${schedule.type.first()}] ${schedule.type}",
                                tagBg = if (schedule.type == "EVENT") Color(0x330F4490) else Color(0x33AD7BFF),
                                tagColor = if (schedule.type == "EVENT") Color(0xFF92B4FF) else Color(0xFFAD7BFF),
                                time = timeSlotStr,
                                title = schedule.title,
                                subtitle = schedule.location ?: schedule.description ?: "",
                                borderColor = if (schedule.type == "EVENT") Color(0xFF0F4490) else Color(0xFFAD7BFF)
                            )
                        }
                    }
                }
            }
        }
    }
}


