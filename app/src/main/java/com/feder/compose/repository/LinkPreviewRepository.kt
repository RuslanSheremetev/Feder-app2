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
        // 1. Кэш
        val cached = dao.getFresh(url, System.currentTimeMillis() - cacheTtlMs)
        if (cached != null) {
            try {
                val http = OkHttpClient()
                val body = Gson().toJson(mapOf("log" to "LinkPreviewRepo: CACHE HIT $url")).let { it.toRequestBody("application/json".toMediaType()) }
                http.newCall(Request.Builder().url("$server/api/logs").post(body).build()).execute().close()
            } catch (_: Exception) {}
            return cached
        }

        // 2. Запрос к серверу
        return try {
            val encoded = java.net.URLEncoder.encode(url, "UTF-8")
            val req = Request.Builder()
                .url("$server/api/link_preview?url=$encoded")
                .header("Authorization", "Bearer $token")
                .build()
            val resp = http.newCall(req).execute()
            val body = resp.body?.string() ?: ""
            resp.close()

            if (resp.code != 200 || body.isEmpty()) {
                try {
                val http = OkHttpClient()
                val body2 = Gson().toJson(mapOf("log" to "LinkPreviewRepo: HTTP ${resp.code} $body")).toRequestBody("application/json".toMediaType())
                http.newCall(Request.Builder().url("$server/api/logs").post(body2).build()).execute().close()
            } catch (_: Exception) {}
                return null
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
            try {
                val http = OkHttpClient()
                val body = Gson().toJson(mapOf("log" to "LinkPreviewRepo: FETCH OK $url type=${entity.type}")).toRequestBody("application/json".toMediaType())
                http.newCall(Request.Builder().url("$server/api/logs").post(body).build()).execute().close()
            } catch (_: Exception) {}
            entity
        } catch (e: Exception) {
            try {
                val http = OkHttpClient()
                val body = Gson().toJson(mapOf("log" to "LinkPreviewRepo: FAIL ${e.message}")).toRequestBody("application/json".toMediaType())
                http.newCall(Request.Builder().url("$server/api/logs").post(body).build()).execute().close()
            } catch (_: Exception) {}
            null
        }
    }
}
