package com.bfy.schedule_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bfy.schedule_app.data.remote.model.FocusStatsDto
import com.bfy.schedule_app.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class FocusUiState(
    val stats: FocusStatsDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val timeLeft: Int = 25 * 60, // 25 minutes in seconds
    val isRunning: Boolean = false
)

class FocusViewModel(private val repository: AppRepository = AppRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow(FocusUiState())
    val uiState: StateFlow<FocusUiState> = _uiState
    private var timerJob: kotlinx.coroutines.Job? = null

    init {
        loadFocusData()
    }

    fun toggleTimer() {
        if (_uiState.value.isRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        _uiState.value = _uiState.value.copy(isRunning = true)
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeLeft > 0) {
                kotlinx.coroutines.delay(1000)
                _uiState.value = _uiState.value.copy(timeLeft = _uiState.value.timeLeft - 1)
            }
            _uiState.value = _uiState.value.copy(isRunning = false)
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(isRunning = false)
    }

    fun resetTimer() {
        pauseTimer()
        _uiState.value = _uiState.value.copy(timeLeft = 25 * 60)
    }

    fun loadFocusData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val stats = repository.getFocusStats()
                _uiState.value = FocusUiState(stats = stats, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    stats = FocusStatsDto(12, 340),
                    error = "Note: Using mock data (${e.message})"
                )
            }
        }
    }
}
