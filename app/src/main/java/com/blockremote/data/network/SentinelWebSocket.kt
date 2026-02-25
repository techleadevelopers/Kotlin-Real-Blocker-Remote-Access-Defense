package com.blockremote.data.network

import com.blockremote.data.network.models.ServerCommand
import com.blockremote.data.network.models.SignalPayload
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

enum class WebSocketState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    AUTHENTICATED,
    ERROR
}

class SentinelWebSocket(
    private val client: OkHttpClient,
    private val gson: Gson = Gson()
) {

    private var webSocket: WebSocket? = null

    private val _connectionState = MutableStateFlow(WebSocketState.DISCONNECTED)
    val connectionState: StateFlow<WebSocketState> = _connectionState.asStateFlow()

    fun connect(url: String, jwtToken: String?): Flow<ServerCommand> = callbackFlow {
        _connectionState.value = WebSocketState.CONNECTING

        val requestBuilder = Request.Builder().url(url)

        if (jwtToken != null) {
            requestBuilder.addHeader("Sec-WebSocket-Protocol", "sentinel-v2, $jwtToken")
        }

        val listener = object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                _connectionState.value = WebSocketState.CONNECTED
                trySend(
                    ServerCommand(
                        action = "CONNECTED",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }

            override fun onMessage(ws: WebSocket, text: String) {
                try {
                    val command = gson.fromJson(text, ServerCommand::class.java)

                    if (command.action == "AUTH_OK") {
                        _connectionState.value = WebSocketState.AUTHENTICATED
                    }

                    trySend(command)
                } catch (e: Exception) {
                    trySend(
                        ServerCommand(
                            action = "PARSE_ERROR",
                            payload = mapOf("raw" to text, "error" to (e.message ?: "")),
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                _connectionState.value = WebSocketState.ERROR
                trySend(
                    ServerCommand(
                        action = "CONNECTION_ERROR",
                        payload = mapOf("error" to (t.message ?: "Unknown error")),
                        timestamp = System.currentTimeMillis()
                    )
                )
                close()
            }

            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                ws.close(1000, null)
                _connectionState.value = WebSocketState.DISCONNECTED
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                _connectionState.value = WebSocketState.DISCONNECTED
            }
        }

        webSocket = client.newWebSocket(requestBuilder.build(), listener)

        awaitClose {
            disconnect()
        }
    }

    fun sendHeartbeat(payload: SignalPayload): Boolean {
        val ws = webSocket ?: return false
        return try {
            val json = gson.toJson(payload)
            ws.send(json)
        } catch (e: Exception) {
            false
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
        _connectionState.value = WebSocketState.DISCONNECTED
    }
}
