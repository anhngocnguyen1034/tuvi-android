package com.example.tuvi.data.local.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuoteDto(
    val id: Int,
    @SerialName("noi_dung") val noiDung: String,
    @SerialName("tieng_anh") val tiengAnh: String? = null,
    @SerialName("tac_gia") val tacGia: String? = null,
    @SerialName("tu_khoa") val tuKhoa: List<String> = emptyList(),
)
