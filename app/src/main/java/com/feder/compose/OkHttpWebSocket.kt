package com.feder.compose

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.util.concurrent.TimeUnit

class OkHttpWebSocket {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // Бесконечно для WS
        .build()
    
    private var webSocket: WebSocket? = null
    var onMessage: ((String) -> Unit)? = null
    var onOpen: (() -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    
    private fun logToServer(message: String) {
        try {
            val logJson = """{"log":"$message"}"""
            val body = logJson.toRequestBody("application/json".toMediaType())
            val logRequest = Request.Builder()
                .url("http://2.26.71.102:8002/api/logs")
                .post(body)
                .build()
            client.newCall(logRequest).execute().close()
        } catch (_: Exception) {}
    }

    fun connect(username: String, token: String) {
        logToServer("OKHTTP_WS_CONNECT: $username")
        val request = Request.Builder()
            .url("ws://2.26.71.102:8002/ws/$username?token=$token")
            .build()
        
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                android.util.Log.d("OkHttpWS", "Connected")
                logToServer("OKHTTP_WS_OPEN")
                onOpen?.invoke()
            }
            
            override fun onMessage(webSocket: WebSocket, text: String) {
                android.util.Log.d("OkHttpWS", "Message: $text")
                logToServer("OKHTTP_WS_MSG: $text")
                onMessage?.invoke(text)
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                android.util.Log.e("OkHttpWS", "Error: ${t.message}")
                logToServer("OKHTTP_WS_ERROR: ${t.message}")
                onError?.invoke(t.message ?: "unknown")
            }
        })
    }
    
    fun send(text: String): Boolean {
        logToServer("OKHTTP_WS_SEND: webSocket=${webSocket != null}")
        if (webSocket == null) {
            logToServer("OKHTTP_WS_SEND_ERROR: webSocket is NULL")
            return false
        }
        val result = webSocket!!.send(text)
        logToServer("OKHTTP_WS_SEND_RESULT: $result")
        return result
    }
    
    fun close() {
        webSocket?.close(1000, "Bye")
    }
}
