package com.bfy.schedule_app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.bfy.schedule_app.ui.model.TimelineItem
import com.bfy.schedule_app.ui.model.TimelineStatus
import com.bfy.schedule_app.ui.model.TimelineType
import com.bfy.schedule_app.ui.theme.BfyTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskCard(
    item: TimelineItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onToggleDone: (Boolean) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDone = item.status == TimelineStatus.DONE
    val contentAlpha = animateFloatAsState(targetValue = if (isDone) 0.58f else 1f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongPress),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = BfyTheme.dimens.cardElevation)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryLabel(text = item.categoryName, colorHex = item.categoryColorHex)
                Text(
                    text = when (item.type) {
                        TimelineType.TODO -> "TODO"
                        TimelineType.TASK -> "TASK"
                        TimelineType.EVENT -> "EVENT"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = when (item.type) {
                        TimelineType.TODO -> BfyTheme.extendedColors.timelineTodo
                        TimelineType.TASK -> BfyTheme.extendedColors.timelineTask
                        TimelineType.EVENT -> BfyTheme.extendedColors.timelineEvent
                    }
                )
            }

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                modifier = Modifier.alpha(contentAlpha.value)
            )

            if (item.description.isNotBlank()) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = BfyTheme.extendedColors.textMuted,
                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                    modifier = Modifier.alpha(contentAlpha.value)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelectionMode || item.type == TimelineType.TODO) {
                    Checkbox(
                        checked = if (isSelectionMode) isSelected else isDone,
                        onCheckedChange = onToggleDone
                    )
                }

                TextButton(onClick = onDelete) {
                    Text("Delete", color = BfyTheme.extendedColors.danger)
                }
            }
        }
    }
}
