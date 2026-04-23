package com.bfy.schedule_app.ui.data

import com.bfy.schedule_app.ui.model.AssignmentItem
import com.bfy.schedule_app.ui.model.BadgeItem
import com.bfy.schedule_app.ui.model.GroupMember
import com.bfy.schedule_app.ui.model.GroupSummary
import com.bfy.schedule_app.ui.model.TimelineItem
import com.bfy.schedule_app.ui.model.TimelineStatus
import com.bfy.schedule_app.ui.model.TimelineType
import com.bfy.schedule_app.ui.state.CollaborationUiState
import com.bfy.schedule_app.ui.state.GroupDetailUiState
import com.bfy.schedule_app.ui.state.HomeContentState
import com.bfy.schedule_app.ui.state.HomeUiState
import com.bfy.schedule_app.ui.state.ProfileUiState
import kotlin.time.Instant

object FakeBfyData {
    const val hasValidToken: Boolean = false

    val timelineItems = listOf(
        TimelineItem(
            id = "t1",
            title = "Math homework chapter 5",
            description = "Finish exercises 1 to 20 before class.",
            type = TimelineType.TASK,
            status = TimelineStatus.DOING,
            categoryName = "Study",
            categoryColorHex = "#1768D0",
            isGroupItem = false,
            deadlineAt = Instant.parse("2026-04-24T12:30:00Z")
        ),
        TimelineItem(
            id = "t2",
            title = "Read 10 pages of clean code",
            description = "Keep notes for one key lesson.",
            type = TimelineType.TODO,
            status = TimelineStatus.PENDING,
            categoryName = "Self-growth",
            categoryColorHex = "#5E60CE",
            isGroupItem = false
        ),
        TimelineItem(
            id = "t3",
            title = "Weekly project sync",
            description = "Review API contracts and next sprint milestones.",
            type = TimelineType.EVENT,
            status = TimelineStatus.PENDING,
            categoryName = "Team",
            categoryColorHex = "#B45309",
            isGroupItem = true,
            startAt = Instant.parse("2026-04-24T08:00:00Z"),
            endAt = Instant.parse("2026-04-24T09:00:00Z")
        ),
        TimelineItem(
            id = "t4",
            title = "Pomodoro review notes",
            description = "Write three blockers and one action item.",
            type = TimelineType.TODO,
            status = TimelineStatus.DONE,
            categoryName = "Focus",
            categoryColorHex = "#0E7C63",
            isGroupItem = false
        )
    )

    val groupList = listOf(
        GroupSummary(id = "g1", name = "DACS Team A", memberCount = 5, isLeader = true),
        GroupSummary(id = "g2", name = "Algorithms Lab", memberCount = 4, isLeader = false)
    )

    val assignments = listOf(
        AssignmentItem(
            id = "a1",
            title = "Prepare architecture slides",
            description = "Summarize KMP module boundaries and API contracts.",
            dueText = "Due in 1 day",
            assignedBy = "Hoai Vu",
            type = TimelineType.TASK
        ),
        AssignmentItem(
            id = "a2",
            title = "Attend sprint demo",
            description = "Show progress for focus and profile screens.",
            dueText = "Today 16:00",
            assignedBy = "Nora",
            type = TimelineType.EVENT
        )
    )

    val groupMembers = listOf(
        GroupMember(id = "m1", name = "Hoai Vu", role = "Leader", isOnline = true),
        GroupMember(id = "m2", name = "Linh Tran", role = "Member", isOnline = true),
        GroupMember(id = "m3", name = "Khoa Nguyen", role = "Member", isOnline = false),
        GroupMember(id = "m4", name = "Minh Le", role = "Member", isOnline = true)
    )

    val badges = listOf(
        BadgeItem(id = "b1", title = "First Focus", isUnlocked = true),
        BadgeItem(id = "b2", title = "Task Finisher", isUnlocked = true),
        BadgeItem(id = "b3", title = "No Delay Week", isUnlocked = false),
        BadgeItem(id = "b4", title = "Team Anchor", isUnlocked = false)
    )

    val homeState = HomeUiState(
        userName = "Hoai",
        currentRank = "Silver",
        expCurrent = 670,
        expTarget = 1000,
        content = if (timelineItems.isEmpty()) HomeContentState.Empty else HomeContentState.Data(timelineItems)
    )

    val collaborationState = CollaborationUiState(
        myGroups = groupList,
        assignedItems = assignments
    )

    fun groupDetailState(groupId: String): GroupDetailUiState {
        val selectedGroup = groupList.firstOrNull { it.id == groupId } ?: groupList.first()
        return GroupDetailUiState(
            groupId = selectedGroup.id,
            groupName = selectedGroup.name,
            members = groupMembers,
            sharedItems = timelineItems.filter { it.isGroupItem },
            isLeader = selectedGroup.isLeader
        )
    }

    val profileState = ProfileUiState(
        fullName = "Hoai Vu",
        email = "hoai.vu@bfy.app",
        bio = "Building BFY one clean screen at a time.",
        timezone = "Asia/Ho_Chi_Minh",
        rank = "Silver",
        totalExp = 670,
        focusedMinutes = 320,
        completedTasks = 48,
        badges = badges,
        darkThemeEnabled = false,
        englishLanguageEnabled = true
    )
}
