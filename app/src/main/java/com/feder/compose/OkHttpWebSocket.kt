package com.feder.compose

import okhttp3.*
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
    
    fun connect(username: String, token: String) {
        val request = Request.Builder()
            .url("ws://2.26.71.102:8002/ws/$username?token=$token")
            .build()
        
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                android.util.Log.d("OkHttpWS", "Connected")
                onOpen?.invoke()
            }
            
            override fun onMessage(webSocket: WebSocket, text: String) {
                android.util.Log.d("OkHttpWS", "Message: $text")
                onMessage?.invoke(text)
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                android.util.Log.e("OkHttpWS", "Error: ${t.message}")
                onError?.invoke(t.message ?: "unknown")
            }
        })
    }
    
    fun send(text: String): Boolean {
        return webSocket?.send(text) ?: false
    }
    
    fun close() {
        webSocket?.close(1000, "Bye")
    }
}
