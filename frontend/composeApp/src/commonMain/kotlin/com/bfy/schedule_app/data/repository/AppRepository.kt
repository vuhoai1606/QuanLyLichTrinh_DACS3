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
    private val client: io.ktor.client.HttpClient get() = ApiClient.client

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

    suspend fun updateProfile(fullName: String? = null, bio: String? = null, avatarUrl: String? = null, gender: String? = null, dob: String? = null): UserDto {
        val response: ApiResponse<UserDto> = client.patch(ApiClient.getUrl("/users/profile")) {
            ApiClient.authToken?.let {
                header("Authorization", "Bearer $it")
            }
            setBody(mapOf(
                "full_name" to fullName,
                "bio" to bio,
                "avatar_url" to avatarUrl,
                "gender" to gender,
                "dob" to dob
            ).filterValues { it != null })
            contentType(ContentType.Application.Json)
        }.body()
        return response.data ?: throw Exception(response.message ?: "Failed to update profile")
    }

    suspend fun updateUserSettings(settings: Map<String, Any?>): Boolean {
        val response: ApiResponse<Unit> = client.patch(ApiClient.getUrl("/settings")) {
            ApiClient.authToken?.let {
                header("Authorization", "Bearer $it")
            }
            setBody(settings)
            contentType(ContentType.Application.Json)
        }.body()
        return response.success == true
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
            ApiClient.setTokens(response.data.token, response.data.refreshToken ?: "")
            return response.data
        } else {
            throw Exception(response.message ?: "Login failed")
        }
    }

    suspend fun register(fullName: String, email: String, password: String, gender: String? = null, dob: String? = null, otp: String): AuthResponseData {
        val response: ApiResponse<AuthResponseData> = client.post(ApiClient.getUrl("/auth/register")) {
            setBody(mapOf(
                "full_name" to fullName,
                "email" to email,
                "password" to password,
                "gender" to gender,
                "dob" to dob
            ).filterValues { it != null })
            contentType(ContentType.Application.Json)
        }.body()
        
        if (response.success == true && response.data != null) {
            ApiClient.setTokens(response.data.token, response.data.refreshToken ?: "")
            return response.data
        } else {
            throw Exception(response.message ?: "Registration failed")
        }
    }

    suspend fun googleLogin(idToken: String): AuthResponseData {
        val response: ApiResponse<AuthResponseData> = client.post(ApiClient.getUrl("/auth/google-login")) {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(mapOf("idToken" to idToken))
        }.body()
        
        if (response.success != true) {
            throw Exception(response.message ?: "Google login failed")
        }
        
        val data = response.data ?: throw Exception("Invalid response data")
        ApiClient.setTokens(data.token, data.refreshToken ?: "")
        return data
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): Boolean {
        val response: ApiResponse<Unit> = client.post(ApiClient.getUrl("/auth/change-password")) {
            ApiClient.authToken?.let {
                header("Authorization", "Bearer $it")
            }
            contentType(ContentType.Application.Json)
            setBody(mapOf(
                "oldPassword" to oldPassword,
                "newPassword" to newPassword
            ))
        }.body()
        
        if (response.success != true) {
            throw Exception(response.message ?: "Failed to change password")
        }
        return true
    }

        suspend fun requestOtp(email: String, purpose: String): Boolean {
        val response: ApiResponse<Unit> = client.post(ApiClient.getUrl("/auth/request-otp")) {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(mapOf("email" to email, "purpose" to purpose))
        }.body()
        return response.success == true
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
            setBody(mapOf("email" to email, "otp" to otp, "purpose" to "FORGOT_PASSWORD"))
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

    suspend fun updateAssigneeStatus(id: String, userId: String, isCompleted: Boolean): Any {
        val response: ApiResponse<Unit> = client.patch(ApiClient.getUrl("/schedules//assignee-status")) {
            if (ApiClient.authToken != null) {
                header("Authorization", "Bearer ")
            }
            setBody(mapOf("user_id" to userId, "is_completed" to isCompleted))
            contentType(io.ktor.http.ContentType.Application.Json)
        }.body()
        if (response.success == true) {
            return response.data ?: Any()
        }
        throw Exception(response.message ?: "Failed to update assignee status")
    }

    suspend fun updateSchedule(scheduleId: String, updates: com.bfy.schedule_app.data.remote.model.UpdateScheduleRequest) {
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

    suspend fun searchUsers(query: String): List<UserDto> {
        val currentUser = getCurrentUser()
        val response: ApiResponse<List<UserDto>> = client.get(ApiClient.getUrl("/users/search?q=$query&user_id=${currentUser.id}")) {
            if (ApiClient.authToken != null) {
                header("Authorization", "Bearer ${ApiClient.authToken}")
            }
        }.body()
        return response.data ?: emptyList()
    }

    suspend fun createGroup(name: String, description: String?, avatarUrl: String?, members: List<GroupMemberRequest>): GroupDto {
        val currentUser = getCurrentUser()
        val response: ApiResponse<GroupDto> = client.post(ApiClient.getUrl("/collaboration/groups")) {
            setBody(CreateGroupRequest(
                name = name,
                description = description,
                avatar_url = avatarUrl,
                leader_id = currentUser.id,
                members = members
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


    suspend fun getGroupMembers(groupId: String): List<GroupMemberDto> {
        val response: ApiResponse<List<GroupMemberDto>> = client.get(ApiClient.getUrl("/collaboration/groups/$groupId/members")) {
            if (ApiClient.authToken != null) {
                header("Authorization", "Bearer ${ApiClient.authToken}")
            }
        }.body()
        return response.data ?: emptyList()
    }

    suspend fun updateGroupInfo(groupId: String, name: String?, description: String?, avatarUrl: String?) {
        val currentUser = getCurrentUser()
        client.put(ApiClient.getUrl("/collaboration/groups/$groupId")) {
            setBody(mapOf(
                "name" to name,
                "description" to description,
                "avatar_url" to avatarUrl,
                "requester_id" to currentUser.id
            ).filterValues { it != null })
            contentType(ContentType.Application.Json)
            if (ApiClient.authToken != null) {
                header("Authorization", "Bearer ${ApiClient.authToken}")
            }
        }
    }

    suspend fun addGroupMember(groupId: String, userId: String, role: String) {
        val currentUser = getCurrentUser()
        client.post(ApiClient.getUrl("/collaboration/groups/$groupId/members")) {
            setBody(mapOf(
                "user_id" to userId,
                "requester_id" to currentUser.id,
                "role" to role
            ))
            contentType(ContentType.Application.Json)
            if (ApiClient.authToken != null) {
                header("Authorization", "Bearer ${ApiClient.authToken}")
            }
        }
    }

    suspend fun updateGroupMemberRole(groupId: String, userId: String, role: String) {
        client.put(ApiClient.getUrl("/collaboration/groups/$groupId/members/$userId/role")) {
            contentType(ContentType.Application.Json)
            setBody(mapOf("role" to role))
            if (ApiClient.authToken != null) {
                header("Authorization", "Bearer ${ApiClient.authToken}")
            }
        }
    }

    suspend fun removeGroupMember(groupId: String, userId: String) {
        val currentUser = getCurrentUser()
        client.delete(ApiClient.getUrl("/collaboration/groups/$groupId/members/$userId")) {
            setBody(mapOf(
                "requester_id" to currentUser.id
            ))
            contentType(ContentType.Application.Json)
            if (ApiClient.authToken != null) {
                header("Authorization", "Bearer ${ApiClient.authToken}")
            }
        }
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


    suspend fun getChatMessages(groupId: String): List<ChatMessageDto> {
        val response: ApiResponse<List<ChatMessageDto>> = client.get(ApiClient.getUrl("/chat/$groupId")).body()
        return response.data ?: emptyList()
    }

    suspend fun sendChatMessage(groupId: String, message: String): ChatMessageDto {
        val response: ApiResponse<ChatMessageDto> = client.post(ApiClient.getUrl("/chat/send")) {
            setBody(mapOf("groupId" to groupId, "message" to message))
            contentType(ContentType.Application.Json)
        }.body()
        return response.data ?: throw Exception(response.message ?: "Failed to send message")
    }

    suspend fun aiBreakdownTask(title: String, description: String): List<String> {
        val response: ApiResponse<List<String>> = client.post(ApiClient.getUrl("/ai/breakdown")) {
            setBody(mapOf("title" to title, "description" to description))
            contentType(ContentType.Application.Json)
        }.body()
        return response.data ?: emptyList()
    }

    suspend fun aiSuggestSchedule(): String {
        val response: ApiResponse<String> = client.get(ApiClient.getUrl("/ai/suggest-schedule")).body()
        return response.data ?: "Plan focused!"
    }

    suspend fun getProductivityStats(): ProductivityStatsDto {
        val response: ApiResponse<ProductivityStatsDto> = client.get(ApiClient.getUrl("/analytics/productivity")).body()
        return response.data ?: throw Exception(response.message ?: "Failed to load stats")
    }

    suspend fun getAmbientSounds(): List<AmbientSoundDto> {
        val response: List<AmbientSoundDto> = client.get(ApiClient.getUrl("/focus/ambient-sounds")).body()
        return response
    }

}
