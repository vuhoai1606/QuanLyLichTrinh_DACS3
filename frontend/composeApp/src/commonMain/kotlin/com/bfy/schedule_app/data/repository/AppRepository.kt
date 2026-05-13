package com.bfy.schedule_app.data.repository

import com.bfy.schedule_app.data.remote.api.ApiClient
import com.bfy.schedule_app.data.remote.model.ScheduleDto
import com.bfy.schedule_app.data.remote.model.UserDto
import com.bfy.schedule_app.data.remote.model.GroupDto
import com.bfy.schedule_app.data.remote.model.GroupTaskDto
import com.bfy.schedule_app.data.remote.model.ApiResponse
import com.bfy.schedule_app.data.remote.model.AuthResponseData
import io.ktor.http.contentType
import io.ktor.http.ContentType
import io.ktor.client.call.*
import io.ktor.client.request.*
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

    suspend fun getFocusStats(): com.bfy.schedule_app.data.remote.model.FocusStatsDto {
        val response: ApiResponse<com.bfy.schedule_app.data.remote.model.FocusStatsDto> = 
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

    suspend fun createSchedule(schedule: ScheduleDto): ScheduleDto {
        try {
            val response: ApiResponse<ScheduleDto> = client.post(ApiClient.getUrl("/schedule")) {
                setBody(schedule)
                contentType(ContentType.Application.Json)
                if (ApiClient.authToken != null) {
                    header("Authorization", "Bearer ${ApiClient.authToken}")
                }
            }.body()
            return response.data ?: throw Exception("Data field is null")
        } catch (e: Exception) {
            // Fallback: If ApiResponse parsing fails, maybe it's returning raw ScheduleDto
            try {
                return client.post(ApiClient.getUrl("/schedule")) {
                    setBody(schedule)
                    contentType(ContentType.Application.Json)
                    if (ApiClient.authToken != null) {
                        header("Authorization", "Bearer ${ApiClient.authToken}")
                    }
                }.body<ScheduleDto>()
            } catch (e2: Exception) {
                throw Exception("Failed to create schedule: ${e2.message}")
            }
        }
    }

    suspend fun getGroupTasks(groupId: String): List<GroupTaskDto> {
        val response: ApiResponse<List<GroupTaskDto>> = 
            client.get(ApiClient.getUrl("/collaboration/groups/$groupId/tasks")) {
                if (ApiClient.authToken != null) {
                    header("Authorization", "Bearer ${ApiClient.authToken}")
                }
            }.body()
        return response.data ?: throw Exception("No tasks found")
    }
}
