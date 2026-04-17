package com.example.tuvi.data.mapper

import com.example.tuvi.data.remote.dto.CungDto
import com.example.tuvi.data.remote.dto.SaoDto
import com.example.tuvi.data.remote.dto.ThienBanDto
import com.example.tuvi.data.remote.dto.TuViResponse
import com.example.tuvi.domain.model.CungInfo
import com.example.tuvi.domain.model.SaoInfo
import com.example.tuvi.domain.model.ThienBanInfo
import com.example.tuvi.domain.model.TuViChart

fun TuViResponse.toDomain(): TuViChart = TuViChart(
    thienBan = thien_ban.toDomain(),
    diaBan   = dia_ban.map { it.toDomain() }
)

fun ThienBanDto.toDomain(): ThienBanInfo = ThienBanInfo(
    ten              = ten,
    gioiTinh         = gioi_tinh,
    ngayDuong        = ngay_duong,
    ngayAm           = ngay_am,
    ngayAmLichTen    = ngayAmLichTen,
    thangNhuan       = thangNhuan,
    gioSinh          = gioSinh,
    chiGioSinh       = chiGioSinh,
    canNam           = canNam,
    chiNam           = chiNam,
    canThang         = canThang,
    chiThang         = chiThang,
    canNgay          = canNgay,
    chiNgay          = chiNgay,
    amDuongNamSinh   = amDuongNamSinh,
    amDuongMenh      = amDuongMenh,
    menh             = menh,
    banMenh          = banMenh,
    cuc              = cuc,
    hanhCuc          = hanhCuc,
    menhChu          = menhChu,
    thanChu          = thanChu,
    sinhKhac         = sinhKhac,
    namXem           = namXem,
    tuoiAm           = tuoiAm
)

fun CungDto.toDomain(): CungInfo = CungInfo(
    cungTen  = cungTen,
    cungChu  = cungChu,
    hanhCung = hanhCung,
    thienCan = thienCan,
    daiHan   = daiHan,
    thang    = thang,
    sao      = sao.map { it.toDomain() },
    tuan     = tuan,
    triet    = triet
)

fun SaoDto.toDomain(): SaoInfo = SaoInfo(
    ten           = ten,
    dacTinh       = dac_tinh,
    nguHanh       = ngu_hanh,
    vongTrangSinh = vongTrangSinh,
    isLuu         = isLuu
)
