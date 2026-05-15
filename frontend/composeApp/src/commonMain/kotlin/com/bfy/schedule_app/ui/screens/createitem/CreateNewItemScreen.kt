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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bfy.schedule_app.ui.viewmodel.CreateItemViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import com.bfy.schedule_app.utils.Localization
import kotlinx.datetime.*
import kotlinx.coroutines.launch


private data class CategoryOption(val name: String, val color: Color)

private data class ReminderOption(val key: String, val label: String)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CreateNewItemScreen(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit = {},
    initialSchedule: com.bfy.schedule_app.data.remote.model.ScheduleDto? = null,
    modifier: Modifier = Modifier
) {
    val viewModel: CreateItemViewModel = viewModel { CreateItemViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            viewModel.resetState()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadCategories()
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSuccess()
            onDismiss()
        }
    }

    val categories = remember(uiState.categories) {
        val list = mutableListOf(
            CategoryOption(Localization.get("study") ?: "Study", Color(0xFFAD7BFF)),
            CategoryOption(Localization.get("work") ?: "Work", Color(0xFF59DBC7)),
            CategoryOption(Localization.get("personal") ?: "Personal", Color(0xFFFFD166))
        )
        uiState.categories.forEach { cat ->
            if (list.none { it.name == cat.name }) {
                list.add(CategoryOption(cat.name, Color(cat.hex_color.removePrefix("#").toLong(16) or 0xFF000000)))
            }
        }
        list
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val segments = listOf(Localization.get("task"), Localization.get("event"))
    val repeatOptions = listOf(
        Localization.get("never"), 
        Localization.get("daily"), 
        Localization.get("mon_fri"), 
        Localization.get("weekly"), 
        Localization.get("monthly"), 
        Localization.get("yearly")
    )
    val taskDateOptions = listOf("Today, 09:00", "Today, 18:00", "Tomorrow, 09:00", "Tomorrow, 18:00")
    val eventTimeOptions = listOf("08:00", "09:00", "10:00", "11:00", "13:00", "15:00", "18:00")
    val customReminderOptions = listOf("2 hours before", "3 hours before", "12 hours before")
    val alarmSounds = listOf("Default", "Bell", "Gentle", "Digital")
    val reminderOptions = listOf(
        ReminderOption("WHEN_STARTS", Localization.get("starts")),
        ReminderOption("MIN_5", Localization.get("min_5")),
        ReminderOption("MIN_10", Localization.get("min_10")),
        ReminderOption("MIN_30", Localization.get("min_30")),
        ReminderOption("HOUR_1", Localization.get("hour_1")),
        ReminderOption("DAY_1", Localization.get("day_1")),
        ReminderOption("WEEK_1", Localization.get("week_1")),
        ReminderOption("CUSTOM", Localization.get("custom"))
    )

    var selectedSegment by remember { 
        mutableStateOf(
            when(initialSchedule?.type) {
                "TASK" -> 0
                "EVENT" -> 1
                else -> 0
            }
        ) 
    }
    var title by remember { mutableStateOf(initialSchedule?.title ?: "") }
    var description by remember { mutableStateOf(initialSchedule?.description ?: "") }
    var setReminder by remember { mutableStateOf(initialSchedule?.start_time != null) }
    var isAllDay by remember { mutableStateOf(false) } 
    var repeatIndex by remember { mutableStateOf(0) }
    var selectedCategoryIndex by remember { mutableStateOf(0) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryColorIndex by remember { mutableStateOf(0) }
    val categoryColors = listOf(
        Color(0xFFAD7BFF), Color(0xFF59DBC7), Color(0xFF92B4FF), Color(0xFFFF7B7B), Color(0xFFFFD166)
    )

    var eventStartIndex by remember { mutableStateOf(1) }
    var eventEndIndex by remember { mutableStateOf(2) }
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
            repeatIndex != 0 ||
            selectedCategoryIndex != 0 ||
            setReminder
    }

    fun validate(): Boolean {
        titleError = null
        taskTimeError = null
        eventTimeError = null

        if (title.isBlank()) {
            titleError = Localization.get("title_required")
            return false
        }

        if (title.length > 100) {
            titleError = Localization.get("title_too_long")
            return false
        }

        if (description.length > 1000) {
            return false
        }

        if (setReminder && eventStartIndex >= eventEndIndex) {
            // Minimal validation for unified time
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
                    text = Localization.get("create_item"),
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
                    label = Localization.get("title"),
                    value = title,
                    onValueChange = { title = it },
                    placeholder = Localization.get("what_needs_done"),
                    isError = titleError != null
                )
                titleError?.let {
                    Text(text = it, color = Color(0xFFFF7B7B), fontSize = 12.sp)
                }

                FormTextField(
                    label = Localization.get("description"),
                    value = description,
                    onValueChange = { description = it },
                    placeholder = Localization.get("add_details"),
                    singleLine = false,
                    minLines = 4
                )

                var categoryExpanded by remember { mutableStateOf(false) }
                Box {
                    FormSelectorRow(
                        label = Localization.get("category"),
                        value = categories[selectedCategoryIndex].name,
                        dotColor = categories[selectedCategoryIndex].color,
                        onClick = { categoryExpanded = true }
                    )
                    androidx.compose.material3.DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                        modifier = Modifier.background(Color(0xFF1E2023)).border(1.dp, Color(0xFF3C4946), RoundedCornerShape(8.dp))
                    ) {
                        categories.forEachIndexed { index, category ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(category.name, color = Color.White) },
                                onClick = {
                                    selectedCategoryIndex = index
                                    categoryExpanded = false
                                },
                                leadingIcon = {
                                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(category.color))
                                }
                            )
                        }
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(Localization.get("add_category"), color = Color(0xFF59DBC7)) },
                            onClick = {
                                categoryExpanded = false
                                showAddCategoryDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF59DBC7)) }
                        )
                    }
                }

                var repeatExpanded by remember { mutableStateOf(false) }
                Box {
                    FormSelectorRow(
                        label = Localization.get("repeat"),
                        value = repeatOptions[repeatIndex],
                        onClick = { repeatExpanded = true }
                    )
                    androidx.compose.material3.DropdownMenu(
                        expanded = repeatExpanded,
                        onDismissRequest = { repeatExpanded = false },
                        modifier = Modifier.background(Color(0xFF1E2023))
                    ) {
                        repeatOptions.forEachIndexed { index, option ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(option, color = Color.White) },
                                onClick = {
                                    repeatIndex = index
                                    repeatExpanded = false
                                }
                            )
                        }
                    }
                }

                FormSwitchRow(
                    label = Localization.get("set_reminder"),
                    subtitle = Localization.get("toggle_time_rem"),
                    checked = setReminder,
                    onCheckedChange = { setReminder = it }
                )

                if (setReminder) {
                    FormSwitchRow(
                        label = Localization.get("all_day"),
                        subtitle = Localization.get("full_day_task_event"),
                        checked = isAllDay,
                        onCheckedChange = { isAllDay = it }
                    )

                    var eventStartExpanded by remember { mutableStateOf(false) }
                    Box {
                        FormSelectorRow(
                            label = Localization.get("start_time"),
                            value = eventTimeOptions[eventStartIndex],
                            onClick = { eventStartExpanded = true },
                            enabled = !isAllDay
                        )
                        androidx.compose.material3.DropdownMenu(
                            expanded = eventStartExpanded && !isAllDay,
                            onDismissRequest = { eventStartExpanded = false },
                            modifier = Modifier.background(Color(0xFF1E2023))
                        ) {
                            eventTimeOptions.forEachIndexed { index, option ->
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text(option, color = Color.White) },
                                    onClick = {
                                        eventStartIndex = index
                                        eventStartExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    var eventEndExpanded by remember { mutableStateOf(false) }
                    Box {
                        FormSelectorRow(
                            label = Localization.get("end_time"),
                            value = eventTimeOptions[eventEndIndex],
                            onClick = { eventEndExpanded = true },
                            enabled = !isAllDay
                        )
                        androidx.compose.material3.DropdownMenu(
                            expanded = eventEndExpanded && !isAllDay,
                            onDismissRequest = { eventEndExpanded = false },
                            modifier = Modifier.background(Color(0xFF1E2023))
                        ) {
                            eventTimeOptions.forEachIndexed { index, option ->
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text(option, color = Color.White) },
                                    onClick = {
                                        eventEndIndex = index
                                        eventEndExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1A1C1F))
                            .border(1.dp, Color(0xFF1E2023), RoundedCornerShape(12.dp))
                            .alpha(if (!isAllDay) 1f else 0.5f)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = Localization.get("reminder_options_label"),
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
                                            .clickable(enabled = !isAllDay) {
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
                        label = Localization.get("alarm_reminders"),
                        subtitle = Localization.get("play_sound_rem"),
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
                        label = Localization.get("countdown_reminder"),
                        subtitle = Localization.get("enable_foreground_countdown"),
                        checked = countdownEnabled,
                        onCheckedChange = { countdownEnabled = it },
                        enabled = !isAllDay
                    )

                    eventTimeError?.let {
                        Text(text = it, color = Color(0xFFFF7B7B), fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.error != null) {
                    Text(uiState.error!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (initialSchedule != null) {
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        com.bfy.schedule_app.data.repository.AppRepository().deleteSchedule(initialSchedule.id)
                                        onSuccess()
                                        onDismiss()
                                    } catch (e: Exception) {
                                        // Handle error
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7B7B)),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !uiState.isLoading
                        ) {
                            Text(
                                text = Localization.get("delete"),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (validate()) {
                                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                                val today = now.date
                                val tomorrow = today.plus(1, DateTimeUnit.DAY)
                                
                                fun parseOption(option: String): String {
                                    val date = if (option.startsWith("Today")) today else tomorrow
                                    val time = option.substringAfter(", ")
                                    return "${date}T${time}:00.000Z"
                                }

                                val startTimeStr = if (setReminder) {
                                    if (isAllDay) "${today}T00:00:00.000Z"
                                    else "${today}T${eventTimeOptions[eventStartIndex]}:00.000Z"
                                } else null

                                val endTimeStr = if (setReminder) {
                                    if (isAllDay) "${today}T23:59:59.000Z"
                                    else "${today}T${eventTimeOptions[eventEndIndex]}:00.000Z"
                                } else null

                                val recurrenceStr = when(repeatIndex) {
                                    1 -> "Daily"
                                    3 -> "Weekly"
                                    4 -> "Monthly"
                                    else -> "Never"
                                }

                                if (initialSchedule != null) {
                                    viewModel.updateItem(
                                        id = initialSchedule.id,
                                        title = title,
                                        description = description,
                                        type = segments[selectedSegment].uppercase(),
                                        startTime = startTimeStr,
                                        endTime = endTimeStr,
                                        deadline = endTimeStr,
                                        isAllDay = isAllDay,
                                        recurrence = recurrenceStr,
                                        reminders = selectedReminderKeys.toList()
                                    )
                                } else {
                                    viewModel.createItem(
                                        title = title,
                                        description = description,
                                        type = segments[selectedSegment].uppercase(),
                                        startTime = startTimeStr,
                                        endTime = endTimeStr,
                                        deadline = endTimeStr,
                                        isAllDay = isAllDay,
                                        recurrence = recurrenceStr,
                                        reminders = selectedReminderKeys.toList()
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF59DBC7)),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !uiState.isLoading
                    ) {
                        val label = if (uiState.isLoading) Localization.get("processing") 
                                    else if (initialSchedule != null) Localization.get("update")
                                    else {
                                        val saveKey = when(selectedSegment) {
                                            0 -> "save_task"
                                            else -> "save_event"
                                        }
                                        Localization.get(saveKey)
                                    }
                        Text(
                            text = label,
                            color = Color(0xFF003731),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(Localization.get("discard_title")) },
            text = { Text(Localization.get("discard_msg")) },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    onDismiss()
                }) {
                    Text(Localization.get("discard_btn"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(Localization.get("keep_editing"))
                }
            }
        )
    }

    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text(Localization.get("add_category_title")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text(Localization.get("category_name")) },
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
                    Text(Localization.get("add"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text(Localization.get("cancel"))
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
    dotColor: Color? = null,
    enabled: Boolean = true
) {
    Column(
        modifier = Modifier.alpha(if (enabled) 1f else 0.5f),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
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
                .clickable(enabled = enabled) { onClick() }
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
    leadingIcon: (@Composable (() -> Unit))? = null,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A1C1F))
            .border(1.dp, Color(0xFF1E2023), RoundedCornerShape(12.dp))
            .alpha(if (enabled) 1f else 0.5f)
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
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF003731),
                checkedTrackColor = Color(0xFF59DBC7),
                uncheckedThumbColor = Color(0xFFBBCAC5),
                uncheckedTrackColor = Color(0xFF333538)
            )
        )
    }
}

