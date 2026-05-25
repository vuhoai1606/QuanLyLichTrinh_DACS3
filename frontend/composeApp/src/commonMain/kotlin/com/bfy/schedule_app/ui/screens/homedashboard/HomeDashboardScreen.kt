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
import kotlinx.datetime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
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
                        showEditScreen = true
                    },
                    onStatusChange = { viewModel.loadDashboardData() },
                    onEditClick = { schedule ->
                        selectedScheduleForAction = schedule
                        showEditScreen = true
                    },
                    onDeleteClick = { schedule ->
                        selectedScheduleForAction = schedule
                        showDeleteConfirm = true
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
                    title = { 
                        Text(
                            text = selectedScheduleForAction?.title ?: "",
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (!selectedScheduleForAction!!.description.isNullOrBlank()) {
                                Text(
                                    text = selectedScheduleForAction!!.description!!,
                                    color = Color(0xFFBBCAC5),
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            }
                            
                            ActionDialogButton(
                                label = Localization.get("mark_done") ?: "Mark as Done",
                                icon = Icons.Default.CheckCircle,
                                iconTint = PrimaryColor,
                                onClick = {
                                    scope.launch {
                                        try {
                                            AppRepository().updateSchedule(
                                                selectedScheduleForAction!!.id,
                                                com.bfy.schedule_app.data.remote.model.UpdateScheduleRequest(status = "DONE")
                                            )
                                            viewModel.loadDashboardData()
                                            showActionDialog = false
                                            selectedScheduleForAction = null
                                        } catch (e: Exception) {}
                                    }
                                }
                            )
                            
                            ActionDialogButton(
                                label = Localization.get("edit") ?: "Edit",
                                icon = Icons.Default.Edit,
                                iconTint = Color.White,
                                onClick = {
                                    showActionDialog = false
                                    showEditScreen = true
                                }
                            )
                            
                            ActionDialogButton(
                                label = Localization.get("delete") ?: "Delete",
                                icon = Icons.Default.Delete,
                                iconTint = Color(0xFFFF7B7B),
                                textColor = Color(0xFFFF7B7B),
                                onClick = {
                                    showActionDialog = false
                                    showDeleteConfirm = true
                                }
                            )
                        }
                    },
                    confirmButton = {
                        androidx.compose.material3.TextButton(onClick = { showActionDialog = false }) {
                            Text(Localization.get("cancel") ?: "Cancel", color = Color(0xFF59DBC7))
                        }
                    },
                    containerColor = Color(0xFF1E2023),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            if (showDeleteConfirm && selectedScheduleForAction != null) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showDeleteConfirm = false },
                    title = { 
                        Text(
                            Localization.get("delete_item"), 
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ) 
                    },
                    text = { 
                        Text(
                            Localization.get("delete_confirm_msg").format(selectedScheduleForAction?.title ?: ""), 
                            color = Color(0xFFBBCAC5),
                            fontSize = 14.sp
                        ) 
                    },
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
                            Text(Localization.get("delete"), color = Color(0xFFFF7B7B), fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(onClick = { showDeleteConfirm = false }) {
                            Text(Localization.get("cancel"), color = Color(0xFF869490))
                        }
                    },
                    containerColor = Color(0xFF1E2023),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            if (showNotificationsDialog) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showNotificationsDialog = false },
                    title = { 
                        androidx.compose.material3.Text(
                            text = Localization.get("notifications") ?: "Notifications",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    text = {
                        Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                            if (notificationUiState.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryColor)
                            } else if (notificationUiState.notifications.isEmpty()) {
                                androidx.compose.material3.Text(
                                    text = Localization.get("no_notifications") ?: "No notifications",
                                    color = Color(0xFFBBCAC5),
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            } else {
                                LazyColumn {
                                    items(notificationUiState.notifications) { notification ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { notificationViewModel.markRead(notification.id) }
                                                .padding(vertical = 12.dp)
                                        ) {
                                            androidx.compose.material3.Text(
                                                text = notification.title,
                                                fontWeight = if (notification.ia_read) FontWeight.Normal else FontWeight.Bold,
                                                color = if (notification.ia_read) Color(0xFF869490) else Color.White,
                                                fontSize = 16.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            androidx.compose.material3.Text(
                                                text = notification.message,
                                                fontSize = 13.sp,
                                                color = Color(0xFFBBCAC5)
                                            )
                                            Divider(
                                                modifier = Modifier.padding(top = 12.dp),
                                                color = Color(0xFF3C4946).copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        androidx.compose.material3.TextButton(onClick = { showNotificationsDialog = false }) {
                            androidx.compose.material3.Text(Localization.get("ok") ?: "OK", color = Color(0xFF59DBC7))
                        }
                    },
                    containerColor = Color(0xFF1E2023),
                    shape = RoundedCornerShape(16.dp)
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
    onItemClick: (ScheduleDto) -> Unit,
    onStatusChange: () -> Unit,
    onEditClick: (ScheduleDto) -> Unit,
    onDeleteClick: (ScheduleDto) -> Unit
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
        item {
            val nowInstant = Clock.System.now()
            val today = nowInstant.toLocalDateTime(TimeZone.currentSystemDefault()).date
            
            val filteredSchedules = uiState.schedules.filter { schedule ->
                val isDone = schedule.status == "DONE"
                val isOverdue = !isDone && schedule.deadline?.let {
                    try {
                        Instant.parse(it) < nowInstant
                    } catch (e: Exception) { false }
                } ?: false

                if (isOverdue) {
                    true
                } else {
                    var isToday = false
                    if (schedule.type == "EVENT") {
                        try {
                            val startLocal = schedule.start_time?.let { Instant.parse(it).toLocalDateTime(TimeZone.currentSystemDefault()).date }
                            val endLocal = schedule.end_time?.let { Instant.parse(it).toLocalDateTime(TimeZone.currentSystemDefault()).date }
                            if (startLocal != null && endLocal != null) {
                                isToday = today in startLocal..endLocal
                            } else if (startLocal != null) {
                                isToday = startLocal == today
                            }
                        } catch (e: Exception) {}
                    } else if (schedule.type == "TASK") {
                        try {
                            val startLocal = schedule.start_time?.let { Instant.parse(it).toLocalDateTime(TimeZone.currentSystemDefault()).date }
                            val deadlineLocal = schedule.deadline?.let { Instant.parse(it).toLocalDateTime(TimeZone.currentSystemDefault()).date }
                            isToday = (startLocal == today) || (deadlineLocal == today)
                            if (schedule.start_time == null && schedule.deadline == null) {
                                isToday = true
                            }
                        } catch (e: Exception) {
                            isToday = true
                        }
                    } else {
                        isToday = true
                    }
                    isToday
                }
            }.sortedWith(compareBy<ScheduleDto> { schedule ->
                val isDone = schedule.status == "DONE"
                val isOverdue = !isDone && schedule.deadline?.let {
                    try {
                        Instant.parse(it) < nowInstant
                    } catch (e: Exception) { false }
                } ?: false
                !isOverdue
            }.thenComparator { a, b ->
                val aIsDone = a.status == "DONE"
                val aIsOverdue = !aIsDone && a.deadline?.let {
                    try {
                        Instant.parse(it) < nowInstant
                    } catch (e: Exception) { false }
                } ?: false

                val bIsDone = b.status == "DONE"
                val bIsOverdue = !bIsDone && b.deadline?.let {
                    try {
                        Instant.parse(it) < nowInstant
                    } catch (e: Exception) { false }
                } ?: false

                if (aIsOverdue && bIsOverdue) {
                    val aDeadline = a.deadline ?: ""
                    val bDeadline = b.deadline ?: ""
                    aDeadline.compareTo(bDeadline)
                } else if (!aIsOverdue && !bIsOverdue) {
                    val aTime = a.start_time ?: a.deadline ?: ""
                    val bTime = b.start_time ?: b.deadline ?: ""
                    aTime.compareTo(bTime)
                } else {
                    0
                }
            })

            TimelineSection(filteredSchedules, onItemClick, onStatusChange, onEditClick, onDeleteClick)
        }
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
fun SwipeToRevealContainer(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    val maxSwipe = -100f // 100px (around 50dp) is perfect to fit 1 icon
    val swipeLimit = -150f
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        // Background revealed actions on the right side
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .padding(end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    offsetX = 0f
                    onDelete()
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF4D4D).copy(alpha = 0.2f))
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF4D4D), modifier = Modifier.size(20.dp))
            }
        }
        
        // Front content card that swipes
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.toInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            offsetX = if (offsetX < maxSwipe / 2) maxSwipe else 0f
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            offsetX = (offsetX + dragAmount).coerceIn(swipeLimit, 0f)
                        }
                    )
                }
        ) {
            content()
        }
    }
}

