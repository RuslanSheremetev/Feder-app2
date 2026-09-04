package com.feder.compose

import java.io.InputStream
import kotlin.concurrent.thread

object PhotoUploader {
    private fun sendLog(message: String) {
        try {
            thread {
                val conn = java.net.URL("http://2.26.71.102:8006/api/logs").openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")
                conn.connectTimeout = 3000
                conn.readTimeout = 3000
                val json = """{"log":"$message"}"""
                conn.outputStream.write(json.toByteArray())
                conn.outputStream.flush()
                conn.outputStream.close()
                conn.inputStream.close()
            }
        } catch (e: Exception) {}
    }

    fun uploadPhoto(inputStream: InputStream, fileName: String, token: String, toUser: String = ""): String? {
        sendLog("PHOTO_UPLOAD_START: user=$toUser")
        val result = FederFileUploader.uploadPhoto(inputStream, fileName, token, toUser)
        sendLog("PHOTO_UPLOAD_RESULT: $result")
        return result
    }
}
