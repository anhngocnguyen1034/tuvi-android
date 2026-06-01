package com.example.tuvi.ads

/**
 * Tên các vị trí quảng cáo trong app. Mỗi tên map tới 1 ad unit id riêng qua
 * Remote Config key [RemoteConfigManager.KEY_AD_UNITS] (JSON ad_name -> ad_id).
 * Code chỉ gọi theo tên; đổi ad id chỉ cần sửa Remote Config, không build lại.
 */
object AdNames {
    // Interstitial
    const val SPLASH_OPEN = "splash_open"      // sau splash -> Home
    const val HOME_TUVI = "home_tuvi"          // Home -> màn nhập liệu
    const val HOME_BROWSER = "home_browser"    // Home -> trình duyệt
    const val HOME_CALENDAR = "home_calendar"  // Home -> lịch
    const val CHART_CREATE = "chart_create"    // bấm tạo lá số -> biểu đồ
    const val AI_OPEN = "ai_open"              // bấm "Xem kết quả AI luận giải" -> màn hỏi AI
    const val AI_REQUEST = "ai_request"        // bấm "Hỏi AI miễn phí"

    // Native
    const val LANGUAGE_NATIVE = "language_native"  // dưới danh sách chọn ngôn ngữ
    const val EXIT_NATIVE = "exit_native"          // giữa màn xác nhận thoát app

    // Banner
    const val EXIT_BANNER = "exit_banner"          // trên cùng màn xác nhận thoát app
}
