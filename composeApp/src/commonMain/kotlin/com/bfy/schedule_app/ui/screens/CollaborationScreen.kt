package com.bfy.schedule_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.bfy.schedule_app.ui.components.BfyButton
import com.bfy.schedule_app.ui.components.BfyButtonStyle
import com.bfy.schedule_app.ui.model.GroupSummary
import com.bfy.schedule_app.ui.state.CollaborationUiState
import com.bfy.schedule_app.ui.theme.BfyTheme
import org.jetbrains.compose.resources.stringResource
import schedule_app.composeapp.generated.resources.Res
import schedule_app.composeapp.generated.resources.accept
import schedule_app.composeapp.generated.resources.assigned_to_me
import schedule_app.composeapp.generated.resources.create_group
import schedule_app.composeapp.generated.resources.decline
import schedule_app.composeapp.generated.resources.my_groups
import schedule_app.composeapp.generated.resources.no_assignments
import schedule_app.composeapp.generated.resources.no_groups

private enum class CollaborationTab {
    MY_GROUPS,
    ASSIGNED
}

@Composable
fun CollaborationScreen(
    state: CollaborationUiState,
    onOpenGroup: (String) -> Unit,
    onOpenAssignment: (String) -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(CollaborationTab.MY_GROUPS) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = BfyTheme.dimens.spacing16),
        verticalArrangement = Arrangement.spacedBy(BfyTheme.dimens.spacing12)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(BfyTheme.dimens.spacing8)) {
            BfyButton(
                text = stringResource(Res.string.my_groups),
                onClick = { selectedTab = CollaborationTab.MY_GROUPS },
                style = if (selectedTab == CollaborationTab.MY_GROUPS) BfyButtonStyle.PRIMARY else BfyButtonStyle.SECONDARY,
                modifier = Modifier.weight(1f)
            )
            BfyButton(
                text = stringResource(Res.string.assigned_to_me),
                onClick = { selectedTab = CollaborationTab.ASSIGNED },
                style = if (selectedTab == CollaborationTab.ASSIGNED) BfyButtonStyle.PRIMARY else BfyButtonStyle.SECONDARY,
                modifier = Modifier.weight(1f)
            )
        }

        when (selectedTab) {
            CollaborationTab.MY_GROUPS -> {
                if (state.myGroups.isEmpty()) {
                    Text(stringResource(Res.string.no_groups))
                    BfyButton(text = stringResource(Res.string.create_group), onClick = {})
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(BfyTheme.dimens.spacing8)) {
                        items(state.myGroups, key = { it.id }) { group ->
                            GroupCard(group = group, onOpenGroup = onOpenGroup)
                        }
                    }
                }
            }

            CollaborationTab.ASSIGNED -> {
                if (state.assignedItems.isEmpty()) {
                    Text(stringResource(Res.string.no_assignments))
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(BfyTheme.dimens.spacing8)) {
                        items(state.assignedItems, key = { it.id }) { assignment ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = BfyTheme.dimens.cardElevation)
                            ) {
                                Column(
                                    modifier = Modifier.padding(BfyTheme.dimens.spacing12),
                                    verticalArrangement = Arrangement.spacedBy(BfyTheme.dimens.spacing8)
                                ) {
                                    Text(assignment.title, style = MaterialTheme.typography.titleMedium)
                                    Text(assignment.description, style = MaterialTheme.typography.bodyMedium)
                                    Text(assignment.dueText, style = MaterialTheme.typography.labelLarge)
                                    Row(horizontalArrangement = Arrangement.spacedBy(BfyTheme.dimens.spacing8)) {
                                        BfyButton(
                                            text = stringResource(Res.string.accept),
                                            onClick = { onOpenAssignment(assignment.id) },
                                            modifier = Modifier.weight(1f)
                                        )
                                        BfyButton(
                                            text = stringResource(Res.string.decline),
                                            onClick = { onOpenAssignment(assignment.id) },
                                            style = BfyButtonStyle.SECONDARY,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupCard(
    group: GroupSummary,
    onOpenGroup: (String) -> Unit
) {
    Card(
        onClick = { onOpenGroup(group.id) },
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = BfyTheme.dimens.cardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BfyTheme.dimens.spacing12),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(BfyTheme.dimens.spacing4)) {
                Text(group.name, style = MaterialTheme.typography.titleMedium)
                Text("${group.memberCount} members", style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                text = if (group.isLeader) "Leader" else "Member",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
