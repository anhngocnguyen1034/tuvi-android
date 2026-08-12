package com.anhnn.tuvi.data.repository

import com.anhnn.tuvi.data.mapper.toDomain
import com.anhnn.tuvi.data.remote.TuViApiService
import com.anhnn.tuvi.data.remote.dto.IapVerifyRequestDto
import com.anhnn.tuvi.data.remote.dto.TuViRequest
import com.anhnn.tuvi.domain.model.CungSlug
import com.anhnn.tuvi.domain.model.IapProduct
import com.anhnn.tuvi.domain.model.IapVerifyResult
import com.anhnn.tuvi.domain.model.QuotaStatus
import com.anhnn.tuvi.domain.model.TuViChart
import com.anhnn.tuvi.domain.model.TuViChartInput
import com.anhnn.tuvi.domain.model.TuViInterpretation
import com.anhnn.tuvi.domain.model.TuViVanHanResult
import com.anhnn.tuvi.domain.repository.TuViRepository

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

    override suspend fun getTuViVanHan(input: TuViChartInput): TuViVanHanResult {
        // Biểu đồ 12 tháng (`timeline` + `summary` + `data_la_so`) đến từ /api/van-han/timeline.
        // Endpoint này deterministic, KHÔNG tốn quota AI. (Chưa cần luận giải AI văn bản ở màn hình này.)
        val body = apiService.interpretVanHanTimeline(buildRequest(input))
        val raw = body.data_la_so ?: error("Empty van-han timeline response")
        return TuViVanHanResult(
            namXem = body.nam_xem ?: input.namXem,
            chart = raw.toDomain(),
            // Luận giải AI văn bản sẽ được lấy riêng ở getTuViVanHanAiReading (dùng sau).
            aiReading = "",
            timeline = body.timeline ?: emptyList(),
            summary = body.summary,
        )
    }

    override suspend fun getTuViVanHanAiReading(input: TuViChartInput): TuViVanHanResult {
        // POST /api/interpret/van-han — luận giải vận hạn năm qua Gemini (tốn quota AI).
        // Tạm thời chưa dùng ở SpecsScreen; để dành cho update sau.
        val body = apiService.interpretVanHan(buildRequest(input))
        val raw = body.data_la_so ?: error("Empty van-han response")
        return TuViVanHanResult(
            namXem = body.nam_xem ?: input.namXem,
            chart = raw.toDomain(),
            aiReading = body.ai_reading.orEmpty(),
            timeline = emptyList(),
            summary = null,
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
