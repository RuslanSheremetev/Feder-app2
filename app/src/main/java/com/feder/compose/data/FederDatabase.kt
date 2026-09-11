package com.feder.compose.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.feder.compose.data.entity.ChatEntity
import com.feder.compose.data.entity.MessageEntity

@Database(
    entities = [MessageEntity::class, ChatEntity::class],
    version = 2,
    exportSchema = false
)
abstract class FederDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: FederDatabase? = null

        fun getInstance(context: Context): FederDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FederDatabase::class.java,
                    "feder_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
