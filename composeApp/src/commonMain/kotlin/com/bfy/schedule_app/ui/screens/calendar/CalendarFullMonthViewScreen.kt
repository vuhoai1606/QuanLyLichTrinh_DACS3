package com.bfy.schedule_app.ui.screens.calendar

import androidx.compose.foundation.background
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

@Composable
fun CalendarFullMonthViewScreen() {
    var selectedDate by remember { mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(horizontal = 24.dp)
    ) {
        item { WeekDaysHeader() }
        item { Spacer(modifier = Modifier.height(8.dp)) }
        item { MonthGrid(selectedDate = selectedDate, onDateSelected = { selectedDate = it }) }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item { AgendaSection() }
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun WeekDaysHeader() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN").forEach { day ->
            Text(day, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), style = androidx.compose.ui.text.TextStyle(textAlign = androidx.compose.ui.text.style.TextAlign.Center))
        }
    }
}

@Composable
fun MonthGrid(selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    val totalDays = 31
    Column {
        for (i in 0 until 5) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                for (j in 0 until 7) {
                    val dayNum = i * 7 + j + 1
                    if (dayNum <= totalDays) {
                        val isSelected = dayNum == 24
                        val isToday = dayNum == 24
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) PrimaryColor else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    dayNum.toString(),
                                    color = if (isSelected) TextDark else TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                )
                                if (dayNum % 3 == 0) {
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
fun AgendaSection() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text("October 24", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text("3 Events, 2 Tasks", color = TextSecondary, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        com.bfy.schedule_app.ui.screens.homedashboard.TimelineEventItem(
            tag = "[E] EVENT",
            tagBg = Color(0x330F4490),
            tagColor = Color(0xFF92B4FF),
            time = "09:00 AM - 10:30 AM",
            title = "Advanced Calculus Lecture",
            subtitle = "Room 402",
            borderColor = Color(0xFF0F4490)
        )
        Spacer(modifier = Modifier.height(12.dp))
        com.bfy.schedule_app.ui.screens.homedashboard.TimelineEventItem(
            tag = "[T] TASK",
            tagBg = Color(0x33AD7BFF),
            tagColor = Color(0xFFAD7BFF),
            time = "Due 2:00 PM",
            title = "Submit Physics Lab Report",
            subtitle = "Upload PDF",
            borderColor = Color(0xFFAD7BFF)
        )
    }
}

