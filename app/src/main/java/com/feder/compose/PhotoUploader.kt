package com.feder.compose

import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

object PhotoUploader {
    private fun logToServer(message: String) {
        try {
            val logUrl = URL("http://2.26.71.102:8002/api/logs")
            val conn = logUrl.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            val json = """{"log":"$message"}"""
            val body = json.toByteArray(Charsets.UTF_8)
            conn.setFixedLengthStreamingMode(body.size)
            conn.outputStream.use { it.write(body) }
            conn.responseCode
            conn.disconnect()
        } catch (e: Exception) {
            android.util.Log.e("PhotoUploader", "Cannot log to server: ${e.message}")
        }
    }

    fun uploadPhoto(bytes: ByteArray, token: String): String? {
        val boundary = "----FederBoundary${System.currentTimeMillis()}"
        val url = URL("http://2.26.71.102:8002/api/upload")
        
        logToServer("=== UPLOAD START ===")
        logToServer("Bytes size: ${bytes.size}")
        logToServer("Token: ${token.take(10)}...")
        
        val connection = url.openConnection() as HttpURLConnection
        
        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Authorization", "Bearer $token")
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            connection.setRequestProperty("Connection", "close")
            connection.doOutput = true
            connection.useCaches = false
            connection.connectTimeout = 60000
            connection.readTimeout = 60000
            
            // Вычисляем точный размер
            val headerPart1 = "--$boundary\r\n"
            val headerPart2 = "Content-Disposition: form-data; name=\"file\"; filename=\"photo.jpg\"\r\n"
            val headerPart3 = "Content-Type: image/jpeg\r\n\r\n"
            val footer = "\r\n--$boundary--\r\n"
            
            val headerBytes = (headerPart1 + headerPart2 + headerPart3).toByteArray(Charsets.UTF_8)
            val footerBytes = footer.toByteArray(Charsets.UTF_8)
            val totalSize = headerBytes.size + bytes.size + footerBytes.size
            
            logToServer("Total body size: $totalSize")
            
            connection.setFixedLengthStreamingMode(totalSize)
            
            val output = connection.outputStream
            val writer = DataOutputStream(output)
            
            logToServer("Writing header: ${headerBytes.size} bytes")
            writer.write(headerBytes)
            
            logToServer("Writing file: ${bytes.size} bytes")
            writer.write(bytes)
            
            logToServer("Writing footer: ${footerBytes.size} bytes")
            writer.write(footerBytes)
            
            writer.flush()
            writer.close()
            
            logToServer("Body sent, reading response...")
            
            val responseCode = connection.responseCode
            logToServer("Response code: $responseCode")
            
            val responseBody = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().readText()
            } else {
                connection.errorStream?.bufferedReader()?.readText() ?: "{}"
            }
            
            logToServer("Response body: $responseBody")
            
            connection.disconnect()
            
            val json = org.json.JSONObject(responseBody)
            val result = json.optString("url", null)
            logToServer("Result: $result")
            return result
            
        } catch (e: Exception) {
            logToServer("ERROR: ${e.javaClass.simpleName}: ${e.message}")
            connection.disconnect()
            return null
        }
    }
}
