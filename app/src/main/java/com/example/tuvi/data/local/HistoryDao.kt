package com.example.tuvi.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    /** Upsert: nếu URL đã tồn tại thì cập nhật timestamp, không tạo dòng mới. */
    @Query("""
        INSERT OR REPLACE INTO browser_history (id, url, title, timestamp)
        VALUES (
            COALESCE((SELECT id FROM browser_history WHERE url = :url), 0),
            :url, :title, :timestamp
        )
    """)
    suspend fun upsert(url: String, title: String, timestamp: Long)

    @Query("SELECT * FROM browser_history ORDER BY timestamp DESC")
    fun getAll(): Flow<List<HistoryItemEntity>>

    @Query("DELETE FROM browser_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM browser_history")
    suspend fun clearAll()
}
