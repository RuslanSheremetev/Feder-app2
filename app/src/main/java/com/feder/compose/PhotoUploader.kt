package com.feder.compose

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object PhotoUploader {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private var jniLoaded = false
    
    init {
        logToServer("INIT_START")
        try {
            System.loadLibrary("photo_uploader")
            jniLoaded = true
            logToServer("INIT_JNI_LOADED_OK")
        } catch (e: UnsatisfiedLinkError) {
            jniLoaded = false
            logToServer("INIT_JNI_FAILED: ${e.message}")
        }
        logToServer("INIT_END: jniLoaded=$jniLoaded")
    }
    
    external fun nativeUploadPhoto(bytes: ByteArray, token: String): String?
    
    private fun logToServer(message: String) {
        try {
            val logJson = """{"log":"$message"}"""
            val body = logJson.toRequestBody("application/json".toMediaType())
            okHttpClient.newCall(
                Request.Builder()
                    .url("http://2.26.71.102:8002/api/logs")
                    .post(body)
                    .build()
            ).execute().close()
        } catch (e: Exception) {
            android.util.Log.e("PhotoUploader", "logToServer failed: ${e.message}")
        }
    }
    
    fun uploadPhoto(bytes: ByteArray, token: String): String? {
        logToServer("STEP_1: uploadPhoto called, bytes=${bytes.size}")
        
        if (jniLoaded) {
            logToServer("STEP_2: JNI loaded, calling native...")
            try {
                val result = nativeUploadPhoto(bytes, token)
                logToServer("STEP_3: JNI returned: $result")
                return result
            } catch (e: Exception) {
                logToServer("STEP_3_ERROR: JNI exception: ${e.message}")
            }
        } else {
            logToServer("STEP_2_ERROR: JNI not loaded!")
        }
        
        logToServer("STEP_4: Trying OkHttp...")
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
            
            logToServer("STEP_5: OkHttp executing...")
            okHttpClient.newCall(request).execute().use { response ->
                logToServer("STEP_6: OkHttp response: ${response.code}")
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: "{}"
                    logToServer("STEP_7: Body: $responseBody")
                    val json = org.json.JSONObject(responseBody)
                    return json.optString("url", null)
                }
            }
        } catch (e: Exception) {
            logToServer("STEP_5_ERROR: ${e.javaClass.simpleName}: ${e.message}")
        }
        
        logToServer("STEP_8: Returning null")
        return null
    }
}
