package com.feder.compose

import java.io.DataOutputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

object PhotoUploader {
    fun uploadPhoto(bytes: ByteArray, token: String): String? {
        val boundary = "----FederBoundary${System.currentTimeMillis()}"
        val url = URL("http://2.26.71.102:8002/api/upload")
        val connection = url.openConnection() as HttpURLConnection
        
        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Authorization", "Bearer $token")
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            connection.setRequestProperty("Connection", "close")
            connection.doOutput = true
            connection.connectTimeout = 60000
            connection.readTimeout = 60000
            
            // Вычисляем точный размер body
            val headerPart1 = "--$boundary\r\n"
            val headerPart2 = "Content-Disposition: form-data; name=\"file\"; filename=\"photo.jpg\"\r\n"
            val headerPart3 = "Content-Type: image/jpeg\r\n\r\n"
            val footer = "\r\n--$boundary--\r\n"
            
            val totalSize = headerPart1.length + headerPart2.length + headerPart3.length + 
                           bytes.size + footer.length
            
            android.util.Log.d("PhotoUploader", "Uploading ${bytes.size} bytes, total body: $totalSize")
            
            // Устанавливаем точный размер
            connection.setFixedLengthStreamingMode(totalSize)
            
            val output = connection.outputStream
            val writer = DataOutputStream(output)
            
            // Пишем multipart
            writer.writeBytes(headerPart1)
            writer.writeBytes(headerPart2)
            writer.writeBytes(headerPart3)
            writer.write(bytes)
            writer.writeBytes(footer)
            writer.flush()
            writer.close()
            
            // Читаем ответ
            val responseCode = connection.responseCode
            android.util.Log.d("PhotoUploader", "Response code: $responseCode")
            
            val responseBody = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().readText()
            } else {
                connection.errorStream?.bufferedReader()?.readText() ?: "{}"
            }
            
            android.util.Log.d("PhotoUploader", "Response body: $responseBody")
            
            connection.disconnect()
            
            val json = org.json.JSONObject(responseBody)
            return json.optString("url", null)
            
        } catch (e: Exception) {
            android.util.Log.e("PhotoUploader", "Upload error: ${e.message}", e)
            connection.disconnect()
            return null
        }
    }
}
