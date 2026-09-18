package com.feder.compose.audio

object IsAudio {
    fun isAudioFile(url: String): Boolean {
        val l = url.lowercase()
        // убираем query-параметры
        val base = l.substringBefore("?").substringBefore("#")
        return base.endsWith(".m4a") || base.endsWith(".mp3")
            || base.endsWith(".aac") || base.endsWith(".ogg")
            || base.endsWith(".wav") || base.endsWith(".opus")
    }
}
