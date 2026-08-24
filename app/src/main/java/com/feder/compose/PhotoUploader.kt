package com.feder.compose

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import org.json.JSONObject

object PhotoUploader {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
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
                    .addFormDataPart("file", extension, object : okhttp3.RequestBody() {
                        override fun contentType() = mimeType.toMediaType()
                        override fun contentLength() = bytes.size.toLong()
                        override fun writeTo(sink: okio.BufferedSink) {
                            val chunk = 64 * 1024
                            var offset = 0
                            while (offset < bytes.size) {
                                val len = minOf(chunk, bytes.size - offset)
                                sink.write(bytes, offset, len)
                                sink.flush()
                                offset += len
                            }
                        }
                    })
                    .build()
                val request = Request.Builder()
                    .url("http://2.26.71.102:8004/api/upload")
                    .header("Authorization", "Bearer $token")
                    .post(body)
                    .build()
                
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string() ?: "{}"
                        android.util.Log.d("PhotoUploader", "Attempt $attempt: $responseBody")
                        val json = JSONObject(responseBody)
                        return json.optString("url", null)
                    }
                    android.util.Log.e("PhotoUploader", "Attempt $attempt: HTTP ${response.code}")
                    if (attempt < 3) {
                        Thread.sleep(1000)  // Задержка 1 секунда перед retry
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("PhotoUploader", "Attempt $attempt error: ${e.message}", e)
                if (attempt < 3) {
                    try { Thread.sleep(1000) } catch (_: InterruptedException) {}
                }
            }
        }
        return null
    }
}
