package com.bfy.schedule_app.data.repository

import com.bfy.schedule_app.data.remote.api.ApiClient
import com.bfy.schedule_app.data.remote.model.*
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.request.patch
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AppRepository {
    private val client = ApiClient.client

    suspend fun getCurrentUser(): UserDto {
        val response: ApiResponse<UserDto> = client.get(ApiClient.getUrl("/users/me")) {
            ApiClient.authToken?.let {
                header("Authorization", "Bearer $it")
            }
        }.body()
        
        if (response.success == true && response.data != null) {
            return response.data
        } else {
            throw Exception(response.message ?: "Failed to load user profile")
        }
    }

    suspend fun getSchedules(): List<ScheduleDto> {
        val response: ApiResponse<List<ScheduleDto>> = client.get(ApiClient.getUrl("/schedule")) {
            ApiClient.authToken?.let {
                header("Authorization", "Bearer $it")
            }
        }.body()
        
        if (response.success == true && response.data != null) {
            return response.data
        } else {
            throw Exception(response.message ?: "Failed to load schedules")
        }
    }

    suspend fun getGroups(): List<GroupDto> {
        val response: ApiResponse<List<GroupDto>> = client.get(ApiClient.getUrl("/collaboration/groups")) {
            ApiClient.authToken?.let {
                header("Authorization", "Bearer $it")
            }
        }.body()
        
        if (response.success == true && response.data != null) {
            return response.data
        } else {
            throw Exception(response.message ?: "Failed to load groups")
        }
    }

    suspend fun getFocusStats(): FocusStatsDto {
        val response: ApiResponse<FocusStatsDto> = 
            client.get(ApiClient.getUrl("/focus/stats")) {
                ApiClient.authToken?.let {
                    header("Authorization", "Bearer $it")
                }
            }.body()
            
        if (response.success == true && response.data != null) {
            return response.data
        } else {
            throw Exception(response.message ?: "Failed to load focus stats")
        }
    }

    suspend fun login(email: String, password: String): AuthResponseData {
        val response: ApiResponse<AuthResponseData> = client.post(ApiClient.getUrl("/auth/login")) {
            setBody(mapOf("email" to email, "password" to password))
            contentType(ContentType.Application.Json)
        }.body()
        
        if (response.success == true && response.data != null) {
            ApiClient.setToken(response.data.token)
            return response.data
        } else {
            throw Exception(response.message ?: "Login failed")
        }
    }

    suspend fun register(fullName: String, email: String, password: String): AuthResponseData {
        val response: ApiResponse<AuthResponseData> = client.post(ApiClient.getUrl("/auth/register")) {
            setBody(mapOf("full_name" to fullName, "email" to email, "password" to password))
            contentType(ContentType.Application.Json)
        }.body()
        
        if (response.success == true && response.data != null) {
            ApiClient.setToken(response.data.token)
            return response.data
        } else {
            throw Exception(response.message ?: "Registration failed")
        }
    }

    suspend fun googleLogin(googleId: String, email: String, fullName: String, avatarUrl: String? = null): AuthResponseData {
        val response: ApiResponse<AuthResponseData> = client.post(ApiClient.getUrl("/auth/google-login")) {
            contentType(ContentType.Application.Json)
            setBody(mapOf(
                "googleId" to googleId,
                "email" to email,
                "fullName" to fullName,
                "avatarUrl" to avatarUrl
            ))
        }.body()
        
        if (response.success != true) {
            throw Exception(response.message ?: "Google login failed")
        }
        
        val data = response.data ?: throw Exception("Invalid response data")
        ApiClient.setToken(data.token)
        return data
    }

    suspend fun forgotPassword(email: String): Boolean {
        val response: ApiResponse<Unit> = client.post(ApiClient.getUrl("/auth/forgot-password")) {
            contentType(ContentType.Application.Json)
            setBody(mapOf("email" to email))
        }.body()
        return response.success == true
    }

    suspend fun verifyOtp(email: String, otp: String): Boolean {
        val response: ApiResponse<Unit> = client.post(ApiClient.getUrl("/auth/verify-otp")) {
            contentType(ContentType.Application.Json)
            setBody(mapOf("email" to email, "otp" to otp))
        }.body()
        return response.success == true
    }

    suspend fun resetPassword(email: String, otp: String, newPassword: String): Boolean {
        val response: ApiResponse<Unit> = client.post(ApiClient.getUrl("/auth/reset-password")) {
            contentType(ContentType.Application.Json)
            setBody(mapOf("email" to email, "otp" to otp, "newPassword" to newPassword))
        }.body()
        return response.success == true
    }

    suspend fun createSchedule(schedule: ScheduleDto): ScheduleDto {
        val response: ApiResponse<ScheduleDto> = client.post(ApiClient.getUrl("/schedule")) {
            setBody(schedule)
            contentType(ContentType.Application.Json)
            if (ApiClient.authToken != null) {
                header("Authorization", "Bearer ${ApiClient.authToken}")
            }
        }.body()
        
        if (response.success == true && response.data != null) {
            return response.data
        } else {
            throw Exception(response.message ?: "Failed to create schedule")
        }
    }

    suspend fun deleteSchedule(scheduleId: String) {
        val response: ApiResponse<Unit> = client.delete(ApiClient.getUrl("/schedule/$scheduleId")) {
            if (ApiClient.authToken != null) {
                header("Authorization", "Bearer ${ApiClient.authToken}")
            }
        }.body()
        
        if (response.success != true) {
            throw Exception(response.message ?: "Failed to delete schedule")
        }
    }

    suspend fun getCategories(): List<CategoryDto> {
        val response: ApiResponse<List<CategoryDto>> = client.get(ApiClient.getUrl("/schedule/categories")) {
            if (ApiClient.authToken != null) {
                header("Authorization", "Bearer ${ApiClient.authToken}")
            }
        }.body()
        return response.data ?: emptyList()
    }

    suspend fun updateSchedule(scheduleId: String, updates: Map<String, Any?>) {
        val response: ApiResponse<Unit> = client.put(ApiClient.getUrl("/schedule/$scheduleId")) {
            if (ApiClient.authToken != null) {
                header("Authorization", "Bearer ${ApiClient.authToken}")
            }
            contentType(ContentType.Application.Json)
            setBody(updates)
        }.body()
        
        if (response.success != true) {
            throw Exception(response.message ?: "Failed to update schedule")
        }
    }

    suspend fun searchSchedules(query: String): List<ScheduleDto> {
        val response: ApiResponse<List<ScheduleDto>> = client.get(ApiClient.getUrl("/schedule/search/$query")) {
            if (ApiClient.authToken != null) {
                header("Authorization", "Bearer ${ApiClient.authToken}")
            }
        }.body()
        return response.data ?: emptyList()
    }

    suspend fun createFocusSession(durationMinutes: Int, status: String): FocusSessionDto {
        val currentUser = getCurrentUser()
        val response: ApiResponse<FocusSessionDto> = client.post(ApiClient.getUrl("/focus/sessions")) {
            setBody(mapOf(
                "user_id" to currentUser.id,
                "duration_minutes" to durationMinutes,
                "status" to status
            ))
            contentType(ContentType.Application.Json)
            if (ApiClient.authToken != null) {
                header("Authorization", "Bearer ${ApiClient.authToken}")
            }
        }.body()
        
        if (response.success == true && response.data != null) {
            return response.data
        } else {
            throw Exception(response.message ?: "Failed to record focus session")
        }
    }

    suspend fun createGroup(name: String, description: String?): GroupDto {
        val currentUser = getCurrentUser()
        val response: ApiResponse<GroupDto> = client.post(ApiClient.getUrl("/collaboration/groups")) {
            setBody(mapOf(
                "name" to name,
                "description" to description,
                "leader_id" to currentUser.id
            ))
            contentType(ContentType.Application.Json)
            if (ApiClient.authToken != null) {
                header("Authorization", "Bearer ${ApiClient.authToken}")
            }
        }.body()
        
        if (response.success == true && response.data != null) {
            return response.data
        } else {
            throw Exception(response.message ?: "Failed to create group")
        }
    }

    suspend fun getNotifications(): List<NotificationDto> {
        val response: ApiResponse<List<NotificationDto>> = 
            client.get(ApiClient.getUrl("/notifications")) {
                if (ApiClient.authToken != null) {
                    header("Authorization", "Bearer ${ApiClient.authToken}")
                }
            }.body()
        return response.data ?: emptyList()
    }

    suspend fun markNotificationRead(id: String) {
        client.patch(ApiClient.getUrl("/notifications/$id/read")) {
            if (ApiClient.authToken != null) {
                header("Authorization", "Bearer ${ApiClient.authToken}")
            }
        }
    }

    suspend fun updateUserSettings(settings: Map<String, Any?>) {
        client.put(ApiClient.getUrl("/settings/user")) {
            if (ApiClient.authToken != null) {
                header("Authorization", "Bearer ${ApiClient.authToken}")
            }
            contentType(ContentType.Application.Json)
            setBody(settings)
        }
    }

    suspend fun updateProfile(fullName: String, bio: String?) {
        client.put(ApiClient.getUrl("/users/me")) {
            if (ApiClient.authToken != null) {
                header("Authorization", "Bearer ${ApiClient.authToken}")
            }
            contentType(ContentType.Application.Json)
            setBody(mapOf("full_name" to fullName, "bio" to bio))
        }
    }

    suspend fun getGroupMembers(groupId: String): List<UserDto> {
        val response: ApiResponse<List<UserDto>> = client.get(ApiClient.getUrl("/collaboration/groups/$groupId/members")) {
            if (ApiClient.authToken != null) {
                header("Authorization", "Bearer ${ApiClient.authToken}")
            }
        }.body()
        return response.data ?: emptyList()
    }

    suspend fun getGroupTasks(groupId: String): List<GroupTaskDto> {
        val response: ApiResponse<List<GroupTaskDto>> = client.get(ApiClient.getUrl("/collaboration/groups/$groupId/tasks")) {
            if (ApiClient.authToken != null) {
                header("Authorization", "Bearer ${ApiClient.authToken}")
            }
        }.body()
        return response.data ?: emptyList()
    }

    suspend fun createGroupTask(groupId: String, title: String, description: String?): GroupTaskDto {
        val response: ApiResponse<GroupTaskDto> = client.post(ApiClient.getUrl("/collaboration/groups/$groupId/tasks")) {
            setBody(mapOf("title" to title, "description" to description))
            contentType(ContentType.Application.Json)
            if (ApiClient.authToken != null) {
                header("Authorization", "Bearer ${ApiClient.authToken}")
            }
        }.body()
        if (response.success == true && response.data != null) {
            return response.data
        } else {
            throw Exception(response.message ?: "Failed to create group task")
        }
    }

    suspend fun getBadges(): List<BadgeDto> {
        val response: ApiResponse<List<BadgeDto>> = client.get(ApiClient.getUrl("/gamification/badges")) {
            if (ApiClient.authToken != null) {
                header("Authorization", "Bearer ${ApiClient.authToken}")
            }
        }.body()
        return response.data ?: emptyList()
    }

    suspend fun getLeaderboard(): List<LeaderboardEntryDto> {
        val response: ApiResponse<List<LeaderboardEntryDto>> = client.get(ApiClient.getUrl("/gamification/leaderboard")) {
            if (ApiClient.authToken != null) {
                header("Authorization", "Bearer ${ApiClient.authToken}")
            }
        }.body()
        return response.data ?: emptyList()
    }
    suspend fun getSharedWithMe(): List<ScheduleDto> {
        val currentUser = getCurrentUser()
        val response: ApiResponse<List<ScheduleDto>> = client.get(ApiClient.getUrl("/collaboration/shared-with-me")) {
            parameter("user_id", currentUser.id)
            if (ApiClient.authToken != null) {
                header("Authorization", "Bearer ${ApiClient.authToken}")
            }
        }.body()
        return response.data ?: emptyList()
    }
}
