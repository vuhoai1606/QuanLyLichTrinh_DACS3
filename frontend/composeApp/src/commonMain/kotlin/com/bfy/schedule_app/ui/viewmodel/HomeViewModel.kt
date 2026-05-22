package com.bfy.schedule_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bfy.schedule_app.data.remote.model.ScheduleDto
import com.bfy.schedule_app.data.remote.model.UserDto
import com.bfy.schedule_app.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*

data class HomeUiState(
    val user: UserDto? = null,
    val schedules: List<ScheduleDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HomeViewModel(private val repository: AppRepository = AppRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadDashboardData()
        observeRealtimeUpdates()
    }

    private fun observeRealtimeUpdates() {
        viewModelScope.launch {
            val token = com.bfy.schedule_app.data.remote.api.ApiClient.authToken ?: return@launch
            try {
                com.bfy.schedule_app.data.remote.api.ApiClient.client.webSocket(
                    com.bfy.schedule_app.data.remote.api.ApiClient.getWsUrl(token)
                ) {
                    for (frame in incoming) {
                        if (frame is io.ktor.websocket.Frame.Text) {
                            val text = frame.readText()
                            // When a realtime update happens (e.g. task updated), reload data
                            loadDashboardData()
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle socket error (silent retry or log)
            }
        }
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val user = repository.getCurrentUser()
                val schedules = repository.getSchedules()
                _uiState.value = HomeUiState(user = user, schedules = schedules, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load dashboard: ${e.message}"
                )
            }
        }
    }

    fun searchSchedules(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                loadDashboardData()
                return@launch
            }
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val results = repository.searchSchedules(query)
                _uiState.value = _uiState.value.copy(schedules = results, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
