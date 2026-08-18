package com.feder.compose

import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class WebSocketManager(
    private val serverUrl: String = "2.26.71.102",
    private val port: Int = 8002
) {
    private var webSocket: WebSocket? = null
    private var isConnected = false
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
    
    private var onMessageCallback: ((String, String, Long, Int) -> Unit)? = null
    private var onReceivedCallback: ((String) -> Unit)? = null
    private var onReadCallback: ((String) -> Unit)? = null
    private var onStatusCallback: ((String) -> Unit)? = null
    private var onSendCallback: ((String, String) -> Unit)? = null
    private var onTypingCallback: ((String) -> Unit)? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    
    private fun logToServer(message: String) {
        try {
            val logJson = gson.toJson(mapOf("log" to message))
            val logBody = logJson.toRequestBody("application/json".toMediaType())
            val logRequest = Request.Builder()
                .url("http://$serverUrl:$port/api/logs")
                .post(logBody)
                .build()
            client.newCall(logRequest).enqueue(object : Callback {
                override fun onFailure(call: Call, e: java.io.IOException) {}
                override fun onResponse(call: Call, response: Response) { response.close() }
            })
        } catch (_: Exception) {}
    }
    
    fun connect(username: String, token: String) {
        logToServer("WS_CONNECT: username=$username token=${token.take(20)}...")
        if (webSocket != null) {
            webSocket?.close(1000, "reconnect")
            webSocket = null
        }
        val url = "ws://$serverUrl:$port/ws/$username?token=$token"
        val request = Request.Builder()
            .url(url)
            .header("Sec-WebSocket-Extensions", "")
            .build()
        try {
            webSocket = client.newWebSocket(request, object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        this@WebSocketManager.webSocket = webSocket
                        isConnected = true
                        logToServer("WS_OPEN: code=${response.code}")
                        android.util.Log.d("WS", "OPEN: ${response.code}")
                        val logJson = gson.toJson(mapOf("log" to "WS_OPEN: code=${response.code}"))
                        val logBody = logJson.toRequestBody("application/json".toMediaType())
                        val httpClient = OkHttpClient()
                        httpClient.newCall(Request.Builder().url("http://2.26.71.102:8002/api/logs").post(logBody).build()).enqueue(object : Callback {
                            override fun onFailure(call: Call, e: java.io.IOException) {}
                            override fun onResponse(call: Call, response: Response) { response.close() }
                        })
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
                            } else if (obj.get("type")?.asString == "typing") {
                                val from = obj.get("from_user")?.asString
                                if (from != null) onTypingCallback?.invoke(from)
                            } else if (obj.get("type")?.asString == "message") {
                                val sender = obj.get("from_user")?.asString ?: "unknown"
                                val msgText = obj.get("text")?.asString ?: return
                                val timeVal = obj.get("timeVal")?.asLong ?: obj.get("time")?.asLong ?: 0L
                                val msgId = obj.get("id")?.asInt ?: 0
                                onMessageCallback?.invoke(sender, msgText, timeVal, msgId)
                            }
                        } catch (e: Exception) { }
                    }
                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        logToServer("WS_FAIL: ${t.message} response=${response?.code}")
                        android.util.Log.e("WS", "FAIL: ${t.message}")
                        val logJson = gson.toJson(mapOf("log" to "WS_FAIL: ${t.message}"))
                        val logBody = logJson.toRequestBody("application/json".toMediaType())
                        val httpClient = OkHttpClient()
                        httpClient.newCall(Request.Builder().url("http://2.26.71.102:8002/api/logs").post(logBody).build()).enqueue(object : Callback {
                            override fun onFailure(call: Call, e: java.io.IOException) {}
                            override fun onResponse(call: Call, response: Response) { response.close() }
                        })
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
    
    fun sendTyping(toUser: String) {
        send("typing", "", toUser)
    }
    fun sendPhoto(imageUrls: List<String>, toUser: String, caption: String = "") {
        logToServer("WS_SEND_PHOTO: images=${imageUrls.size} to=$toUser caption=$caption isConnected=$isConnected socket=${webSocket != null}")
        val combinedText = if (caption.isNotEmpty()) {
            caption + "\n" + imageUrls.joinToString(",")
        } else {
            imageUrls.joinToString(",")
        }
        val json = gson.toJson(mapOf(
            "type" to "message",
            "text" to combinedText,
            "to_user" to toUser,
            "imageUrls" to imageUrls,
            "caption" to caption
        ))
        if (webSocket != null) {
            webSocket?.send(json)
            android.util.Log.d("WS", "PHOTO_SENT: $json")
            onSendCallback?.invoke(toUser, combinedText)
        } else {
            android.util.Log.e("WS", "WebSocket NULL in sendPhoto! Retrying...")
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                if (webSocket != null) {
                    webSocket?.send(json)
                    android.util.Log.d("WS", "PHOTO_SENT after retry: $json")
                    onSendCallback?.invoke(toUser, combinedText)
                } else {
                    android.util.Log.e("WS", "STILL NULL in sendPhoto")
                }
            }, 1000)
        }
    }
    
    fun send(type: String, text: String, toUser: String) {
        val json = gson.toJson(mapOf("type" to type, "text" to text, "to_user" to toUser))
        logToServer("WS_SEND: isConnected=$isConnected socket=${webSocket != null} json=$json")
        if (webSocket != null) {
            webSocket?.send(json)
            android.util.Log.d("WS", "SENT: $json")
            if (type == "message") {
                onSendCallback?.invoke(toUser, text)
            }
        } else {
            android.util.Log.e("WS", "WebSocket is NULL! Cannot send: $json")
            // Пробуем переподключиться и отправить
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                if (webSocket != null) {
                    webSocket?.send(json)
                    android.util.Log.d("WS", "SENT after retry: $json")
                } else {
                    android.util.Log.e("WS", "STILL NULL after retry")
                }
            }, 1000)
        }
    }
    
    fun disconnect() {
        webSocket?.close(1000, "disconnect")
        webSocket = null
    }
    
    fun onMessage(callback: (String, String, Long, Int) -> Unit) {
        onMessageCallback = callback
    }
    
    fun onReceived(callback: (String) -> Unit) { onReceivedCallback = callback }
    fun onRead(callback: (String) -> Unit) { onReadCallback = callback }
    fun onStatus(callback: (String) -> Unit) {
        onStatusCallback = callback
    }
    fun onTyping(callback: (String) -> Unit) { onTypingCallback = callback }
    fun onSend(callback: (String, String) -> Unit) {
        onSendCallback = callback
    }
}
