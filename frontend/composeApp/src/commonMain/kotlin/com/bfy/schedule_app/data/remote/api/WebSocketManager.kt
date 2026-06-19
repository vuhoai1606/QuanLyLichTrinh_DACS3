package com.bfy.schedule_app.data.remote.api

import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.json.*

data class WsMessage(
    val type: String,
    val groupId: String? = null,
    val message: String? = null
)

object WebSocketManager {
    private var webSocketSession: DefaultClientWebSocketSession? = null
    private var job: Job? = null
    private val _events = MutableSharedFlow<WsMessage>(extraBufferCapacity = 10)
    val events: SharedFlow<WsMessage> = _events
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun connect() {
        if (job?.isActive == true) return
        val token = ApiClient.authToken ?: return
        
        job = scope.launch {
            try {
                ApiClient.client.webSocket(ApiClient.getWsUrl(token)) {
                    webSocketSession = this
                    
                    // Send auth message
                    val authMessage = buildJsonObject {
                        put("type", "auth")
                        put("token", token)
                    }.toString()
                    send(authMessage)
                    
                    // Listen for incoming messages
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            try {
                                val json = Json { ignoreUnknownKeys = true }
                                val jsonElement = json.parseToJsonElement(text).jsonObject
                                val type = jsonElement["type"]?.jsonPrimitive?.content ?: ""
                                val groupId = jsonElement["groupId"]?.jsonPrimitive?.content
                                val message = jsonElement["message"]?.jsonPrimitive?.content
                                _events.tryEmit(WsMessage(type, groupId, message))
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                delay(5000)
                connect() // Reconnect on failure
            } finally {
                webSocketSession = null
            }
        }
    }

    fun disconnect() {
        scope.launch {
            try {
                webSocketSession?.close(CloseReason(CloseReason.Codes.NORMAL, "User logged out"))
            } catch (e: Exception) {}
            webSocketSession = null
            job?.cancel()
        }
    }
}
