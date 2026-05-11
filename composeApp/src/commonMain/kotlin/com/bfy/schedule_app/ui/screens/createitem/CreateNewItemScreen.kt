package com.bfy.schedule_app.ui.screens.createitem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class CategoryOption(val name: String, val color: Color)

private data class ReminderOption(val key: String, val label: String)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CreateNewItemScreen(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val segments = listOf("To-do", "Task", "Event")
    val repeatOptions = listOf("Never", "Daily", "Mon-Fri", "Weekly", "Monthly", "Yearly")
    val taskDateOptions = listOf("Today, 09:00", "Today, 18:00", "Tomorrow, 09:00", "Tomorrow, 18:00")
    val eventTimeOptions = listOf("08:00", "09:00", "10:00", "11:00", "13:00", "15:00", "18:00")
    val customReminderOptions = listOf("2 hours before", "3 hours before", "12 hours before")
    val alarmSounds = listOf("Default", "Bell", "Gentle", "Digital")
    val reminderOptions = listOf(
        ReminderOption("WHEN_STARTS", "Starts"),
        ReminderOption("MIN_5", "5m"),
        ReminderOption("MIN_10", "10m"),
        ReminderOption("MIN_30", "30m"),
        ReminderOption("HOUR_1", "1h"),
        ReminderOption("DAY_1", "1 day"),
        ReminderOption("WEEK_1", "1 week"),
        ReminderOption("CUSTOM", "Custom")
    )

    var selectedSegment by remember { mutableStateOf(0) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var repeatIndex by remember { mutableStateOf(0) }
    var selectedCategoryIndex by remember { mutableStateOf(0) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryColorIndex by remember { mutableStateOf(0) }
    val categoryColors = listOf(
        Color(0xFFAD7BFF), Color(0xFF59DBC7), Color(0xFF92B4FF), Color(0xFFFF7B7B), Color(0xFFFFD166)
    )
    val categories = remember {
        mutableStateListOf(
            CategoryOption("Study", Color(0xFFAD7BFF)),
            CategoryOption("Work", Color(0xFF59DBC7)),
            CategoryOption("Personal", Color(0xFFFFD166))
        )
    }

    val todoItems = remember { mutableStateListOf<String>() }
    var taskStartIndex by remember { mutableStateOf(0) }
    var taskDeadlineIndex by remember { mutableStateOf(1) }
    var eventStartIndex by remember { mutableStateOf(1) }
    var eventEndIndex by remember { mutableStateOf(2) }
    var eventAllDay by remember { mutableStateOf(false) }
    val selectedReminderKeys = remember { mutableStateListOf<String>() }
    var customReminderIndex by remember { mutableStateOf(0) }
    var alarmEnabled by remember { mutableStateOf(false) }
    var alarmSoundIndex by remember { mutableStateOf(0) }
    var countdownEnabled by remember { mutableStateOf(false) }

    var titleError by remember { mutableStateOf<String?>(null) }
    var taskTimeError by remember { mutableStateOf<String?>(null) }
    var eventTimeError by remember { mutableStateOf<String?>(null) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    fun isDirty(): Boolean {
        return title.isNotBlank() ||
            description.isNotBlank() ||
            todoItems.isNotEmpty() ||
            repeatIndex != 0 ||
            selectedCategoryIndex != 0 ||
            taskStartIndex != 0 ||
            taskDeadlineIndex != 1 ||
            eventStartIndex != 1 ||
            eventEndIndex != 2 ||
            eventAllDay ||
            selectedReminderKeys.isNotEmpty() ||
            alarmEnabled ||
            countdownEnabled
    }

    fun validate(): Boolean {
        titleError = null
        taskTimeError = null
        eventTimeError = null

        if (selectedSegment == 0) {
            val hasTodoValue = title.isNotBlank() || todoItems.any { it.isNotBlank() }
            if (!hasTodoValue) {
                titleError = "Title is required."
                return false
            }
        } else if (title.isBlank()) {
            titleError = "Title is required."
            return false
        }

        if (title.length > 100) {
            titleError = "Title must be at most 100 characters."
            return false
        }

        if (description.length > 1000) {
            return false
        }

        if (selectedSegment == 1 && taskDeadlineIndex < taskStartIndex) {
            taskTimeError = "Deadline must be after start date."
            return false
        }

        if (selectedSegment == 2 && eventEndIndex <= eventStartIndex) {
            eventTimeError = "End time must be later than start time."
            return false
        }

        return true
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (isDirty()) showDiscardDialog = true else onDismiss()
        },
        sheetState = sheetState,
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3C4946))
                )
            }
        },
        containerColor = Color(0xFF282A2D),
        scrimColor = Color(0x66000000),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Create Item",
                    color = Color(0xFFE2E2E6),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E2023))
                        .border(1.dp, Color(0xFF3C4946).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    segments.forEachIndexed { index, segment ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selectedSegment == index) Color(0xFF282A2D) else Color.Transparent)
                                .clickable { selectedSegment = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = segment,
                                color = if (selectedSegment == index) Color(0xFFE2E2E6) else Color(0xFFBBCAC5),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                FormTextField(
                    label = "Title",
                    value = title,
                    onValueChange = { title = it },
                    placeholder = "What needs to be done?",
                    isError = titleError != null
                )
                titleError?.let {
                    Text(text = it, color = Color(0xFFFF7B7B), fontSize = 12.sp)
                }

                if (selectedSegment == 0) {
                    TextButton(
                        onClick = {
                            if (title.isNotBlank()) {
                                todoItems.add(title.trim())
                                title = ""
                                titleError = null
                            } else {
                                todoItems.add("")
                            }
                        }
                    ) {
                        Text("Add more (To-do)", color = Color(0xFF59DBC7))
                    }
                    todoItems.forEachIndexed { index, item ->
                        FormTextField(
                            label = "To-do ${index + 1}",
                            value = item,
                            onValueChange = { todoItems[index] = it },
                            placeholder = "Enter to-do item"
                        )
                    }
                }

                if (selectedSegment == 1 || selectedSegment == 2) {
                    FormTextField(
                        label = "Description",
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Add details...",
                        singleLine = false,
                        minLines = 4
                    )
                }

                FormSelectorRow(
                    label = "Category",
                    value = categories[selectedCategoryIndex].name,
                    dotColor = categories[selectedCategoryIndex].color,
                    onClick = {
                        selectedCategoryIndex = (selectedCategoryIndex + 1) % categories.size
                    }
                )
                TextButton(
                    onClick = { showAddCategoryDialog = true }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = "Add category", tint = Color(0xFF59DBC7))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add category (+)", color = Color(0xFF59DBC7))
                    }
                }

                if (selectedSegment == 1 || selectedSegment == 2) {
                    FormSelectorRow(
                        label = "Repeat",
                        value = repeatOptions[repeatIndex],
                        onClick = { repeatIndex = (repeatIndex + 1) % repeatOptions.size }
                    )
                }

                if (selectedSegment == 1) {
                    FormSelectorRow(
                        label = "Start date",
                        value = taskDateOptions[taskStartIndex],
                        onClick = { taskStartIndex = (taskStartIndex + 1) % taskDateOptions.size }
                    )
                    FormSelectorRow(
                        label = "Deadline",
                        value = taskDateOptions[taskDeadlineIndex],
                        onClick = { taskDeadlineIndex = (taskDeadlineIndex + 1) % taskDateOptions.size }
                    )
                    taskTimeError?.let {
                        Text(text = it, color = Color(0xFFFF7B7B), fontSize = 12.sp)
                    }
                }

                if (selectedSegment == 2) {
                    FormSelectorRow(
                        label = "Start time",
                        value = eventTimeOptions[eventStartIndex],
                        onClick = { eventStartIndex = (eventStartIndex + 1) % eventTimeOptions.size }
                    )
                    FormSelectorRow(
                        label = "End time",
                        value = eventTimeOptions[eventEndIndex],
                        onClick = { eventEndIndex = (eventEndIndex + 1) % eventTimeOptions.size }
                    )
                    FormSwitchRow(
                        label = "All day",
                        subtitle = "Turn on for full-day event",
                        checked = eventAllDay,
                        onCheckedChange = { eventAllDay = it }
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1A1C1F))
                            .border(1.dp, Color(0xFF1E2023), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Reminder multi-select",
                            color = Color(0xFFBBCAC5),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        reminderOptions.chunked(4).forEach { rowItems ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                rowItems.forEach { option ->
                                    val selected = selectedReminderKeys.contains(option.key)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(if (selected) Color(0xFF59DBC7) else Color(0xFF1E2023))
                                            .border(
                                                1.dp,
                                                if (selected) Color.Transparent else Color(0xFF3C4946),
                                                RoundedCornerShape(20.dp)
                                            )
                                            .clickable {
                                                if (selected) selectedReminderKeys.remove(option.key)
                                                else selectedReminderKeys.add(option.key)
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = option.label,
                                            color = if (selected) Color(0xFF003731) else Color(0xFFE2E2E6),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (selectedReminderKeys.contains("CUSTOM")) {
                        FormSelectorRow(
                            label = "Custom reminder picker",
                            value = customReminderOptions[customReminderIndex],
                            onClick = { customReminderIndex = (customReminderIndex + 1) % customReminderOptions.size }
                        )
                    }

                    FormSwitchRow(
                        label = "Alarm reminders",
                        subtitle = "Play sound when reminder triggers",
                        checked = alarmEnabled,
                        onCheckedChange = { alarmEnabled = it },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Alarm",
                                tint = Color(0xFF59DBC7),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )

                    if (alarmEnabled) {
                        FormSelectorRow(
                            label = "Alarm sound",
                            value = alarmSounds[alarmSoundIndex],
                            onClick = { alarmSoundIndex = (alarmSoundIndex + 1) % alarmSounds.size }
                        )
                    }

                    FormSwitchRow(
                        label = "Countdown reminder",
                        subtitle = "Enable foreground countdown notification",
                        checked = countdownEnabled,
                        onCheckedChange = { countdownEnabled = it }
                    )

                    eventTimeError?.let {
                        Text(text = it, color = Color(0xFFFF7B7B), fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (validate()) onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF59DBC7)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Save ${segments[selectedSegment]}",
                        color = Color(0xFF003731),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard changes?") },
            text = { Text("You have unsaved data. Do you want to discard it?") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    onDismiss()
                }) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Keep editing")
                }
            }
        )
    }

    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Add category") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("Category name") },
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        categoryColors.forEachIndexed { index, color ->
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        if (newCategoryColorIndex == index) 2.dp else 0.dp,
                                        Color.White,
                                        CircleShape
                                    )
                                    .clickable { newCategoryColorIndex = index }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val trimmed = newCategoryName.trim()
                    if (trimmed.isNotEmpty() && trimmed.length <= 50) {
                        categories.add(CategoryOption(trimmed, categoryColors[newCategoryColorIndex]))
                        selectedCategoryIndex = categories.lastIndex
                        newCategoryName = ""
                        newCategoryColorIndex = 0
                        showAddCategoryDialog = false
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun FormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    minLines: Int = 1,
    isError: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            color = Color(0xFFBBCAC5),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFF869490)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1E2023),
                unfocusedContainerColor = Color(0xFF1E2023),
                focusedBorderColor = if (isError) Color(0xFFFF7B7B) else Color(0xFF3C4946),
                unfocusedBorderColor = if (isError) Color(0xFFFF7B7B) else Color(0xFF3C4946),
                focusedTextColor = Color(0xFFE2E2E6),
                unfocusedTextColor = Color(0xFFE2E2E6),
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = singleLine,
            minLines = minLines,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun FormSelectorRow(
    label: String,
    value: String,
    onClick: () -> Unit,
    dotColor: Color? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            color = Color(0xFFBBCAC5),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E2023))
                .border(1.dp, Color(0xFF3C4946), RoundedCornerShape(8.dp))
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (dotColor != null) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text = value, color = Color(0xFFE2E2E6), fontSize = 15.sp)
            }
            Text(text = "▼", color = Color(0xFFE2E2E6), fontSize = 10.sp)
        }
    }
}

@Composable
private fun FormSwitchRow(
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    leadingIcon: (@Composable (() -> Unit))? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A1C1F))
            .border(1.dp, Color(0xFF1E2023), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x3300A896)),
                    contentAlignment = Alignment.Center
                ) {
                    leadingIcon()
                }
            }
            Column {
                Text(
                    text = label,
                    color = Color(0xFFE2E2E6),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = Color(0xFFBBCAC5),
                    fontSize = 12.sp
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF003731),
                checkedTrackColor = Color(0xFF59DBC7),
                uncheckedThumbColor = Color(0xFFBBCAC5),
                uncheckedTrackColor = Color(0xFF333538)
            )
        )
    }
}

