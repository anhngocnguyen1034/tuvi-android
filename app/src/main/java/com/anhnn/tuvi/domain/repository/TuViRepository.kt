package com.anhnn.tuvi.domain.repository

import com.anhnn.tuvi.data.remote.dto.VanHanResponse
import com.anhnn.tuvi.domain.model.CungSlug
import com.anhnn.tuvi.domain.model.IapProduct
import com.anhnn.tuvi.domain.model.IapVerifyResult
import com.anhnn.tuvi.domain.model.QuotaStatus
import com.anhnn.tuvi.domain.model.TuViChart
import com.anhnn.tuvi.domain.model.TuViChartInput
import com.anhnn.tuvi.domain.model.TuViInterpretation
import com.anhnn.tuvi.domain.model.TuViVanHanResult

interface TuViRepository {
    suspend fun getTuViChart(input: TuViChartInput): TuViChart

    /** POST /api/interpret cho 1 cung — chart trong `data_la_so`, text trong `ai_reading`. */
    suspend fun getTuViInterpretation(input: TuViChartInput, cung: CungSlug): TuViInterpretation

    /** POST /api/interpret/hoi — trả lời câu hỏi tự do `cauHoi` về lá số. */
    suspend fun getTuViHoi(input: TuViChartInput, cauHoi: String): TuViInterpretation

    /** GET /api/quota — số lượt AI còn lại của thiết bị. */
    suspend fun getQuota(): QuotaStatus

    /** GET /api/iap/products — danh mục gói nạp lượt. */
    suspend fun getIapProducts(): List<IapProduct>

    /** POST /api/iap/verify — verify giao dịch Play rồi cấp credit. */
    suspend fun verifyPurchase(productId: String, purchaseToken: String): IapVerifyResult

    /** POST /api/van-han/timeline — biểu đồ vận hạn 12 tháng năm `input.namXem`. Không tốn quota AI. */
    suspend fun getTuViVanHan(input: TuViChartInput): TuViVanHanResult

    /** POST /api/interpret/van-han — luận giải văn bản (AI) vận hạn năm `input.namXem`. */
    suspend fun getTuViVanHanAiReading(input: TuViChartInput): TuViVanHanResult
}
