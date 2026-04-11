package com.example.tuvi.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [SavedChartEntity::class, HistoryItemEntity::class, BookmarkItemEntity::class, TabSessionEntity::class],
    version = 6,
    exportSchema = false
)
abstract class TuViDatabase : RoomDatabase() {
    abstract fun savedChartDao(): SavedChartDao
    abstract fun historyDao(): HistoryDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun tabSessionDao(): TabSessionDao

    companion object {
        @Volatile private var INSTANCE: TuViDatabase? = null

        // Migration v4→v5: chỉ thêm bảng tab_sessions, giữ nguyên history + bookmarks
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS tab_sessions (
                        id TEXT NOT NULL PRIMARY KEY,
                        url TEXT NOT NULL,
                        title TEXT NOT NULL,
                        sortOrder INTEGER NOT NULL,
                        isActive INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        // Migration v5→v6: thêm 2 cột lịch sử navigation vào tab_sessions
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tab_sessions ADD COLUMN navHistoryJson TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE tab_sessions ADD COLUMN navHistoryIndex INTEGER NOT NULL DEFAULT -1")
            }
        }

        fun getInstance(context: Context): TuViDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TuViDatabase::class.java,
                    "tuvi_database"
                )
                .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
                .fallbackToDestructiveMigration(true)
                .build().also { INSTANCE = it }
            }
    }
}
