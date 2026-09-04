package com.feder.compose.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.feder.compose.data.entity.ChatEntity
import com.feder.compose.data.entity.MessageEntity

@Database(
    entities = [MessageEntity::class, ChatEntity::class],
    version = 3,  // ✅ Увеличили версию с 2 до 3
    exportSchema = false
)
abstract class FederDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: FederDatabase? = null

        // ✅ Миграция с версии 2 на 3
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Удаляем старую таблицу и создаём новую
                db.execSQL("DROP TABLE IF EXISTS messages")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS messages (
                        id INTEGER PRIMARY KEY NOT NULL,
                        fromUser TEXT NOT NULL,
                        toUser TEXT NOT NULL,
                        text TEXT NOT NULL,
                        timeVal INTEGER NOT NULL,
                        isRead INTEGER NOT NULL DEFAULT 0,
                        isStarred INTEGER NOT NULL DEFAULT 0,
                        posX REAL,
                        posY REAL,
                        imageUrls TEXT
                    )
                """)
            }
        }

        fun getInstance(context: Context): FederDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FederDatabase::class.java,
                    "feder_db"
                )
                .addMigrations(MIGRATION_2_3)  // ✅ Добавляем миграцию
                .fallbackToDestructiveMigration()  // ✅ Если миграция не поможет - пересоздать
                .build()
                .also { INSTANCE = it }
            }
        }
    }
}
