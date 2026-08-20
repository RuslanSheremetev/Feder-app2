package com.feder.compose

object PhotoUploader {
    init {
        try {
            System.loadLibrary("photo_uploader")
            android.util.Log.d("PhotoUploader", "JNI library loaded")
        } catch (e: UnsatisfiedLinkError) {
            android.util.Log.e("PhotoUploader", "FAILED to load JNI: ${e.message}")
        }
    }
    
    external fun nativeUploadPhoto(bytes: ByteArray, token: String): String?
    
    fun uploadPhoto(bytes: ByteArray, token: String): String? {
        return nativeUploadPhoto(bytes, token)
    }
}
