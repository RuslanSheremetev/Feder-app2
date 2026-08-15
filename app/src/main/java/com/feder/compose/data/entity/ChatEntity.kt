package com.feder.compose.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val username: String,
    val name: String,
    val avatarUrl: String? = null,
    val avatarColor: String? = null,
    val lastMessage: String? = null,
    val lastTime: Long? = null,
    val unread: Int = 0,
    val isMuted: Boolean = false,
    val online: Boolean = false,
    val lastSeen: Long? = null
)
