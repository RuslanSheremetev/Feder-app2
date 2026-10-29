package com.feder.compose.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: Long,
    val fromUser: String,
    val toUser: String,
    val text: String,
    val timeVal: Long,
    val isRead: Boolean = false,
    val messageType: String = "text",
    val stickerId: String? = null,
    val stickerText: String? = null,
    val isStarred: Boolean = false,
    val posX: Float? = null,
    val posY: Float? = null,
    val imageUrls: String? = null,
    val reactionsJson: String? = null
)
