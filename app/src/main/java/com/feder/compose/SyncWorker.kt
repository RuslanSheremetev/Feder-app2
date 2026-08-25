package com.feder.compose

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.feder.compose.data.FederDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import java.util.concurrent.TimeUnit

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .build()
            val gson = Gson()
            val server = "http://2.26.71.102:8004"
            
            // Логин
            val loginJson = gson.toJson(mapOf("username" to "demo", "password" to "demo"))
            val loginBody = loginJson.toByteArray()
            val loginRequest = Request.Builder()
                .url("$server/api/login")
                .post(okhttp3.RequestBody.create("application/json".toMediaType(), loginBody))
                .build()
            val loginResponse = client.newCall(loginRequest).execute()
            val token = gson.fromJson(loginResponse.body?.string(), Map::class.java)["access_token"] as String
            
            // Загрузка чатов
            val chatsRequest = Request.Builder()
                .url("$server/api/chat_settings/all?me=demo")
                .header("Authorization", "Bearer $token")
                .build()
            val chatsResponse = client.newCall(chatsRequest).execute()
            val chatsJson = chatsResponse.body?.string() ?: "[]"
            
            // Сохраняем в Room
            val db = FederDatabase.getInstance(applicationContext)
            val type = object : TypeToken<List<ChatItem>>() {}.type
            val chats: List<ChatItem> = gson.fromJson(chatsJson, type)
            
            db.chatDao().insertAll(chats.map { chat ->
                com.feder.compose.data.entity.ChatEntity(
                    username = chat.username,
                    name = chat.name,
                    avatarUrl = chat.avatarUrl,
                    avatarColor = chat.avatarColor,
                    lastMessage = chat.lastMessage,
                    lastTime = try { 
                        chat.timestamp?.let { 
                            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).parse(it)?.time 
                        } 
                    } catch (e: Exception) { null },
                    unread = chat.unread,
                    isMuted = chat.isMuted,
                    online = chat.online,
                    lastSeen = chat.lastSeen
                )
            })
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
