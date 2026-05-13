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
import com.bfy.schedule_app.ui.viewmodel.CalendarViewModel
import com.bfy.schedule_app.ui.viewmodel.CalendarUiState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.datetime.*

@Composable
fun CalendarWeekViewScreen(viewModel: CalendarViewModel = viewModel { CalendarViewModel() }) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        WeekDaysBar(viewModel)
        Spacer(modifier = Modifier.height(16.dp))
        WeekTimeGrid(uiState)
    }
}

@Composable
fun WeekDaysBar(viewModel: CalendarViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedDate = uiState.selectedDate
    
    // Calculate the start of the week (Monday)
    val dayOfWeek = selectedDate.dayOfWeek.isoDayNumber // 1 (Mon) to 7 (Sun)
    val monday = selectedDate.minus(dayOfWeek - 1, DateTimeUnit.DAY)
    
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 60.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val days = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
        
        days.forEachIndexed { index, dayName ->
            val date = monday.plus(index, DateTimeUnit.DAY)
            val isSelected = date == selectedDate
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally, 
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.onDateSelected(date) }
            ) {
                Text(
                    dayName, 
                    color = if (isSelected) PrimaryColor else TextSecondary, 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) PrimaryColor else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        date.dayOfMonth.toString(), 
                        color = if (isSelected) TextDark else TextPrimary, 
                        fontSize = 16.sp, 
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun WeekTimeGrid(uiState: CalendarUiState) {
    val selectedDate = uiState.selectedDate
    val dayOfWeek = selectedDate.dayOfWeek.isoDayNumber
    val monday = selectedDate.minus(dayOfWeek - 1, DateTimeUnit.DAY)
    val sunday = monday.plus(6, DateTimeUnit.DAY)

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
        
        // Display events from backend for the entire week
        uiState.schedules.forEach { schedule ->
            val startTimeStr = schedule.start_time
            if (startTimeStr != null && startTimeStr.length >= 10) {
                try {
                    val eventDate = LocalDate.parse(startTimeStr.substring(0, 10))
                    // Check if event is within the current week
                    if (eventDate >= monday && eventDate <= sunday) {
                        val dayIndex = (eventDate.dayOfMonth - monday.dayOfMonth + 31) % 31 // simplified, but better calculate properly
                        // Real calculation:
                        val daysBetween = eventDate.toEpochDays() - monday.toEpochDays()
                        
                        if (daysBetween in 0..6) {
                            val hour = startTimeStr.substringAfter("T").substringBefore(":").toIntOrNull() ?: 9
                            if (hour in 9..17) {
                                val topOffset = (hour - 9) * 60
                                val leftOffset = 60 + (daysBetween * ((1.0 / 7.0))) // This won't work with offset.dp directly
                                
                                Box(
                                    modifier = Modifier
                                        .padding(start = 60.dp, end = 16.dp)
                                        .offset(
                                            x = (daysBetween * ( (400 - 60 - 16) / 7)).dp, // Rough estimation of width
                                            y = topOffset.dp
                                        )
                                        .fillMaxWidth(0.12f)
                                        .height(60.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (schedule.type == "EVENT") Color(0x330F4490) else Color(0x33AD7BFF))
                                        .border(1.dp, if (schedule.type == "EVENT") Color(0xFF0F4490) else Color(0xFFAD7BFF), RoundedCornerShape(4.dp))
                                        .padding(4.dp)
                                ) {
                                    Text(schedule.title, color = if (schedule.type == "EVENT") Color(0xFF92B4FF) else Color(0xFFAD7BFF), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {}
            }
        }
    }
}


