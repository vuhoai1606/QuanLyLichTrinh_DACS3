package com.bfy.schedule_app.platform

import com.bfy.schedule_app.data.remote.model.ExternalEventDto

actual object CalendarSyncManager {
    actual fun getNativeCalendarEvents(context: Any, email: String): List<ExternalEventDto> {
        return emptyList()
    }
}

@androidx.compose.runtime.Composable
actual fun rememberCalendarPermissionLauncher(onResult: (Boolean) -> Unit): () -> Unit {
    return { onResult(false) }
}

@androidx.compose.runtime.Composable
actual fun rememberGoogleAuthLauncher(onToken: (String?) -> Unit): () -> Unit {
    return { onToken(null) }
}
