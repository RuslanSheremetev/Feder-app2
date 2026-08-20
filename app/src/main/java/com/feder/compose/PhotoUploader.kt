package com.feder.compose

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit
import okio.Buffer

object PhotoUploader {
    private val loggingInterceptor = Interceptor { chain ->
        val request = chain.request()
        
        logToServer("=== OKHTTP REQUEST ===")
        logToServer("URL: ${request.url}")
        logToServer("METHOD: ${request.method}")
        logToServer("HEADERS: ${request.headers}")
        
        val requestBody = request.body
        if (requestBody != null) {
            logToServer("BODY_TYPE: ${requestBody.contentType()}")
            logToServer("BODY_LENGTH: ${requestBody.contentLength()}")
        }
        
        val response = chain.proceed(request)
        
        logToServer("=== OKHTTP RESPONSE ===")
        logToServer("CODE: ${response.code}")
        logToServer("MESSAGE: ${response.message}")
        logToServer("HEADERS: ${response.headers}")
        
        response
    }
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
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
            logToServer("UPLOAD_START: ${bytes.size} bytes")
            
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "photo.jpg", bytes.toRequestBody("image/jpeg".toMediaType()))
                .build()

            val request = Request.Builder()
                .url("http://2.26.71.102:8002/api/upload")
                .header("Authorization", "Bearer $token")
                .header("Connection", "close")
                .post(body)
                .build()

            logToServer("EXECUTING_REQUEST...")
            client.newCall(request).execute().use { response ->
                logToServer("GOT_RESPONSE: ${response.code}")
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: "{}"
                    logToServer("RESPONSE_BODY: $responseBody")
                    val json = org.json.JSONObject(responseBody)
                    return json.optString("url", null)
                } else {
                    logToServer("HTTP_ERROR: ${response.code}")
                    return null
                }
            }
        } catch (e: Exception) {
            logToServer("EXCEPTION: ${e.javaClass.simpleName}: ${e.message}")
            return null
        }
    }
}
