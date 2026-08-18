package com.feder.compose

import java.io.*
import java.net.Socket
import java.security.MessageDigest
import java.util.Base64
import kotlin.concurrent.thread
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.gson.Gson

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
    var onReceivedCallback: ((String) -> Unit)? = null
    var onReadCallback: ((String) -> Unit)? = null
    var onSendCallback: ((String, String) -> Unit)? = null
    
    private val gson = Gson()
    private val client = OkHttpClient()
    
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
    
    fun onMessage(callback: (String, String, Long, Int) -> Unit) {
        onMessageCallback = { json ->
            callback("unknown", json, System.currentTimeMillis() / 1000, 0)
        }
    }
    fun onStatus(callback: (String) -> Unit) { onStatusCallback = callback }
    fun onReceived(callback: (String) -> Unit) { onReceivedCallback = callback }
    fun onRead(callback: (String) -> Unit) { onReadCallback = callback }
    fun onSend(callback: (String, String) -> Unit) { onSendCallback = callback }
    
    fun connect(username: String, token: String) {
        logToServer("SW_SOCKET_CREATE: connecting...")
        thread {
            try {
                socket = Socket(serverUrl, port)
                logToServer("SW_SOCKET_CREATE: connected")
                socket?.soTimeout = 0
                socket?.tcpNoDelay = true
                socket?.keepAlive = true
                input = socket?.getInputStream()
                output = socket?.getOutputStream()
                logToServer("SW_SOCKET_CREATE: input=${input != null} output=${output != null}")
                
                val keyBytes = ByteArray(16)
                java.util.Random().nextBytes(keyBytes)
                val key = Base64.getEncoder().encodeToString(keyBytes)
                
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
                }
            } catch (e: Exception) {
                isConnected = false
                logToServer("SW_SOCKET_CONNECT_ERROR: msg=${e.message} cause=${e.cause}")
                onStatusCallback?.invoke("error: ${e.message}")
            }
        }
    }
    
    fun send(json: String) {
        logToServer("SW_SOCKET_SEND: isConnected=$isConnected output=${output != null}")
        try {
            val out = output
            if (out == null) {
                logToServer("SW_SOCKET_SEND_ERROR: output is NULL")
                return
            }
            
            val payload = json.toByteArray()
            val mask = ByteArray(4) { 
                (java.util.Random().nextInt(256) - 128).toByte() 
            }
            
            val headerSize: Int
            val frame: ByteArray
            if (payload.size < 126) {
                headerSize = 2
                frame = ByteArray(payload.size + headerSize + 4)
                frame[0] = 0x81.toByte()
                frame[1] = (0x80 or payload.size).toByte()
            } else if (payload.size < 65536) {
                headerSize = 4
                frame = ByteArray(payload.size + headerSize + 4)
                frame[0] = 0x81.toByte()
                frame[1] = (0x80 or 126).toByte()
                frame[2] = ((payload.size shr 8) and 0xFF).toByte()
                frame[3] = (payload.size and 0xFF).toByte()
            } else {
                headerSize = 10
                frame = ByteArray(payload.size + headerSize + 4)
                frame[0] = 0x81.toByte()
                frame[1] = (0x80 or 127).toByte()
                for (i in 0 until 8) {
                    frame[2 + i] = ((payload.size shr (56 - i * 8)) and 0xFF).toByte()
                }
            }
            
            System.arraycopy(mask, 0, frame, headerSize, 4)
            for (i in payload.indices) {
                frame[headerSize + 4 + i] = (payload[i].toInt() xor mask[i % 4].toInt()).toByte()
            }
            
            out.write(frame)
            out.flush()
            logToServer("SW_SOCKET_SEND_OK: ${frame.size} bytes")
            onSendCallback?.invoke("", "")
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
