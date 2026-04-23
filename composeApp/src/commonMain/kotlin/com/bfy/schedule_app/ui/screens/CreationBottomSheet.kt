package com.bfy.schedule_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.bfy.schedule_app.ui.components.BfyButton
import com.bfy.schedule_app.ui.components.BfyButtonStyle
import com.bfy.schedule_app.ui.components.BfyTextField
import com.bfy.schedule_app.ui.model.TimelineType
import com.bfy.schedule_app.ui.theme.BfyTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant
import org.jetbrains.compose.resources.stringResource
import schedule_app.composeapp.generated.resources.Res
import schedule_app.composeapp.generated.resources.alarm
import schedule_app.composeapp.generated.resources.all_day
import schedule_app.composeapp.generated.resources.cancel
import schedule_app.composeapp.generated.resources.category
import schedule_app.composeapp.generated.resources.countdown
import schedule_app.composeapp.generated.resources.deadline
import schedule_app.composeapp.generated.resources.description
import schedule_app.composeapp.generated.resources.end_date
import schedule_app.composeapp.generated.resources.event
import schedule_app.composeapp.generated.resources.reminder
import schedule_app.composeapp.generated.resources.repeat
import schedule_app.composeapp.generated.resources.save
import schedule_app.composeapp.generated.resources.start_date
import schedule_app.composeapp.generated.resources.task
import schedule_app.composeapp.generated.resources.title
import schedule_app.composeapp.generated.resources.todo

private enum class DateTarget {
    DEADLINE,
    START,
    END
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreationBottomSheet(
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var selectedType by remember { mutableStateOf(TimelineType.TODO) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Study") }
    var repeat by remember { mutableStateOf("Never") }
    var reminder by remember { mutableStateOf("5 minutes before") }

    var deadlineMillis by remember { mutableLongStateOf(0L) }
    var startMillis by remember { mutableLongStateOf(0L) }
    var endMillis by remember { mutableLongStateOf(0L) }

    var allDay by remember { mutableStateOf(false) }
    var alarmEnabled by remember { mutableStateOf(false) }
    var countdownEnabled by remember { mutableStateOf(false) }

    var dateTarget by remember { mutableStateOf<DateTarget?>(null) }
    val datePickerState = rememberDatePickerState()

    if (dateTarget != null) {
        DatePickerDialog(
            onDismissRequest = { dateTarget = null },
            confirmButton = {
                TextButton(onClick = {
                    val selected = datePickerState.selectedDateMillis ?: 0L
                    when (dateTarget) {
                        DateTarget.DEADLINE -> deadlineMillis = selected
                        DateTarget.START -> startMillis = selected
                        DateTarget.END -> endMillis = selected
                        null -> Unit
                    }
                    dateTarget = null
                }) {
                    Text(stringResource(Res.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { dateTarget = null }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(BfyTheme.dimens.spacing16),
        verticalArrangement = Arrangement.spacedBy(BfyTheme.dimens.spacing12)
    ) {
        item {
            TypeSelector(
                selectedType = selectedType,
                onTypeChanged = { selectedType = it }
            )
        }

        item {
            BfyTextField(
                value = title,
                onValueChange = { title = it },
                label = stringResource(Res.string.title)
            )
        }

        if (selectedType != TimelineType.TODO) {
            item {
                BfyTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = stringResource(Res.string.description),
                    singleLine = false
                )
            }
        }

        item {
            BfyTextField(
                value = category,
                onValueChange = { category = it },
                label = stringResource(Res.string.category)
            )
        }

        if (selectedType == TimelineType.TASK) {
            item {
                BfyButton(
                    text = "${stringResource(Res.string.deadline)}: ${millisToDateText(deadlineMillis)}",
                    onClick = { dateTarget = DateTarget.DEADLINE },
                    style = BfyButtonStyle.SECONDARY
                )
            }
        }

        if (selectedType == TimelineType.EVENT) {
            item {
                BfyButton(
                    text = "${stringResource(Res.string.start_date)}: ${millisToDateText(startMillis)}",
                    onClick = { dateTarget = DateTarget.START },
                    style = BfyButtonStyle.SECONDARY
                )
            }
            item {
                BfyButton(
                    text = "${stringResource(Res.string.end_date)}: ${millisToDateText(endMillis)}",
                    onClick = { dateTarget = DateTarget.END },
                    style = BfyButtonStyle.SECONDARY
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(Res.string.all_day), style = MaterialTheme.typography.titleMedium)
                    Switch(checked = allDay, onCheckedChange = { allDay = it })
                }
            }
        }

        if (selectedType != TimelineType.TODO) {
            item {
                BfyTextField(
                    value = repeat,
                    onValueChange = { repeat = it },
                    label = stringResource(Res.string.repeat)
                )
            }
            item {
                BfyTextField(
                    value = reminder,
                    onValueChange = { reminder = it },
                    label = stringResource(Res.string.reminder)
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(Res.string.alarm), style = MaterialTheme.typography.titleMedium)
                    Switch(checked = alarmEnabled, onCheckedChange = { alarmEnabled = it })
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(Res.string.countdown), style = MaterialTheme.typography.titleMedium)
                    Switch(checked = countdownEnabled, onCheckedChange = { countdownEnabled = it })
                }
            }
        }

        item {
            BfyButton(
                text = stringResource(Res.string.save),
                onClick = {
                    onSave()
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun TypeSelector(
    selectedType: TimelineType,
    onTypeChanged: (TimelineType) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(BfyTheme.dimens.spacing8)) {
        SegmentedOption(
            label = stringResource(Res.string.todo),
            selected = selectedType == TimelineType.TODO,
            onClick = { onTypeChanged(TimelineType.TODO) },
            modifier = Modifier.weight(1f)
        )
        SegmentedOption(
            label = stringResource(Res.string.task),
            selected = selectedType == TimelineType.TASK,
            onClick = { onTypeChanged(TimelineType.TASK) },
            modifier = Modifier.weight(1f)
        )
        SegmentedOption(
            label = stringResource(Res.string.event),
            selected = selectedType == TimelineType.EVENT,
            onClick = { onTypeChanged(TimelineType.EVENT) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SegmentedOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BfyButton(
        text = label,
        onClick = onClick,
        style = if (selected) BfyButtonStyle.PRIMARY else BfyButtonStyle.SECONDARY,
        modifier = modifier
    )
}

private fun millisToDateText(value: Long): String {
    if (value <= 0L) {
        return "Not selected"
    }
    val localDateTime = Instant.fromEpochMilliseconds(value)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.date}"
}
