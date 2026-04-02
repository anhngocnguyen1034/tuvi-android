package com.example.tuvi.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(item: BookmarkItemEntity): Long

    @Query("DELETE FROM bookmarks WHERE url = :url")
    suspend fun deleteBookmarkByUrl(url: String)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: Long)

    @Query("SELECT * FROM bookmarks ORDER BY created_time DESC")
    fun getAllBookmarks(): Flow<List<BookmarkItemEntity>>

    /** Flow<Boolean> — tự cập nhật khi bảng thay đổi. */
    @Query("SELECT COUNT(*) > 0 FROM bookmarks WHERE url = :url")
    fun isBookmarked(url: String): Flow<Boolean>

    @Query("UPDATE bookmarks SET title = :title WHERE id = :id")
    suspend fun updateTitle(id: Long, title: String)
}
