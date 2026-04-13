package com.example.tuvi.domain.model

import androidx.compose.runtime.Immutable

/** Lá số đã lưu vào cơ sở dữ liệu cục bộ. */
@Immutable
data class SavedChart(
    val id: Long = 0,
    val ten: String,
    val ngaySinh: String,          // "dd/MM/yyyy"
    val gioiTinh: String,          // "Nam" / "Nữ"
    val nhom: String,              // Gia đình / Bạn bè / Đồng nghiệp / Khác / tuỳ chỉnh
    val ngayLuu: Long,             // System.currentTimeMillis()
    val inputJson: String,         // JSON serialize của TuViChartInput
    val chartJson: String          // JSON serialize của TuViChart
)
