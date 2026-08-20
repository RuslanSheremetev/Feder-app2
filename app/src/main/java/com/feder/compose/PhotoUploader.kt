package com.feder.compose

object PhotoUploader {
    init {
        System.loadLibrary("photo_uploader")
    }
    
    external fun nativeUploadPhoto(bytes: ByteArray, token: String): String?
    
    fun uploadPhoto(bytes: ByteArray, token: String): String? {
        return nativeUploadPhoto(bytes, token)
    }
}
