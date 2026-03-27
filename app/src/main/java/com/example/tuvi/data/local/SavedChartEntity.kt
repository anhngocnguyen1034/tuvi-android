package com.example.tuvi.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_charts")
data class SavedChartEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "ten") val ten: String,
    @ColumnInfo(name = "ngay_sinh") val ngaySinh: String,
    @ColumnInfo(name = "gioi_tinh") val gioiTinh: String,
    @ColumnInfo(name = "nhom") val nhom: String,
    @ColumnInfo(name = "ngay_luu") val ngayLuu: Long,
    @ColumnInfo(name = "input_json") val inputJson: String,
    @ColumnInfo(name = "chart_json") val chartJson: String
)
