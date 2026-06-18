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
    val timeLeft: Int = 25 * 60,
    val targetMinutes: Int = 25,
    val presetMinutes: List<Int> = listOf(15, 25, 45, 60, 90),
    val isRunning: Boolean = false,
    val showStartConfirmation: Boolean = false,
    val showExitConfirmation: Boolean = false,
    val ambientSounds: List<com.bfy.schedule_app.data.remote.model.AmbientSoundDto> = emptyList(),
    val selectedSound: com.bfy.schedule_app.data.remote.model.AmbientSoundDto? = null
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
        giveUpSession()
    }

    fun cancelExitFocus() {
        _uiState.update { it.copy(showExitConfirmation = false) }
    }

    fun triggerExitConfirmation() {
        // No-op or we can keep it as is, but UI will not use it
    }

    fun giveUpSession() {
        pauseTimer()
        val target = _uiState.value.targetMinutes
        _uiState.update { it.copy(isRunning = false, timeLeft = target * 60) }
        viewModelScope.launch {
            try {
                repository.createFocusSession(target, "FAILED", isStrictMode = true)
                loadFocusData()
            } catch (e: Exception) {}
        }
    }

    fun sessionCompleted() {
        pauseTimer()
        val target = _uiState.value.targetMinutes
        _uiState.update { it.copy(isRunning = false, timeLeft = target * 60) }
        viewModelScope.launch {
            try {
                repository.createFocusSession(target, "COMPLETED", isStrictMode = true)
                loadFocusData()
            } catch (e: Exception) {}
        }
    }

    private fun startTimer() {
        _uiState.update { it.copy(isRunning = true) }
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeLeft > 0) {
                kotlinx.coroutines.delay(1000)
                _uiState.update { it.copy(timeLeft = it.timeLeft - 1) }
            }
            sessionCompleted()
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(isRunning = false) }
    }

    fun resetTimer() {
        pauseTimer()
        _uiState.update { it.copy(timeLeft = _uiState.value.targetMinutes * 60) }
    }

    fun updateTimeLeft(seconds: Int) {
        timerJob?.cancel()
        _uiState.update { it.copy(timeLeft = seconds) }
        startTimer()
    }

    fun setFocusTime(minutes: Int) {
        val validMinutes = if (minutes < 1) 1 else if (minutes > 1440) 1440 else minutes // Limit between 1 min and 24h
        if (!_uiState.value.isRunning) {
            _uiState.update { it.copy(targetMinutes = validMinutes, timeLeft = validMinutes * 60) }
        }
    }

    fun incrementTime() {
        if (!_uiState.value.isRunning) {
            setFocusTime(_uiState.value.targetMinutes + 5)
        }
    }

    fun decrementTime() {
        if (!_uiState.value.isRunning) {
            setFocusTime(_uiState.value.targetMinutes - 5)
        }
    }

    fun loadFocusData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val stats = repository.getFocusStats()
                val sounds = repository.getAmbientSounds()
                _uiState.update { it.copy(stats = stats, ambientSounds = sounds, isLoading = false) }
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

    fun selectSound(sound: com.bfy.schedule_app.data.remote.model.AmbientSoundDto?) {
        _uiState.update { it.copy(selectedSound = sound) }
    }
}
