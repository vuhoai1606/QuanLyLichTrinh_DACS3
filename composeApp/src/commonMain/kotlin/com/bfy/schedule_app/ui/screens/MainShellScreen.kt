package com.bfy.schedule_app.ui.screens

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.bfy.schedule_app.ui.components.BfyBottomNav
import com.bfy.schedule_app.ui.components.BfyTopBar
import com.bfy.schedule_app.ui.components.BottomNavItem
import com.bfy.schedule_app.ui.data.FakeBfyData
import com.bfy.schedule_app.ui.model.MainTab
import com.bfy.schedule_app.ui.model.TimelineStatus
import com.bfy.schedule_app.ui.state.FocusUiState
import com.bfy.schedule_app.ui.theme.BfyTheme
import org.jetbrains.compose.resources.stringResource
import schedule_app.composeapp.generated.resources.Res
import schedule_app.composeapp.generated.resources.cancel
import schedule_app.composeapp.generated.resources.collaboration
import schedule_app.composeapp.generated.resources.delete
import schedule_app.composeapp.generated.resources.delete_confirm_message
import schedule_app.composeapp.generated.resources.delete_confirm_title
import schedule_app.composeapp.generated.resources.done
import schedule_app.composeapp.generated.resources.focus
import schedule_app.composeapp.generated.resources.home
import schedule_app.composeapp.generated.resources.profile
import schedule_app.composeapp.generated.resources.select_mode

class MainShellScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }
        var showCreationSheet by remember { mutableStateOf(false) }

        var homeState by remember { mutableStateOf(FakeBfyData.homeState) }
        var focusState by remember { mutableStateOf(FocusUiState()) }
        var profileState by remember { mutableStateOf(FakeBfyData.profileState) }

        var deleteTargetId by remember { mutableStateOf<String?>(null) }

        if (showCreationSheet) {
            ModalBottomSheet(onDismissRequest = { showCreationSheet = false }) {
                CreationBottomSheet(
                    onDismiss = { showCreationSheet = false },
                    onSave = {}
                )
            }
        }

        if (deleteTargetId != null) {
            AlertDialog(
                onDismissRequest = { deleteTargetId = null },
                title = { Text(stringResource(Res.string.delete_confirm_title)) },
                text = { Text(stringResource(Res.string.delete_confirm_message)) },
                confirmButton = {
                    TextButton(onClick = {
                        val current = homeState.content
                        if (current is com.bfy.schedule_app.ui.state.HomeContentState.Data) {
                            val filtered = current.items.filterNot { it.id == deleteTargetId }
                            homeState = homeState.copy(
                                content = if (filtered.isEmpty()) com.bfy.schedule_app.ui.state.HomeContentState.Empty
                                else com.bfy.schedule_app.ui.state.HomeContentState.Data(filtered),
                                selectedIds = homeState.selectedIds - setOfNotNull(deleteTargetId)
                            )
                        }
                        deleteTargetId = null
                    }) {
                        Text(stringResource(Res.string.delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deleteTargetId = null }) {
                        Text(stringResource(Res.string.cancel))
                    }
                }
            )
        }

        val bottomItems = listOf(
            BottomNavItem(MainTab.HOME, stringResource(Res.string.home)),
            BottomNavItem(MainTab.FOCUS, stringResource(Res.string.focus)),
            BottomNavItem(MainTab.COLLAB, stringResource(Res.string.collaboration)),
            BottomNavItem(MainTab.PROFILE, stringResource(Res.string.profile))
        )

        Scaffold(
            topBar = {
                BfyTopBar(
                    title = when {
                        selectedTab == MainTab.HOME && homeState.isSelectionMode -> {
                            "${stringResource(Res.string.select_mode)} (${homeState.selectedIds.size})"
                        }

                        selectedTab == MainTab.HOME -> stringResource(Res.string.home)
                        selectedTab == MainTab.FOCUS -> stringResource(Res.string.focus)
                        selectedTab == MainTab.COLLAB -> stringResource(Res.string.collaboration)
                        selectedTab == MainTab.PROFILE -> stringResource(Res.string.profile)
                        else -> stringResource(Res.string.home)
                    },
                    actions = {
                        if (selectedTab == MainTab.HOME && homeState.isSelectionMode) {
                            TextButton(onClick = {
                                val current = homeState.content
                                if (current is com.bfy.schedule_app.ui.state.HomeContentState.Data) {
                                    val updated = current.items.map { item ->
                                        if (homeState.selectedIds.contains(item.id)) {
                                            item.copy(status = TimelineStatus.DONE)
                                        } else {
                                            item
                                        }
                                    }
                                    homeState = homeState.copy(
                                        content = com.bfy.schedule_app.ui.state.HomeContentState.Data(updated),
                                        isSelectionMode = false,
                                        selectedIds = emptySet()
                                    )
                                }
                            }) {
                                Text(stringResource(Res.string.done), color = MaterialTheme.colorScheme.primary)
                            }
                            TextButton(onClick = {
                                deleteTargetId = homeState.selectedIds.firstOrNull()
                            }) {
                                Text(stringResource(Res.string.delete), color = BfyTheme.extendedColors.danger)
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                if (selectedTab == MainTab.HOME) {
                    FloatingActionButton(onClick = { showCreationSheet = true }) {
                        Text("+")
                    }
                }
            },
            bottomBar = {
                BfyBottomNav(
                    items = bottomItems,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (selectedTab) {
                    MainTab.HOME -> HomeDashboardScreen(
                        state = homeState,
                        onRetry = {
                            homeState = homeState.copy(content = com.bfy.schedule_app.ui.state.HomeContentState.Data(FakeBfyData.timelineItems))
                        },
                        onCreateItem = { showCreationSheet = true },
                        onItemClick = { itemId ->
                            if (homeState.isSelectionMode) {
                                val isSelected = homeState.selectedIds.contains(itemId)
                                homeState = homeState.copy(
                                    selectedIds = if (isSelected) homeState.selectedIds - itemId else homeState.selectedIds + itemId
                                )
                            } else {
                                val target = FakeBfyData.timelineItems.firstOrNull { it.id == itemId }
                                if (target?.isGroupItem == true) {
                                    navigator.push(AssignmentDetailScreen(FakeBfyData.assignments.first().id))
                                }
                            }
                        },
                        onItemLongPress = { itemId ->
                            homeState = homeState.copy(
                                isSelectionMode = true,
                                selectedIds = homeState.selectedIds + itemId
                            )
                        },
                        onToggleDone = { itemId, checked ->
                            val current = homeState.content
                            if (current is com.bfy.schedule_app.ui.state.HomeContentState.Data) {
                                homeState = homeState.copy(
                                    content = com.bfy.schedule_app.ui.state.HomeContentState.Data(
                                        current.items.map { item ->
                                            if (item.id == itemId) {
                                                item.copy(status = if (checked) TimelineStatus.DONE else TimelineStatus.PENDING)
                                            } else {
                                                item
                                            }
                                        }
                                    )
                                )
                            }
                        },
                        onDeleteItem = { deleteTargetId = it }
                    )

                    MainTab.FOCUS -> FocusScreen(
                        state = focusState,
                        onStart = { focusState = focusState.copy(isRunning = true, minuteText = "24:59") },
                        onGiveUp = { focusState = focusState.copy(isRunning = false, minuteText = "25:00") }
                    )

                    MainTab.COLLAB -> CollaborationScreen(
                        state = FakeBfyData.collaborationState,
                        onOpenGroup = { groupId -> navigator.push(GroupDetailScreen(groupId)) },
                        onOpenAssignment = { assignmentId -> navigator.push(AssignmentDetailScreen(assignmentId)) }
                    )

                    MainTab.PROFILE -> ProfileScreen(
                        state = profileState,
                        onProfileUpdate = { fullName, email, bio, timezone ->
                            profileState = profileState.copy(
                                fullName = fullName,
                                email = email,
                                bio = bio,
                                timezone = timezone
                            )
                        },
                        onToggleTheme = { value -> profileState = profileState.copy(darkThemeEnabled = value) },
                        onToggleLanguage = { value -> profileState = profileState.copy(englishLanguageEnabled = value) },
                        onLogout = { navigator.replaceAll(AuthScreen()) }
                    )
                }
            }
        }
    }
}
