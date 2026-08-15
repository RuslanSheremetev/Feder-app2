package com.feder.compose.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.feder.compose.data.entity.MessageEntity

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE (fromUser = :me AND toUser = :user) OR (fromUser = :user AND toUser = :me) ORDER BY timeVal ASC")
    suspend fun getMessages(me: String, user: String): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<MessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)

    @Query("UPDATE messages SET text = :text WHERE id = :id")
    suspend fun updateText(id: Long, text: String)

    @Query("UPDATE messages SET isRead = 1 WHERE fromUser = :fromUser AND toUser = :toUser")
    suspend fun markRead(fromUser: String, toUser: String)

    @Query("DELETE FROM messages")
    suspend fun clearAll()
}
