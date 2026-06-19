package com.bfy.schedule_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bfy.schedule_app.data.remote.model.GroupTaskDto
import com.bfy.schedule_app.data.remote.model.ReminderDto
import com.bfy.schedule_app.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class GroupDetailUiState(
    val group: com.bfy.schedule_app.data.remote.model.GroupDto? = null,
    val tasks: List<GroupTaskDto> = emptyList(),
    val members: List<com.bfy.schedule_app.data.remote.model.GroupMemberDto> = emptyList(),
    val messages: List<com.bfy.schedule_app.data.remote.model.ChatMessageDto> = emptyList(),
    val currentUser: com.bfy.schedule_app.data.remote.model.UserDto? = null,
    val searchResults: List<com.bfy.schedule_app.data.remote.model.UserDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class GroupDetailViewModel(private val repository: AppRepository = AppRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState

    private var currentGroupId: String? = null

    init {
        viewModelScope.launch {
            com.bfy.schedule_app.data.remote.api.WebSocketManager.events.collect { event ->
                if (event.type == "GROUP_TASKS_UPDATED" && event.groupId == currentGroupId) {
                    currentGroupId?.let { loadTasks(it) }
                }
            }
        }
    }

    fun loadTasks(groupId: String) {
        currentGroupId = groupId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Fetch tasks
                val tasks = repository.getGroupTasks(groupId)
                val members = repository.getGroupMembers(groupId)
                
                // Fetch group info
                val groups = repository.getGroups()
                val group = groups.find { it.id == groupId }
                
                val currentUser = repository.getCurrentUser()
                
                _uiState.value = GroupDetailUiState(
                    group = group, 
                    tasks = tasks, 
                    members = members,
                    currentUser = currentUser,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load tasks: ${e.message}"
                )
            }
        }
    }

    fun createSchedule(
        groupId: String,
        type: String,
        title: String,
        description: String?,
        assignees: List<String>,
        priority: String = "MEDIUM",
        startTime: String? = null,
        endTime: String? = null,
        deadline: String? = null,
        isAllDay: Boolean = false,
        recurrenceType: String? = null,
        reminders: List<String> = emptyList(),
        categoryName: String? = null,
        isAlarm: Boolean = false,
        isCountdown: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                val createdSchedule = repository.createSchedule(
                    com.bfy.schedule_app.data.remote.model.ScheduleDto(
                        id = "",
                        creator_id = _uiState.value.currentUser?.id ?: "",
                        title = title,
                        description = description,
                        type = type,
                        status = "PENDING",
                        priority = priority,
                        group_id = groupId,
                        assignees = assignees,
                        start_time = startTime,
                        end_time = endTime,
                        deadline = deadline,
                        is_all_day = isAllDay,
                        is_recurring = recurrenceType != null && recurrenceType != "Never",
                        recurrence_type = recurrenceType,
                        reminders = reminders.map {
                            ReminderDto(trigger_type = it, is_alarm = isAlarm)
                        },
                        category_name = categoryName,
                        is_countdown_enabled = isCountdown
                    )
                )

                if (isCountdown) {
                    val targetTime = startTime ?: deadline ?: endTime
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
                    val targetTime = startTime ?: deadline ?: endTime
                    if (targetTime != null) {
                        try {
                            val targetMillis = kotlinx.datetime.Instant.parse(
                                if (!targetTime.contains("T")) "${targetTime}T00:00:00Z" 
                                else if (!targetTime.endsWith("Z") && !targetTime.contains("+")) "${targetTime}Z" 
                                else targetTime
                            ).toEpochMilliseconds()
                            
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
                        } catch (e: Exception) { }
                    }
                } else if (isAlarm) {
                    val targetTime = startTime ?: deadline ?: endTime
                    if (targetTime != null) {
                        try {
                            val targetMillis = kotlinx.datetime.Instant.parse(
                                if (!targetTime.contains("T")) "${targetTime}T00:00:00Z" 
                                else if (!targetTime.endsWith("Z") && !targetTime.contains("+")) "${targetTime}Z" 
                                else targetTime
                            ).toEpochMilliseconds()
                            
                            val triggerMillis = targetMillis
                            com.bfy.schedule_app.platform.ScheduleNotifierProvider.notifier?.scheduleAlarm(
                                createdSchedule.id,
                                createdSchedule.title,
                                createdSchedule.description ?: "Task reminder",
                                triggerMillis,
                                isAlarm
                            )
                        } catch (e: Exception) { }
                    }
                }

                loadTasks(groupId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateGroupSettings(groupId: String, name: String, avatarUrl: String?) {
        viewModelScope.launch {
            try {
                repository.updateGroupInfo(groupId, name, null, avatarUrl)
                loadTasks(groupId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteSchedule(groupId: String, scheduleId: String) {
        viewModelScope.launch {
            try {
                repository.deleteSchedule(scheduleId)
                loadTasks(groupId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun removeMember(groupId: String, userId: String) {
        viewModelScope.launch {
            try {
                repository.removeGroupMember(groupId, userId)
                loadTasks(groupId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun addMember(groupId: String, userId: String, role: String) {
        viewModelScope.launch {
            try {
                repository.addGroupMember(groupId, userId, role)
                loadTasks(groupId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun searchUsers(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _uiState.value = _uiState.value.copy(searchResults = emptyList())
                return@launch
            }
            try {
                val results = repository.searchUsers(query)
                _uiState.value = _uiState.value.copy(searchResults = results)
            } catch (e: Exception) { }
        }
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(searchResults = emptyList())
    }

    fun saveGroupChanges(
        groupId: String,
        name: String,
        avatarUrl: String?,
        membersToAdd: List<Pair<String, String>>, // userId to role
        membersToRemove: List<String>, // userIds
        membersToUpdateRole: List<Pair<String, String>> // userId to role
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Update name if changed
                if (name != _uiState.value.group?.name) {
                    repository.updateGroupInfo(groupId, name, null, avatarUrl)
                }

                // Add members
                for (member in membersToAdd) {
                    repository.addGroupMember(groupId, member.first, member.second)
                }

                // Remove members
                for (userId in membersToRemove) {
                    repository.removeGroupMember(groupId, userId)
                }

                // Update roles
                for (member in membersToUpdateRole) {
                    try {
                        repository.updateGroupMemberRole(groupId, member.first, member.second)
                    } catch(e: Exception) {
                        // Backend might not support it yet, ignore for now
                    }
                }

                loadTasks(groupId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
    fun loadMessages(groupId: String) {
        viewModelScope.launch {
            try {
                val msgs = repository.getChatMessages(groupId)
                _uiState.value = _uiState.value.copy(messages = msgs)
            } catch (e: Exception) {}
        }
    }

    fun sendMessage(groupId: String, message: String) {
        viewModelScope.launch {
            try {
                repository.sendChatMessage(groupId, message)
                loadMessages(groupId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
