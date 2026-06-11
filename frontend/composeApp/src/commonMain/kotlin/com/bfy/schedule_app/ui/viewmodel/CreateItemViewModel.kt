package com.bfy.schedule_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bfy.schedule_app.data.remote.model.ScheduleDto
import com.bfy.schedule_app.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CreateItemUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val categories: List<com.bfy.schedule_app.data.remote.model.CategoryDto> = emptyList(),
    val subTasks: List<String> = emptyList()
)

class CreateItemViewModel(private val repository: AppRepository = AppRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateItemUiState())
    val uiState: StateFlow<CreateItemUiState> = _uiState

    fun loadCategories() {
        viewModelScope.launch {
            try {
                val cats = repository.getCategories()
                _uiState.value = _uiState.value.copy(categories = cats)
            } catch (e: Exception) {}
        }
    }

    fun createItem(
        title: String,
        description: String,
        type: String,
        priority: String = "MEDIUM",
        startTime: String? = null,
        endTime: String? = null,
        deadline: String? = null,
        isAllDay: Boolean = false,
        recurrence: String? = null, // "Never", "Daily", "Weekly", "Monthly"
        reminders: List<String> = emptyList(),
        categoryName: String? = null,
        categoryColor: String? = null,
        isAlarm: Boolean = false,
        isCountdown: Boolean = false
    ) {
        viewModelScope.launch {
            _uiState.value = CreateItemUiState(isLoading = true)
            try {
                val schedule = ScheduleDto(
                    id = "", 
                    creator_id = "", 
                    title = title,
                    description = description,
                    type = type,
                    priority = priority,
                    start_time = startTime,
                    end_time = endTime,
                    deadline = deadline,
                    status = "PENDING",
                    is_all_day = isAllDay,
                    is_recurring = recurrence != null && recurrence != "Never",
                    recurrence_type = when(recurrence) {
                        "Daily" -> "DAILY"
                        "Weekly" -> "WEEKLY"
                        "Monthly" -> "MONTHLY"
                        "Mon-Fri" -> "MON_FRI"
                        "Yearly" -> "YEARLY"
                        else -> null
                    },
                    reminders = reminders.map {
                        com.bfy.schedule_app.data.remote.model.ReminderDto(
                            trigger_type = it,
                            is_alarm = isAlarm
                        )
                    },
                    category_name = categoryName,
                    category_color = categoryColor,
                    is_countdown_enabled = isCountdown
                )
                val createdSchedule = repository.createSchedule(schedule)
                
                if (isCountdown) {
                    val targetTime = deadline ?: endTime ?: startTime
                    if (targetTime != null) {
                        try {
                            val targetMillis = kotlinx.datetime.Instant.parse(
                                if (!targetTime.contains("T")) "${targetTime}T00:00:00Z" 
                                else if (!targetTime.endsWith("Z") && !targetTime.contains("+")) "${targetTime}Z" 
                                else targetTime
                            ).toEpochMilliseconds()
                            
                            com.bfy.schedule_app.platform.ScheduleNotifierProvider.notifier?.startCountdown(
                                createdSchedule.id, 
                                createdSchedule.title, 
                                targetMillis
                            )
                        } catch (e: Exception) { }
                    }
                }

                if (reminders.isNotEmpty() || isAlarm) {
                    val targetTime = deadline ?: startTime
                    if (targetTime != null) {
                        try {
                            val targetMillis = kotlinx.datetime.Instant.parse(
                                if (!targetTime.contains("T")) "${targetTime}T00:00:00Z" 
                                else if (!targetTime.endsWith("Z") && !targetTime.contains("+")) "${targetTime}Z" 
                                else targetTime
                            ).toEpochMilliseconds()
                            
                            if (reminders.isNotEmpty()) {
                                reminders.forEach { reminderKey ->
                                    val offsetMillis = com.bfy.schedule_app.platform.getReminderOffsetMillis(reminderKey)
                                    val triggerMillis = targetMillis - offsetMillis
                                    com.bfy.schedule_app.platform.ScheduleNotifierProvider.notifier?.scheduleAlarm(
                                        "${createdSchedule.id}_$reminderKey",
                                        createdSchedule.title,
                                        createdSchedule.description ?: "Task reminder",
                                        triggerMillis,
                                        isAlarm
                                    )
                                }
                            } else {
                                com.bfy.schedule_app.platform.ScheduleNotifierProvider.notifier?.scheduleAlarm(
                                    createdSchedule.id,
                                    createdSchedule.title,
                                    createdSchedule.description ?: "Task reminder",
                                    targetMillis,
                                    isAlarm
                                )
                            }
                        } catch (e: Exception) { }
                    }
                }

                _uiState.value = CreateItemUiState(isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = CreateItemUiState(isLoading = false, error = e.message ?: "Unknown error occurred")
            }
        }
    }

    fun updateItem(
        id: String,
        title: String,
        description: String,
        type: String,
        priority: String = "MEDIUM",
        startTime: String? = null,
        endTime: String? = null,
        deadline: String? = null,
        isAllDay: Boolean = false,
        recurrence: String? = null,
        reminders: List<String> = emptyList(),
        categoryName: String? = null,
        categoryColor: String? = null,
        isAlarm: Boolean = false,
        isCountdown: Boolean = false
    ) {
        viewModelScope.launch {
            _uiState.value = CreateItemUiState(isLoading = true)
            try {
                val isRecurring = recurrence != null && recurrence != "Never"
                val recurrenceType = if (recurrence != null) {
                    when(recurrence) {
                        "Daily" -> "DAILY"
                        "Weekly" -> "WEEKLY"
                        "Monthly" -> "MONTHLY"
                        "Mon-Fri" -> "MON_FRI"
                        "Yearly" -> "YEARLY"
                        else -> null
                    }
                } else null

                val updates = com.bfy.schedule_app.data.remote.model.UpdateScheduleRequest(
                    title = title,
                    description = description,
                    type = type,
                    priority = priority,
                    start_time = startTime,
                    end_time = endTime,
                    deadline = deadline,
                    is_all_day = isAllDay,
                    category_name = categoryName,
                    category_color = categoryColor,
                    is_recurring = isRecurring,
                    recurrence_type = recurrenceType,
                    is_countdown_enabled = isCountdown,
                    reminders = reminders.map {
                        com.bfy.schedule_app.data.remote.model.ReminderDto(
                            trigger_type = it,
                            is_alarm = isAlarm
                        )
                    }
                )
                repository.updateSchedule(id, updates)

                if (isCountdown) {
                    val targetTime = deadline ?: endTime ?: startTime
                    if (targetTime != null) {
                        try {
                            val targetMillis = kotlinx.datetime.Instant.parse(
                                if (!targetTime.contains("T")) "${targetTime}T00:00:00Z" 
                                else if (!targetTime.endsWith("Z") && !targetTime.contains("+")) "${targetTime}Z" 
                                else targetTime
                            ).toEpochMilliseconds()
                            
                            com.bfy.schedule_app.platform.ScheduleNotifierProvider.notifier?.startCountdown(
                                id, 
                                title, 
                                targetMillis
                            )
                        } catch (e: Exception) { }
                    }
                } else {
                    com.bfy.schedule_app.platform.ScheduleNotifierProvider.notifier?.stopCountdown(id)
                }

                // First cancel previous alarms (we can just cancel the main one, or let it get overwritten)
                // Actually, best to just overwrite with new values.
                if (reminders.isNotEmpty() || isAlarm) {
                    val targetTime = deadline ?: startTime
                    if (targetTime != null) {
                        try {
                            val targetMillis = kotlinx.datetime.Instant.parse(
                                if (!targetTime.contains("T")) "${targetTime}T00:00:00Z" 
                                else if (!targetTime.endsWith("Z") && !targetTime.contains("+")) "${targetTime}Z" 
                                else targetTime
                            ).toEpochMilliseconds()
                            
                            if (reminders.isNotEmpty()) {
                                reminders.forEach { reminderKey ->
                                    val offsetMillis = com.bfy.schedule_app.platform.getReminderOffsetMillis(reminderKey)
                                    val triggerMillis = targetMillis - offsetMillis
                                    com.bfy.schedule_app.platform.ScheduleNotifierProvider.notifier?.scheduleAlarm(
                                        "${id}_$reminderKey",
                                        title,
                                        description,
                                        triggerMillis,
                                        isAlarm
                                    )
                                }
                            } else {
                                com.bfy.schedule_app.platform.ScheduleNotifierProvider.notifier?.scheduleAlarm(
                                    id,
                                    title,
                                    description,
                                    targetMillis,
                                    isAlarm
                                )
                            }
                        } catch (e: Exception) { }
                    }
                }

                _uiState.value = CreateItemUiState(isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = CreateItemUiState(isLoading = false, error = e.message ?: "Update failed")
            }
        }
    }

    fun suggestSubTasks(title: String, description: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val suggestions = repository.aiBreakdownTask(title, description)
                _uiState.value = _uiState.value.copy(subTasks = suggestions, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun resetState() {
        _uiState.value = CreateItemUiState()
    }
}
