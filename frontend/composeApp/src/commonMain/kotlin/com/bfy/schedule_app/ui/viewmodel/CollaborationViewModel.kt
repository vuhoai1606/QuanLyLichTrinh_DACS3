package com.bfy.schedule_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bfy.schedule_app.data.remote.model.GroupDto
import com.bfy.schedule_app.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CollaborationUiState(
    val groups: List<GroupDto> = emptyList(),
    val sharedSchedules: List<com.bfy.schedule_app.data.remote.model.ScheduleDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class CollaborationViewModel(private val repository: AppRepository = AppRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow(CollaborationUiState())
    val uiState: StateFlow<CollaborationUiState> = _uiState

    init {
        loadGroups()
    }

    fun loadGroups() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val groups = repository.getGroups()
                val shared = try { repository.getSharedWithMe() } catch (e: Exception) { emptyList() }
                _uiState.value = CollaborationUiState(
                    groups = groups, 
                    sharedSchedules = shared,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load groups: ${e.message}"
                )
            }
        }
    }

    fun createGroup(name: String, description: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                repository.createGroup(name, description)
                loadGroups()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to create group: ${e.message}"
                )
            }
        }
    }
}
