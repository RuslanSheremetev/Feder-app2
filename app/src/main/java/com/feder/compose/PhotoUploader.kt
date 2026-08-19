package com.feder.compose

import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

object PhotoUploader {
    
    fun uploadPhoto(bytes: ByteArray, token: String): String? {
        val boundary = "----FederBoundary${System.currentTimeMillis()}"
        val url = URL("http://2.26.71.102:8002/api/upload")
        val connection = url.openConnection() as HttpURLConnection
        
        connection.requestMethod = "POST"
        connection.setRequestProperty("Authorization", "Bearer $token")
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        connection.setRequestProperty("Connection", "close")
        connection.setFixedLengthStreamingMode(bytes.size + 200)  // Приблизительный размер
        connection.doOutput = true
        connection.connectTimeout = 30000
        connection.readTimeout = 30000
        
        try {
            val output = connection.outputStream
            val writer = DataOutputStream(output)
            
            // Заголовок multipart
            writer.writeBytes("--$boundary\r\n")
            writer.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"photo.jpg\"\r\n")
            writer.writeBytes("Content-Type: image/jpeg\r\n\r\n")
            
            // Данные файла
            writer.write(bytes)
            writer.writeBytes("\r\n")
            
            // Завершение multipart
            writer.writeBytes("--$boundary--\r\n")
            writer.flush()
            writer.close()
            
            // Читаем ответ
            val responseCode = connection.responseCode
            val responseBody = if (responseCode == 200) {
                connection.inputStream.bufferedReader().readText()
            } else {
                connection.errorStream?.bufferedReader()?.readText() ?: "{}"
            }
            
            connection.disconnect()
            
            // Парсим JSON ответ
            val json = org.json.JSONObject(responseBody)
            return json.optString("url", null)
            
        } catch (e: Exception) {
            android.util.Log.e("PhotoUploader", "Upload error: ${e.message}", e)
            connection.disconnect()
            return null
        }
    }
}
