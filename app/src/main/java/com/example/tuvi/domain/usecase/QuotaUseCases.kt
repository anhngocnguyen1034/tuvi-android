package com.example.tuvi.domain.usecase

import com.example.tuvi.domain.model.IapProduct
import com.example.tuvi.domain.model.IapVerifyResult
import com.example.tuvi.domain.model.QuotaStatus
import com.example.tuvi.domain.repository.TuViRepository

/** GET /api/quota — số lượt AI còn lại của thiết bị. */
class GetQuotaUseCase(private val repository: TuViRepository) {
    suspend operator fun invoke(): Result<QuotaStatus> =
        runCatching { repository.getQuota() }
}

/** GET /api/iap/products — danh mục gói nạp lượt. */
class GetIapProductsUseCase(private val repository: TuViRepository) {
    suspend operator fun invoke(): Result<List<IapProduct>> =
        runCatching { repository.getIapProducts() }
}

/** POST /api/iap/verify — verify giao dịch Play rồi cấp credit. */
class VerifyPurchaseUseCase(private val repository: TuViRepository) {
    suspend operator fun invoke(
        productId: String,
        purchaseToken: String,
    ): Result<IapVerifyResult> =
        runCatching { repository.verifyPurchase(productId, purchaseToken) }
}
