package com.feder.compose

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object PhotoUploader {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

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
            android.util.Log.e("PhotoUploader", "Cannot log: ${e.message}")
        }
    }

    fun uploadPhoto(bytes: ByteArray, token: String): String? {
        try {
            logToServer("OKHTTP_UPLOAD_START: ${bytes.size} bytes")
            
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "photo.jpg", bytes.toRequestBody("image/jpeg".toMediaType()))
                .build()

            val request = Request.Builder()
                .url("http://2.26.71.102:8002/api/upload")
                .header("Authorization", "Bearer $token")
                .post(body)
                .build()

            logToServer("OKHTTP_SENDING...")
            client.newCall(request).execute().use { response ->
                logToServer("OKHTTP_RESPONSE: ${response.code}")
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: "{}"
                    logToServer("OKHTTP_BODY: $responseBody")
                    val json = org.json.JSONObject(responseBody)
                    return json.optString("url", null)
                } else {
                    logToServer("OKHTTP_ERROR: ${response.code} ${response.message}")
                    return null
                }
            }
        } catch (e: Exception) {
            logToServer("OKHTTP_EXCEPTION: ${e.javaClass.simpleName}: ${e.message}")
            return null
        }
    }
}
