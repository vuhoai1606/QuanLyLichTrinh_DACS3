package com.bfy.schedule_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bfy.schedule_app.data.remote.model.FocusStatsDto
import com.bfy.schedule_app.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FocusUiState(
    val stats: FocusStatsDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val timeLeft: Int = 25 * 60, // 25 minutes in seconds
    val isRunning: Boolean = false,
    val showStartConfirmation: Boolean = false,
    val showExitConfirmation: Boolean = false
)

class FocusViewModel(private val repository: AppRepository = AppRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow(FocusUiState())
    val uiState: StateFlow<FocusUiState> = _uiState
    private var timerJob: kotlinx.coroutines.Job? = null

    init {
        loadFocusData()
    }

    fun onStartFocusClick() {
        if (!_uiState.value.isRunning) {
            _uiState.update { it.copy(showStartConfirmation = true) }
        } else {
            // If already running, maybe show exit confirmation if they try to pause?
            // The user said: "khi người dùng mà thoát khỏi app thì nó sẽ hiển thị thông báo"
            // For now, let's keep pause simple or follow the requirement.
            _uiState.update { it.copy(showExitConfirmation = true) }
        }
    }

    fun confirmStartFocus() {
        _uiState.update { it.copy(showStartConfirmation = false) }
        startTimer()
    }

    fun cancelStartFocus() {
        _uiState.update { it.copy(showStartConfirmation = false) }
    }

    fun confirmExitFocus() {
        _uiState.update { it.copy(showExitConfirmation = false) }
        resetTimer()
    }

    fun cancelExitFocus() {
        _uiState.update { it.copy(showExitConfirmation = false) }
    }

    fun triggerExitConfirmation() {
        if (_uiState.value.isRunning) {
            _uiState.update { it.copy(showExitConfirmation = true) }
        }
    }

    private fun startTimer() {
        _uiState.update { it.copy(isRunning = true) }
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeLeft > 0) {
                kotlinx.coroutines.delay(1000)
                _uiState.update { it.copy(timeLeft = it.timeLeft - 1) }
            }
            _uiState.update { it.copy(isRunning = false) }
            
            // Record completed session to BE
            try {
                repository.createFocusSession(25, "COMPLETED")
                loadFocusData() // Refresh stats
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to save session: ${e.message}") }
            }
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(isRunning = false) }
    }

    fun resetTimer() {
        pauseTimer()
        _uiState.update { it.copy(timeLeft = 25 * 60) }
    }

    fun loadFocusData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val stats = repository.getFocusStats()
                _uiState.update { it.copy(stats = stats, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load focus data: ${e.message}"
                    )
                }
            }
        }
    }
}
