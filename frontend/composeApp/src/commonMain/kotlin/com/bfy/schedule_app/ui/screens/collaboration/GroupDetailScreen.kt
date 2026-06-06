package com.bfy.schedule_app.ui.screens.collaboration

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.datetime.*
import com.bfy.schedule_app.ui.screens.homedashboard.DashboardTab
import com.bfy.schedule_app.ui.viewmodel.GroupDetailViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bfy.schedule_app.utils.Localization

@Composable
fun GroupDetailScreen(
    groupId: String,
    onBackClick: () -> Unit,
    onTabSelected: (DashboardTab) -> Unit
) {
    val viewModel: GroupDetailViewModel = viewModel { GroupDetailViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var addTaskType by remember { mutableStateOf("ANNOUNCEMENT") }

    LaunchedEffect(groupId) {
        viewModel.loadTasks(groupId)
    }

    val myRole = uiState.members.find { it.id == uiState.currentUser?.id }?.role ?: "MEMBER"
    val canAssign = myRole == "LEADER" || myRole == "DEPUTY"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111316))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 96.dp, start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                GroupHeaderCard(uiState.group, uiState.members)
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item {
                SharedTasksHeader(canAssign = canAssign, onAddClick = { type -> addTaskType = type; showAddTaskDialog = true })
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            if (uiState.isLoading) {
                item { CircularProgressIndicator(color = Color(0xFF59DBC7), modifier = Modifier.padding(16.dp)) }
            } else if (uiState.tasks.isEmpty()) {
                item {
                    Text(Localization.get("no_groups"), color = Color(0xFFBBCAC5), modifier = Modifier.padding(16.dp))
                }
            }

            items(uiState.tasks.size) { index ->
                val task = uiState.tasks[index]
                val isLeader = uiState.group?.leader_id == uiState.currentUser?.id
                GroupTaskCard(
                    status = when(task.type) {
                        "ANNOUNCEMENT" -> GroupTaskStatus.ANNOUNCEMENT
                        "EVENT" -> {
                            val now = Clock.System.now()
                            val endTimeInstant = task.end_time?.let {
                                try { Instant.parse(if (!it.contains("T")) "${it}T00:00:00Z" else if (!it.endsWith("Z") && !it.contains("+")) "${it}Z" else it) } catch (e: Exception) { null }
                            }
                            val isOverdue = endTimeInstant != null && endTimeInstant < now
                            when {
                                task.status == "DONE" -> GroupTaskStatus.DONE
                                isOverdue -> GroupTaskStatus.OVERDUE
                                else -> GroupTaskStatus.EVENT
                            }
                        }
                        else -> {
                            val now = Clock.System.now()
                            val deadlineInstant = task.deadline?.let {
                                try { Instant.parse(if (!it.contains("T")) "${it}T00:00:00Z" else if (!it.endsWith("Z") && !it.contains("+")) "${it}Z" else it) } catch (e: Exception) { null }
                            }
                            val startTimeInstant = task.start_time?.let {
                                try { Instant.parse(if (!it.contains("T")) "${it}T00:00:00Z" else if (!it.endsWith("Z") && !it.contains("+")) "${it}Z" else it) } catch (e: Exception) { null }
                            }
                            
                            val isOverdue = deadlineInstant != null && deadlineInstant < now
                            val isStarted = startTimeInstant != null && startTimeInstant <= now
                            
                            when {
                                task.status == "DONE" -> GroupTaskStatus.DONE
                                task.status == "OVERDUE" || isOverdue -> GroupTaskStatus.OVERDUE
                                task.status == "IN_PROGRESS" || isStarted -> GroupTaskStatus.IN_PROGRESS
                                else -> GroupTaskStatus.TODO
                            }
                        }
                    },
                    dateText = when(task.type) {
                        "ANNOUNCEMENT" -> formatRelativeTime(task.created_at)
                        "EVENT" -> formatRelativeTime(task.start_time ?: task.created_at)
                        else -> formatRelativeTime(task.deadline ?: task.start_time ?: task.created_at)
                    },
                    title = task.title,
                    description = task.description ?: "",
                    assignees = task.assignees,
                    canDelete = isLeader || task.creator_id == uiState.currentUser?.id,
                    onDeleteClick = { viewModel.deleteSchedule(groupId, task.id) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        TopBar(
            groupName = uiState.group?.name ?: Localization.get("group_details"),
            onBackClick = onBackClick,
            onSettingsClick = { showSettingsDialog = true }
        )



        if (showAddTaskDialog) {
            AssignTaskDialog(
                groupId = groupId,
                viewModel = viewModel,
                initialType = addTaskType,
                onDismissRequest = { showAddTaskDialog = false }
            )
        }

        if (showSettingsDialog) {
            GroupSettingsDialog(
                groupId = groupId,
                viewModel = viewModel,
                onDismissRequest = { showSettingsDialog = false }
            )
        }
    }
}

@Composable
private fun TopBar(
    groupName: String,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Surface(
        color = Color(0xFF020617),
        elevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .padding(top = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF59DBC7), modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color(0x4D14B8A6), CircleShape)
                        .background(Color(0xFF2A2D31)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFFE2E2E6), modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = groupName,
                    color = Color(0xFF2DD4BF),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1).sp,
                    maxLines = 1
                )
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable { onSettingsClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color(0xFF59DBC7), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun GroupHeaderCard(
    group: com.bfy.schedule_app.data.remote.model.GroupDto?,
    members: List<com.bfy.schedule_app.data.remote.model.GroupMemberDto>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A1C1F))
            .border(1.dp, Color(0xFF333538), RoundedCornerShape(12.dp))
            .padding(25.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group?.name ?: Localization.get("loading"),
                    color = Color(0xFFE2E2E6),
                    fontSize = 30.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Group, contentDescription = null, tint = Color(0xFFBBCAC5), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${members.size} Members • Top 10% Overall",
                        color = Color(0xFFBBCAC5),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = Localization.get("active_members"),
            color = Color(0xFFBBCAC5),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.6.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy((-12).dp), verticalAlignment = Alignment.CenterVertically) {
            val displayMembers = members.take(4)
            displayMembers.forEachIndexed { index, member ->
                ActiveMemberAvatar(
                    color = Color(0xFF59DBC7), 
                    initials = member.full_name.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(2), 
                    active = true,
                    zIndex = (4 - index).toFloat()
                )
            }
            if (members.size > 4) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF282A2D))
                        .border(1.dp, Color(0xFF3C4946), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+${members.size - 4}", color = Color(0xFFBBCAC5), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ActiveMemberAvatar(color: Color, initials: String, active: Boolean, zIndex: Float = 1f) {
    Box(
        modifier = Modifier
            .zIndex(zIndex)
            .size(48.dp)
            .shadow(
                elevation = if (active) 8.dp else 0.dp,
                shape = CircleShape
            )
            .clip(CircleShape)
            .background(color)
            .border(if (active) 2.dp else 1.dp, if (active) Color(0xFF59DBC7) else Color(0xFF333538), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(initials, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        if (active) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 1.dp, y = 1.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF59DBC7))
                    .border(2.dp, Color(0xFF1A1C1F), CircleShape)
            )
        }
    }
}

