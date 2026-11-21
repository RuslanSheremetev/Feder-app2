package com.feder.compose.stories

import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * StoryApi — HTTP-клиент к серверу stories (порт 8020)
 *
 * Endpoints:
 *   GET  /api/stories/feed?me=X      → список users + их stories
 *   GET  /api/stories/{user}?me=X    → stories одного юзера
 *   POST /api/stories/{id}/view      → отметить просмотренной
 *   POST /api/upload?user=X          → загрузить story (multipart)
 */
object StoryApi {

    const val BASE_URL = "http://2.26.71.102:8020"
    private const val TAG = "StoryApi"

    // ─── Модели данных ──────────────────────────────────────────────────

    data class Story(
        val id: Int,
        val filename: String,
        val mediaType: String,     // "image" | "video"
        val createdAt: String,
        val viewed: Boolean
    ) {
        val fullUrl: String get() = "$BASE_URL/stories/$filename"
    }

    data class StoryUser(
        val username: String,
        val avatarUrl: String,
        val stories: List<Story>
    )

    // ─── GET /api/stories/feed?me=X ─────────────────────────────────────

    /**
     * Возвращает список StoryUser с их stories.
     * @param me — кто смотрит (для флага viewed)
     * @param token — JWT
     */
    fun fetchFeed(me: String, token: String): List<StoryUser> {
        val url = URL("$BASE_URL/api/stories/feed?me=$me")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Authorization", "Bearer $token")
            connectTimeout = 10_000
            readTimeout = 15_000
        }
        return try {
            val code = conn.responseCode
            if (code != 200) {
                Log.e(TAG, "fetchFeed HTTP $code")
                return emptyList()
            }
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            parseFeed(body)
        } catch (e: Exception) {
            Log.e(TAG, "fetchFeed error: ${e.message}", e)
            emptyList()
        } finally {
            conn.disconnect()
        }
    }

    private fun parseFeed(json: String): List<StoryUser> {
        val result = mutableListOf<StoryUser>()
        try {
            val root = JSONObject(json)
            val arr = root.optJSONArray("users") ?: return emptyList()
            for (i in 0 until arr.length()) {
                val u = arr.getJSONObject(i)
                val storiesArr = u.optJSONArray("stories") ?: continue
                val stories = mutableListOf<Story>()
                for (j in 0 until storiesArr.length()) {
                    val s = storiesArr.getJSONObject(j)
                    stories.add(
                        Story(
                            id = s.optInt("id"),
                            filename = s.optString("filename"),
                            mediaType = s.optString("mediaType", "image"),
                            createdAt = s.optString("createdAt"),
                            viewed = s.optBoolean("viewed", false)
                        )
                    )
                }
                if (stories.isNotEmpty()) {
                    result.add(
                        StoryUser(
                            username = u.optString("username"),
                            avatarUrl = u.optString("avatarUrl"),
                            stories = stories
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "parseFeed error: ${e.message}", e)
        }
        return result
    }

    // ─── POST /api/stories/{id}/view ────────────────────────────────────

    /**
     * Отметить story просмотренной.
     */
    fun markViewed(storyId: Int, viewer: String, token: String): Boolean {
        val url = URL("$BASE_URL/api/stories/$storyId/view")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
            connectTimeout = 10_000
            readTimeout = 10_000
        }
        return try {
            val body = """{"viewer":"$viewer"}"""
            conn.outputStream.use { it.write(body.toByteArray()) }
            val code = conn.responseCode
            code == 200
        } catch (e: Exception) {
            Log.e(TAG, "markViewed error: ${e.message}", e)
            false
        } finally {
            conn.disconnect()
        }
    }

    // ─── POST /api/upload (multipart) ───────────────────────────────────

    /**
     * Загрузить story.
     * @param fileBytes — байты файла (JPG или MP4)
     * @param fileName — имя файла
     * @param mimeType — "image/jpeg" или "video/mp4"
     * @param user — от чьего имени
     * @param token — JWT
     * @return имя файла на сервере или null
     */
    fun uploadStory(
        fileBytes: ByteArray,
        fileName: String,
        mimeType: String,
        user: String,
        token: String
    ): String? {
        val url = URL("$BASE_URL/api/upload?token=$token&user=$user")
        val boundary = "----FederStoryBoundary${System.currentTimeMillis()}"
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            doOutput = true
            connectTimeout = 30_000
            readTimeout = 120_000
        }
        return try {
            conn.outputStream.use { out ->
                writeMultipart(out, boundary, fileName, mimeType, fileBytes)
            }
            val code = conn.responseCode
            if (code != 200) {
                Log.e(TAG, "uploadStory HTTP $code")
                return null
            }
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            json.optString("filename", null)
        } catch (e: Exception) {
            Log.e(TAG, "uploadStory error: ${e.message}", e)
            null
        } finally {
            conn.disconnect()
        }
    }

    private fun writeMultipart(
        out: OutputStream,
        boundary: String,
        fileName: String,
        mimeType: String,
        bytes: ByteArray
    ) {
        val crlf = "\r\n"
        val header = "--$boundary$crlf" +
                "Content-Disposition: form-data; name=\"file\"; filename=\"$fileName\"$crlf" +
                "Content-Type: $mimeType$crlf$crlf"
        out.write(header.toByteArray())
        out.write(bytes)
        out.write(crlf.toByteArray())
        out.write("--$boundary--$crlf".toByteArray())
        out.flush()
    }
}
