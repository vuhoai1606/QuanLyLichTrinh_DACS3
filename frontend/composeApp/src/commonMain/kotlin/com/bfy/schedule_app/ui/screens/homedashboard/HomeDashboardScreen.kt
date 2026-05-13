package com.bfy.schedule_app.ui.screens.homedashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bfy.schedule_app.ui.screens.calendar.CalendarScreen
import com.bfy.schedule_app.ui.screens.collaboration.CollaborationScreen
import com.bfy.schedule_app.ui.screens.collaboration.GroupDetailScreen
import com.bfy.schedule_app.ui.screens.createitem.CreateNewItemScreen
import com.bfy.schedule_app.ui.screens.focusmode.FocusModeScreen
import com.bfy.schedule_app.ui.screens.profile.ProfileScreen
import com.bfy.schedule_app.ui.theme.*
import com.bfy.schedule_app.ui.viewmodel.HomeViewModel
import com.bfy.schedule_app.ui.viewmodel.HomeUiState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.bfy.schedule_app.utils.Localization
import com.bfy.schedule_app.utils.SettingsManager


enum class DashboardTab {
    HOME, CALENDAR, FOCUS, COLLAB, PROFILE
}

@Composable
fun HomeDashboardScreen(onLogout: () -> Unit = {}) {
    var selectedTab by remember { mutableStateOf(DashboardTab.HOME) }
    var showGroupDetail by remember { mutableStateOf(false) }
    var selectedGroupId by remember { mutableStateOf<String?>(null) }
    var showCreateNewItem by remember { mutableStateOf(false) }
    
    val viewModel: HomeViewModel = viewModel { HomeViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()


    if (showGroupDetail && selectedGroupId != null) {
        GroupDetailScreen(
            groupId = selectedGroupId!!,
            onBackClick = { 
                showGroupDetail = false 
                selectedGroupId = null
            },
            onTabSelected = { tab ->
                selectedTab = tab
                showGroupDetail = false
                selectedGroupId = null
            }
        )
        return
    }

    val scaffoldState = rememberScaffoldState(drawerState = rememberDrawerState(DrawerValue.Closed))

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = { 
            DashboardTopAppBar(
                user = uiState.user,
                onMenuClick = { scope.launch { scaffoldState.drawerState.open() } },
                onNotificationClick = {
                    SettingsManager.notificationMessage = "No new notifications."
                },
                onProfileClick = { selectedTab = DashboardTab.PROFILE }
            ) 
        },
        drawerContent = {
            DashboardDrawerContent(
                onTabSelected = { 
                    selectedTab = it
                    scope.launch { scaffoldState.drawerState.close() }
                },
                onLogout = {
                    scope.launch { scaffoldState.drawerState.close() }
                    onLogout()
                }
            )
        },
        drawerBackgroundColor = MaterialTheme.colors.background,
        bottomBar = {
            DashboardBottomNavBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onAddClick = { showCreateNewItem = true }
            )
        },
        backgroundColor = MaterialTheme.colors.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                DashboardTab.HOME -> HomeContent(uiState)

                DashboardTab.CALENDAR -> CalendarScreen()
                DashboardTab.FOCUS -> FocusModeScreen()
                DashboardTab.COLLAB -> CollaborationScreen(onGroupClick = { id -> 
                    selectedGroupId = id
                    showGroupDetail = true 
                })
                DashboardTab.PROFILE -> ProfileScreen(onLogout = onLogout)
            }

            if (showCreateNewItem) {
                CreateNewItemScreen(onDismiss = { showCreateNewItem = false })
            }

            // Mock Notification Banner
            SettingsManager.notificationMessage?.let { msg ->
                MockNotificationBanner(
                    message = msg,
                    onDismiss = { SettingsManager.notificationMessage = null }
                )
            }
        }
    }
}

