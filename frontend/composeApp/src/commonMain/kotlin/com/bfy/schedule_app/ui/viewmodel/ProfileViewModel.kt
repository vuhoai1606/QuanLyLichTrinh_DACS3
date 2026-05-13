package com.bfy.schedule_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bfy.schedule_app.data.remote.model.UserDto
import com.bfy.schedule_app.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: UserDto? = null,
    val focusStats: com.bfy.schedule_app.data.remote.model.FocusStatsDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ProfileViewModel(private val repository: AppRepository = AppRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val user = repository.getCurrentUser()
                val focusStats = try {
                    repository.getFocusStats()
                } catch (e: Exception) {
                    null // Optional, don't fail profile load if stats fail
                }
                _uiState.value = ProfileUiState(user = user, focusStats = focusStats, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load profile: ${e.message}"
                )
            }
        }
    }
}
