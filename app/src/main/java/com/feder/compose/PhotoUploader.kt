package com.feder.compose

import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

object PhotoUploader {
    fun uploadPhoto(bytes: ByteArray, token: String): String? {
        val boundary = "----FederBoundary${System.currentTimeMillis()}"
        val url = URL("http://2.26.71.102:8002/api/upload")
        
        android.util.Log.d("PhotoUploader", "=== START UPLOAD ===")
        android.util.Log.d("PhotoUploader", "Bytes: ${bytes.size}")
        android.util.Log.d("PhotoUploader", "Token: ${token.take(10)}...")
        android.util.Log.d("PhotoUploader", "URL: $url")
        
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
            
            // Вычисляем точный размер body
            val headerPart1 = "--$boundary\r\n"
            val headerPart2 = "Content-Disposition: form-data; name=\"file\"; filename=\"photo.jpg\"\r\n"
            val headerPart3 = "Content-Type: image/jpeg\r\n\r\n"
            val footer = "\r\n--$boundary--\r\n"
            
            val headerBytes = (headerPart1 + headerPart2 + headerPart3).toByteArray(Charsets.UTF_8)
            val footerBytes = footer.toByteArray(Charsets.UTF_8)
            val totalSize = headerBytes.size + bytes.size + footerBytes.size
            
            android.util.Log.d("PhotoUploader", "Total body size: $totalSize")
            
            // Устанавливаем точный размер
            connection.setFixedLengthStreamingMode(totalSize)
            
            val output = connection.outputStream
            val writer = DataOutputStream(output)
            
            android.util.Log.d("PhotoUploader", "Writing header...")
            writer.write(headerBytes)
            
            android.util.Log.d("PhotoUploader", "Writing file data: ${bytes.size} bytes")
            writer.write(bytes)
            
            android.util.Log.d("PhotoUploader", "Writing footer...")
            writer.write(footerBytes)
            
            writer.flush()
            writer.close()
            output.close()
            
            android.util.Log.d("PhotoUploader", "Body sent, reading response...")
            
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
            val result = json.optString("url", null)
            android.util.Log.d("PhotoUploader", "Result URL: $result")
            return result
            
        } catch (e: Exception) {
            android.util.Log.e("PhotoUploader", "ERROR: ${e.javaClass.simpleName}: ${e.message}", e)
            
            // Пробуем получить больше информации
            try {
                val errorStream = connection.errorStream
                if (errorStream != null) {
                    val errorBody = errorStream.bufferedReader().readText()
                    android.util.Log.e("PhotoUploader", "Error body: $errorBody")
                }
            } catch (e2: Exception) {
                android.util.Log.e("PhotoUploader", "Cannot read error stream: ${e2.message}")
            }
            
            connection.disconnect()
            return null
        }
    }
}
