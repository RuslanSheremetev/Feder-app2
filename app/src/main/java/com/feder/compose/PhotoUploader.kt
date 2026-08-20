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

    fun uploadPhoto(bytes: ByteArray, token: String): String? {
        try {
            val body = okhttp3.MultipartBody.Builder()
                .setType(okhttp3.MultipartBody.FORM)
                .addFormDataPart("file", "photo.jpg", bytes.toRequestBody("image/jpeg".toMediaType()))
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
