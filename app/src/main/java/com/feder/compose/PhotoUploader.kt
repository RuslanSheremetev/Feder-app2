package com.feder.compose

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object PhotoUploader {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun detectMimeType(bytes: ByteArray): String {
        if (bytes.size >= 8) {
            // PNG: 89 50 4E 47
            if (bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() && bytes[2] == 0x4E.toByte() && bytes[3] == 0x47.toByte()) return "image/png"
            // WebP: RIFF....WEBP
            if (bytes[0] == 0x52.toByte() && bytes[1] == 0x49.toByte() && bytes[2] == 0x46.toByte() && bytes[3] == 0x46.toByte()) return "image/webp"
            // GIF: GIF8
            if (bytes[0] == 0x47.toByte() && bytes[1] == 0x49.toByte() && bytes[2] == 0x46.toByte()) return "image/gif"
            // BMP: BM
            if (bytes[0] == 0x42.toByte() && bytes[1] == 0x4D.toByte()) return "image/bmp"
        }
        return "image/jpeg"
    }

    fun uploadPhoto(bytes: ByteArray, token: String): String? {
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
                .url("http://2.26.71.102:8002/api/upload")
                .header("Authorization", "Bearer $token")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val json = org.json.JSONObject(response.body?.string() ?: "{}")
                    return json.optString("url", null)
                }
                return null
            }
        } catch (e: Exception) {
            android.util.Log.e("PhotoUploader", "Error: ${e.message}")
            return null
        }
    }
}
