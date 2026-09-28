package com.feder.compose.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.feder.compose.data.entity.ChatEntity

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats ORDER BY lastTime DESC")
    suspend fun getChats(): List<ChatEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chats: List<ChatEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chat: ChatEntity)

    @Query("UPDATE chats SET lastMessage = :lastMessage, lastTime = :lastTime, unread = unread + 1 WHERE username = :username")
    suspend fun updateLastMessage(username: String, lastMessage: String, lastTime: Long)

    @Query("UPDATE chats SET unread = 0 WHERE username = :username")
    suspend fun markRead(username: String)

    @Query("DELETE FROM chats")
    suspend fun clearAll()
}
