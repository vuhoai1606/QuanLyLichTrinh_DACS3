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

    fun goToToday() {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        _uiState.value = _uiState.value.copy(selectedDate = today)
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

    fun syncExternalCalendar(context: Any) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val user = repository.getCurrentUser()
                val email = user.email ?: throw Exception("User email not found. Cannot sync calendar.")
                val events = com.bfy.schedule_app.platform.CalendarSyncManager.getNativeCalendarEvents(context, email)
                
                if (events.isEmpty()) {
                    throw Exception("No events found for account: $email")
                }
                
                repository.syncCalendar(events)
                loadSchedules() // Reload to get newly synced events
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Sync failed: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun syncGoogleTwoWay(token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val googleApi = com.bfy.schedule_app.data.remote.api.GoogleApiService()
                val externalEvents = mutableListOf<com.bfy.schedule_app.data.remote.model.ExternalEventDto>()
                
                val googleEventsMap = mutableMapOf<String, com.bfy.schedule_app.data.remote.api.GoogleEvent>()
                val googleTasksMap = mutableMapOf<String, Pair<String, com.bfy.schedule_app.data.remote.api.GoogleTask>>()

                // 1. Fetch Events from Google Calendar
                try {
                    val events = googleApi.getEvents(token)
                    events.forEach { e ->
                        if (e.id != null && e.start != null) {
                            googleEventsMap[e.id] = e
                            val startTime = e.start.dateTime ?: e.start.date ?: return@forEach
                            val endTime = e.end?.dateTime ?: e.end?.date ?: startTime
                            val isAllDay = e.start.dateTime == null
                            externalEvents.add(
                                com.bfy.schedule_app.data.remote.model.ExternalEventDto(
                                    title = e.summary,
                                    description = e.description,
                                    start_time = startTime,
                                    end_time = endTime,
                                    is_all_day = isAllDay,
                                    type = "EVENT",
                                    external_id = e.id,
                                    external_source = "GOOGLE_CALENDAR",
                                    updated_at = e.updated
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    println("Failed to fetch Google Events: ${e.message}")
                }

                // 2. Fetch Tasks from Google Tasks
                try {
                    val taskLists = googleApi.getTaskLists(token)
                    for (list in taskLists) {
                        val tasks = googleApi.getTasks(token, list.id)
                        tasks.forEach { t ->
                            if (t.id != null) {
                                googleTasksMap[t.id] = Pair(list.id, t)
                                val due = t.due ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString()
                                externalEvents.add(
                                    com.bfy.schedule_app.data.remote.model.ExternalEventDto(
                                        title = t.title,
                                        description = t.notes,
                                        start_time = due,
                                        end_time = due,
                                        is_all_day = false,
                                        type = "TASK",
                                        external_id = t.id,
                                        external_source = "GOOGLE_TASKS",
                                        updated_at = t.updated
                                    )
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("Failed to fetch Google Tasks: ${e.message}")
                }

                // 3. Sync to BFY Backend (pulls new/updated things from Google)
                if (externalEvents.isNotEmpty()) {
                    repository.syncCalendar(externalEvents)
                }

                // 4. Push BFY items to Google (Two-Way)
                val currentSchedules = repository.getSchedules()
                var pushCount = 0
                
                for (schedule in currentSchedules) {
                    try {
                        if (schedule.external_id == null) {
                            // NEW in BFY -> push to Google
                            if (schedule.type == "TASK" || schedule.type == "TODO") {
                                val newTask = googleApi.createTask(token, "@default", com.bfy.schedule_app.data.remote.api.GoogleTask(
                                    title = schedule.title,
                                    notes = schedule.description,
                                    due = schedule.deadline ?: schedule.start_time
                                ))
                                repository.updateSchedule(schedule.id, com.bfy.schedule_app.data.remote.model.UpdateScheduleRequest(
                                    external_id = newTask.id,
                                    external_source = "GOOGLE_TASKS"
                                ))
                            } else {
                                val newEvent = googleApi.createEvent(token, "primary", com.bfy.schedule_app.data.remote.api.GoogleEvent(
                                    summary = schedule.title,
                                    description = schedule.description,
                                    start = com.bfy.schedule_app.data.remote.api.GoogleEventDateTime(dateTime = schedule.start_time),
                                    end = com.bfy.schedule_app.data.remote.api.GoogleEventDateTime(dateTime = schedule.end_time ?: schedule.start_time)
                                ))
                                repository.updateSchedule(schedule.id, com.bfy.schedule_app.data.remote.model.UpdateScheduleRequest(
                                    external_id = newEvent.id,
                                    external_source = "GOOGLE_CALENDAR"
                                ))
                            }
                            pushCount++
                        } else {
                            // Already linked. Check which is newer.
                            val bUpdated = schedule.updated_at?.let { Instant.parse(it) } ?: Instant.DISTANT_PAST
                            var gUpdated = Instant.DISTANT_FUTURE // default to not push if we can't tell
                            var shouldPush = false
                            
                            if (schedule.external_source == "GOOGLE_CALENDAR") {
                                val gEvent = googleEventsMap[schedule.external_id]
                                if (gEvent != null) {
                                    gUpdated = gEvent.updated?.let { Instant.parse(it) } ?: Instant.DISTANT_PAST
                                    shouldPush = bUpdated > gUpdated
                                    if (shouldPush) {
                                        googleApi.updateEvent(token, "primary", com.bfy.schedule_app.data.remote.api.GoogleEvent(
                                            id = schedule.external_id,
                                            summary = schedule.title,
                                            description = schedule.description,
                                            start = com.bfy.schedule_app.data.remote.api.GoogleEventDateTime(dateTime = schedule.start_time),
                                            end = com.bfy.schedule_app.data.remote.api.GoogleEventDateTime(dateTime = schedule.end_time ?: schedule.start_time)
                                        ))
                                        pushCount++
                                    }
                                }
                            } else if (schedule.external_source == "GOOGLE_TASKS") {
                                val gTaskInfo = googleTasksMap[schedule.external_id]
                                if (gTaskInfo != null) {
                                    gUpdated = gTaskInfo.second.updated?.let { Instant.parse(it) } ?: Instant.DISTANT_PAST
                                    shouldPush = bUpdated > gUpdated
                                    if (shouldPush) {
                                        googleApi.updateTask(token, gTaskInfo.first, com.bfy.schedule_app.data.remote.api.GoogleTask(
                                            id = schedule.external_id,
                                            title = schedule.title,
                                            notes = schedule.description,
                                            due = schedule.deadline ?: schedule.start_time
                                        ))
                                        pushCount++
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        println("Failed to push schedule ${schedule.id} to Google: ${e.message}")
                    }
                }

                // Reload final state
                loadSchedules()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Đồng bộ thành công! Kéo về ${externalEvents.size} mục, đẩy lên Google $pushCount mục." 
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Lỗi đồng bộ Google: ${e.message}"
                )
            }
        }
    }
}