@Composable
fun TimelineSection(
    schedules: List<ScheduleDto>,
    onItemClick: (ScheduleDto) -> Unit,
    onStatusChange: () -> Unit,
    onEditClick: (ScheduleDto) -> Unit,
    onDeleteClick: (ScheduleDto) -> Unit
) {
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
                
                // Content with Swipe to Reveal Delete Icon
                Column(modifier = Modifier.weight(1f).padding(bottom = 24.dp)) {
                    SwipeToRevealContainer(
                        onDelete = { onDeleteClick(schedule) }
                    ) {
                        TimelineCard(schedule, onClick = { onItemClick(schedule) }, onStatusChange = onStatusChange)
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineCard(
    schedule: ScheduleDto,
    onClick: () -> Unit,
    onStatusChange: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var showConfirmDoneDialog by remember { mutableStateOf(false) }

    if (showConfirmDoneDialog) {
        androidx.compose.material.AlertDialog(
            onDismissRequest = { showConfirmDoneDialog = false },
            title = { Text("Confirm Action", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Do you want to confirm marking this task as done?", color = Color(0xFFBBCAC5)) },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDoneDialog = false
                        scope.launch {
                            try {
                                AppRepository().updateSchedule(
                                    schedule.id,
                                    com.bfy.schedule_app.data.remote.model.UpdateScheduleRequest(status = "DONE")
                                )
                                onStatusChange()
                            } catch (e: Exception) {}
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF59DBC7))
                ) {
                    Text("Confirm", color = Color(0xFF003731))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDoneDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            backgroundColor = Color(0xFF1E2023)
        )
    }

    if (schedule.type == "TODO") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E2023))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    schedule.title,
                    color = if (schedule.status == "DONE") TextSecondary else TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (schedule.status == "DONE") TextDecoration.LineThrough else null
                )
            }
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (schedule.status == "DONE") Color(0xFF59DBC7) else Color.Transparent)
                    .border(2.dp, if (schedule.status == "DONE") Color(0xFF59DBC7) else Color(0xFF869490), CircleShape)
                    .clickable { 
                        if (schedule.status != "DONE") {
                            showConfirmDoneDialog = true 
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (schedule.status == "DONE") {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF003731), modifier = Modifier.size(16.dp))
                }
            }
        }
    } else {
        val isDone = schedule.status == "DONE"
        
        // Overdue logic
        val isOverdue = !isDone && schedule.deadline?.let {
            try {
                val deadline = Instant.parse(it).toLocalDateTime(TimeZone.currentSystemDefault())
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                deadline < now
            } catch (e: Exception) { false }
        } ?: false

        val accentColor = when {
            isDone -> Color(0xFF59DBC7)
            isOverdue -> Color(0xFFFF7B7B)
            schedule.type == "EVENT" -> Color(0xFF0F4490)
            else -> Color(0xFFAD7BFF)
        }
        val tagBg = when {
            isDone -> Color(0x3359DBC7)
            isOverdue -> Color(0x33FF7B7B)
            schedule.type == "EVENT" -> Color(0x330F4490)
            else -> Color(0x33AD7BFF)
        }
        val tagColor = when {
            isDone -> Color(0xFF59DBC7)
            isOverdue -> Color(0xFFFF7B7B)
            schedule.type == "EVENT" -> Color(0xFF92B4FF)
            else -> Color(0xFFD5BAFF)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E2023))
                .border(1.dp, accentColor.copy(alpha = if (isDone) 0.3f else 0.5f), RoundedCornerShape(12.dp))
                .alpha(if (isDone) 0.6f else 1f)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(tagBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (schedule.type == "EVENT") "[E] EVENT" else "[T] TASK",
                            color = tagColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    if (isDone) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF59DBC7).copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "DONE",
                                color = Color(0xFF59DBC7),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    } else if (isOverdue) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFF7B7B).copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = Localization.get("overdue") ?: "OVERDUE",
                                color = Color(0xFFFF7B7B),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (schedule.type == "EVENT") {
                            try {
                                val start = Instant.parse(schedule.start_time!!).toLocalDateTime(TimeZone.currentSystemDefault())
                                val end = Instant.parse(schedule.end_time!!).toLocalDateTime(TimeZone.currentSystemDefault())
                                "${start.hour.toString().padStart(2, '0')}:${start.minute.toString().padStart(2, '0')} - ${end.hour.toString().padStart(2, '0')}:${end.minute.toString().padStart(2, '0')}"
                            } catch (e: Exception) { schedule.start_time ?: "" }
                        } else {
                            try {
                                val deadlineStr = schedule.deadline ?: schedule.start_time
                                val dt = Instant.parse(deadlineStr!!).toLocalDateTime(TimeZone.currentSystemDefault())
                                "Due ${dt.hour.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')}"
                            } catch (e: Exception) { schedule.deadline ?: "" }
                        },
                        color = if (isDone) Color(0xFF869490) else TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Small circular checkbox on the top right
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isDone) Color(0xFF59DBC7) else Color.Transparent)
                        .border(2.dp, if (isDone) Color(0xFF59DBC7) else Color(0xFF869490), CircleShape)
                        .clickable { 
                            if (!isDone) {
                                showConfirmDoneDialog = true 
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF003731), modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = schedule.title,
                color = if (isDone) Color(0xFF869490) else TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = if (isDone) TextDecoration.LineThrough else null
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                schedule.location ?: schedule.description ?: "",
                color = if (isDone) Color(0xFF869490).copy(alpha = 0.7f) else TextSecondary,
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

@Composable
private fun ActionDialogButton(
    label: String,
    icon: ImageVector,
    iconTint: Color,
    textColor: Color = Color.White,
    onClick: () -> Unit
) {
    androidx.compose.material3.Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
