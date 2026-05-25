package com.bfy.schedule_app.ui.screens.collaboration

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bfy.schedule_app.data.remote.model.GroupMemberDto
import com.bfy.schedule_app.ui.theme.*
import com.bfy.schedule_app.ui.viewmodel.GroupDetailViewModel
import com.bfy.schedule_app.utils.Localization
import kotlinx.datetime.*
import com.bfy.schedule_app.ui.screens.createitem.BFYDatePickerDialog
import com.bfy.schedule_app.ui.screens.createitem.BFYTimePickerDialog

private data class CategoryOption(val name: String, val color: Color)
private data class ReminderOption(val key: String, val label: String)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AssignTaskDialog(
    groupId: String,
    viewModel: GroupDetailViewModel,
    initialType: String,
    onDismissRequest: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var type by remember { mutableStateOf(initialType) } // ANNOUNCEMENT, TASK, EVENT
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    // For assignees
    var assigneeExpanded by remember { mutableStateOf(false) }
    var selectedAssignees by remember { mutableStateOf<List<GroupMemberDto>>(emptyList()) }

    // Form states for TASK / EVENT
    var setReminder by remember { mutableStateOf(true) }
    var isAllDay by remember { mutableStateOf(false) }
    var repeatIndex by remember { mutableStateOf(0) }
    var selectedCategoryIndex by remember { mutableStateOf(0) }
    
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryColorIndex by remember { mutableStateOf(0) }
    val categoryColors = listOf(
        Color(0xFFAD7BFF), Color(0xFF59DBC7), Color(0xFF92B4FF), Color(0xFFFF7B7B), Color(0xFFFFD166)
    )

    // Time Picker States
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var startHour by remember { mutableStateOf(9) }
    var startMinute by remember { mutableStateOf(0) }
    var endHour by remember { mutableStateOf(10) }
    var endMinute by remember { mutableStateOf(0) }

    // Date state variables
    var startDate by remember { mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date) }
    var endDate by remember { mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // For Task deadline
    var hasDeadline by remember { mutableStateOf(false) }
    var deadlineDate by remember { mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date) }
    var deadlineHour by remember { mutableStateOf(23) }
    var deadlineMinute by remember { mutableStateOf(59) }
    var showDeadlineDatePicker by remember { mutableStateOf(false) }
    var showDeadlineTimePicker by remember { mutableStateOf(false) }

    val selectedReminderKeys = remember { mutableStateListOf<String>() }
    var alarmEnabled by remember { mutableStateOf(false) }
    var countdownEnabled by remember { mutableStateOf(false) }

    // Categories local state loaded from API
    var categories by remember { mutableStateOf<List<CategoryOption>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        try {
            val cats = com.bfy.schedule_app.data.repository.AppRepository().getCategories()
            val list = mutableListOf(
                CategoryOption("Study", Color(0xFFAD7BFF)),
                CategoryOption("Work", Color(0xFF59DBC7)),
                CategoryOption("Personal", Color(0xFFFFD166))
            )
            cats.forEach { cat ->
                if (list.none { it.name == cat.name }) {
                    list.add(CategoryOption(cat.name, Color(cat.hex_color.removePrefix("#").toLong(16) or 0xFF000000)))
                }
            }
            categories = list
        } catch (e: Exception) {
            categories = listOf(
                CategoryOption("Study", Color(0xFFAD7BFF)),
                CategoryOption("Work", Color(0xFF59DBC7)),
                CategoryOption("Personal", Color(0xFFFFD166))
            )
        }
    }

    val repeatOptions = listOf(
        "Never", 
        "Daily", 
        "Mon-Fri", 
        "Weekly", 
        "Monthly", 
        "Yearly"
    )

    val reminderOptions = listOf(
        ReminderOption("WHEN_STARTS", "When starts"),
        ReminderOption("MIN_5", "5m before"),
        ReminderOption("MIN_10", "10m before"),
        ReminderOption("MIN_30", "30m before"),
        ReminderOption("HOUR_1", "1h before"),
        ReminderOption("DAY_1", "1 day before"),
        ReminderOption("WEEK_1", "1 week before")
    )

    fun formatTime(hour: Int, minute: Int): String {
        return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
    }

    fun formatDate(date: LocalDate): String {
        val dayOfWeek = when(date.dayOfWeek) {
            DayOfWeek.MONDAY -> "T2"
            DayOfWeek.TUESDAY -> "T3"
            DayOfWeek.WEDNESDAY -> "T4"
            DayOfWeek.THURSDAY -> "T5"
            DayOfWeek.FRIDAY -> "T6"
            DayOfWeek.SATURDAY -> "T7"
            DayOfWeek.SUNDAY -> "CN"
        }
        return "$dayOfWeek, ${date.dayOfMonth} Thg ${date.monthNumber}, ${date.year}"
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = BackgroundColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Assign Task/Event", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable content area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Type Selection
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("ANNOUNCEMENT" to "Announcement", "TASK" to "Task", "EVENT" to "Event").forEach { (key, label) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .background(if (type == key) PrimaryColor else Color(0xFF282A2D), RoundedCornerShape(8.dp))
                                    .clickable { 
                                        type = key 
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, color = if (type == key) Color(0xFF003731) else TextSecondary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = TextPrimary,
                            unfocusedBorderColor = Color(0xFF333538),
                            focusedBorderColor = PrimaryColor
                        ),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = TextPrimary,
                            unfocusedBorderColor = Color(0xFF333538),
                            focusedBorderColor = PrimaryColor
                        ),
                        minLines = 3
                    )

                    // If not ANNOUNCEMENT, show the additional fields
                    if (type != "ANNOUNCEMENT") {
                        
                        // Category selection
                        if (categories.isNotEmpty()) {
                            var categoryExpanded by remember { mutableStateOf(false) }
                            Box {
                                FormSelectorRow(
                                    label = "Category",
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
                                        text = { Text("Add new category", color = Color(0xFF59DBC7)) },
                                        onClick = {
                                            categoryExpanded = false
                                            showAddCategoryDialog = true
                                        },
                                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF59DBC7)) }
                                    )
                                }
                            }
                        }

                        // Repeat selection
                        var repeatExpanded by remember { mutableStateOf(false) }
                        Box {
                            FormSelectorRow(
                                label = "Repeat",
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

                        // Time parameters (Always show for Task/Event, Set Reminder toggle is removed)
                        if (type == "EVENT" || setReminder) {
                            FormSwitchRow(
                                label = "All day",
                                subtitle = "Full-day task/event",
                                checked = isAllDay,
                                onCheckedChange = { isAllDay = it }
                            )

                            // Start time/date
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Start time",
                                    color = Color(0xFFBBCAC5),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF1E2023))
                                            .border(1.dp, Color(0xFF3C4946), RoundedCornerShape(8.dp))
                                            .clickable { showStartDatePicker = true }
                                            .padding(horizontal = 12.dp, vertical = 13.dp)
                                    ) {
                                        Text(text = formatDate(startDate), color = Color(0xFFE2E2E6), fontSize = 14.sp)
                                    }
                                    if (!isAllDay) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF1E2023))
                                                .border(1.dp, Color(0xFF3C4946), RoundedCornerShape(8.dp))
                                                .clickable { showStartTimePicker = true }
                                                .padding(horizontal = 12.dp, vertical = 13.dp)
                                        ) {
                                            Text(text = formatTime(startHour, startMinute), color = Color(0xFFE2E2E6), fontSize = 14.sp)
                                        }
                                    }
                                }
                            }

                            // End time/date (ONLY for Event)
                            if (type == "EVENT") {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "End time",
                                        color = Color(0xFFBBCAC5),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF1E2023))
                                                .border(1.dp, Color(0xFF3C4946), RoundedCornerShape(8.dp))
                                                .clickable { showEndDatePicker = true }
                                                .padding(horizontal = 12.dp, vertical = 13.dp)
                                        ) {
                                            Text(text = formatDate(endDate), color = Color(0xFFE2E2E6), fontSize = 14.sp)
                                        }
                                        if (!isAllDay) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFF1E2023))
                                                    .border(1.dp, Color(0xFF3C4946), RoundedCornerShape(8.dp))
                                                    .clickable { showEndTimePicker = true }
                                                    .padding(horizontal = 12.dp, vertical = 13.dp)
                                            ) {
                                                Text(text = formatTime(endHour, endMinute), color = Color(0xFFE2E2E6), fontSize = 14.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Deadline (ONLY for Task)
                        if (type == "TASK") {
                            if (!hasDeadline) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { hasDeadline = true }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF59DBC7), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add deadline", color = Color(0xFF59DBC7), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Deadline", color = Color(0xFFBBCAC5), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove deadline",
                                            tint = Color(0xFFFF7B7B),
                                            modifier = Modifier.size(18.dp).clickable { hasDeadline = false }
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF1E2023))
                                                .border(1.dp, Color(0xFF3C4946), RoundedCornerShape(8.dp))
                                                .clickable { showDeadlineDatePicker = true }
                                                .padding(horizontal = 12.dp, vertical = 13.dp)
                                        ) {
                                            Text(text = formatDate(deadlineDate), color = Color(0xFFE2E2E6), fontSize = 14.sp)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF1E2023))
                                                .border(1.dp, Color(0xFF3C4946), RoundedCornerShape(8.dp))
                                                .clickable { showDeadlineTimePicker = true }
                                                .padding(horizontal = 12.dp, vertical = 13.dp)
                                        ) {
                                            Text(text = formatTime(deadlineHour, deadlineMinute), color = Color(0xFFE2E2E6), fontSize = 14.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Reminder settings (If event or task with reminder)
                        if (type == "EVENT" || setReminder) {
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
                                    text = "Reminder options",
                                    color = Color(0xFFBBCAC5),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    reminderOptions.forEach { option ->
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

                            // Alarm settings
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

                            // Countdown settings
                            FormSwitchRow(
                                label = "Countdown reminder",
                                subtitle = "Enable foreground countdown notification",
                                checked = countdownEnabled,
                                onCheckedChange = { countdownEnabled = it }
                            )
                        }

                        // Assignee Picker
                        Spacer(modifier = Modifier.height(8.dp))
                        Box {
                            OutlinedTextField(
                                value = selectedAssignees.joinToString(", ") { it.full_name },
                                onValueChange = { },
                                label = { Text("Assignees") },
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { assigneeExpanded = !assigneeExpanded }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextPrimary)
                                    }
                                },
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    textColor = TextPrimary,
                                    unfocusedBorderColor = Color(0xFF333538),
                                    focusedBorderColor = PrimaryColor
                                )
                            )
                            
                            // Transparent box to capture clicks
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { assigneeExpanded = true }
                            )
                            
                            DropdownMenu(
                                expanded = assigneeExpanded,
                                onDismissRequest = { assigneeExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f).background(Color(0xFF282A2D))
                            ) {
                                uiState.members.forEach { member ->
                                    val isSelected = selectedAssignees.contains(member)
                                    DropdownMenuItem(onClick = {
                                        if (isSelected) {
                                            selectedAssignees = selectedAssignees - member
                                        } else {
                                            selectedAssignees = selectedAssignees + member
                                        }
                                    }) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = null,
                                                colors = CheckboxDefaults.colors(checkedColor = PrimaryColor)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(member.full_name, color = TextPrimary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Button
                Button(
                    onClick = {
                        val assignees = if (type != "ANNOUNCEMENT") selectedAssignees.map { it.id } else emptyList()

                        val startTimeStr = if (type == "EVENT" || (type == "TASK" && setReminder)) {
                            val dt = if (isAllDay) {
                                LocalDateTime(startDate.year, startDate.monthNumber, startDate.dayOfMonth, 0, 0)
                            } else {
                                LocalDateTime(startDate.year, startDate.monthNumber, startDate.dayOfMonth, startHour, startMinute)
                            }
                            dt.toInstant(TimeZone.currentSystemDefault()).toString()
                        } else null

                        val endTimeStr = if (type == "EVENT") {
                            val dt = if (isAllDay) {
                                LocalDateTime(endDate.year, endDate.monthNumber, endDate.dayOfMonth, 23, 59)
                            } else {
                                LocalDateTime(endDate.year, endDate.monthNumber, endDate.dayOfMonth, endHour, endMinute)
                            }
                            dt.toInstant(TimeZone.currentSystemDefault()).toString()
                        } else null

                        val deadlineStr = if (type == "TASK" && hasDeadline) {
                            val dt = LocalDateTime(deadlineDate.year, deadlineDate.monthNumber, deadlineDate.dayOfMonth, deadlineHour, deadlineMinute)
                            dt.toInstant(TimeZone.currentSystemDefault()).toString()
                        } else null

                        val recurrenceStr = when (repeatIndex) {
                            1 -> "DAILY"
                            2 -> "MON_FRI"
                            3 -> "WEEKLY"
                            4 -> "MONTHLY"
                            5 -> "YEARLY"
                            else -> null
                        }

                        viewModel.createSchedule(
                            groupId = groupId,
                            type = type,
                            title = title,
                            description = description,
                            assignees = assignees,
                            priority = "MEDIUM",
                            startTime = startTimeStr,
                            endTime = endTimeStr,
                            deadline = deadlineStr,
                            isAllDay = isAllDay,
                            recurrenceType = recurrenceStr,
                            reminders = selectedReminderKeys.toList(),
                            categoryName = if (categories.isNotEmpty() && selectedCategoryIndex in categories.indices) categories[selectedCategoryIndex].name else null,
                            isAlarm = alarmEnabled,
                            isCountdown = countdownEnabled
                        )
                        onDismissRequest()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryColor),
                    shape = RoundedCornerShape(25.dp),
                    enabled = title.isNotBlank() && !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Create", color = Color(0xFF003731), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Dialogs
    if (showStartDatePicker) {
        BFYDatePickerDialog(
            initialDate = startDate,
            onDismiss = { showStartDatePicker = false },
            onConfirm = { date -> startDate = date; showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        BFYDatePickerDialog(
            initialDate = endDate,
            onDismiss = { showEndDatePicker = false },
            onConfirm = { date -> endDate = date; showEndDatePicker = false }
        )
    }

    if (showDeadlineDatePicker) {
        BFYDatePickerDialog(
            initialDate = deadlineDate,
            onDismiss = { showDeadlineDatePicker = false },
            onConfirm = { date -> deadlineDate = date; showDeadlineDatePicker = false }
        )
    }

    if (showDeadlineTimePicker) {
        BFYTimePickerDialog(
            initialHour = deadlineHour,
            initialMinute = deadlineMinute,
            onDismiss = { showDeadlineTimePicker = false },
            onConfirm = { h, m -> deadlineHour = h; deadlineMinute = m; showDeadlineTimePicker = false }
        )
    }

    if (showStartTimePicker) {
        BFYTimePickerDialog(
            initialHour = startHour,
            initialMinute = startMinute,
            onDismiss = { showStartTimePicker = false },
            onConfirm = { h, m -> startHour = h; startMinute = m; showStartTimePicker = false }
        )
    }

    if (showEndTimePicker) {
        BFYTimePickerDialog(
            initialHour = endHour,
            initialMinute = endMinute,
            onDismiss = { showEndTimePicker = false },
            onConfirm = { h, m -> endHour = h; endMinute = m; showEndTimePicker = false }
        )
    }

    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Add new category", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("Category name") },
                        singleLine = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = TextPrimary,
                            focusedBorderColor = PrimaryColor
                        )
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
                        val list = categories.toMutableList()
                        list.add(CategoryOption(trimmed, categoryColors[newCategoryColorIndex]))
                        categories = list
                        selectedCategoryIndex = categories.lastIndex
                        newCategoryName = ""
                        newCategoryColorIndex = 0
                        showAddCategoryDialog = false
                    }
                }) {
                    Text("Add", color = PrimaryColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            backgroundColor = BackgroundColor
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
