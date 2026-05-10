package com.bfy.schedule_app.ui.screens.homedashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bfy.schedule_app.ui.screens.calendar.CalendarScreen
import com.bfy.schedule_app.ui.screens.collaboration.CollaborationScreen
import com.bfy.schedule_app.ui.screens.collaboration.GroupDetailScreen
import com.bfy.schedule_app.ui.screens.focusmode.FocusModeScreen
import com.bfy.schedule_app.ui.screens.profile.ProfileScreen
import com.bfy.schedule_app.ui.theme.*

enum class DashboardTab {
    HOME, CALENDAR, FOCUS, COLLAB, PROFILE
}

@Composable
fun HomeDashboardScreen() {
    var selectedTab by remember { mutableStateOf(DashboardTab.HOME) }
    var showGroupDetail by remember { mutableStateOf(false) }

    if (showGroupDetail) {
        GroupDetailScreen(
            onBackClick = { showGroupDetail = false },
            onTabSelected = { tab ->
                selectedTab = tab
                showGroupDetail = false
            }
        )
        return
    }

    Scaffold(
        topBar = { DashboardTopAppBar() },
        bottomBar = {
            DashboardBottomNavBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        },
        backgroundColor = BackgroundColor
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                DashboardTab.HOME -> HomeContent()
                DashboardTab.CALENDAR -> CalendarScreen()
                DashboardTab.FOCUS -> FocusModeScreen()
                DashboardTab.COLLAB -> CollaborationScreen(onGroupClick = { showGroupDetail = true })
                DashboardTab.PROFILE -> ProfileScreen()
            }
        }
    }
}

@Composable
private fun HomeContent() {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item { UserInfoSection() }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item { EXPProgressBar() }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item { TimelineSection() }
        item { Spacer(modifier = Modifier.height(100.dp)) } // Spacer for floating nav
    }
}

@Composable
fun DashboardTopAppBar() {
    TopAppBar(
        backgroundColor = Color(0xFF020617),
        elevation = 1.dp,
        contentPadding = PaddingValues(horizontal = 24.dp),
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "BFY",
                    color = Color(0xFF2DD4BF),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1).sp
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                        .border(1.dp, Color(0xFF869490), CircleShape)
                )
            }
        }
    }
}

@Composable
fun UserInfoSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text("Hello, Alex!", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Rank: Gold Scholar", fontSize = 16.sp, color = TextSecondary)
        }
        Text("Level 42", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = PrimaryColor)
    }
}

@Composable
fun EXPProgressBar() {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E2023))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f) // Progress
                    .fillMaxHeight()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(PrimaryColor, Color(0xFFAD7BFF))
                        )
                    )
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("1,240 EXP", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            Text("2,000 EXP to Next Level", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun TimelineSection() {
    Column {
        Text("Today, Oct 24", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF869490))
        Spacer(modifier = Modifier.height(12.dp))
        
        // Example Timeline Items
        TimelineEventItem(
            tag = "[E] EVENT",
            tagBg = Color(0x330F4490),
            tagColor = Color(0xFF92B4FF),
            time = "09:00 AM - 10:30 AM",
            title = "Advanced Calculus Lecture",
            subtitle = "Room 402, Science Building",
            borderColor = Color(0xFF0F4490)
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        TimelineEventItem(
            tag = "[T] TASK",
            tagBg = Color(0x33AD7BFF),
            tagColor = Color(0xFFAD7BFF),
            time = "Due 2:00 PM",
            title = "Submit Physics Lab Report",
            subtitle = "Upload PDF to student portal.",
            borderColor = Color(0xFFAD7BFF)
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        TimelineTodoItem(
            title = "Read Chapter 4 of History Text",
            completed = false
        )
        Spacer(modifier = Modifier.height(12.dp))
        TimelineTodoItem(
            title = "Reply to Professor's Email",
            completed = true
        )
    }
}

@Composable
fun TimelineEventItem(
    tag: String, tagBg: Color, tagColor: Color,
    time: String, title: String, subtitle: String, borderColor: Color
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(100.dp)
                .background(Color(0xFF282A2D))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceColor)
                .border(width = 0.dp, color = Color.Transparent, shape = RoundedCornerShape(8.dp))
                // Note: Compose lacks simple built-in left-border only, we simulate with a Box on the left
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.width(4.dp).height(100.dp).background(borderColor))
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(tagBg).padding(horizontal = 8.dp, vertical = 2.dp)) {
                            Text(tag, color = tagColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(time, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(title, color = TextPrimary, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(subtitle, color = TextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun TimelineTodoItem(title: String, completed: Boolean) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.width(2.dp).height(60.dp).background(Color(0xFF282A2D)))
        Spacer(modifier = Modifier.width(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (completed) Color(0xFF1A1C1F) else SurfaceColor)
                .border(1.dp, Color(0xFF282A2D), RoundedCornerShape(8.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (completed) PrimaryColor else Color.Transparent)
                    .border(if (completed) 0.dp else 1.dp, Color(0xFF869490), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (completed) Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                color = if (completed) TextSecondary else TextPrimary,
                fontSize = 18.sp,
                textDecoration = if (completed) TextDecoration.LineThrough else TextDecoration.None
            )
        }
    }
}

@Composable
private fun DashboardBottomNavBar(
    selectedTab: DashboardTab,
    onTabSelected: (DashboardTab) -> Unit
) {
    // This floating nav bar matches the Figma design overlapping the screen content
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(Color(0xE6282A2D))
                    .border(1.dp, Color(0xFF333538), RoundedCornerShape(9999.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(if (selectedTab == DashboardTab.HOME) PrimaryColor else Color.Transparent)
                        .clickable { onTabSelected(DashboardTab.HOME) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = "Home",
                        tint = if (selectedTab == DashboardTab.HOME) Color.Black else TextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(if (selectedTab == DashboardTab.CALENDAR) PrimaryColor else Color.Transparent)
                        .clickable { onTabSelected(DashboardTab.CALENDAR) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Calendar",
                        tint = if (selectedTab == DashboardTab.CALENDAR) Color.Black else TextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(if (selectedTab == DashboardTab.FOCUS) PrimaryColor else Color.Transparent)
                        .clickable { onTabSelected(DashboardTab.FOCUS) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Focus",
                        tint = if (selectedTab == DashboardTab.FOCUS) Color.Black else TextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(if (selectedTab == DashboardTab.COLLAB) PrimaryColor else Color.Transparent)
                        .clickable { onTabSelected(DashboardTab.COLLAB) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Collab",
                        tint = if (selectedTab == DashboardTab.COLLAB) Color.Black else TextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(if (selectedTab == DashboardTab.PROFILE) PrimaryColor else Color.Transparent)
                        .clickable { onTabSelected(DashboardTab.PROFILE) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = if (selectedTab == DashboardTab.PROFILE) Color.Black else TextSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xE6282A2D))
                    .border(1.dp, Color(0xFF333538), CircleShape)
                    .clickable { onTabSelected(DashboardTab.FOCUS) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
            }
        }
    }
}

