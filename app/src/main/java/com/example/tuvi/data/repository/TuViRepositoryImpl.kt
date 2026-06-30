package com.example.tuvi.data.repository

import com.example.tuvi.data.mapper.toDomain
import com.example.tuvi.data.remote.TuViApiService
import com.example.tuvi.data.remote.dto.IapVerifyRequestDto
import com.example.tuvi.data.remote.dto.TuViRequest
import com.example.tuvi.domain.model.CungSlug
import com.example.tuvi.domain.model.IapProduct
import com.example.tuvi.domain.model.IapVerifyResult
import com.example.tuvi.domain.model.QuotaStatus
import com.example.tuvi.domain.model.TuViChart
import com.example.tuvi.domain.model.TuViChartInput
import com.example.tuvi.domain.model.TuViInterpretation
import com.example.tuvi.domain.repository.TuViRepository

class TuViRepositoryImpl(
    private val apiService: TuViApiService
) : TuViRepository {

    override suspend fun getTuViChart(input: TuViChartInput): TuViChart {
        val request = buildRequest(input)
        return apiService.getTuVi(request).toDomain()
    }

    override suspend fun getTuViInterpretation(
        input: TuViChartInput,
        cung: CungSlug,
    ): TuViInterpretation {
        val request = buildRequest(input).copy(cung = cung.slug)
        val body = apiService.interpret(request)
        val raw = body.data_la_so ?: error("Empty interpret response")
        return TuViInterpretation(
            chart = raw.toDomain(),
            aiReading = body.ai_reading.orEmpty(),
        )
    }

    override suspend fun getTuViVanHan(input: TuViChartInput): TuViInterpretation {
        val body = apiService.interpretVanHan(buildRequest(input))
        val raw = body.data_la_so ?: error("Empty van-han response")
        return TuViInterpretation(
            chart = raw.toDomain(),
            aiReading = body.ai_reading.orEmpty(),
        )
    }

    override suspend fun getTuViHoi(
        input: TuViChartInput,
        cauHoi: String,
    ): TuViInterpretation {
        val request = buildRequest(input).copy(cau_hoi = cauHoi)
        val body = apiService.interpretHoi(request)
        val raw = body.data_la_so ?: error("Empty hoi response")
        return TuViInterpretation(
            chart = raw.toDomain(),
            aiReading = body.ai_reading.orEmpty(),
        )
    }

    override suspend fun getQuota(): QuotaStatus {
        val dto = apiService.getQuota()
        return QuotaStatus(
            freeLimit = dto.freeLimit,
            granted = dto.granted,
            used = dto.used,
            remaining = dto.remaining,
        )
    }

    override suspend fun getIapProducts(): List<IapProduct> =
        apiService.getIapProducts().products.map {
            IapProduct(productId = it.productId, credits = it.credits)
        }

    override suspend fun verifyPurchase(
        productId: String,
        purchaseToken: String,
    ): IapVerifyResult {
        val dto = apiService.verifyIap(
            IapVerifyRequestDto(productId = productId, purchaseToken = purchaseToken)
        )
        return IapVerifyResult(granted = dto.granted, remaining = dto.remaining)
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
