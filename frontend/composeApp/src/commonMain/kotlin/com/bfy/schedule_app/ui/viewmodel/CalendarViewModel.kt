package com.bfy.schedule_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bfy.schedule_app.data.remote.model.ScheduleDto
import com.bfy.schedule_app.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.*

data class CalendarUiState(
    val schedules: List<ScheduleDto> = emptyList(),
    val selectedDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val isLoading: Boolean = false,
    val error: String? = null
)

class CalendarViewModel(private val repository: AppRepository = AppRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState

    init {
        loadSchedules()
    }

    fun loadSchedules() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val schedules = repository.getSchedules()
                _uiState.value = _uiState.value.copy(schedules = schedules, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load calendar: ${e.message}"
                )
            }
        }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
    }

    fun nextMonth() {
        val current = _uiState.value.selectedDate
        val next = if (current.monthNumber == 12) {
            LocalDate(current.year + 1, 1, 1)
        } else {
            LocalDate(current.year, current.monthNumber + 1, 1)
        }
        _uiState.value = _uiState.value.copy(selectedDate = next)
    }

    fun prevMonth() {
        val current = _uiState.value.selectedDate
        val prev = if (current.monthNumber == 1) {
            LocalDate(current.year - 1, 12, 1)
        } else {
            LocalDate(current.year, current.monthNumber - 1, 1)
        }
        _uiState.value = _uiState.value.copy(selectedDate = prev)
    }

    fun nextDay() {
        val current = _uiState.value.selectedDate
        val next = current.plus(1, DateTimeUnit.DAY)
        _uiState.value = _uiState.value.copy(selectedDate = next)
    }

    fun prevDay() {
        val current = _uiState.value.selectedDate
        val prev = current.minus(1, DateTimeUnit.DAY)
        _uiState.value = _uiState.value.copy(selectedDate = prev)
    }

    fun nextWeek() {
        val current = _uiState.value.selectedDate
        val next = current.plus(1, DateTimeUnit.WEEK)
        _uiState.value = _uiState.value.copy(selectedDate = next)
    }

    fun prevWeek() {
        val current = _uiState.value.selectedDate
        val prev = current.minus(1, DateTimeUnit.WEEK)
        _uiState.value = _uiState.value.copy(selectedDate = prev)
    }

    fun nextYear() {
        val current = _uiState.value.selectedDate
        val next = current.plus(1, DateTimeUnit.YEAR)
        _uiState.value = _uiState.value.copy(selectedDate = next)
    }

    fun prevYear() {
        val current = _uiState.value.selectedDate
        val prev = current.minus(1, DateTimeUnit.YEAR)
        _uiState.value = _uiState.value.copy(selectedDate = prev)
    }
}