@Composable
private fun SharedTasksHeader(canAssign: Boolean, onAddClick: (String) -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Shared Tasks",
            color = Color(0xFFE2E2E6),
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold
        )
        if (canAssign) {
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF59DBC7))
                        .clickable { menuExpanded = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color(0xFF003731), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Assign Task", color = Color(0xFF003731), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(Color(0xFF282A2D))
                ) {
                    DropdownMenuItem(onClick = { menuExpanded = false; onAddClick("ANNOUNCEMENT") }) {
                        Text("Notification", color = Color(0xFFE2E2E6))
                    }
                    DropdownMenuItem(onClick = { menuExpanded = false; onAddClick("TASK") }) {
                        Text("Công việc", color = Color(0xFFE2E2E6))
                    }
                    DropdownMenuItem(onClick = { menuExpanded = false; onAddClick("EVENT") }) {
                        Text("Sự kiện", color = Color(0xFFE2E2E6))
                    }
                }
            }
        }
    }
}

private enum class GroupTaskStatus(
    val badgeText: String,
    val borderColor: Color,
    val badgeBackground: Color,
    val badgeBorder: Color,
    val badgeTextColor: Color,
    val accentStripe: Color?
) {
    OVERDUE(
        badgeText = Localization.get("overdue"),
        borderColor = Color(0x4DFFB4AB),
        badgeBackground = Color(0x3393000A),
        badgeBorder = Color(0x4DFFB4AB),
        badgeTextColor = Color(0xFFFFB4AB),
        accentStripe = Color(0xFFFFB4AB)
    ),
    IN_PROGRESS(
        badgeText = Localization.get("in_progress"),
        borderColor = Color(0xFF0F4490),
        badgeBackground = Color(0x330F4490),
        badgeBorder = Color(0x4DAEC6FF),
        badgeTextColor = Color(0xFFAEC6FF),
        accentStripe = Color(0xFFAEC6FF)
    ),
    TODO(
        badgeText = Localization.get("todo"),
        borderColor = Color(0xFF333538),
        badgeBackground = Color(0xFF333538),
        badgeBorder = Color(0xFF3C4946),
        badgeTextColor = Color(0xFFBBCAC5),
        accentStripe = Color(0xFF3C4946)
    ),
    DONE(
        badgeText = Localization.get("done") ?: "Done",
        borderColor = Color(0x4D59DBC7),
        badgeBackground = Color(0x33003731),
        badgeBorder = Color(0x4D59DBC7),
        badgeTextColor = Color(0xFF59DBC7),
        accentStripe = Color(0xFF59DBC7)
    ),
    ANNOUNCEMENT(
        badgeText = "Notification",
        borderColor = Color(0xFF6E4000),
        badgeBackground = Color(0x33FFD166),
        badgeBorder = Color(0xFF6E4000),
        badgeTextColor = Color(0xFFFFD166),
        accentStripe = Color(0xFFFFD166)
    ),
    EVENT(
        badgeText = "Event",
        borderColor = Color(0xFF3B1A66),
        badgeBackground = Color(0x33AD7BFF),
        badgeBorder = Color(0xFF3B1A66),
        badgeTextColor = Color(0xFFAD7BFF),
        accentStripe = Color(0xFFAD7BFF)
    )
}

