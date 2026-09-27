package com.feder.compose.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Кэш превью ссылок (7 дней).
 * type: youtube | article | image | generic
 */
@Entity(tableName = "link_previews")
data class LinkPreviewEntity(
    @PrimaryKey val url: String,
    val type: String,
    val title: String?,
    val description: String?,
    val image: String?,
    val siteName: String?,
    val videoId: String?,
    val fetchedAt: Long = System.currentTimeMillis()
)
