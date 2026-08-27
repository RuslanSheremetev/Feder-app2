package com.feder.compose

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class ProWebSocket {
    private var socket: Socket? = null
    private var input: BufferedInputStream? = null
    private var output: BufferedOutputStream? = null
    private val isConnected = AtomicBoolean(false)
    private val sendQueue = ConcurrentLinkedQueue<ByteArray>()
    private var username: String = ""
    private var token: String = ""
    private var host: String = "2.26.71.102"
    private var port: Int = 8002
    private var reconnectThread: Thread? = null
    
    var onMessage: ((String) -> Unit)? = null
    var onOpen: (() -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    var onClose: (() -> Unit)? = null
    
    private fun logToDb(message: String) {
        thread {
            try {
                val url = URL("http://2.26.71.102:8004/api/logs")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")
                val json = """{"log":"$message"}"""
                conn.outputStream.write(json.toByteArray())
                conn.outputStream.flush()
                conn.inputStream.close()
            } catch (_: Exception) {}
        }
    }
    
    fun connect(username: String, token: String, host: String = "2.26.71.102", port: Int = 8002) {
        this.username = username
        this.token = token
        this.host = host
        this.port = port
        
        thread(priority = Thread.MAX_PRIORITY) {
            while (true) {
                if (doConnect()) {
                    startReader()
                    startWriter()
                    break
                }
                Thread.sleep(2000) // Пауза перед переподключением
            }
        }
    }
    
    private fun doConnect(): Boolean {
        try {
            socket = Socket()
            socket?.tcpNoDelay = true
            socket?.keepAlive = true
            socket?.receiveBufferSize = 1024 * 1024
            socket?.sendBufferSize = 1024 * 1024
            socket?.connect(InetSocketAddress(host, port), 5000)
            
            input = BufferedInputStream(socket?.getInputStream(), 64 * 1024)
            output = BufferedOutputStream(socket?.getOutputStream(), 64 * 1024)
            
            val key = Base64.getEncoder().encodeToString(ByteArray(16) { (Math.random() * 256).toInt().toByte() })
            val handshake = "GET /ws/$username?token=$token HTTP/1.1\r\n" +
                "Host: $host:$port\r\n" +
                "Upgrade: websocket\r\n" +
                "Connection: Upgrade\r\n" +
                "Sec-WebSocket-Key: $key\r\n" +
                "Sec-WebSocket-Version: 13\r\n\r\n"
            
            output?.write(handshake.toByteArray(StandardCharsets.UTF_8))
            output?.flush()
            
            val response = StringBuilder()
            val buffer = ByteArray(1024)
            while (true) {
                val read = input?.read(buffer) ?: -1
                if (read <= 0) break
                response.append(String(buffer, 0, read, StandardCharsets.UTF_8))
                if (response.contains("\r\n\r\n")) break
            }
            
            logToDb("PRO_WS_HANDSHAKE: $response")
            if (response.contains("101")) {
                isConnected.set(true)
                logToDb("PRO_WS_CONNECTED")
                onOpen?.invoke()
                return true
            }
        } catch (_: Exception) {}
        return false
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
                            scheduleReconnect()
                            return@thread
                        }
                        0x9 -> sendFrame(0xA, frame.payload)
                    }
                }
            } catch (_: Exception) {
                isConnected.set(false)
                scheduleReconnect()
            }
        }
    }
    
    private fun scheduleReconnect() {
        if (reconnectThread?.isAlive == true) return
        reconnectThread = thread {
            Thread.sleep(1000)
            try {
                socket?.close()
            } catch (_: Exception) {}
            isConnected.set(false)
            connect(username, token, host, port)
        }
    }
    
    private fun startWriter() {
        thread(priority = Thread.MAX_PRIORITY) {
            while (isConnected.get()) {
                val payload = sendQueue.poll()
                if (payload == null) {
                    Thread.sleep(1)
                    continue
                }
                try {
                    sendFrame(0x1, payload)
                } catch (_: Exception) {}
            }
        }
    }
    
    fun send(text: String): Boolean {
        if (!isConnected.get()) { logToDb("PRO_WS_SEND_FAILED: not connected"); return false }
        logToDb("PRO_WS_SEND_QUEUED: $text")
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
            
            // Маска для клиентских frame
            val maskKey = ByteArray(4) { (Math.random() * 256).toInt().toByte() }
            
            if (payload.size < 126) {
                output.write(0x80 or payload.size)
            } else if (payload.size < 65536) {
                output.write(0x80 or 126)
                output.write((payload.size shr 8) and 0xFF)
                output.write(payload.size and 0xFF)
            } else {
                output.write(0x80 or 127)
                for (i in 7 downTo 0) {
                    output.write((payload.size shr (i * 8)) and 0xFF)
                }
            }
            
            output.write(maskKey)
            val masked = ByteArray(payload.size)
            for (i in payload.indices) {
                masked[i] = (payload[i].toInt() xor maskKey[i % 4].toInt()).toByte()
            }
            output.write(masked)
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
