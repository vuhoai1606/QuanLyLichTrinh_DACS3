package com.bfy.schedule_app.data.remote.api

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import com.bfy.schedule_app.data.remote.model.ApiResponse
import com.bfy.schedule_app.utils.SettingsManager
import com.bfy.schedule_app.data.remote.model.AuthResponseData
import io.ktor.client.call.body

object ApiClient {
    private val BASE_URL: String get() {
        val ip = SettingsManager.customServerIp.ifBlank { "10.0.2.2" }
        return "http://$ip:3000/api"
    }
    private val WS_URL: String get() {
        val ip = SettingsManager.customServerIp.ifBlank { "10.0.2.2" }
        return "ws://$ip:3000/ws"
    }
    
    var authToken: String? = null
    var refreshToken: String? = null

    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 30000
            socketTimeoutMillis = 30000
        }
        install(Logging) {
            level = LogLevel.BODY
        }
        install(WebSockets)
        install(Auth) {
            bearer {
                loadTokens {
                    if (authToken != null) BearerTokens(authToken!!, refreshToken ?: "") else null
                }
                refreshTokens {
                    if (refreshToken == null) return@refreshTokens null
                    
                    try {
                        val response: ApiResponse<AuthResponseData> = client.post(getUrl("/auth/refresh")) {
                            setBody(mapOf("token" to refreshToken))
                            contentType(ContentType.Application.Json)
                            markAsRefreshTokenRequest()
                        }.body()
                        
                        if (response.success == true && response.data != null) {
                            setTokens(response.data.token, response.data.refreshToken ?: "")
                            BearerTokens(response.data.token, response.data.refreshToken ?: "")
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }
    }

    fun getUrl(path: String): String {
        return "$BASE_URL$path"
    }

    fun getWsUrl(token: String): String {
        return "$WS_URL?token=$token"
    }

    fun setTokens(token: String, refresh: String) {
        authToken = token
        refreshToken = refresh
    }

    fun clearTokens() {
        authToken = null
        refreshToken = null
    }
}
