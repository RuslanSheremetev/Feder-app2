package com.feder.compose

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object PhotoUploader {
    private val client = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private fun logToServer(message: String) {
        try {
            val logUrl = java.net.URL("http://2.26.71.102:8002/api/logs")
            val conn = logUrl.openConnection() as java.net.HttpURLConnection
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
            android.util.Log.e("PhotoUploader", "Cannot log: ${e.message}")
        }
    }

    fun uploadPhoto(bytes: ByteArray, token: String): String? {
        try {
            logToServer("MULTIPART_START: ${bytes.size} bytes, token=${token.take(10)}")
            
            // Создаём multipart с явным contentLength
            val fileBody = bytes.toRequestBody("image/jpeg".toMediaType())
            
            val multipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "photo.jpg", fileBody)
                .build()
            
            logToServer("MULTIPART_BODY_SIZE: ${multipartBody.contentLength()}")
            
            val request = Request.Builder()
                .url("http://2.26.71.102:8002/api/upload")
                .header("Authorization", "Bearer $token")
                .header("Connection", "close")
                .post(multipartBody)
                .build()
            
            logToServer("MULTIPART_SENDING...")
            val response = client.newCall(request).execute()
            logToServer("MULTIPART_RESPONSE_CODE: ${response.code}")
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: "{}"
                logToServer("MULTIPART_RESPONSE_BODY: $responseBody")
                response.close()
                val json = org.json.JSONObject(responseBody)
                return json.optString("url", null)
            } else {
                logToServer("MULTIPART_ERROR: ${response.code} ${response.message}")
                response.close()
                return null
            }
        } catch (e: Exception) {
            logToServer("MULTIPART_EXCEPTION: ${e.javaClass.simpleName}: ${e.message}")
            return null
        }
    }
}
