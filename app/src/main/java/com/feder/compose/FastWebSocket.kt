package com.feder.compose

import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.net.URI
import java.security.MessageDigest
import java.util.Base64
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class FastWebSocket {
    private var socket: Socket? = null
    private var input: InputStream? = null
    private var output: OutputStream? = null
    private val isConnected = AtomicBoolean(false)
    
    var onMessage: ((String) -> Unit)? = null
    var onOpen: (() -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    var onClose: (() -> Unit)? = null
    
    fun connect(username: String, token: String) {
        thread {
            try {
                val uri = URI("ws://2.26.71.102:8002/ws/$username?token=$token")
                val host = uri.host
                val port = if (uri.port > 0) uri.port else 8002
                
                socket = Socket(host, port)
                socket?.soTimeout = 0
                socket?.tcpNoDelay = true
                socket?.keepAlive = true
                
                input = socket?.getInputStream()
                output = socket?.getOutputStream()
                
                // WebSocket handshake
                val key = Base64.getEncoder().encodeToString(ByteArray(16) { (Math.random() * 256).toInt().toByte() })
                val handshake = buildString {
                    append("GET /ws/$username?token=$token HTTP/1.1\r\n")
                    append("Host: $host:$port\r\n")
                    append("Upgrade: websocket\r\n")
                    append("Connection: Upgrade\r\n")
                    append("Sec-WebSocket-Key: $key\r\n")
                    append("Sec-WebSocket-Version: 13\r\n")
                    append("\r\n")
                }
                
                output?.write(handshake.toByteArray())
                output?.flush()
                
                // Читаем ответ handshake
                val response = StringBuilder()
                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (true) {
                    bytesRead = input?.read(buffer) ?: -1
                    if (bytesRead <= 0) break
                    response.append(String(buffer, 0, bytesRead))
                    if (response.contains("\r\n\r\n")) break
                }
                
                if (response.contains("101")) {
                    isConnected.set(true)
                    onOpen?.invoke()
                    startReading()
                } else {
                    onError?.invoke("Handshake failed: $response")
                }
            } catch (e: Exception) {
                isConnected.set(false)
                onError?.invoke(e.message ?: "Connection failed")
            }
        }
    }
    
    private fun startReading() {
        thread {
            try {
                val buffer = ByteArray(1024 * 1024) // 1MB
                while (isConnected.get()) {
                    val first = input?.read() ?: break
                    val second = input?.read() ?: break
                    
                    val opcode = first and 0x0F
                    val length = second and 0x7F
                    
                    // Определяем длину
                    var payloadLength = length.toLong()
                    if (length == 126) {
                        payloadLength = ((input?.read() ?: 0) shl 8 or (input?.read() ?: 0)).toLong()
                    } else if (length == 127) {
                        payloadLength = 0
                        for (i in 0 until 8) {
                            payloadLength = (payloadLength shl 8) or (input?.read()?.toLong() ?: 0)
                        }
                    }
                    
                    // Читаем payload
                    val payload = ByteArray(payloadLength.toInt())
                    var totalRead = 0
                    while (totalRead < payloadLength) {
                        val read = input?.read(payload, totalRead, (payloadLength - totalRead).toInt()) ?: -1
                        if (read <= 0) break
                        totalRead += read
                    }
                    
                    when (opcode) {
                        0x1 -> { // Text frame
                            val text = String(payload, Charsets.UTF_8)
                            onMessage?.invoke(text)
                        }
                        0x8 -> { // Close
                            isConnected.set(false)
                            onClose?.invoke()
                            break
                        }
                        0x9 -> { // Ping
                            sendFrame(0xA, payload) // Pong
                        }
                    }
                }
            } catch (e: Exception) {
                isConnected.set(false)
                onError?.invoke(e.message ?: "Read error")
            }
        }
    }
    
    fun send(text: String): Boolean {
        if (!isConnected.get()) return false
        return try {
            sendFrame(0x1, text.toByteArray(Charsets.UTF_8))
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun sendFrame(opcode: Int, payload: ByteArray) {
        val output = output ?: return
        synchronized(output) {
            output.write(0x80 or opcode)
            
            if (payload.size < 126) {
                output.write(payload.size)
            } else if (payload.size < 65536) {
                output.write(126)
                output.write((payload.size shr 8) and 0xFF)
                output.write(payload.size and 0xFF)
            } else {
                output.write(127)
                for (i in 7 downTo 0) {
                    output.write((payload.size shr (i * 8)) and 0xFF)
                }
            }
            
            output.write(payload)
            output.flush()
        }
    }
    
    fun close() {
        try {
            sendFrame(0x8, ByteArray(0))
            isConnected.set(false)
            socket?.close()
        } catch (_: Exception) {}
    }
}
