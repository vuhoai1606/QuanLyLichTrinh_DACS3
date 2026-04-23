package com.bfy.schedule_app.ui.model

import kotlin.time.Instant

enum class MainTab {
    HOME,
    FOCUS,
    COLLAB,
    PROFILE
}

enum class AuthMode {
    SIGN_IN,
    SIGN_UP
}

enum class TimelineType {
    TODO,
    TASK,
    EVENT
}

enum class TimelineStatus {
    PENDING,
    DOING,
    DONE,
    OVERDUE
}

data class TimelineItem(
    val id: String,
    val title: String,
    val description: String,
    val type: TimelineType,
    val status: TimelineStatus,
    val categoryName: String,
    val categoryColorHex: String,
    val isGroupItem: Boolean,
    val startAt: Instant? = null,
    val endAt: Instant? = null,
    val deadlineAt: Instant? = null
)

data class GroupSummary(
    val id: String,
    val name: String,
    val memberCount: Int,
    val isLeader: Boolean
)

data class GroupMember(
    val id: String,
    val name: String,
    val role: String,
    val isOnline: Boolean
)

data class AssignmentItem(
    val id: String,
    val title: String,
    val description: String,
    val dueText: String,
    val assignedBy: String,
    val type: TimelineType
)

data class BadgeItem(
    val id: String,
    val title: String,
    val isUnlocked: Boolean
)
