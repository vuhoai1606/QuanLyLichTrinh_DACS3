package com.bfy.schedule_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.bfy.schedule_app.ui.components.BfyButton
import com.bfy.schedule_app.ui.components.BfyButtonStyle
import com.bfy.schedule_app.ui.components.TaskCard
import com.bfy.schedule_app.ui.state.HomeContentState
import com.bfy.schedule_app.ui.state.HomeUiState
import com.bfy.schedule_app.ui.theme.BfyTheme
import org.jetbrains.compose.resources.stringResource
import schedule_app.composeapp.generated.resources.Res
import schedule_app.composeapp.generated.resources.create_first_item
import schedule_app.composeapp.generated.resources.hello_user
import schedule_app.composeapp.generated.resources.load_error
import schedule_app.composeapp.generated.resources.no_plans_today
import schedule_app.composeapp.generated.resources.retry

@Composable
fun HomeDashboardScreen(
    state: HomeUiState,
    onRetry: () -> Unit,
    onCreateItem: () -> Unit,
    onItemClick: (String) -> Unit,
    onItemLongPress: (String) -> Unit,
    onToggleDone: (String, Boolean) -> Unit,
    onDeleteItem: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = BfyTheme.dimens.spacing16),
        verticalArrangement = Arrangement.spacedBy(BfyTheme.dimens.spacing12)
    ) {
        HeaderSection(
            userName = state.userName,
            rank = state.currentRank,
            expCurrent = state.expCurrent,
            expTarget = state.expTarget
        )

        when (val content = state.content) {
            HomeContentState.Loading -> {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(BfyTheme.dimens.spacing32 * 3)
                    )
                }
            }

            HomeContentState.Empty -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = BfyTheme.dimens.spacing24),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(BfyTheme.dimens.spacing12)
                ) {
                    Text(
                        text = stringResource(Res.string.no_plans_today),
                        style = MaterialTheme.typography.titleMedium
                    )
                    BfyButton(
                        text = stringResource(Res.string.create_first_item),
                        onClick = onCreateItem,
                        modifier = Modifier.fillMaxWidth(0.7f)
                    )
                }
            }

            is HomeContentState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = BfyTheme.dimens.spacing24),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(BfyTheme.dimens.spacing12)
                ) {
                    Text(
                        text = stringResource(Res.string.load_error),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(text = content.message, color = MaterialTheme.colorScheme.error)
                    BfyButton(
                        text = stringResource(Res.string.retry),
                        onClick = onRetry,
                        style = BfyButtonStyle.SECONDARY,
                        modifier = Modifier.fillMaxWidth(0.6f)
                    )
                }
            }

            is HomeContentState.Data -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(BfyTheme.dimens.spacing12),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(content.items, key = { it.id }) { item ->
                        TaskCard(
                            item = item,
                            isSelected = state.selectedIds.contains(item.id),
                            isSelectionMode = state.isSelectionMode,
                            onClick = { onItemClick(item.id) },
                            onLongPress = { onItemLongPress(item.id) },
                            onToggleDone = { checked -> onToggleDone(item.id, checked) },
                            onDelete = { onDeleteItem(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(
    userName: String,
    rank: String,
    expCurrent: Int,
    expTarget: Int
) {
    val progress = if (expTarget == 0) 0f else expCurrent.toFloat() / expTarget.toFloat()
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(BfyTheme.dimens.spacing8)
    ) {
        Text(
            text = "${stringResource(Res.string.hello_user)}, $userName",
            style = MaterialTheme.typography.headlineMedium
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Rank: $rank", style = MaterialTheme.typography.titleMedium)
            Text(text = "$expCurrent/$expTarget EXP", style = MaterialTheme.typography.labelLarge)
        }
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
