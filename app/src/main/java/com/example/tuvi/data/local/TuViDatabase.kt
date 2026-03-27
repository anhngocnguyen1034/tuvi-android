package com.example.tuvi.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SavedChartEntity::class, HistoryItemEntity::class],
    version = 2,
    exportSchema = false
)
abstract class TuViDatabase : RoomDatabase() {
    abstract fun savedChartDao(): SavedChartDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile private var INSTANCE: TuViDatabase? = null

        fun getInstance(context: Context): TuViDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TuViDatabase::class.java,
                    "tuvi_database"
                )
                .fallbackToDestructiveMigration(true)
                .build().also { INSTANCE = it }
            }
    }
}
