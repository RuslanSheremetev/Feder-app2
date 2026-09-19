package com.feder.compose.audio

object IsVideo {
    fun isVideoFile(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val l = url.lowercase()
        return l.endsWith(".mp4") || l.endsWith(".mov") ||
               l.endsWith(".webm") || l.endsWith(".mkv") ||
               l.endsWith(".avi") || l.endsWith(".3gp")
    }
}
