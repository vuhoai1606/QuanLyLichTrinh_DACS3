package com.bfy.schedule_app.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object SettingsManager {
    var isDarkTheme by mutableStateOf(true)
    var notificationsEnabled by mutableStateOf(false)
    var notificationMessage by mutableStateOf<String?>(null)
}
