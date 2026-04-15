package com.example.tuvi.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SuKienDao {

    @Query("SELECT * FROM su_kien WHERE ngay_duong = :ngay AND thang_duong = :thang AND nam_duong = :nam ORDER BY id ASC")
    fun getBuoi(ngay: Int, thang: Int, nam: Int): Flow<List<SuKienEntity>>

    @Query("SELECT * FROM su_kien WHERE thang_duong = :thang AND nam_duong = :nam ORDER BY ngay_duong ASC, id ASC")
    fun getThang(thang: Int, nam: Int): Flow<List<SuKienEntity>>

    @Query("SELECT * FROM su_kien ORDER BY nam_duong ASC, thang_duong ASC, ngay_duong ASC")
    fun getAll(): Flow<List<SuKienEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SuKienEntity): Long

    @Update
    suspend fun update(entity: SuKienEntity)

    @Delete
    suspend fun delete(entity: SuKienEntity)

    @Query("DELETE FROM su_kien WHERE id = :id")
    suspend fun deleteById(id: Long)
}
