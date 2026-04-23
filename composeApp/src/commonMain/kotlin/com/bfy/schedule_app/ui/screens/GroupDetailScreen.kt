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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.bfy.schedule_app.ui.components.BfyButton
import com.bfy.schedule_app.ui.components.BfyTopBar
import com.bfy.schedule_app.ui.data.FakeBfyData
import com.bfy.schedule_app.ui.theme.BfyTheme
import org.jetbrains.compose.resources.stringResource
import schedule_app.composeapp.generated.resources.Res
import schedule_app.composeapp.generated.resources.assign_task
import schedule_app.composeapp.generated.resources.group_detail
import schedule_app.composeapp.generated.resources.members
import schedule_app.composeapp.generated.resources.shared_tasks

data class GroupDetailScreen(
    private val groupId: String
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val state = FakeBfyData.groupDetailState(groupId)

        Column(modifier = Modifier.fillMaxSize()) {
            BfyTopBar(
                title = stringResource(Res.string.group_detail),
                onBack = { navigator.pop() }
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = BfyTheme.dimens.spacing16),
                verticalArrangement = Arrangement.spacedBy(BfyTheme.dimens.spacing12)
            ) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(BfyTheme.dimens.spacing16)) {
                            Text(text = state.groupName, style = MaterialTheme.typography.headlineMedium)
                            Text(
                                text = "${state.members.size} ${stringResource(Res.string.members)}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = stringResource(Res.string.members),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                items(state.members, key = { it.id }) { member ->
                    Card(
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
                            Column {
                                Text(member.name, style = MaterialTheme.typography.titleMedium)
                                Text(member.role, style = MaterialTheme.typography.bodyMedium)
                            }
                            Text(
                                text = if (member.isOnline) "Online" else "Offline",
                                color = if (member.isOnline) BfyTheme.extendedColors.success else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = stringResource(Res.string.shared_tasks),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                items(state.sharedItems, key = { it.id }) { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(BfyTheme.dimens.spacing12)) {
                            Text(item.title, style = MaterialTheme.typography.titleMedium)
                            Text(item.description, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                if (state.isLeader) {
                    item {
                        BfyButton(
                            text = stringResource(Res.string.assign_task),
                            onClick = {}
                        )
                    }
                }
            }
        }
    }
}
