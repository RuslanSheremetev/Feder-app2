package com.feder.compose

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object PhotoUploader {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private fun detectMimeType(bytes: ByteArray): String {
        if (bytes.size >= 8) {
            if (bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() && bytes[2] == 0x4E.toByte() && bytes[3] == 0x47.toByte()) return "image/png"
            if (bytes[0] == 0x52.toByte() && bytes[1] == 0x49.toByte() && bytes[2] == 0x46.toByte() && bytes[3] == 0x46.toByte()) return "image/webp"
            if (bytes[0] == 0x47.toByte() && bytes[1] == 0x49.toByte() && bytes[2] == 0x46.toByte()) return "image/gif"
            if (bytes[0] == 0x42.toByte() && bytes[1] == 0x4D.toByte()) return "image/bmp"
        }
        return "image/jpeg"
    }

    fun uploadPhoto(bytes: ByteArray, token: String): String? {
        // Пробуем до 3 раз
        for (attempt in 1..3) {
            try {
                val mimeType = detectMimeType(bytes)
                val extension = when (mimeType) {
                    "image/png" -> "photo.png"
                    "image/webp" -> "photo.webp"
                    "image/gif" -> "photo.gif"
                    "image/bmp" -> "photo.bmp"
                    else -> "photo.jpg"
                }

                val body = okhttp3.MultipartBody.Builder()
                    .setType(okhttp3.MultipartBody.FORM)
                    .addFormDataPart("file", extension, bytes.toRequestBody(mimeType.toMediaType()))
                    .build()

                val request = Request.Builder()
                    .url("http://2.26.71.102:8004/api/upload")
                    .header("Authorization", "Bearer $token")
                    .header("Connection", "close")
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string() ?: "{}"
                        android.util.Log.d("PhotoUploader", "Attempt $attempt: $responseBody")
                        val json = org.json.JSONObject(responseBody)
                        val url = json.optString("url", null)
                        if (url != null && url.isNotEmpty()) {
                            return url
                        }
                    } else {
                        android.util.Log.e("PhotoUploader", "Attempt $attempt: HTTP ${response.code}")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("PhotoUploader", "Attempt $attempt failed: ${e.message}")
                // Закрываем и пересоздаём клиент при ошибке
                client.dispatcher.cancelAll()
                client.connectionPool.evictAll()
            }
            
            // Пауза между попытками
            Thread.sleep(1000)
        }
        return null
    }
}
