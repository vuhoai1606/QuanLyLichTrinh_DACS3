package com.bfy.schedule_app.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class Language(val code: String, val label: String) {
    ENGLISH("en", "English"),
    VIETNAMESE("vi", "Tiếng Việt")
}

object Localization {
    var currentLanguage by mutableStateOf(Language.ENGLISH)

    private val translations = mapOf(
        Language.ENGLISH to mapOf(
            "app_settings" to "APP SETTINGS",
            "language" to "Language",
            "dark_theme" to "Dark Theme",
            "notifications" to "Notifications",
            "focus_reminders" to "Focus Reminders",
            "logout" to "LOGOUT",
            "focus_stats" to "FOCUS STATS",
            "badge_board" to "BADGE BOARD",
            "total_focus_time" to "Total Focus Time",
            "day_streak" to "Day Streak",
            "daily_avg" to "Daily Avg",
            "view_all" to "View All",
            "hello" to "Hello",
            "rank" to "Rank",
            "level" to "Level",
            "today" to "Today",
            "no_activities" to "No activities planned for today.",
            "create_item" to "Create Item",
            "title" to "Title",
            "description" to "Description",
            "category" to "Category",
            "save" to "Save",
            "cancel" to "Cancel",
            "day" to "Day",
            "week" to "Week",
            "month" to "Month",
            "year" to "Year",
            "collaboration" to "Collaboration",
            "my_groups" to "My Groups",
            "assigned_to_me" to "Assigned to me",
            "active_groups" to "Active Groups",
            "no_groups" to "No active groups found.",
            "deep_work" to "Deep Work Session",
            "today_goal" to "Today's Goal",
            "start_focus" to "Start Focus",
            "pause_focus" to "Pause Focus",
            "completed" to "COMPLETED",
            "sessions" to "sessions",
            "total_time_stat" to "TOTAL TIME",
            "mins" to "mins",
            "group_tasks" to "Group Tasks",
            "add_category" to "Add category (+)",
            "add_todo" to "Add more (To-do)",
            "save_todo" to "Save To-do",
            "save_task" to "Save Task",
            "save_event" to "Save Event",
            "discard_title" to "Discard changes?",
            "discard_msg" to "You have unsaved data. Do you want to discard it?",
            "discard_btn" to "Discard",
            "keep_editing" to "Keep editing"
        ),
        Language.VIETNAMESE to mapOf(
            "app_settings" to "CÀI ĐẶT ỨNG DỤNG",
            "language" to "Ngôn ngữ",
            "dark_theme" to "Chế độ tối",
            "notifications" to "Thông báo",
            "focus_reminders" to "Nhắc nhở tập trung",
            "logout" to "ĐĂNG XUẤT",
            "focus_stats" to "THỐNG KÊ TẬP TRUNG",
            "badge_board" to "BẢNG DANH HIỆU",
            "total_focus_time" to "Tổng thời gian tập trung",
            "day_streak" to "Chuỗi ngày",
            "daily_avg" to "T.bình hàng ngày",
            "view_all" to "Xem tất cả",
            "hello" to "Xin chào",
            "rank" to "Hạng",
            "level" to "Cấp độ",
            "today" to "Hôm nay",
            "no_activities" to "Không có kế hoạch nào cho hôm nay.",
            "create_item" to "Tạo mới",
            "title" to "Tiêu đề",
            "description" to "Mô tả",
            "category" to "Danh mục",
            "save" to "Lưu",
            "cancel" to "Hủy",
            "day" to "Ngày",
            "week" to "Tuần",
            "month" to "Tháng",
            "year" to "Năm",
            "collaboration" to "Cộng tác",
            "my_groups" to "Nhóm của tôi",
            "assigned_to_me" to "Giao cho tôi",
            "active_groups" to "Nhóm đang hoạt động",
            "no_groups" to "Không tìm thấy nhóm nào.",
            "deep_work" to "Phiên tập trung",
            "today_goal" to "Mục tiêu hôm nay",
            "start_focus" to "Bắt đầu tập trung",
            "pause_focus" to "Tạm dừng",
            "completed" to "ĐÃ HOÀN THÀNH",
            "sessions" to "phiên",
            "total_time_stat" to "TỔNG THỜI GIAN",
            "mins" to "phút",
            "group_tasks" to "Công việc nhóm",
            "add_category" to "Thêm danh mục (+)",
            "add_todo" to "Thêm việc cần làm",
            "save_todo" to "Lưu việc cần làm",
            "save_task" to "Lưu nhiệm vụ",
            "save_event" to "Lưu sự kiện",
            "discard_title" to "Hủy thay đổi?",
            "discard_msg" to "Bạn có dữ liệu chưa lưu. Bạn có muốn hủy không?",
            "discard_btn" to "Hủy bỏ",
            "keep_editing" to "Tiếp tục sửa"
        )
    )

    fun get(key: String): String {
        return translations[currentLanguage]?.get(key) ?: key
    }
}
