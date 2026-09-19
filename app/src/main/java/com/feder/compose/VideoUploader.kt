package com.feder.compose

import java.io.InputStream

object VideoUploader {
    fun uploadVideo(
        inputStream: InputStream,
        fileName: String,
        token: String,
        toUser: String = ""
    ): String? = FederFileUploader.uploadVideo(inputStream, fileName, token, toUser)
}
