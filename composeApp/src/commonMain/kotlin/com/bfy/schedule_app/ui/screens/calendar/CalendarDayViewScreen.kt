package com.bfy.schedule_app.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bfy.schedule_app.ui.theme.*

@Composable
fun CalendarDayViewScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        DayHeader()
        Spacer(modifier = Modifier.height(16.dp))
        DayTimeGrid()
    }
}

@Composable
fun DayHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("THU", color = PrimaryColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(PrimaryColor),
                contentAlignment = Alignment.Center
            ) {
                Text("24", color = TextDark, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text("October 2024", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun DayTimeGrid() {
    Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Column {
            for (hour in 9..17) {
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
                .padding(start = 70.dp, end = 24.dp, top = 0.dp)
                .fillMaxWidth()
        ) {
            com.bfy.schedule_app.ui.screens.homedashboard.TimelineEventItem(
                tag = "[E] EVENT",
                tagBg = Color(0x330F4490),
                tagColor = Color(0xFF92B4FF),
                time = "09:00 AM - 10:30 AM",
                title = "Advanced Calculus Lecture",
                subtitle = "Room 402",
                borderColor = Color(0xFF0F4490)
            )
        }
    }
}

