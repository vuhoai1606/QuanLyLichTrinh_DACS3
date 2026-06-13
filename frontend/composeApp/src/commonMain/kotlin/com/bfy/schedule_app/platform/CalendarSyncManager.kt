package com.bfy.schedule_app.platform

import com.bfy.schedule_app.data.remote.model.ExternalEventDto
import androidx.compose.runtime.Composable

expect object CalendarSyncManager {
    fun getNativeCalendarEvents(context: Any, email: String): List<ExternalEventDto>
}

@Composable
expect fun rememberCalendarPermissionLauncher(onResult: (Boolean) -> Unit): () -> Unit

@Composable
expect fun rememberGoogleAuthLauncher(onToken: (String?) -> Unit): () -> Unit