@Composable
fun MockNotificationBanner(message: String, onDismiss: () -> Unit) {
    androidx.compose.runtime.LaunchedEffect(message) {
        kotlinx.coroutines.delay(3000)
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .statusBarsPadding()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onDismiss() },
            color = Color(0xFF1E2023),
            elevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = PrimaryColor)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Notification", color = PrimaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(message, color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun HomeContent(uiState: HomeUiState) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item { 
            UserInfoSection(
                name = uiState.user?.full_name ?: "User",
                rank = uiState.user?.current_rank ?: "Rookie",
                level = (uiState.user?.total_exp ?: 0) / 100 // Simple level calculation
            ) 
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item { 
            EXPProgressBar(
                exp = uiState.user?.total_exp ?: 0,
                nextLevelExp = (((uiState.user?.total_exp ?: 0) / 100) + 1) * 100
            ) 
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item { TimelineSection(uiState.schedules) }
        item { Spacer(modifier = Modifier.height(100.dp)) } // Spacer for floating nav
    }
}


@Composable
fun DashboardTopAppBar(
    user: UserDto?,
    onMenuClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    TopAppBar(
        backgroundColor = if (MaterialTheme.colors.isLight) Color.White else Color(0xFF020617),
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
                Icon(
                    Icons.Default.Menu, 
                    contentDescription = "Menu", 
                    tint = MaterialTheme.colors.onSurface,
                    modifier = Modifier.clickable { onMenuClick() }
                )
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
                Icon(
                    Icons.Default.Notifications, 
                    contentDescription = "Notifications", 
                    tint = MaterialTheme.colors.onSurface,
                    modifier = Modifier.clickable { onNotificationClick() }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (user?.avatar_url != null) Color.Transparent else Color.Gray)
                        .border(1.dp, Color(0xFF869490), CircleShape)
                        .clickable { onProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (user?.avatar_url != null) {
                        // In real app, load image from URL. For KMP, we might need a library like Coil.
                        // For now, placeholder with initials
                        Text(user.full_name.take(1).uppercase(), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun UserInfoSection(name: String, rank: String, level: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text("${Localization.get("hello")}, $name!", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("${Localization.get("rank")}: $rank", fontSize = 16.sp, color = TextSecondary)
        }
        Text("${Localization.get("level")} $level", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = PrimaryColor)
    }
}


@Composable
fun EXPProgressBar(exp: Int, nextLevelExp: Int) {
    val progress = if (nextLevelExp > 0) exp.toFloat() / nextLevelExp else 0f
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colors.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f)) // Progress
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
            Text("$exp EXP", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            Text("${nextLevelExp - exp} EXP to Next Level", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
        }
    }
}


@Composable
fun TimelineSection(schedules: List<com.bfy.schedule_app.data.remote.model.ScheduleDto>) {
    Column {
        // You could use kotlinx-datetime to get actual date
        Text(Localization.get("today"), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF869490))
        Spacer(modifier = Modifier.height(12.dp))
        
        if (schedules.isEmpty()) {
            Text(Localization.get("no_activities"), color = TextSecondary, modifier = Modifier.padding(vertical = 16.dp))
        }

        schedules.forEach { schedule ->
            if (schedule.type == "TODO") {
                TimelineTodoItem(
                    title = schedule.title,
                    completed = schedule.status == "DONE"
                )
            } else {
                TimelineEventItem(
                    tag = "[${schedule.type.first()}] ${schedule.type}",
                    tagBg = if (schedule.type == "EVENT") Color(0x330F4490) else Color(0x33AD7BFF),
                    tagColor = if (schedule.type == "EVENT") Color(0xFF92B4FF) else Color(0xFFAD7BFF),
                    time = schedule.start_time?.substringAfter("T")?.substringBefore(".") ?: "All Day",
                    title = schedule.title,
                    subtitle = schedule.location ?: schedule.description ?: "",
                    borderColor = if (schedule.type == "EVENT") Color(0xFF0F4490) else Color(0xFFAD7BFF)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
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
                .background(MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colors.surface)
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
        Box(modifier = Modifier.width(2.dp).height(60.dp).background(MaterialTheme.colors.onSurface.copy(alpha = 0.1f)))
        Spacer(modifier = Modifier.width(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (completed) MaterialTheme.colors.onSurface.copy(alpha = 0.05f) else MaterialTheme.colors.surface)
                .border(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
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
    onTabSelected: (DashboardTab) -> Unit,
    onAddClick: () -> Unit
) {
    // This floating nav bar matches the Figma design overlapping the screen content
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 24.dp), // Adjust padding to match bottom-[24px] px-[16px]
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Main NavBar background
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(66.dp) // Based on size of 48dp items inside 9dp paddings (approx)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(if (MaterialTheme.colors.isLight) Color.White.copy(alpha = 0.9f) else Color(0xE6282A2D)) // backdrop-blur-[6px] bg-[rgba(40,42,45,0.9)] mapping
                    .border(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.1f), RoundedCornerShape(9999.dp))
                    .padding(9.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home 
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(if (selectedTab == DashboardTab.HOME) Color(0xFF59DBC7) else Color.Transparent)
                        .clickable { onTabSelected(DashboardTab.HOME) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Home,
                        contentDescription = "Home",
                        tint = if (selectedTab == DashboardTab.HOME) Color(0xFF003731) else Color(0xFFBBCAC5),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Calendar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(if (selectedTab == DashboardTab.CALENDAR) Color(0xFF59DBC7) else Color.Transparent)
                        .clickable { onTabSelected(DashboardTab.CALENDAR) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.CalendarMonth,
                        contentDescription = "Calendar",
                        tint = if (selectedTab == DashboardTab.CALENDAR) Color(0xFF003731) else Color(0xFFBBCAC5),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Focus
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(if (selectedTab == DashboardTab.FOCUS) Color(0xFF59DBC7) else Color.Transparent)
                        .clickable { onTabSelected(DashboardTab.FOCUS) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Timer,
                        contentDescription = "Focus",
                        tint = if (selectedTab == DashboardTab.FOCUS) Color(0xFF003731) else Color(0xFFBBCAC5),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Collab
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(if (selectedTab == DashboardTab.COLLAB) Color(0xFF59DBC7) else Color.Transparent)
                        .clickable { onTabSelected(DashboardTab.COLLAB) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.People,
                        contentDescription = "Collab",
                        tint = if (selectedTab == DashboardTab.COLLAB) Color(0xFF003731) else Color(0xFFBBCAC5),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Profile
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(if (selectedTab == DashboardTab.PROFILE) Color(0xFF59DBC7) else Color.Transparent)
                        .clickable { onTabSelected(DashboardTab.PROFILE) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Person,
                        contentDescription = "Profile",
                        tint = if (selectedTab == DashboardTab.PROFILE) Color(0xFF003731) else Color(0xFFBBCAC5),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Add Button Spacer (padding removed from design and used flex)
            Spacer(modifier = Modifier.width(8.dp)) // approx value for visual gap if not justified completely
            
            // Add Button (FAB adjacent)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(if (MaterialTheme.colors.isLight) Color.White.copy(alpha = 0.9f) else Color(0xE6282A2D))
                    .border(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.1f), CircleShape)
                    .clickable { onAddClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Add, 
                    contentDescription = "Add", 
                    tint = MaterialTheme.colors.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun DashboardDrawerContent(
    onTabSelected: (DashboardTab) -> Unit,
    onLogout: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxHeight().width(300.dp),
        color = MaterialTheme.colors.background
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "BFY Menu",
                color = PrimaryColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 24.dp)
            )
            
            Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
            
            DrawerItem("Home", Icons.Default.Home) { onTabSelected(DashboardTab.HOME) }
            DrawerItem("Calendar", Icons.Default.DateRange) { onTabSelected(DashboardTab.CALENDAR) }
            DrawerItem("Focus", Icons.Default.Timer) { onTabSelected(DashboardTab.FOCUS) }
            DrawerItem("Collaboration", Icons.Default.Group) { onTabSelected(DashboardTab.COLLAB) }
            DrawerItem("Profile", Icons.Default.Person) { onTabSelected(DashboardTab.PROFILE) }
            
            Spacer(modifier = Modifier.weight(1f))
            
            DrawerItem("Logout", Icons.Default.ExitToApp, Color(0xFFFFB4AB)) { onLogout() }
        }
    }
}

@Composable
private fun DrawerItem(label: String, icon: ImageVector, color: Color = Color.White, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, color = color, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

