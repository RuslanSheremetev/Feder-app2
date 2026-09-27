package com.feder.compose.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.feder.compose.data.entity.LinkPreviewEntity

@Dao
interface LinkPreviewDao {

    @Query("SELECT * FROM link_previews WHERE url = :url AND fetchedAt > :minTime LIMIT 1")
    suspend fun getFresh(url: String, minTime: Long): LinkPreviewEntity?

    @Query("SELECT * FROM link_previews WHERE url = :url LIMIT 1")
    suspend fun getAny(url: String): LinkPreviewEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LinkPreviewEntity)

    @Query("DELETE FROM link_previews WHERE fetchedAt < :minTime")
    suspend fun deleteOlderThan(minTime: Long)
}
