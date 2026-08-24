package com.feder.compose

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream
import java.util.concurrent.TimeUnit
import org.json.JSONObject

object PhotoUploader {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    fun uploadPhoto(inputStream: InputStream, fileName: String, token: String): String? {
        val bytes = inputStream.readBytes()
        
        for (attempt in 1..3) {
            try {
                val fileBody = bytes.toRequestBody("image/jpeg".toMediaType())
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", fileName, fileBody)
                    .build()
                val request = Request.Builder()
                    .url("http://2.26.71.102:8004/api/upload")
                    .header("Authorization", "Bearer $token")
                    .post(requestBody)
                    .build()
                
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val json = JSONObject(response.body?.string() ?: "{}")
                        return json.optString("url", null)
                    }
                }
                if (attempt < 3) Thread.sleep(100)
            } catch (e: Exception) {
                if (attempt < 3) Thread.sleep(100)
            }
        }
        return null
    }
}
