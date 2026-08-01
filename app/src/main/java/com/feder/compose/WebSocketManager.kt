package com.feder.compose

import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.*
import java.util.concurrent.TimeUnit

class WebSocketManager(
    private val serverUrl: String = "2.26.71.102",
    private val port: Int = 8002
) {
    private var webSocket: WebSocket? = null
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
    
    private var onMessageCallback: ((String, String, Long) -> Unit)? = null
    private var onReadCallback: ((String) -> Unit)? = null
    private var onStatusCallback: ((String) -> Unit)? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    
    fun connect(username: String, token: String) {
        disconnect()
        val url = "ws://$serverUrl:$port/ws/$username?token=$token"
        mainHandler.post {
            try {
                webSocket = client.newWebSocket(Request.Builder().url(url).build(), object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        onStatusCallback?.invoke("connected")
                    }
                    override fun onMessage(webSocket: WebSocket, text: String) {
                        try {
                            val obj = JsonParser.parseString(text).asJsonObject
                            if (obj.get("type")?.asString == "received") {
                                val from = obj.get("from")?.asString
                                if (from != null) { onReceivedCallback?.invoke(from) }
                            } else if (obj.get("type")?.asString == "read") {
                                val from = obj.get("from")?.asString
                                if (from != null) { onReadCallback?.invoke(from) }
                            } else if (obj.get("type")?.asString == "message") {
                                val sender = obj.get("from_user")?.asString ?: "unknown"
                                val msgText = obj.get("text")?.asString ?: return
                                val timeVal = obj.get("timeVal")?.asLong ?: obj.get("time")?.asLong ?: 0L
                                onMessageCallback?.invoke(sender, msgText, timeVal)
                            }
                        } catch (e: Exception) { }
                    }
                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        onStatusCallback?.invoke("error: ${t.message}")
                    }
                    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                        webSocket.close(1000, null)
                    }
                })
            } catch (e: Exception) {
                onStatusCallback?.invoke("error: ${e.message}")
            }
        }
    }
    
    fun send(type: String, text: String, toUser: String) {
        val json = gson.toJson(mapOf("type" to type, "text" to text, "to_user" to toUser))
        webSocket?.send(json)
    }
    
    fun disconnect() {
        webSocket?.close(1000, "disconnect")
        webSocket = null
    }
    
    fun onMessage(callback: (String, String, Long) -> Unit) {
        onMessageCallback = callback
    }
    
    fun onRead(callback: (String) -> Unit) { onReadCallback = callback }
    fun onStatus(callback: (String) -> Unit) {
        onStatusCallback = callback
    }
}
