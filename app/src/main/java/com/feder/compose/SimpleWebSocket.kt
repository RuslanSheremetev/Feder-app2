package com.feder.compose

import java.io.*
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
    private var output: OutputStream? = null
    var isConnected = false
    var onMessageCallback: ((String) -> Unit)? = null
    var onStatusCallback: ((String) -> Unit)? = null
    
    fun connect(username: String, token: String) {
        thread {
            try {
                socket = Socket(serverUrl, port)
                input = socket?.getInputStream()
                output = socket?.getOutputStream()
                
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
                
                output?.write(handshake.toByteArray())
                output?.flush()
                
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
                
                if (response.contains("101")) {
                    isConnected = true
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
                onStatusCallback?.invoke("error: ${e.message}")
            }
        }
    }
    
    fun send(json: String) {
        try {
            val payload = json.toByteArray()
            val frame = ByteArray(payload.size + 2)
            frame[0] = 0x81.toByte() // FIN + text
            frame[1] = payload.size.toByte()
            System.arraycopy(payload, 0, frame, 2, payload.size)
            
            output?.write(frame)
            output?.flush()
        } catch (e: Exception) {
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
