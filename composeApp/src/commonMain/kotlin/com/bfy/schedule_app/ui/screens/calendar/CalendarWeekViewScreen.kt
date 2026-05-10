package com.bfy.schedule_app.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun CalendarWeekViewScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        WeekDaysBar()
        Spacer(modifier = Modifier.height(16.dp))
        WeekTimeGrid()
    }
}

@Composable
fun WeekDaysBar() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 60.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val days = listOf("MON" to "21", "TUE" to "22", "WED" to "23", "THU" to "24", "FRI" to "25", "SAT" to "26", "SUN" to "27")
        days.forEach { (day, date) ->
            val isSelected = date == "24"
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text(day, color = if (isSelected) PrimaryColor else TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(if (isSelected) PrimaryColor else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(date, color = if (isSelected) TextDark else TextPrimary, fontSize = 16.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}

@Composable
fun WeekTimeGrid() {
    Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Column {
            for (hour in 9..17) {
                Row(modifier = Modifier.fillMaxWidth().height(60.dp)) {
                    Box(modifier = Modifier.width(60.dp).padding(end = 8.dp), contentAlignment = Alignment.TopEnd) {
                        Text("$hour:00", color = TextSecondary, fontSize = 12.sp)
                    }
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFF282A2D)))
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
        
        Box(
            modifier = Modifier
                .padding(start = 60.dp, end = 16.dp, top = 0.dp)
                .fillMaxWidth()
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.weight(3f))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .offset(y = 10.dp)
                        .height(80.dp)
                        .padding(end = 4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x330F4490))
                        .border(1.dp, Color(0xFF0F4490), RoundedCornerShape(4.dp))
                        .padding(4.dp)
                ) {
                    Text("Calculus", color = Color(0xFF92B4FF), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
                }
                Spacer(modifier = Modifier.weight(3f))
            }
        }
    }
}

