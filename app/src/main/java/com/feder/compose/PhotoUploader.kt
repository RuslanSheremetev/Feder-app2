package com.feder.compose

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.io.InputStream
import java.util.concurrent.TimeUnit
import org.json.JSONObject

object PhotoUploader {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    fun uploadPhoto(inputStream: InputStream, fileName: String, token: String): String? {
        for (attempt in 1..3) {
            try {
                val body = object : RequestBody() {
                    override fun contentType() = "image/jpeg".toMediaType()
                    override fun contentLength() = -1L
                    override fun writeTo(sink: okio.BufferedSink) {
                        inputStream.use { input ->
                            val buffer = ByteArray(64 * 1024)
                            var bytesRead: Int
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                sink.write(buffer, 0, bytesRead)
                                sink.flush()
                            }
                        }
                    }
                }

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", fileName, body)
                    .build()

                val request = Request.Builder()
                    .url("http://2.26.71.102:8004/api/upload")
                    .header("Authorization", "Bearer $token")
                    .header("Connection", "close")
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string() ?: "{}"
                        val json = JSONObject(responseBody)
                        return json.optString("url", null)
                    }
                    if (attempt < 3) Thread.sleep(1000)
                }
            } catch (e: Exception) {
                if (attempt < 3) Thread.sleep(1000)
            }
        }
        return null
    }
}
