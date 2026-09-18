package com.feder.compose

import java.io.InputStream

object AudioUploader {
    fun uploadAudio(inputStream: InputStream, fileName: String, token: String, toUser: String = ""): String? {
        return FederFileUploader.uploadAudio(inputStream, fileName, token, toUser)
    }
}
