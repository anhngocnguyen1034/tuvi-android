package com.example.tuvi.data.repository

import com.example.tuvi.data.mapper.toDomain
import com.example.tuvi.data.remote.TuViApiService
import com.example.tuvi.data.remote.dto.TuViRequest
import com.example.tuvi.domain.model.TuViChart
import com.example.tuvi.domain.model.TuViChartInput
import com.example.tuvi.domain.repository.TuViRepository

class TuViRepositoryImpl(
    private val apiService: TuViApiService
) : TuViRepository {

    override suspend fun getTuViChart(input: TuViChartInput): TuViChart {
        val gioSinh = ((input.gio + 1) / 2) % 12 + 1
        val request = TuViRequest(
            ten        = input.ten,
            ngay       = input.ngay,
            thang      = input.thang,
            nam        = input.nam,
            gio        = input.gio,
            phut       = input.phut,
            gio_sinh   = gioSinh,
            gioi_tinh  = input.gioiTinh
        )
        return apiService.getTuVi(request).toDomain()
    }
}