@Composable
private fun GroupTaskCard(
    completedAssignees: Int = 0,
    totalAssignees: Int = 0,
    status: GroupTaskStatus,
    dateText: String,
    title: String,
    description: String,
    assignees: List<String>,
    canDelete: Boolean = false,
    onDeleteClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, status.borderColor, RoundedCornerShape(12.dp))
            .background(Color(0xFF1E2023))
    ) {
        status.accentStripe?.let { stripeColor ->
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(stripeColor)
                    .align(Alignment.CenterStart)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(21.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(status.badgeBackground)
                        .border(1.dp, status.badgeBorder, RoundedCornerShape(4.dp))
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                ) {
                    Text(
                        status.badgeText,
                        color = status.badgeTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.48.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFFBBCAC5), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(dateText, color = Color(0xFFBBCAC5), fontSize = 12.sp, letterSpacing = 0.48.sp)
                    
                    if (canDelete) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFFFB4AB),
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onDeleteClick?.invoke() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                title, 
                color = if (status == GroupTaskStatus.DONE) Color(0xFFBBCAC5) else Color(0xFFE2E2E6), 
                fontSize = 18.sp, 
                fontWeight = FontWeight.SemiBold, 
                lineHeight = 28.sp,
                textDecoration = if (status == GroupTaskStatus.DONE) TextDecoration.LineThrough else null
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, color = Color(0xFFBBCAC5), fontSize = 16.sp, lineHeight = 24.sp)

            if (status == GroupTaskStatus.IN_PROGRESS || status == GroupTaskStatus.TODO) {
                if (totalAssignees > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val progressValue = if (totalAssignees > 0) completedAssignees.toFloat() / totalAssignees.toFloat() else 0f
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF2D1B4A))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progressValue)
                                    .background(status.borderColor)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${completedAssignees}/${totalAssignees} Done", color = Color(0xFFBBCAC5), fontSize = 12.sp)
                    }
                } else if (status == GroupTaskStatus.IN_PROGRESS) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val progressValue = 0.5f
                    Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(Color(0xFF333538))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressValue)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(9999.dp))
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF59DBC7), Color(0xFFD5BAFF))
                                )
                            )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("${(progressValue * 100).toInt()}% ${Localization.get("complete")}", color = Color(0xFFBBCAC5), fontSize = 12.sp, letterSpacing = 0.48.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (status == GroupTaskStatus.TODO || status == GroupTaskStatus.ANNOUNCEMENT || status == GroupTaskStatus.EVENT) {
                if (assignees.isEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color(0xFF3C4946), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF3C4946), modifier = Modifier.size(12.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(Localization.get("unassigned"), color = Color(0xFF3C4946), fontSize = 12.sp, fontStyle = FontStyle.Italic, letterSpacing = 0.48.sp, fontWeight = FontWeight.Medium)
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy((-8).dp), verticalAlignment = Alignment.CenterVertically) {
                        assignees.take(2).forEachIndexed { index, _ ->
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (index == 0) Color(0xFF2D1B4A) else Color(0xFF2A2D31))
                                    .border(1.dp, Color(0xFF1E2023), CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(assignees.joinToString(", "), color = Color(0xFFBBCAC5), fontSize = 12.sp, letterSpacing = 0.48.sp, fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy((-8).dp), verticalAlignment = Alignment.CenterVertically) {
                    assignees.take(2).forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (index == 0) Color(0xFF2D1B4A) else Color(0xFF2A2D31))
                                .border(1.dp, Color(0xFF1E2023), CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(assignees.joinToString(", "), color = Color(0xFFBBCAC5), fontSize = 12.sp, letterSpacing = 0.48.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun NavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (active) Color(0xFF59DBC7) else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = if (active) Color(0xFF003731) else Color(0xFFBBCAC5))
    }
}

@Composable
private fun BoxScope.BottomNavBar(
    selectedTab: DashboardTab,
    onTabSelected: (DashboardTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
            .align(Alignment.BottomCenter)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(9999.dp))
                .background(Color(0xE6282A2D))
                .border(1.dp, Color(0xFF333538), RoundedCornerShape(9999.dp))
                .padding(horizontal = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NavItem(icon = Icons.Default.Home, active = selectedTab == DashboardTab.HOME) { onTabSelected(DashboardTab.HOME) }
            NavItem(icon = Icons.Default.DateRange, active = selectedTab == DashboardTab.CALENDAR) { onTabSelected(DashboardTab.CALENDAR) }
            NavItem(icon = Icons.Default.Star, active = selectedTab == DashboardTab.FOCUS) { onTabSelected(DashboardTab.FOCUS) }
            NavItem(icon = Icons.Default.Share, active = selectedTab == DashboardTab.COLLAB) { onTabSelected(DashboardTab.COLLAB) }
            NavItem(icon = Icons.Default.Person, active = selectedTab == DashboardTab.PROFILE) { onTabSelected(DashboardTab.PROFILE) }
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 28.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xE6282A2D))
                .border(1.dp, Color(0xFF333538), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
        }
    }
}

private fun formatRelativeTime(dateString: String?): String {
    if (dateString.isNullOrBlank()) return Localization.get("no_time") ?: "No time"
    try {
        val parseStr = if (!dateString.contains("T")) "${dateString}T00:00:00Z" 
                       else if (!dateString.endsWith("Z") && !dateString.contains("+")) "${dateString}Z" 
                       else dateString
        val instant = Instant.parse(parseStr)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val targetDate = localDateTime.date
        
        val hour = if (localDateTime.hour == 0) 12 else if (localDateTime.hour > 12) localDateTime.hour - 12 else localDateTime.hour
        val amPm = if (localDateTime.hour >= 12) "PM" else "AM"
        val min = localDateTime.minute.toString().padStart(2, '0')
        val timeStr = "$hour:$min $amPm"

        val daysDiff = targetDate.toEpochDays() - today.toEpochDays()
        return when (daysDiff) {
            0 -> "Today, $timeStr"
            1 -> "Tomorrow"
            -1 -> "Yesterday"
            else -> "${targetDate.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }} ${targetDate.dayOfMonth}, $timeStr"
        }
    } catch (e: Exception) {
        return dateString.split("T").firstOrNull() ?: dateString
    }
}
