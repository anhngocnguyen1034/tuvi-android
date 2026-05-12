package com.example.tuvi.data.repository

import com.example.tuvi.data.mapper.toDomain
import com.example.tuvi.data.remote.TuViApiService
import com.example.tuvi.data.remote.dto.TuViRequest
import com.example.tuvi.domain.AiInterpretationUnavailableException
import com.example.tuvi.domain.model.TuViChart
import com.example.tuvi.domain.model.TuViChartInput
import com.example.tuvi.domain.model.TuViInterpretation
import com.example.tuvi.domain.repository.TuViRepository
import retrofit2.HttpException

class TuViRepositoryImpl(
    private val apiService: TuViApiService
) : TuViRepository {

    override suspend fun getTuViChart(input: TuViChartInput): TuViChart {
        val request = buildRequest(input)
        return apiService.getTuVi(request).toDomain()
    }

    override suspend fun getTuViInterpretation(input: TuViChartInput): TuViInterpretation {
        val request = buildRequest(input)
        return try {
            val body = apiService.interpret(request)
            val raw = body.data_la_so
                ?: error("Server response missing data_la_so")
            TuViInterpretation(
                chart = raw.toDomain(),
                aiReading = body.ai_reading.orEmpty(),
            )
        } catch (e: HttpException) {
            if (e.code() == 503) throw AiInterpretationUnavailableException()
            throw e
        }
    }

    private fun buildRequest(input: TuViChartInput): TuViRequest {
        val gioSinh = ((input.gio + 1) / 2) % 12 + 1
        return TuViRequest(
            ten = input.ten,
            ngay = input.ngay,
            thang = input.thang,
            nam = input.nam,
            nam_xem = input.namXem,
            gio = input.gio,
            phut = input.phut,
            gio_sinh = gioSinh,
            gioi_tinh = input.gioiTinh,
            duong_lich = input.duongLich,
        )
    }
}
