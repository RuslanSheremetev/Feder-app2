package com.feder.compose.repository

import com.feder.compose.data.ChatDao
import com.feder.compose.data.MessageDao
import com.feder.compose.data.entity.ChatEntity
import com.feder.compose.data.entity.MessageEntity

class ChatRepository(
    private val messageDao: MessageDao,
    private val chatDao: ChatDao
) {
    suspend fun getMessages(me: String, user: String): List<MessageEntity> {
        return messageDao.getMessages(me, user)
    }

    suspend fun saveMessages(messages: List<MessageEntity>) {
        messageDao.insertAll(messages)
    }

    suspend fun saveMessage(message: MessageEntity) {
        messageDao.insert(message)
    }

    suspend fun getChats(): List<ChatEntity> {
        return chatDao.getChats()
    }

    suspend fun saveChats(chats: List<ChatEntity>) {
        chatDao.insertAll(chats)
    }

    suspend fun saveChat(chat: ChatEntity) {
        chatDao.insert(chat)
    }

    suspend fun updateLastMessage(username: String, lastMessage: String, lastTime: Long) {
        chatDao.updateLastMessage(username, lastMessage, lastTime)
    }

    suspend fun markRead(username: String) {
        chatDao.markRead(username)
        messageDao.markRead(username, username)
    }
}
