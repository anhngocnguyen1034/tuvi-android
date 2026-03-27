package com.example.tuvi.data.local

import com.example.tuvi.domain.model.SavedChart

fun SavedChartEntity.toDomain() = SavedChart(
    id = id,
    ten = ten,
    ngaySinh = ngaySinh,
    gioiTinh = gioiTinh,
    nhom = nhom,
    ngayLuu = ngayLuu,
    inputJson = inputJson,
    chartJson = chartJson
)

fun SavedChart.toEntity() = SavedChartEntity(
    id = id,
    ten = ten,
    ngaySinh = ngaySinh,
    gioiTinh = gioiTinh,
    nhom = nhom,
    ngayLuu = ngayLuu,
    inputJson = inputJson,
    chartJson = chartJson
)
