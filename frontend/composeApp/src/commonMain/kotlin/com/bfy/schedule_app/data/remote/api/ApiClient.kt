package com.bfy.schedule_app.data.remote.api

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:3000/api" // Backend routes: /api/auth, /api/schedule, etc
    var authToken: String? = null

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
    }

    fun getUrl(path: String): String {
        return "$BASE_URL$path"
    }

    fun setToken(token: String) {
        authToken = token
    }

    fun clearToken() {
        authToken = null
    }
}
