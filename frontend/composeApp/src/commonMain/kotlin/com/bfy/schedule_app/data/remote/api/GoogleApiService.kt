package com.bfy.schedule_app.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.delete
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
data class GoogleTaskList(
    val id: String,
    val title: String
)

@Serializable
data class GoogleTaskListsResponse(
    val items: List<GoogleTaskList>? = null
)

@Serializable
data class GoogleTask(
    val id: String? = null,
    val title: String,
    val notes: String? = null,
    val status: String? = null,
    val due: String? = null,
    val updated: String? = null
)

@Serializable
data class GoogleTasksResponse(
    val items: List<GoogleTask>? = null
)

@Serializable
data class GoogleEventDateTime(
    val dateTime: String? = null,
    val date: String? = null,
    val timeZone: String? = null
)

@Serializable
data class GoogleEvent(
    val id: String? = null,
    val summary: String,
    val description: String? = null,
    val start: GoogleEventDateTime? = null,
    val end: GoogleEventDateTime? = null,
    val updated: String? = null
)

@Serializable
data class GoogleEventsResponse(
    val items: List<GoogleEvent>? = null
)

class GoogleApiService(private val client: HttpClient = ApiClient.client) {

    suspend fun getTaskLists(accessToken: String): List<GoogleTaskList> {
        val response: GoogleTaskListsResponse = client.get("https://tasks.googleapis.com/tasks/v1/users/@me/lists") {
            header("Authorization", "Bearer $accessToken")
        }.body()
        return response.items ?: emptyList()
    }

    suspend fun getTasks(accessToken: String, taskListId: String): List<GoogleTask> {
        val response: GoogleTasksResponse = client.get("https://tasks.googleapis.com/tasks/v1/lists/$taskListId/tasks") {
            header("Authorization", "Bearer $accessToken")
        }.body()
        return response.items ?: emptyList()
    }

    suspend fun createTask(accessToken: String, taskListId: String, task: GoogleTask): GoogleTask {
        return client.post("https://tasks.googleapis.com/tasks/v1/lists/$taskListId/tasks") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(task)
        }.body()
    }

    suspend fun updateTask(accessToken: String, taskListId: String, task: GoogleTask): GoogleTask {
        return client.put("https://tasks.googleapis.com/tasks/v1/lists/$taskListId/tasks/${task.id}") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(task)
        }.body()
    }

    suspend fun getEvents(accessToken: String, calendarId: String = "primary"): List<GoogleEvent> {
        val response: GoogleEventsResponse = client.get("https://www.googleapis.com/calendar/v3/calendars/$calendarId/events") {
            header("Authorization", "Bearer $accessToken")
        }.body()
        return response.items ?: emptyList()
    }

    suspend fun createEvent(accessToken: String, calendarId: String = "primary", event: GoogleEvent): GoogleEvent {
        return client.post("https://www.googleapis.com/calendar/v3/calendars/$calendarId/events") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(event)
        }.body()
    }

    suspend fun updateEvent(accessToken: String, calendarId: String = "primary", event: GoogleEvent): GoogleEvent {
        return client.put("https://www.googleapis.com/calendar/v3/calendars/$calendarId/events/${event.id}") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(event)
        }.body()
    }
}
