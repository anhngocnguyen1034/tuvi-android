package com.example.tuvi.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Sự kiện cá nhân trong lịch.
 * [ngayDuong], [thangDuong], [namDuong] là ngày dương lịch của sự kiện.
 * [alarmEpoch] = unix-millis lúc cần bắn thông báo (0 = không nhắc).
 */
@Entity(tableName = "su_kien")
data class SuKienEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "tieu_de")     val tieuDe: String,
    @ColumnInfo(name = "ghi_chu")     val ghiChu: String = "",
    @ColumnInfo(name = "ngay_duong")  val ngayDuong: Int,
    @ColumnInfo(name = "thang_duong") val thangDuong: Int,
    @ColumnInfo(name = "nam_duong")   val namDuong: Int,
    @ColumnInfo(name = "alarm_epoch") val alarmEpoch: Long = 0L,   // 0 = tắt nhắc
)
