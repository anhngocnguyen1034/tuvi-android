package com.anhnn.tuvi

/**
 * Cờ bật/tắt tính năng ở mức build. Dùng để giấu tính năng chưa hoàn thiện khỏi bản phát hành
 * mà vẫn giữ nguyên code (route, screen, ViewModel) để tiếp tục làm ở nhánh riêng.
 */
object FeatureFlags {
    /**
     * Luận giải AI + màn Cửa hàng (mua token cho AI).
     *
     * Đang tắt: ẩn nút "Xem kết quả AI luận giải" ở màn lá số và icon cửa hàng ở Home.
     * Route `ai_reading` / `store` vẫn đăng ký trong NavHost nhưng không còn lối vào.
     * Bật lại bằng cách đổi thành `true` (không cần sửa chỗ nào khác).
     */
    const val AI_READING_ENABLED = false

    /**
     * Toàn bộ quảng cáo (banner / native / interstitial / app-open).
     *
     * Đang tắt cho bản đầu lên CH Play: chặn ở `adsEnabled` của [com.anhnn.ads.AdsConfig] nên
     * mọi placement tự ẩn, không cần sửa từng screen. Bật lại bằng cách đổi thành `true`,
     * hoặc để `true` và điều khiển từ xa qua Remote Config `ads_enabled`.
     */
    const val ADS_ENABLED = false
}
