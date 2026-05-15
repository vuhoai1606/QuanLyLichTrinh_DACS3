package com.bfy.schedule_app.ui.screens.homedashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
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
import com.bfy.schedule_app.data.remote.model.UserDto
import com.bfy.schedule_app.data.remote.model.ScheduleDto
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bfy.schedule_app.utils.Localization
import com.bfy.schedule_app.utils.SettingsManager
import com.bfy.schedule_app.data.repository.AppRepository
import kotlinx.datetime.*

enum class DashboardTab {
    HOME, CALENDAR, FOCUS, COLLAB, PROFILE, LEADERBOARD
}

@Composable
fun HomeDashboardScreen(onLogout: () -> Unit = {}) {
    var selectedTab by remember { mutableStateOf(DashboardTab.HOME) }
    var showGroupDetail by remember { mutableStateOf(false) }
    var selectedGroupId by remember { mutableStateOf<String?>(null) }
    var showCreateNewItem by remember { mutableStateOf(false) }
    var selectedScheduleForAction by remember { mutableStateOf<ScheduleDto?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showEditScreen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showActionDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    
    val viewModel: HomeViewModel = viewModel { HomeViewModel() }
    val focusViewModel: com.bfy.schedule_app.ui.viewmodel.FocusViewModel = viewModel { com.bfy.schedule_app.ui.viewmodel.FocusViewModel() }
    val notificationViewModel: com.bfy.schedule_app.ui.viewmodel.NotificationViewModel = viewModel { com.bfy.schedule_app.ui.viewmodel.NotificationViewModel() }
    
    val uiState by viewModel.uiState.collectAsState()
    val focusUiState by focusViewModel.uiState.collectAsState()
    val notificationUiState by notificationViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    val handleTabSelection: (DashboardTab) -> Unit = { tab ->
        if (!(focusUiState.isRunning && selectedTab == DashboardTab.FOCUS)) {
            selectedTab = tab
        }
    }

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

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            DashboardTopAppBar(
                user = uiState.user,
                onMenuClick = { 
                    if (!(focusUiState.isRunning && selectedTab == DashboardTab.FOCUS)) {
                        scope.launch { scaffoldState.drawerState.open() } 
                    }
                },
                onNotificationClick = {
                    if (!(focusUiState.isRunning && selectedTab == DashboardTab.FOCUS)) {
                        showNotificationsDialog = true
                        notificationViewModel.loadNotifications()
                    }
                },
                onProfileClick = { handleTabSelection(DashboardTab.PROFILE) },
                enabled = !(focusUiState.isRunning && selectedTab == DashboardTab.FOCUS)
            ) 
        },
        drawerContent = {
            DashboardDrawerContent(
                onTabSelected = { 
                    handleTabSelection(it)
                    scope.launch { scaffoldState.drawerState.close() }
                },
                onLogout = {
                    scope.launch { scaffoldState.drawerState.close() }
                    onLogout()
                }
            )
        },
        floatingActionButton = {},
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                DashboardBottomNavBar(
                    selectedTab = selectedTab,
                    onTabSelected = { handleTabSelection(it) },
                    onAddClick = { 
                        if (!(focusUiState.isRunning && selectedTab == DashboardTab.FOCUS)) {
                            showCreateNewItem = true 
                        }
                    },
                    enabled = !(focusUiState.isRunning && selectedTab == DashboardTab.FOCUS)
                )
            }
        },
        backgroundColor = MaterialTheme.colors.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                DashboardTab.HOME -> HomeContent(
                    uiState = uiState,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { 
                        searchQuery = it
                        viewModel.searchSchedules(it)
                    },
                    onLeaderboardClick = { selectedTab = DashboardTab.LEADERBOARD },
                    onItemClick = { schedule ->
                        selectedScheduleForAction = schedule
                        showActionDialog = true
                    }
                )

                DashboardTab.CALENDAR -> CalendarScreen()
                DashboardTab.FOCUS -> FocusModeScreen(viewModel = focusViewModel)
                DashboardTab.COLLAB -> CollaborationScreen(onGroupClick = { id -> 
                    selectedGroupId = id
                    showGroupDetail = true 
                })
                DashboardTab.PROFILE -> ProfileScreen(onLogout = onLogout)
                DashboardTab.LEADERBOARD -> com.bfy.schedule_app.ui.screens.leaderboard.LeaderboardScreen(onBackClick = { selectedTab = DashboardTab.HOME })
            }

            if (showCreateNewItem) {
                CreateNewItemScreen(
                    onDismiss = { showCreateNewItem = false },
                    onSuccess = { viewModel.loadDashboardData() }
                )
            }

            if (showEditScreen && selectedScheduleForAction != null) {
                CreateNewItemScreen(
                    initialSchedule = selectedScheduleForAction,
                    onDismiss = { 
                        showEditScreen = false 
                        selectedScheduleForAction = null
                    },
                    onSuccess = { viewModel.loadDashboardData() }
                )
            }

            if (showActionDialog && selectedScheduleForAction != null) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showActionDialog = false },
                    title = { Text(selectedScheduleForAction?.title ?: "") },
                    text = {
                        Column {
                            androidx.compose.material3.TextButton(
                                onClick = {
                                    scope.launch {
                                        try {
                                            AppRepository().updateSchedule(
                                                selectedScheduleForAction!!.id,
                                                mapOf("status" to "DONE")
                                            )
                                            viewModel.loadDashboardData()
                                            showActionDialog = false
                                            selectedScheduleForAction = null
                                        } catch (e: Exception) {}
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryColor)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(Localization.get("mark_done") ?: "Mark as Done")
                                }
                            }
                            androidx.compose.material3.TextButton(
                                onClick = {
                                    showActionDialog = false
                                    showEditScreen = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(Localization.get("edit") ?: "Edit")
                                }
                            }
                            androidx.compose.material3.TextButton(
                                onClick = {
                                    showActionDialog = false
                                    showDeleteConfirm = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(Localization.get("delete") ?: "Delete", color = Color.Red)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        androidx.compose.material3.TextButton(onClick = { showActionDialog = false }) {
                            Text(Localization.get("cancel") ?: "Cancel")
                        }
                    }
                )
            }

            if (showDeleteConfirm && selectedScheduleForAction != null) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showDeleteConfirm = false },
                    title = { Text(Localization.get("delete_item")) },
                    text = { Text(Localization.get("delete_confirm_msg").format(selectedScheduleForAction?.title ?: "")) },
                    confirmButton = {
                        androidx.compose.material3.TextButton(onClick = {
                            scope.launch {
                                try {
                                    AppRepository().deleteSchedule(selectedScheduleForAction!!.id)
                                    viewModel.loadDashboardData()
                                    showDeleteConfirm = false
                                    selectedScheduleForAction = null
                                } catch (e: Exception) {}
                            }
                        }) {
                            Text(Localization.get("delete"), color = Color.Red)
                        }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(onClick = { showDeleteConfirm = false }) {
                            Text(Localization.get("cancel"))
                        }
                    }
                )
            }

            if (showNotificationsDialog) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showNotificationsDialog = false },
                    title = { androidx.compose.material3.Text(Localization.get("notifications")) },
                    text = {
                        Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                            if (notificationUiState.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryColor)
                            } else if (notificationUiState.notifications.isEmpty()) {
                                androidx.compose.material3.Text(Localization.get("no_notifications"), modifier = Modifier.align(Alignment.Center))
                            } else {
                                LazyColumn {
                                    items(notificationUiState.notifications) { notification ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { notificationViewModel.markRead(notification.id) }
                                                .padding(vertical = 8.dp)
                                        ) {
                                            androidx.compose.material3.Text(
                                                text = notification.title,
                                                fontWeight = if (notification.is_read) FontWeight.Normal else FontWeight.Bold,
                                                color = if (notification.is_read) TextSecondary else TextPrimary
                                            )
                                            androidx.compose.material3.Text(
                                                text = notification.message,
                                                fontSize = 12.sp,
                                                color = TextSecondary
                                            )
                                            Divider(modifier = Modifier.padding(top = 8.dp), color = Color.Gray.copy(alpha = 0.2f))
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        androidx.compose.material3.TextButton(onClick = { showNotificationsDialog = false }) {
                            androidx.compose.material3.Text(Localization.get("ok"))
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun HomeContent(
    uiState: HomeUiState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onLeaderboardClick: () -> Unit,
    onItemClick: (ScheduleDto) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp)
    ) {
        item {
            UserInfoSection(
                name = uiState.user?.full_name ?: "",
                rank = uiState.user?.current_rank ?: "Rookie",
                level = (uiState.user?.total_exp ?: 0) / 1000 + 1,
                onLeaderboardClick = onLeaderboardClick
            ) 
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item { 
            val exp = uiState.user?.total_exp ?: 0
            val currentLevel = (exp / 1000) + 1
            val nextLevelExp = currentLevel * 1000
            EXPProgressBar(
                exp = exp,
                nextLevelExp = nextLevelExp
            ) 
        }
        item { Spacer(modifier = Modifier.height(32.dp)) }
        item {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            Text(
                "${Localization.getMonth(now.month)} ${now.dayOfMonth}, ${now.year}",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item { TimelineSection(uiState.schedules, onItemClick) }
    }
}

@Composable
fun DashboardTopAppBar(
    user: UserDto?,
    onMenuClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    enabled: Boolean = true
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
                    tint = if (enabled) MaterialTheme.colors.onSurface else MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.clickable(enabled = enabled) { onMenuClick() }
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
                    tint = if (enabled) MaterialTheme.colors.onSurface else MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.clickable(enabled = enabled) { onNotificationClick() }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                        .border(1.dp, Color(0xFF869490), CircleShape)
                        .clickable(enabled = enabled) { onProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(user?.full_name?.take(1)?.uppercase() ?: "?", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DashboardBottomNavBar(
    selectedTab: DashboardTab,
    onTabSelected: (DashboardTab) -> Unit,
    onAddClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Capsule Menu
        Row(
            modifier = Modifier
                .weight(1f)
                .height(64.dp)
                .clip(RoundedCornerShape(9999.dp))
                .background(Color(0xFF1E2023))
                .border(1.dp, Color(0x1A869490), RoundedCornerShape(9999.dp))
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BottomNavItem(Icons.Default.Home, selectedTab == DashboardTab.HOME, enabled) { onTabSelected(DashboardTab.HOME) }
            BottomNavItem(Icons.Default.DateRange, selectedTab == DashboardTab.CALENDAR, enabled) { onTabSelected(DashboardTab.CALENDAR) }
            BottomNavItem(Icons.Default.Schedule, selectedTab == DashboardTab.FOCUS, enabled) { onTabSelected(DashboardTab.FOCUS) }
            BottomNavItem(Icons.Default.Group, selectedTab == DashboardTab.COLLAB, enabled) { onTabSelected(DashboardTab.COLLAB) }
            BottomNavItem(Icons.Default.Person, selectedTab == DashboardTab.PROFILE, enabled) { onTabSelected(DashboardTab.PROFILE) }
        }

        // Separate Circular Add Button
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E2023))
                .border(1.dp, Color(0x1A869490), CircleShape)
                .clickable(enabled = enabled) { onAddClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add",
                tint = Color(0xFF59DBC7),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun BottomNavItem(icon: ImageVector, active: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val bgModifier = if (active) {
        Modifier
            .clip(RoundedCornerShape(9999.dp))
            .background(PrimaryColor)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    } else {
        Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    }

    Box(
        modifier = Modifier
            .clickable(enabled = enabled) { onClick() }
            .then(bgModifier),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (active) Color(0xFF003731) else (if (enabled) TextSecondary else TextSecondary.copy(alpha = 0.3f)),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun DashboardDrawerContent(onTabSelected: (DashboardTab) -> Unit, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111316))
            .padding(24.dp)
    ) {
        Text("BFY Schedule", color = PrimaryColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        DrawerItem(Localization.get("home"), Icons.Default.Home) { onTabSelected(DashboardTab.HOME) }
        DrawerItem(Localization.get("calendar"), Icons.Default.DateRange) { onTabSelected(DashboardTab.CALENDAR) }
        DrawerItem(Localization.get("focus_mode"), Icons.Default.Star) { onTabSelected(DashboardTab.FOCUS) }
        DrawerItem(Localization.get("collaboration"), Icons.Default.Group) { onTabSelected(DashboardTab.COLLAB) }
        DrawerItem(Localization.get("profile"), Icons.Default.Person) { onTabSelected(DashboardTab.PROFILE) }
        Spacer(modifier = Modifier.weight(1f))
        DrawerItem(Localization.get("logout"), Icons.Default.ExitToApp, Color(0xFFFFB4AB)) { onLogout() }
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

@Composable
fun UserInfoSection(name: String, rank: String, level: Int, onLeaderboardClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Hello, $name!", color = TextPrimary, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text("Rank: $rank", color = TextSecondary, fontSize = 16.sp)
        }
        Text(
            "Level $level",
            color = PrimaryColor,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onLeaderboardClick() }
        )
    }
}

@Composable
fun EXPProgressBar(exp: Int, nextLevelExp: Int) {
    val progress = exp.toFloat() / nextLevelExp.toFloat()
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape)
                .background(Color(0x33869490))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(Color(0xFF59DBC7), Color(0xFFAD7BFF))))
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${exp.toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1,")} EXP", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("${nextLevelExp.toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1,")} EXP to Next Level", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TimelineSection(schedules: List<ScheduleDto>, onItemClick: (ScheduleDto) -> Unit) {
    if (schedules.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(Localization.get("no_schedules_today") ?: "No schedules for today", color = TextSecondary)
        }
        return
    }

    val items = schedules

    Column(modifier = Modifier.fillMaxWidth()) {
        items.forEachIndexed { index, schedule ->
            Row(modifier = Modifier.fillMaxWidth()) {
                // Timeline Line and Dot
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(32.dp)
                ) {
                    val dotColor = when(schedule.type) {
                        "EVENT" -> Color(0xFF0F4490)
                        "TASK" -> Color(0xFFAD7BFF)
                        else -> if (schedule.status == "DONE") Color(0xFF59DBC7) else Color(0x33869490)
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                    
                    if (index < items.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(if (schedule.type == "TODO") 80.dp else 120.dp)
                                .background(Color(0x1A869490))
                        )
                    }
                }
                
                // Content
                Column(modifier = Modifier.weight(1f).padding(bottom = 24.dp)) {
                    TimelineCard(schedule, onClick = { onItemClick(schedule) })
                }
            }
        }
    }
}

@Composable
fun TimelineCard(schedule: ScheduleDto, onClick: () -> Unit) {
    if (schedule.type == "TODO") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E2023))
                .clickable { onClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .border(2.dp, if (schedule.status == "DONE") Color(0xFF59DBC7) else Color(0x33869490), RoundedCornerShape(4.dp))
                    .background(if (schedule.status == "DONE") Color(0xFF59DBC7) else Color.Transparent, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (schedule.status == "DONE") {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF003731), modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                schedule.title,
                color = if (schedule.status == "DONE") TextSecondary else TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textDecoration = if (schedule.status == "DONE") TextDecoration.LineThrough else null
            )
        }
    } else {
        val accentColor = if (schedule.type == "EVENT") Color(0xFF0F4490) else Color(0xFFAD7BFF)
        val tagBg = if (schedule.type == "EVENT") Color(0x330F4490) else Color(0x33AD7BFF)
        val tagColor = if (schedule.type == "EVENT") Color(0xFF92B4FF) else Color(0xFFD5BAFF)
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E2023))
                .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .clickable { onClick() }
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(tagBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        if (schedule.type == "EVENT") "[E] EVENT" else "[T] TASK",
                        color = tagColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    if (schedule.type == "EVENT") {
                        "${schedule.start_time?.substringAfter("T")?.substringBefore(":")}:${schedule.start_time?.substringAfter(":")?.substringBefore(":")} AM - ${schedule.end_time?.substringAfter("T")?.substringBefore(":")}:${schedule.end_time?.substringAfter(":")?.substringBefore(":")} AM"
                    } else {
                        "Due ${schedule.deadline?.substringAfter("T")?.substringBefore(":")}:00 PM"
                    },
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(schedule.title, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                schedule.location ?: schedule.description ?: "",
                color = TextSecondary,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun TimelineEventItem(
    tag: String,
    tagBg: Color,
    tagColor: Color,
    time: String,
    title: String,
    subtitle: String,
    borderColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E2023))
            .border(1.dp, borderColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(tagBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(tag, color = tagColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(time, color = TextSecondary, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            if (subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, color = TextSecondary, fontSize = 14.sp)
            }
        }
    }
}
