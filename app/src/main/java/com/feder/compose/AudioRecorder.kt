package com.feder.compose

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

/**
 * Запись голосовых сообщений в M4A (AAC).
 * API сервера: POST http://2.26.71.102:8014/upload (multipart, поле file, Content-Type audio/mp4)
 */
class AudioRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null
    private var startTime: Long = 0L

    val isRecording: Boolean
        get() = recorder != null

    /** Длительность текущей/завершённой записи, сек */
    val durationSec: Int
        get() = if (startTime == 0L) 0 else ((System.currentTimeMillis() - startTime) / 1000).toInt()

    /**
     * Амплитуда (0..32767). Для анимации пульсации.
     * Возвращает 0, если не записываем.
     */
    fun getAmplitude(): Int {
        val r = recorder ?: return 0
        return try {
            r.maxAmplitude
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Начать запись в файл в cacheDir.
     * @return File — куда пишем, или null если ошибка
     */
    fun start(): File? {
        if (isRecording) return null
        val dir = File(context.cacheDir, "audio").apply { mkdirs() }
        val f = File(dir, "voice_${System.currentTimeMillis()}.m4a")
        val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        return try {
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setAudioChannels(1)
            r.setAudioSamplingRate(44100)
            r.setAudioEncodingBitRate(64000)
            r.setOutputFile(f.absolutePath)
            r.prepare()
            r.start()
            recorder = r
            currentFile = f
            startTime = System.currentTimeMillis()
            f
        } catch (e: Exception) {
            android.util.Log.e("AudioRecorder", "start failed: ${e.message}")
            try { r.release() } catch (_: Exception) {}
            recorder = null
            currentFile = null
            startTime = 0
            null
        }
    }

    /**
     * Остановить запись.
     * @return Pair<File, durationSec> или null
     */
    fun stop(): Pair<File, Int>? {
        val r = recorder ?: return null
        val f = currentFile
        val dur = durationSec
        return try {
            r.stop()
            r.release()
            recorder = null
            currentFile = null
            startTime = 0
            if (f != null && f.exists() && f.length() > 0) f to dur else null
        } catch (e: Exception) {
            android.util.Log.e("AudioRecorder", "stop failed: ${e.message}")
            try { r.release() } catch (_: Exception) {}
            recorder = null
            currentFile?.delete()
            currentFile = null
            startTime = 0
            null
        }
    }

    /** Отмена — без сохранения */
    fun cancel() {
        try {
            recorder?.stop()
        } catch (_: Exception) {}
        try {
            recorder?.release()
        } catch (_: Exception) {}
        recorder = null
        currentFile?.delete()
        currentFile = null
        startTime = 0
    }
}
