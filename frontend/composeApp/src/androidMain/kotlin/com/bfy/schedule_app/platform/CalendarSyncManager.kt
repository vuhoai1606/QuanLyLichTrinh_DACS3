package com.bfy.schedule_app.platform

import android.content.Context
import android.database.Cursor
import android.provider.CalendarContract
import com.bfy.schedule_app.data.remote.model.ExternalEventDto
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.GoogleAuthUtil
import android.app.Activity

actual object CalendarSyncManager {
    actual fun getNativeCalendarEvents(context: Any, email: String): List<ExternalEventDto> {
        val androidContext = context as? Context ?: return emptyList()
        val eventsList = mutableListOf<ExternalEventDto>()
        
        try {
            val projection = arrayOf(
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DESCRIPTION,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND,
                CalendarContract.Events.ALL_DAY
            )
            
            val now = System.currentTimeMillis()
            val thirtyDaysAgo = now - 30L * 24 * 60 * 60 * 1000
            val thirtyDaysFromNow = now + 30L * 24 * 60 * 60 * 1000
            
            val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ? AND ${CalendarContract.Events.ACCOUNT_NAME} = ?"
            val selectionArgs = arrayOf(thirtyDaysAgo.toString(), thirtyDaysFromNow.toString(), email)
            
            val cursor: Cursor? = androidContext.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )

            cursor?.use { c ->
                val titleIndex = c.getColumnIndex(CalendarContract.Events.TITLE)
                val descIndex = c.getColumnIndex(CalendarContract.Events.DESCRIPTION)
                val locIndex = c.getColumnIndex(CalendarContract.Events.EVENT_LOCATION)
                val dtstartIndex = c.getColumnIndex(CalendarContract.Events.DTSTART)
                val dtendIndex = c.getColumnIndex(CalendarContract.Events.DTEND)
                val allDayIndex = c.getColumnIndex(CalendarContract.Events.ALL_DAY)

                while (c.moveToNext()) {
                    val title = c.getString(titleIndex) ?: "Untitled Event"
                    val description = if (descIndex >= 0) c.getString(descIndex) else null
                    val location = if (locIndex >= 0) c.getString(locIndex) else null
                    val dtstart = c.getLong(dtstartIndex)
                    val dtend = if (dtendIndex >= 0 && !c.isNull(dtendIndex)) c.getLong(dtendIndex) else dtstart
                    val allDay = if (allDayIndex >= 0) c.getInt(allDayIndex) == 1 else false

                    val startTime = Instant.fromEpochMilliseconds(dtstart).toLocalDateTime(TimeZone.currentSystemDefault())
                    val endTime = Instant.fromEpochMilliseconds(dtend).toLocalDateTime(TimeZone.currentSystemDefault())

                    eventsList.add(
                        ExternalEventDto(
                            title = title,
                            description = description,
                            location = location,
                            start_time = startTime.toString(),
                            end_time = endTime.toString(),
                            is_all_day = allDay
                        )
                    )
                }
            }
            
            if (eventsList.isEmpty()) {
                // Debug to see what accounts actually have events
                val debugCursor = androidContext.contentResolver.query(
                    CalendarContract.Events.CONTENT_URI,
                    arrayOf(CalendarContract.Events.ACCOUNT_NAME),
                    "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?",
                    arrayOf(thirtyDaysAgo.toString(), thirtyDaysFromNow.toString()),
                    null
                )
                val foundAccounts = mutableSetOf<String>()
                debugCursor?.use { dc ->
                    val accIndex = dc.getColumnIndex(CalendarContract.Events.ACCOUNT_NAME)
                    while (dc.moveToNext()) {
                        foundAccounts.add(dc.getString(accIndex))
                    }
                }
                if (foundAccounts.isNotEmpty()) {
                    throw Exception("No events found for account: $email. \nBut we found events in these accounts instead: ${foundAccounts.joinToString(", ")}. \nPlease make sure you selected your email when creating the event in the Calendar app!")
                }
            }

            return eventsList
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return eventsList
    }
}

@androidx.compose.runtime.Composable
actual fun rememberCalendarPermissionLauncher(onResult: (Boolean) -> Unit): () -> Unit {
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
        onResult = onResult
    )
    return { launcher.launch(android.Manifest.permission.READ_CALENDAR) }
}

@androidx.compose.runtime.Composable
actual fun rememberGoogleAuthLauncher(onToken: (String?) -> Unit): () -> Unit {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val authManager = androidx.compose.runtime.remember { GoogleAuthManager(context) }

    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    val account = task.await()
                    if (account.account != null) {
                        val token = GoogleAuthUtil.getToken(
                            context,
                            account.account!!,
                            "oauth2:https://www.googleapis.com/auth/tasks https://www.googleapis.com/auth/calendar"
                        )
                        withContext(Dispatchers.Main) {
                            onToken(token)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            onToken(null)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        onToken(null)
                    }
                }
            }
        } else {
            onToken(null)
        }
    }

    return { launcher.launch(authManager.getSignInIntent()) }
}
