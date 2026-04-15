package com.example.tuvi.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [SavedChartEntity::class, HistoryItemEntity::class, BookmarkItemEntity::class, TabSessionEntity::class, SuKienEntity::class],
    version = 7,
    exportSchema = false
)
abstract class TuViDatabase : RoomDatabase() {
    abstract fun savedChartDao(): SavedChartDao
    abstract fun historyDao(): HistoryDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun tabSessionDao(): TabSessionDao
    abstract fun suKienDao(): SuKienDao

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

        // Migration v6→v7: thêm bảng su_kien
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS su_kien (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        tieu_de TEXT NOT NULL,
                        ghi_chu TEXT NOT NULL DEFAULT '',
                        ngay_duong INTEGER NOT NULL,
                        thang_duong INTEGER NOT NULL,
                        nam_duong INTEGER NOT NULL,
                        alarm_epoch INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        fun getInstance(context: Context): TuViDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TuViDatabase::class.java,
                    "tuvi_database"
                )
                .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                .fallbackToDestructiveMigration(true)
                .build().also { INSTANCE = it }
            }
    }
}
