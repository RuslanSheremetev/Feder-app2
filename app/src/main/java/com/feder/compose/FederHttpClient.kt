package com.feder.compose

import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

/**
 * FederHttpClient — высоконагруженная библиотека передачи файлов
 * - Thread Pool для параллельных загрузок
 * - Rate limiting
 * - Retry с exponential backoff
 * - Connection pooling
 * - Progress callback
 * - Автоматическое восстановление
 */
class FederHttpClient(
    private val connectTimeout: Int = 15,
    private val readTimeout: Int = 120,
    private val maxRetries: Int = 5,
    private val maxConcurrent: Int = 8
) {
    companion object {
        private const val BOUNDARY_PREFIX = "----FederBoundary"
        private val activeUploads = AtomicInteger(0)
        private val executor = ThreadPoolExecutor(
            4, 16, 60L, TimeUnit.SECONDS,
            LinkedBlockingQueue(1000),
            ThreadPoolExecutor.CallerRunsPolicy()
        )
    }

    interface ProgressListener {
        fun onProgress(sentBytes: Long, totalBytes: Long)
        fun onComplete(url: String)
        fun onError(error: String)
    }

    /**
     * Асинхронная загрузка файла
     */
    fun uploadAsync(
        url: String,
        fileName: String,
        fileBytes: ByteArray,
        token: String,
        listener: ProgressListener
    ) {
        executor.execute {
            try {
                val result = uploadWithRetry(url, fileName, fileBytes, token, listener)
                if (result != null) {
                    listener.onComplete(result)
                } else {
                    listener.onError("Upload failed after $maxRetries attempts")
                }
            } catch (e: Exception) {
                listener.onError(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Синхронная загрузка с retry и backoff
     */
    fun uploadSync(
        url: String,
        fileName: String,
        fileBytes: ByteArray,
        token: String,
        listener: ProgressListener? = null
    ): String? {
        return uploadWithRetry(url, fileName, fileBytes, token, listener)
    }

    private fun uploadWithRetry(
        url: String,
        fileName: String,
        fileBytes: ByteArray,
        token: String,
        listener: ProgressListener?
    ): String? {
        var lastError: String? = null
        
        for (attempt in 1..maxRetries) {
            try {
                val result = doUpload(url, fileName, fileBytes, token, listener)
                if (result != null) return result
            } catch (e: Exception) {
                lastError = e.message
                android.util.Log.e("FederHttp", "Attempt $attempt: ${e.message}")
            }
            
            // Exponential backoff: 1s, 2s, 4s, 8s, 16s
            if (attempt < maxRetries) {
                val delay = 1000L shl (attempt - 1)
                Thread.sleep(delay)
            }
        }
        
        android.util.Log.e("FederHttp", "All attempts failed: $lastError")
        return null
    }

    private fun doUpload(
        url: String,
        fileName: String,
        fileBytes: ByteArray,
        token: String,
        listener: ProgressListener?
    ): String? {
        if (activeUploads.get() >= maxConcurrent) {
            listener?.onError("Too many concurrent uploads")
            return null
        }
        
        activeUploads.incrementAndGet()
        
        try {
            val boundary = "$BOUNDARY_PREFIX${System.nanoTime()}"
            val connection = URL(url).openConnection() as HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.doInput = true
            connection.connectTimeout = connectTimeout * 1000
            connection.readTimeout = readTimeout * 1000
            connection.useCaches = false
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            connection.setRequestProperty("Connection", "keep-alive")
            connection.setRequestProperty("Accept", "*/*")
            
            val output = DataOutputStream(BufferedOutputStream(connection.outputStream, 65536))
            
            // Part headers
            output.writeBytes("--$boundary\r\n")
            output.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"$fileName\"\r\n")
            output.writeBytes("Content-Type: image/jpeg\r\n")
            output.writeBytes("\r\n")
            
            // Файл чанками
            val chunkSize = 131072 // 128KB
            var sent = 0
            while (sent < fileBytes.size) {
                val len = minOf(chunkSize, fileBytes.size - sent)
                output.write(fileBytes, sent, len)
                sent += len
                listener?.onProgress(sent.toLong(), fileBytes.size.toLong())
            }
            
            output.writeBytes("\r\n")
            output.writeBytes("--$boundary--\r\n")
            output.flush()
            output.close()
            
            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val responseBody = stream?.bufferedReader()?.readText() ?: ""
            stream?.close()
            connection.disconnect()
            
            if (responseCode in 200..299) {
                return parseUrl(responseBody)
            }
            
            android.util.Log.e("FederHttp", "Upload failed: $responseCode, body: $responseBody")
            return null
        } finally {
            activeUploads.decrementAndGet()
        }
    }

    /**
     * Скачивание файла с потоковой записью
     */
    fun downloadToFile(
        url: String,
        token: String,
        destFile: File,
        listener: ProgressListener? = null
    ): Boolean {
        return try {
            val fullUrl = "$url?token=${URLEncoder.encode(token, "UTF-8")}"
            val connection = URL(fullUrl).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = connectTimeout * 1000
            connection.readTimeout = readTimeout * 1000
            
            if (connection.responseCode != 200) {
                connection.disconnect()
                return false
            }
            
            val contentLength = connection.contentLengthLong
            val input = BufferedInputStream(connection.inputStream, 65536)
            val output = BufferedOutputStream(FileOutputStream(destFile), 65536)
            val buffer = ByteArray(131072)
            var total = 0L
            
            while (true) {
                val n = input.read(buffer)
                if (n <= 0) break
                output.write(buffer, 0, n)
                total += n
                listener?.onProgress(total, contentLength)
            }
            
            output.flush()
            output.close()
            input.close()
            connection.disconnect()
            true
        } catch (e: Exception) {
            android.util.Log.e("FederHttp", "Download failed: ${e.message}")
            false
        }
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

    fun shutdown() {
        executor.shutdown()
    }
}

object FederFileUploader {
    private val client = FederHttpClient()

    fun uploadPhoto(
        inputStream: InputStream,
        fileName: String,
        token: String,
        onProgress: ((Long, Long) -> Unit)? = null,
        onComplete: ((String) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ): String? {
        val bytes = inputStream.readBytes()
        
        // Для маленьких файлов — синхронно
        if (bytes.size < 1 * 1024 * 1024) {
            return client.uploadSync(
                url = "http://2.26.71.102:8012/api/upload?token=${URLEncoder.encode(token, "UTF-8")}",
                fileName = fileName,
                fileBytes = bytes,
                token = token,
                listener = object : FederHttpClient.ProgressListener {
                    override fun onProgress(sentBytes: Long, totalBytes: Long) {
                        onProgress?.invoke(sentBytes, totalBytes)
                    }
                    override fun onComplete(url: String) {
                        onComplete?.invoke(url)
                    }
                    override fun onError(error: String) {
                        onError?.invoke(error)
                    }
                }
            )
        }
        
        // Для больших — асинхронно
        client.uploadAsync(
            url = "http://2.26.71.102:8012/api/upload?token=${URLEncoder.encode(token, "UTF-8")}",
            fileName = fileName,
            fileBytes = bytes,
            token = token,
            listener = object : FederHttpClient.ProgressListener {
                override fun onProgress(sentBytes: Long, totalBytes: Long) {
                    onProgress?.invoke(sentBytes, totalBytes)
                }
                override fun onComplete(url: String) {
                    onComplete?.invoke(url)
                }
                override fun onError(error: String) {
                    onError?.invoke(error)
                }
            }
        )
        return null
    }
}
