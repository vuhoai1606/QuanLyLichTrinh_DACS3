package com.bfy.schedule_app.ui.screens.collaboration

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.bfy.schedule_app.ui.screens.homedashboard.DashboardTab
import com.bfy.schedule_app.ui.viewmodel.GroupDetailViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import com.bfy.schedule_app.utils.Localization


@Composable
fun GroupDetailScreen(
    groupId: String,
    onBackClick: () -> Unit,
    onTabSelected: (DashboardTab) -> Unit
) {
    val viewModel: GroupDetailViewModel = viewModel { GroupDetailViewModel() }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(groupId) {
        viewModel.loadTasks(groupId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111316))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 80.dp, start = 24.dp, end = 24.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                GroupHeaderCard(uiState.group)
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item {
                SharedTasksHeader()
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
                GroupTaskCard(
                    status = when(task.status) {
                        "OVERDUE" -> GroupTaskStatus.OVERDUE
                        "IN_PROGRESS" -> GroupTaskStatus.IN_PROGRESS
                        "DONE" -> GroupTaskStatus.TODO
                        else -> GroupTaskStatus.TODO
                    },
                    dateText = task.deadline ?: "No deadline",
                    title = task.title,
                    description = task.description ?: "",
                    assignees = task.assignees
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

        }

        TopBar(
            groupName = uiState.group?.name ?: "Group Details",
            onBackClick = onBackClick,
            onSettingsClick = { }
        )

        BottomNavBar(
            selectedTab = DashboardTab.COLLAB,
            onTabSelected = onTabSelected
        )
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
            .height(64.dp)
            .padding(top = 0.dp)
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
private fun GroupHeaderCard(group: com.bfy.schedule_app.data.remote.model.GroupDto?) {
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
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group?.name ?: "Loading...",
                    color = Color(0xFFE2E2E6),
                    fontSize = 34.sp,
                    lineHeight = 40.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Group, contentDescription = null, tint = Color(0xFFBBCAC5), modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = group?.description ?: "No description available",
                        color = Color(0xFFBBCAC5),
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(Color(0x332D1B4A))
                    .border(1.dp, Color(0x4DAD7BFF), RoundedCornerShape(9999.dp))
                    .padding(horizontal = 13.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "Rank:\nGold\nScholar",
                    color = Color(0xFFD5BAFF),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "ACTIVE MEMBERS",
            color = Color(0xFFBBCAC5),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.6.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ActiveMemberAvatar(color = Color(0xFF59DBC7), initials = "AR", active = true)
            ActiveMemberAvatar(color = Color(0xFF59DBC7), initials = "SC", active = true)
            ActiveMemberAvatar(color = Color(0xFF333538), initials = "DK", active = false)
            ActiveMemberAvatar(color = Color(0xFF333538), initials = "MP", active = false)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF282A2D))
                    .border(1.dp, Color(0xFF3C4946), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("+8", color = Color(0xFFBBCAC5), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ActiveMemberAvatar(color: Color, initials: String, active: Boolean) {
    Box(
        modifier = Modifier
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
private fun SharedTasksHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = Localization.get("group_tasks"),
            color = Color(0xFFE2E2E6),
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold
        )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(Color(0xFF59DBC7))
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(9999.dp)
                    )
                    .clickable { }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF003731), modifier = Modifier.size(10.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Assign Task", color = Color(0xFF003731), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
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
        badgeText = "Overdue",
        borderColor = Color(0x4DFFB4AB),
        badgeBackground = Color(0x3393000A),
        badgeBorder = Color(0x4DFFB4AB),
        badgeTextColor = Color(0xFFFFB4AB),
        accentStripe = Color(0xFFFFB4AB)
    ),
    IN_PROGRESS(
        badgeText = "In Progress",
        borderColor = Color(0xFF0F4490),
        badgeBackground = Color(0x330F4490),
        badgeBorder = Color(0x4DAEC6FF),
        badgeTextColor = Color(0xFFAEC6FF),
        accentStripe = Color(0xFFAEC6FF)
    ),
    TODO(
        badgeText = "To-do",
        borderColor = Color(0xFF333538),
        badgeBackground = Color(0xFF333538),
        badgeBorder = Color(0xFF3C4946),
        badgeTextColor = Color(0xFFBBCAC5),
        accentStripe = Color(0xFF3C4946)
    )
}

@Composable
private fun GroupTaskCard(
    status: GroupTaskStatus,
    dateText: String,
    title: String,
    description: String,
    assignees: List<String>
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
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(title, color = Color(0xFFE2E2E6), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, lineHeight = 28.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, color = Color(0xFFBBCAC5), fontSize = 16.sp, lineHeight = 24.sp)

            if (status == GroupTaskStatus.IN_PROGRESS) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(Color(0xFF333538))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
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
                Text("60% Complete", color = Color(0xFFBBCAC5), fontSize = 12.sp, letterSpacing = 0.48.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (status == GroupTaskStatus.TODO) {
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
                    Text("Unassigned", color = Color(0xFF3C4946), fontSize = 12.sp, fontStyle = FontStyle.Italic, letterSpacing = 0.48.sp, fontWeight = FontWeight.Medium)
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
private fun CompletedTaskCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0C0E11))
            .border(1.dp, Color(0x4D3C4946), RoundedCornerShape(12.dp))
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
                    .background(Color(0x1A59DBC7))
                    .border(1.dp, Color(0x3359DBC7), RoundedCornerShape(4.dp))
                    .padding(horizontal = 9.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF79F7E3), modifier = Modifier.size(12.dp))
                    Text("Done", color = Color(0xFF79F7E3), fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.48.sp)
                }
            }
            Text("Completed Oct 12", color = Color(0xFF3C4946), fontSize = 12.sp, letterSpacing = 0.48.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Setup Discord Server",
            color = Color(0xFF3C4946),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 28.sp,
            textDecoration = TextDecoration.LineThrough
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Create channels for different topics and\ninvite all group members.",
            color = Color(0xFF3C4946),
            fontSize = 16.sp,
            lineHeight = 24.sp
        )
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
