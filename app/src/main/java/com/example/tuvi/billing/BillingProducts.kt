package com.example.tuvi.billing

/**
 * Product id (Play Console) của app. Tập trung một chỗ để [com.example.tuvi.TuViApplication]
 * (khởi tạo IAP) và các màn UI (vd Gỡ quảng cáo trong Settings) dùng chung, tránh lệch chuỗi.
 *
 * Lưu ý: [REMOVE_ADS] phải là sản phẩm **non-consumable** đúng id này trong Play Console.
 */
object BillingProducts {
    const val REMOVE_ADS = "remove_ads"
}
