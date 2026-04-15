package com.example.tuvi.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NgayInfoDto(
    @SerialName("ngay_duong")    val ngayDuong: Int,
    @SerialName("thang_duong")   val thangDuong: Int,
    @SerialName("nam_duong")     val namDuong: Int,
    val thu: String,
    val jd: Int,
    @SerialName("ngay_am")       val ngayAm: Int,
    @SerialName("thang_am")      val thangAm: Int,
    @SerialName("nam_am")        val namAm: Int,
    @SerialName("thang_nhuan")   val thangNhuan: Boolean,
    @SerialName("am_lich_text")  val amLichText: String,
    @SerialName("can_chi_ngay")  val canChiNgay: String,
    @SerialName("can_chi_thang") val canChiThang: String,
    @SerialName("can_chi_nam")   val canChiNam: String,
    @SerialName("le_duong_lich") val leDuongLich: String? = null,
    @SerialName("le_am_lich")    val leAmLich: String? = null,
)

@Serializable
data class ThangLichDto(
    val thang: Int,
    val nam: Int,
    @SerialName("can_chi_thang") val canChiThang: String,
    @SerialName("can_chi_nam")   val canChiNam: String,
    @SerialName("so_ngay")       val soNgay: Int,
    val ngay: List<NgayInfoDto>,
)
