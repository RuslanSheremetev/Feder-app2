package com.feder.compose.stories

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * StoryPicker — выбор и загрузка story (JPG или MP4).
 *
 * Использование:
 *   val picker = StoryPicker.rememberStoryPicker(
 *       user = "demo",
 *       token = "eyJ...",
 *       onResult = { result -> ... }
 *   )
 *   picker.launch()
 */
object StoryPicker {

    sealed class Result {
        data class Success(val filename: String) : Result()
        data class Error(val message: String) : Result()
    }

    suspend fun uploadUri(
        context: Context,
        uri: Uri,
        user: String,
        token: String
    ): Result = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver

            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val isVideo = mimeType.startsWith("video/")

            val ext = when {
                mimeType.contains("mp4") -> "mp4"
                mimeType.contains("quicktime") -> "mov"
                mimeType.contains("webm") -> "webm"
                mimeType.contains("png") -> "png"
                mimeType.contains("webp") -> "webp"
                else -> "jpg"
            }

            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@withContext Result.Error("Не удалось прочитать файл")

            Log.d("StoryPicker", "Upload: ${bytes.size} bytes, mime=$mimeType, ext=$ext, video=$isVideo")

            val filename = StoryApi.uploadStory(
                fileBytes = bytes,
                fileName = "story.$ext",
                mimeType = mimeType,
                user = user,
                token = token
            )

            if (filename != null) {
                Result.Success(filename)
            } else {
                Result.Error("Сервер вернул ошибку")
            }
        } catch (e: Exception) {
            Log.e("StoryPicker", "upload error", e)
            Result.Error(e.message ?: "unknown")
        }
    }

    @Composable
    fun rememberStoryPicker(
        user: String,
        token: String,
        onResult: (Result) -> Unit
    ): StoryPickerLauncher {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->
            if (uri == null) {
                Log.d("StoryPicker", "Cancelled")
                return@rememberLauncherForActivityResult
            }
            scope.launch {
                val result = uploadUri(context, uri, user, token)
                onResult(result)
            }
        }

        return StoryPickerLauncher {
            launcher.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageAndVideo
                )
            )
        }
    }

    class StoryPickerLauncher(private val action: () -> Unit) {
        fun launch() = action()
    }
}
