package com.example.tuvi.domain.repository

import com.example.tuvi.domain.model.CungSlug
import com.example.tuvi.domain.model.IapProduct
import com.example.tuvi.domain.model.IapVerifyResult
import com.example.tuvi.domain.model.QuotaStatus
import com.example.tuvi.domain.model.TuViChart
import com.example.tuvi.domain.model.TuViChartInput
import com.example.tuvi.domain.model.TuViInterpretation

interface TuViRepository {
    suspend fun getTuViChart(input: TuViChartInput): TuViChart

    /** POST /api/interpret cho 1 cung — chart trong `data_la_so`, text trong `ai_reading`. */
    suspend fun getTuViInterpretation(input: TuViChartInput, cung: CungSlug): TuViInterpretation

    /** POST /api/interpret/van-han — luận vận hạn năm `input.namXem`. */
    suspend fun getTuViVanHan(input: TuViChartInput): TuViInterpretation

    /** POST /api/interpret/hoi — trả lời câu hỏi tự do `cauHoi` về lá số. */
    suspend fun getTuViHoi(input: TuViChartInput, cauHoi: String): TuViInterpretation

    /** GET /api/quota — số lượt AI còn lại của thiết bị. */
    suspend fun getQuota(): QuotaStatus

    /** GET /api/iap/products — danh mục gói nạp lượt. */
    suspend fun getIapProducts(): List<IapProduct>

    /** POST /api/iap/verify — verify giao dịch Play rồi cấp credit. */
    suspend fun verifyPurchase(productId: String, purchaseToken: String): IapVerifyResult
}
