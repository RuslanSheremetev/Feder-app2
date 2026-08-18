package com.feder.compose

import java.io.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.gson.Gson
import java.net.Socket
import java.security.MessageDigest
import java.util.Base64
import kotlin.concurrent.thread

class SimpleWebSocket(
    private val serverUrl: String = "2.26.71.102",
    private val port: Int = 8002
) {
    private var socket: Socket? = null
    private var input: InputStream? = null
    @Volatile
    private var output: OutputStream? = null
    var isConnected = false
    var onMessageCallback: ((String) -> Unit)? = null
    var onStatusCallback: ((String) -> Unit)? = null
    
    private fun logToServer(message: String) {
        try {
            val logJson = gson.toJson(mapOf("log" to message))
            val logBody = logJson.toRequestBody("application/json".toMediaType())
            val logRequest = Request.Builder()
                .url("http://$serverUrl:$port/api/logs")
                .post(logBody)
                .build()
            client.newCall(logRequest).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {}
                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) { response.close() }
            })
        } catch (_: Exception) {}
    }
    var onReceivedCallback: ((String) -> Unit)? = null
    var onReadCallback: ((String) -> Unit)? = null
    var onSendCallback: ((String, String) -> Unit)? = null
    
    fun onMessage(callback: (String, String, Long, Int) -> Unit) {
        onMessageCallback = { json ->
            // Простая обработка
            callback("unknown", json, System.currentTimeMillis() / 1000, 0)
        }
    }
    fun onStatus(callback: (String) -> Unit) { onStatusCallback = callback }
    fun onReceived(callback: (String) -> Unit) { onReceivedCallback = callback }
    fun onRead(callback: (String) -> Unit) { onReadCallback = callback }
    fun onSend(callback: (String, String) -> Unit) { onSendCallback = callback }
    
    private val gson = Gson()
    private val client = OkHttpClient()
    
    fun connect(username: String, token: String) {
        logToServer("SW_SOCKET_CONNECT: username=$username")
        thread {
            try {
                logToServer("SW_SOCKET_CREATE: connecting...")
                socket = Socket(serverUrl, port)
                logToServer("SW_SOCKET_CREATE: connected to $serverUrl:$port")
                socket?.soTimeout = 0
                socket?.tcpNoDelay = true
                socket?.keepAlive = true
                input = socket?.getInputStream()
                output = socket?.getOutputStream()
                logToServer("SW_SOCKET_CREATE: input=${input != null} output=${output != null}")
                
                // Генерируем ключ
                val keyBytes = ByteArray(16)
                java.util.Random().nextBytes(keyBytes)
                val key = Base64.getEncoder().encodeToString(keyBytes)
                
                // Handshake
                val handshake = buildString {
                    append("GET /ws/$username?token=$token HTTP/1.1\r\n")
                    append("Host: $serverUrl:$port\r\n")
                    append("Upgrade: websocket\r\n")
                    append("Connection: Upgrade\r\n")
                    append("Sec-WebSocket-Key: $key\r\n")
                    append("Sec-WebSocket-Version: 13\r\n")
                    append("\r\n")
                }
                
                logToServer("SW_SOCKET_HANDSHAKE_SEND: ${handshake.length} bytes")
                output?.write(handshake.toByteArray())
                output?.flush()
                logToServer("SW_SOCKET_HANDSHAKE_SENT")
                
                // Читаем ответ
                val response = StringBuilder()
                val buffer = ByteArray(1024)
                var totalRead = 0
                while (totalRead < 4096) {
                    val read = input?.read(buffer) ?: -1
                    if (read == -1) break
                    response.append(String(buffer, 0, read))
                    totalRead += read
                    if (response.contains("\r\n\r\n")) break
                }
                
                logToServer("SW_SOCKET_HANDSHAKE: response=${response.take(50)}")
                if (response.contains("101")) {
                    isConnected = true
                    logToServer("SW_SOCKET_OPEN")
                    onStatusCallback?.invoke("connected")
                    
                    // Читаем сообщения
                    while (isConnected) {
                        val firstByte = input?.read() ?: break
                        if (firstByte == -1) break
                        
                        val secondByte = input?.read() ?: break
                        if (secondByte == -1) break
                        
                        var msgLen = secondByte and 0x7F
                        var pos = 2
                        
                        if (msgLen == 126) {
                            val b1 = input?.read() ?: break
                            val b2 = input?.read() ?: break
                            msgLen = (b1 shl 8) or b2
                            pos = 4
                        } else if (msgLen == 127) {
                            msgLen = 0
                            for (i in 0 until 8) {
                                val b = input?.read() ?: break
                                msgLen = (msgLen shl 8) or b
                            }
                            pos = 10
                        }
                        
                        // Проверяем mask bit
                        val masked = (secondByte and 0x80) != 0
                        val mask = ByteArray(4)
                        if (masked) {
                            for (i in 0 until 4) {
                                mask[i] = (input?.read() ?: break).toByte()
                            }
                            pos += 4
                        }
                        
                        val msgBytes = ByteArray(msgLen)
                        var bytesRead = 0
                        while (bytesRead < msgLen) {
                            val read = input?.read(msgBytes, bytesRead, msgLen - bytesRead) ?: break
                            if (read == -1) break
                            bytesRead += read
                        }
                        
                        if (masked) {
                            for (i in 0 until msgLen) {
                                msgBytes[i] = (msgBytes[i].toInt() xor mask[i % 4].toInt()).toByte()
                            }
                        }
                        
                        val message = String(msgBytes)
                        onMessageCallback?.invoke(message)
                    }
                } else {
                    onStatusCallback?.invoke("error: handshake failed")
                }
            } catch (e: Exception) {
                isConnected = false
                logToServer("SW_SOCKET_CONNECT_ERROR: ${e.message} cause=${e.cause}")
                onStatusCallback?.invoke("error: ${e.message}")
            }
        }
    }
    
    @Synchronized
    fun send(json: String) {
        logToServer("SW_SOCKET_SEND: isConnected=$isConnected json=$json")
        try {
            if (output == null) {
                logToServer("SW_SOCKET_SEND_ERROR: output is NULL!")
                onStatusCallback?.invoke("error: output is null")
                return
            }
            
            val payload = json.toByteArray()
            val mask = ByteArray(4)
            java.util.Random().nextBytes(mask)
            
            val frame = ByteArray(payload.size + 6)
            frame[0] = 0x81.toByte() // FIN + text
            frame[1] = (0x80 or payload.size).toByte() // Mask + length
            System.arraycopy(mask, 0, frame, 2, 4) // Mask key
            
            // Маскируем данные
            for (i in payload.indices) {
                frame[6 + i] = (payload[i].toInt() xor mask[i % 4].toInt()).toByte()
            }
            
            output?.write(frame)
            output?.flush()
            logToServer("SW_SOCKET_SEND_OK: ${frame.size} bytes written")
        } catch (e: Exception) {
            logToServer("SW_SOCKET_SEND_ERROR: msg=${e.message} cause=${e.cause} socket=${socket?.isConnected} closed=${socket?.isClosed}")
            onStatusCallback?.invoke("error: ${e.message}")
        }
    }
    
    fun disconnect() {
        isConnected = false
        try {
            socket?.close()
        } catch (_: Exception) {}
    }
}
