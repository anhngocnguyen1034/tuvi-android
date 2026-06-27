package com.example.tuvi.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Quote(
    val id: Int,
    val noiDung: String,
    val tiengAnh: String?,
    val tacGia: String?,
    val tuKhoa: List<String>,
)
