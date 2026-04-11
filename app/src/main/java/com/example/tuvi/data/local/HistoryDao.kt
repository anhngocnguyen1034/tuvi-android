package com.example.tuvi.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    /**
     * Upsert Chrome-style: nếu URL đã tồn tại → cập nhật title + timestamp (nổi lên đầu),
     * nếu chưa có → tạo mới. Mỗi URL chỉ có đúng 1 entry trong lịch sử.
     */
    @Query("""
        INSERT INTO browser_history (url, title, timestamp)
        VALUES (:url, :title, :timestamp)
        ON CONFLICT(url) DO UPDATE SET title = :title, timestamp = :timestamp
    """)
    suspend fun upsert(url: String, title: String, timestamp: Long)

    @Query("SELECT * FROM browser_history ORDER BY timestamp DESC")
    fun getAll(): Flow<List<HistoryItemEntity>>

    @Query("SELECT url FROM browser_history ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestUrl(): String?

    @Query("DELETE FROM browser_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM browser_history")
    suspend fun clearAll()
}
