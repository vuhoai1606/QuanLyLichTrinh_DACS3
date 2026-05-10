package com.bfy.schedule_app.ui.screens.focusmode

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bfy.schedule_app.ui.theme.*

@Composable
fun FocusModeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Focus Mode",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        TimerSection()
        
        Spacer(modifier = Modifier.height(32.dp))
        
        TodayTasksSection()
    }
}

@Composable
fun TimerSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Gradient Circle Timer Placeholder
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
                .border(8.dp, PrimaryColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "25:00",
                    color = TextPrimary,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-2).sp
                )
                Text(
                    text = "Pomodoro",
                    color = TextSecondary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Pomodoro", color = PrimaryColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.width(24.dp))
            Text("Short Break", color = TextSecondary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.width(24.dp))
            Text("Long Break", color = TextSecondary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Start Button
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(PrimaryColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Start", tint = TextDark, modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
fun TodayTasksSection() {
    Column(modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Today's Tasks", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text("See All", color = PrimaryColor, fontSize = 14.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Task Item
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E2125))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(24.dp).border(2.dp, TextSecondary, RoundedCornerShape(4.dp)))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Design System Setup", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text("Due 5:00 PM", color = TextSecondary, fontSize = 12.sp)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0x3359DBC7))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("High", color = PrimaryColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

