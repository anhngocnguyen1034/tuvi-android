package com.example.tuvi.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TabSessionDao {

    @Query("SELECT * FROM tab_sessions ORDER BY sortOrder ASC")
    suspend fun getAll(): List<TabSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tabs: List<TabSessionEntity>)

    @Query("DELETE FROM tab_sessions")
    suspend fun clearAll()
}
