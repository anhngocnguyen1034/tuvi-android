package com.example.tuvi.ads

import com.anhnn.ads.AdFormat

/**
 * Tên các vị trí quảng cáo trong app. Mỗi tên map tới 1 ad unit id riêng qua
 * Remote Config key [RemoteConfigManager.KEY_AD_UNITS] (JSON ad_name -> ad_id).
 * Code chỉ gọi theo tên; đổi ad id chỉ cần sửa Remote Config, không build lại.
 *
 * [formatOf] khai báo định dạng từng vị trí cho module `anhnn-components-ads`.
 */
object AdNames {
    // Interstitial
    const val SPLASH_OPEN = "splash_open"      // sau splash -> Home
    const val INTRO_DONE = "intro_done"        // sau intro (lần đầu) -> Home
    const val HOME_TUVI = "home_tuvi"          // Home -> màn nhập liệu
    const val HOME_BROWSER = "home_browser"    // Home -> trình duyệt
    const val HOME_CALENDAR = "home_calendar"  // Home -> lịch
    const val CHART_CREATE = "chart_create"    // bấm tạo lá số -> biểu đồ
    const val AI_OPEN = "ai_open"              // bấm "Xem kết quả AI luận giải" -> màn hỏi AI
    const val AI_REQUEST = "ai_request"        // bấm "Hỏi AI miễn phí"
    const val CHART_DOWNLOAD = "chart_download" // bấm tải lá số xuống ảnh

    // Native
    const val LANGUAGE_NATIVE = "language_native"  // dưới danh sách chọn ngôn ngữ
    const val EXIT_NATIVE = "exit_native"          // giữa màn xác nhận thoát app

    // Banner
    const val EXIT_BANNER = "exit_banner"          // trên cùng màn xác nhận thoát app
    const val SAVED_BANNER = "saved_banner"        // dưới cùng màn Lá số đã lưu
    const val HOME_BANNER = "home_banner"          // dưới cùng màn Home
    const val INTRO_BANNER = "intro_banner"        // dưới cùng màn intro (onboarding)

    /** Định dạng của từng vị trí — module ads dựa vào đây để nạp/cache đúng kiểu. */
    fun formatOf(adName: String): AdFormat? = when (adName) {
        SPLASH_OPEN, INTRO_DONE, HOME_TUVI, HOME_BROWSER, HOME_CALENDAR,
        CHART_CREATE, AI_OPEN, AI_REQUEST, CHART_DOWNLOAD -> AdFormat.INTERSTITIAL
        LANGUAGE_NATIVE, EXIT_NATIVE -> AdFormat.NATIVE
        EXIT_BANNER, SAVED_BANNER, HOME_BANNER, INTRO_BANNER -> AdFormat.BANNER
        else -> null
    }
}
