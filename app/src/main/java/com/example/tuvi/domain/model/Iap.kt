package com.example.tuvi.domain.model

/** Số lượt AI còn lại của thiết bị (nguồn sự thật từ backend /api/quota). */
data class QuotaStatus(
    val freeLimit: Int,
    val granted: Int,
    val used: Int,
    val remaining: Int,
)

/** Một gói nạp lượt trong danh mục backend (giá lấy từ Google Play). */
data class IapProduct(
    val productId: String,
    val credits: Int,
)

/** Kết quả verify giao dịch IAP: số credit vừa cấp + số dư sau khi cộng. */
data class IapVerifyResult(
    val granted: Int,
    val remaining: Int,
)
