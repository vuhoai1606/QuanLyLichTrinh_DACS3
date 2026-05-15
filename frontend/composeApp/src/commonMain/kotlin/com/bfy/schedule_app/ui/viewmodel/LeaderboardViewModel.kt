package com.bfy.schedule_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bfy.schedule_app.data.remote.model.LeaderboardEntryDto
import com.bfy.schedule_app.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LeaderboardUiState(
    val entries: List<LeaderboardEntryDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class LeaderboardViewModel(private val repository: AppRepository = AppRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState

    fun loadLeaderboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val entries = repository.getLeaderboard()
                _uiState.value = LeaderboardUiState(entries = entries, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
