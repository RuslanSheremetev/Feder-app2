package com.feder.compose

import java.io.InputStream

object PhotoUploader {
    fun uploadPhoto(inputStream: InputStream, fileName: String, token: String): String? {
        return FederFileUploader.uploadPhoto(inputStream, fileName, token)
    }
}
