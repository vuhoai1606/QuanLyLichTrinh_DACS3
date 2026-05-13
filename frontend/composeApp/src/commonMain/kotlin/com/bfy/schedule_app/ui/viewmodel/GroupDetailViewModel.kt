package com.bfy.schedule_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bfy.schedule_app.data.remote.model.GroupTaskDto
import com.bfy.schedule_app.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class GroupDetailUiState(
    val group: com.bfy.schedule_app.data.remote.model.GroupDto? = null,
    val tasks: List<GroupTaskDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class GroupDetailViewModel(private val repository: AppRepository = AppRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState

    fun loadTasks(groupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Fetch tasks
                val tasks = repository.getGroupTasks(groupId)
                
                // Fetch group info (by finding it in the user's groups)
                val groups = repository.getGroups()
                val group = groups.find { it.id == groupId }
                
                _uiState.value = GroupDetailUiState(group = group, tasks = tasks, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load tasks: ${e.message}"
                )
            }
        }
    }
}
