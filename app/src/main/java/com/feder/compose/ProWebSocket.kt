package com.feder.compose

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * ProWebSocket — профессиональная реализация WebSocket клиента.
 * Быстрее OkHttp за счет:
 * - Прямой сокет без HTTP-стека
 * - Буферизированный I/O
 * - Очередь отправки (неблокирующая)
 * - Минимальные аллокации
 * - Прямой контроль frame
 */
class ProWebSocket {
    private var socket: Socket? = null
    private var input: BufferedInputStream? = null
    private var output: BufferedOutputStream? = null
    private val isConnected = AtomicBoolean(false)
    private val sendQueue = ConcurrentLinkedQueue<ByteArray>()
    
    var onMessage: ((String) -> Unit)? = null
    var onOpen: (() -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    var onClose: (() -> Unit)? = null
    
    fun connect(username: String, token: String, host: String = "2.26.71.102", port: Int = 8002) {
        thread(priority = Thread.MAX_PRIORITY) {
            try {
                socket = Socket()
                socket?.tcpNoDelay = true
                socket?.keepAlive = true
                socket?.receiveBufferSize = 1024 * 1024
                socket?.sendBufferSize = 1024 * 1024
                socket?.connect(InetSocketAddress(host, port), 5000)
                
                input = BufferedInputStream(socket?.getInputStream(), 64 * 1024)
                output = BufferedOutputStream(socket?.getOutputStream(), 64 * 1024)
                
                // Handshake
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
                
                output?.write(handshake.toByteArray(StandardCharsets.UTF_8))
                output?.flush()
                
                // Читаем ответ
                val response = ByteArray(4096)
                val responseStr = StringBuilder()
                var total = 0
                while (total < response.size) {
                    val read = input?.read(response, total, response.size - total) ?: -1
                    if (read <= 0) break
                    total += read
                    responseStr.append(String(response, 0, read, StandardCharsets.UTF_8))
                    if (responseStr.contains("\r\n\r\n")) break
                }
                
                if (responseStr.contains("101")) {
                    isConnected.set(true)
                    onOpen?.invoke()
                    startReader()
                    startWriter()
                } else {
                    onError?.invoke("Handshake failed")
                }
            } catch (e: Exception) {
                isConnected.set(false)
                onError?.invoke(e.message ?: "Connection failed")
            }
        }
    }
    
    private fun startReader() {
        thread(priority = Thread.MAX_PRIORITY) {
            try {
                while (isConnected.get()) {
                    val frame = readFrame() ?: break
                    
                    when (frame.opcode) {
                        0x1 -> onMessage?.invoke(String(frame.payload, StandardCharsets.UTF_8))
                        0x8 -> {
                            isConnected.set(false)
                            onClose?.invoke()
                            break
                        }
                        0x9 -> sendFrame(0xA, frame.payload) // Pong
                    }
                }
            } catch (e: Exception) {
                isConnected.set(false)
                onError?.invoke(e.message ?: "Read error")
            }
        }
    }
    
    private fun startWriter() {
        thread(priority = Thread.MAX_PRIORITY) {
            while (isConnected.get()) {
                val payload = sendQueue.poll() ?: run {
                    Thread.sleep(1)
                    null
                }
                try {
                    sendFrame(0x1, payload!!)
                } catch (_: Exception) {}
            }
        }
    }
    
    fun send(text: String): Boolean {
        if (!isConnected.get()) return false
        sendQueue.add(text.toByteArray(StandardCharsets.UTF_8))
        return true
    }
    
    private fun readFrame(): Frame? {
        val input = input ?: return null
        
        val first = input.read()
        if (first == -1) return null
        
        val second = input.read()
        if (second == -1) return null
        
        val opcode = first and 0x0F
        val masked = (second and 0x80) != 0
        var length = (second and 0x7F).toLong()
        
        if (length == 126L) {
            length = ((input.read() shl 8) or input.read()).toLong()
        } else if (length == 127L) {
            length = 0
            for (i in 0 until 8) {
                length = (length shl 8) or input.read().toLong()
            }
        }
        
        val maskKey = if (masked) ByteArray(4) { input.read().toByte() } else null
        
        val payload = ByteArray(length.toInt())
        var totalRead = 0
        while (totalRead < length) {
            val read = input.read(payload, totalRead, (length - totalRead).toInt())
            if (read <= 0) break
            totalRead += read
        }
        
        if (maskKey != null) {
            for (i in payload.indices) {
                payload[i] = (payload[i].toInt() xor maskKey[i % 4].toInt()).toByte()
            }
        }
        
        return Frame(opcode, payload)
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
    
    private data class Frame(val opcode: Int, val payload: ByteArray)
}
