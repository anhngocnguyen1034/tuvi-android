package com.example.tuvi.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** GET /api/quota — số lượt AI còn lại của thiết bị. */
@Serializable
data class QuotaStatusDto(
    @SerialName("free_limit") val freeLimit: Int = 0,
    val granted: Int = 0,
    val used: Int = 0,
    val remaining: Int = 0,
)

/** Một gói trong danh mục IAP (GET /api/iap/products). Giá lấy từ Google Play. */
@Serializable
data class IapProductDto(
    @SerialName("product_id") val productId: String,
    val credits: Int,
)

/** GET /api/iap/products — danh mục gói nạp lượt. */
@Serializable
data class IapProductsDto(
    val products: List<IapProductDto> = emptyList(),
)

/** POST /api/iap/verify — gửi giao dịch Google Play để backend cấp credit. */
@Serializable
data class IapVerifyRequestDto(
    @SerialName("product_id") val productId: String,
    @SerialName("purchase_token") val purchaseToken: String,
)

/** Kết quả verify: số credit vừa cấp + số dư còn lại. */
@Serializable
data class IapVerifyResponseDto(
    val status: String? = null,
    val granted: Int = 0,
    val remaining: Int = 0,
)
