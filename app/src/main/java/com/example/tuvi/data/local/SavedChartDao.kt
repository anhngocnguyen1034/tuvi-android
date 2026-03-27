package com.example.tuvi.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedChartDao {

    @Query("SELECT * FROM saved_charts ORDER BY ngay_luu DESC")
    fun getAll(): Flow<List<SavedChartEntity>>

    @Query("SELECT * FROM saved_charts WHERE ten LIKE '%' || :query || '%' ORDER BY ngay_luu DESC")
    fun search(query: String): Flow<List<SavedChartEntity>>

    @Query("SELECT * FROM saved_charts WHERE nhom = :nhom ORDER BY ngay_luu DESC")
    fun getByGroup(nhom: String): Flow<List<SavedChartEntity>>

    @Query("SELECT DISTINCT nhom FROM saved_charts ORDER BY nhom ASC")
    fun getAllGroups(): Flow<List<String>>

    @Query("SELECT * FROM saved_charts WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SavedChartEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SavedChartEntity): Long

    @Query("DELETE FROM saved_charts WHERE id = :id")
    suspend fun deleteById(id: Long)
}
