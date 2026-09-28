package com.feder.compose.repository

import android.util.Log
import com.feder.compose.data.LinkPreviewDao
import com.feder.compose.data.entity.LinkPreviewEntity
import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Репозиторий превью ссылок.
 * 1. Проверяет кэш в Room (7 дней).
 * 2. Если нет — запрашивает /api/link_preview?url=...
 * 3. Сохраняет в Room.
 */
class LinkPreviewRepository(
    private val dao: LinkPreviewDao,
    private val server: String = "http://2.26.71.102:8004"
) {
    private val gson = Gson()
    private val http = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val cacheTtlMs = 7L * 24 * 60 * 60 * 1000

    suspend fun getPreview(url: String, token: String): LinkPreviewEntity? {
        // com.feder.compose.FederHttpClient().sendLog("LinkPreviewRepo: getPreview START url=$url")

        // 1. Кэш
        val cached = dao.getFresh(url, System.currentTimeMillis() - cacheTtlMs)
        if (cached != null) {
            // com.feder.compose.FederHttpClient().sendLog("LinkPreviewRepo: CACHE HIT $url")
            return cached
        }

        // 2. Запрос к серверу — В ФОНОВОМ ПОТОКЕ
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // com.feder.compose.FederHttpClient().sendLog("LinkPreviewRepo: HTTP GET start")
                val encoded = java.net.URLEncoder.encode(url, "UTF-8")
                val req = Request.Builder()
                    .url("$server/api/link_preview?url=$encoded")
                    .header("Authorization", "Bearer $token")
                    .build()
                val resp = http.newCall(req).execute()
                val body = resp.body?.string() ?: ""
                resp.close()
                // com.feder.compose.FederHttpClient().sendLog("LinkPreviewRepo: HTTP ${resp.code} len=${body.length}")

                if (resp.code != 200 || body.isEmpty()) {
                    // com.feder.compose.FederHttpClient().sendLog("LinkPreviewRepo: HTTP FAIL ${resp.code}")
                    return@withContext null
                }

                val json = JsonParser.parseString(body).asJsonObject
                val entity = LinkPreviewEntity(
                    url = url,
                    type = json.get("type")?.asString ?: "generic",
                    title = json.get("title")?.asString,
                    description = json.get("description")?.asString,
                    image = json.get("image")?.asString,
                    siteName = json.get("site_name")?.asString,
                    videoId = json.get("video_id")?.asString,
                    fetchedAt = System.currentTimeMillis()
                )
                dao.insert(entity)
                // com.feder.compose.FederHttpClient().sendLog("LinkPreviewRepo: FETCH OK type=${entity.type} title=${entity.title}")
                entity
            } catch (e: Exception) {
                // com.feder.compose.FederHttpClient().sendLog("LinkPreviewRepo: FAIL ${e.javaClass.simpleName}: ${e.message}")
                null
            }
        }
    }
}
