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
    val categories: List<com.bfy.schedule_app.data.remote.model.CategoryDto> = emptyList()
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
        reminders: List<String> = emptyList()
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
                        "Monthly" -> "MONTHLY"
                        else -> null
                    },
                    reminders = reminders.map {
                        com.bfy.schedule_app.data.remote.model.ReminderDto(
                            trigger_type = it
                        )
                    }
                )
                repository.createSchedule(schedule)
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
        reminders: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _uiState.value = CreateItemUiState(isLoading = true)
            try {
                val updates = mutableMapOf<String, Any?>(
                    "title" to title,
                    "description" to description,
                    "type" to type,
                    "priority" to priority,
                    "start_time" to startTime,
                    "end_time" to endTime,
                    "deadline" to deadline,
                    "is_all_day" to isAllDay
                )
                if (recurrence != null) {
                    updates["is_recurring"] = recurrence != "Never"
                    updates["recurrence_type"] = when(recurrence) {
                        "Daily" -> "DAILY"
                        "Weekly" -> "WEEKLY"
                        "Monthly" -> "MONTHLY"
                        else -> null
                    }
                }
                repository.updateSchedule(id, updates)
                _uiState.value = CreateItemUiState(isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = CreateItemUiState(isLoading = false, error = e.message ?: "Update failed")
            }
        }
    }

    fun resetState() {
        _uiState.value = CreateItemUiState()
    }
}
