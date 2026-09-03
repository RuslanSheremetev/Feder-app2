package com.feder.compose

import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * FederHttpClient — собственная библиотека для передачи файлов
 * Не хуже OkHttp:
 * - Multipart загрузка
 * - Таймауты
 * - Retry с backoff
 * - Progress callback
 * - JWT в URL
 */
class FederHttpClient(
    private val connectTimeout: Int = 15,
    private val readTimeout: Int = 60,
    private val maxRetries: Int = 3
) {
    companion object {
        private const val BOUNDARY = "----FederBoundary${System.currentTimeMillis()}"
    }

    interface ProgressListener {
        fun onProgress(sentBytes: Long, totalBytes: Long)
    }

    /**
     * Загрузка файла как multipart form-data
     */
    fun uploadMultipart(
        url: String,
        fileName: String,
        fileBytes: ByteArray,
        token: String,
        progressListener: ProgressListener? = null
    ): String? {
        var lastException: Exception? = null
        
        for (attempt in 1..maxRetries) {
            try {
                val result = doUpload(url, fileName, fileBytes, token, progressListener)
                if (result != null) return result
            } catch (e: Exception) {
                lastException = e
                android.util.Log.e("FederHttpClient", "Attempt $attempt failed: ${e.message}")
            }
            
            if (attempt < maxRetries) {
                Thread.sleep(1000L * attempt)
            }
        }
        
        android.util.Log.e("FederHttpClient", "All attempts failed", lastException)
        return null
    }

    private fun doUpload(
        url: String,
        fileName: String,
        fileBytes: ByteArray,
        token: String,
        progressListener: ProgressListener?
    ): String? {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.connectTimeout = connectTimeout * 1000
        connection.readTimeout = readTimeout * 1000
        connection.useCaches = false
        
        // Multipart заголовки
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$BOUNDARY")
        connection.setRequestProperty("Connection", "close")
        
        val output = DataOutputStream(connection.outputStream)
        
        // Part: file
        output.writeBytes("--$BOUNDARY\r\n")
        output.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"$fileName\"\r\n")
        output.writeBytes("Content-Type: image/jpeg\r\n")
        output.writeBytes("\r\n")
        
        // Отправляем файл кусками с прогрессом
        val chunkSize = 65536
        var sent = 0
        while (sent < fileBytes.size) {
            val len = minOf(chunkSize, fileBytes.size - sent)
            output.write(fileBytes, sent, len)
            sent += len
            progressListener?.onProgress(sent.toLong(), fileBytes.size.toLong())
        }
        
        output.writeBytes("\r\n")
        output.writeBytes("--$BOUNDARY--\r\n")
        output.flush()
        output.close()
        
        val responseCode = connection.responseCode
        val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
        val responseBody = stream?.bufferedReader()?.readText() ?: ""
        stream?.close()
        connection.disconnect()
        
        android.util.Log.d("FederHttpClient", "Response: $responseCode, body: $responseBody")
        
        if (responseCode in 200..299) {
            return parseUrl(responseBody)
        }
        
        return null
    }

    /**
     * Скачивание файла
     */
    fun download(
        url: String,
        token: String,
        progressListener: ProgressListener? = null
    ): ByteArray? {
        val connection = URL("$url?token=${URLEncoder.encode(token, "UTF-8")}").openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = connectTimeout * 1000
        connection.readTimeout = readTimeout * 1000
        
        val responseCode = connection.responseCode
        if (responseCode != 200) {
            connection.disconnect()
            return null
        }
        
        val contentLength = connection.contentLengthLong
        val input = connection.inputStream
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(65536)
        var total = 0L
        
        while (true) {
            val n = input.read(buffer)
            if (n <= 0) break
            output.write(buffer, 0, n)
            total += n
            progressListener?.onProgress(total, contentLength)
        }
        
        input.close()
        connection.disconnect()
        return output.toByteArray()
    }

    private fun parseUrl(json: String): String? {
        return try {
            val obj = org.json.JSONObject(json)
            val url = obj.optString("url", "")
            if (url.isEmpty()) null else url
        } catch (e: Exception) {
            null
        }
    }
}

object FederFileUploader {
    private val client = FederHttpClient()

    fun uploadPhoto(
        inputStream: InputStream,
        fileName: String,
        token: String,
        onProgress: ((Long, Long) -> Unit)? = null
    ): String? {
        val bytes = inputStream.readBytes()
        return client.uploadMultipart(
            url = "http://2.26.71.102:8012/api/upload?token=${URLEncoder.encode(token, "UTF-8")}",
            fileName = fileName,
            fileBytes = bytes,
            token = token,
            progressListener = object : FederHttpClient.ProgressListener {
                override fun onProgress(sentBytes: Long, totalBytes: Long) {
                    onProgress?.invoke(sentBytes, totalBytes)
                }
            }
        )
    }
}
