package com.bfy.schedule_app.ui.state

import com.bfy.schedule_app.ui.model.AssignmentItem
import com.bfy.schedule_app.ui.model.AuthMode
import com.bfy.schedule_app.ui.model.BadgeItem
import com.bfy.schedule_app.ui.model.GroupMember
import com.bfy.schedule_app.ui.model.GroupSummary
import com.bfy.schedule_app.ui.model.TimelineItem

data class AuthUiState(
    val mode: AuthMode = AuthMode.SIGN_IN,
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val fieldErrors: Map<String, String> = emptyMap()
)

sealed interface HomeContentState {
    data object Loading : HomeContentState
    data class Error(val message: String) : HomeContentState
    data object Empty : HomeContentState
    data class Data(val items: List<TimelineItem>) : HomeContentState
}

data class HomeUiState(
    val userName: String,
    val currentRank: String,
    val expCurrent: Int,
    val expTarget: Int,
    val isSelectionMode: Boolean = false,
    val selectedIds: Set<String> = emptySet(),
    val content: HomeContentState = HomeContentState.Loading
)

data class FocusUiState(
    val isRunning: Boolean = false,
    val minuteText: String = "25:00",
    val completedSessions: Int = 0,
    val warningMessage: String? = null
)

data class CollaborationUiState(
    val myGroups: List<GroupSummary> = emptyList(),
    val assignedItems: List<AssignmentItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class GroupDetailUiState(
    val groupId: String,
    val groupName: String,
    val members: List<GroupMember>,
    val sharedItems: List<TimelineItem>,
    val isLeader: Boolean
)

data class ProfileUiState(
    val fullName: String,
    val email: String,
    val bio: String,
    val timezone: String,
    val avatarUrl: String? = null,
    val rank: String,
    val totalExp: Int,
    val focusedMinutes: Int,
    val completedTasks: Int,
    val badges: List<BadgeItem>,
    val darkThemeEnabled: Boolean,
    val englishLanguageEnabled: Boolean
)
