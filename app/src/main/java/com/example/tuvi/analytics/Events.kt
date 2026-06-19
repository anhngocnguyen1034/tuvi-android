package com.example.tuvi.analytics

/**
 * Danh sách event analytics RIÊNG của app Tử Vi (module `anhnn-components-analytics` chỉ lo hạ
 * tầng, không biết event cụ thể). Gọi qua `Analytics.logEvent(Events.X, mapOf(...))`.
 *
 * ⚠️ KHÔNG log dữ liệu sinh (tên/ngày/giờ) — chỉ enum/boolean phi danh tính.
 */
object Events {
    // Funnel lá số — KHÔNG track lúc tạo/xem (tránh log giới tính, ngày/giờ sinh).
    // Chỉ giữ các hành động phi-danh-tính bên dưới.
    const val CHART_VIEW_ERROR = "chart_view_error"       // tính lá số lỗi
    const val CHART_SAVE = "chart_save"                   // lưu lá số vào thư viện
    const val CHART_UNSAVE = "chart_unsave"               // bỏ lưu
    const val CHART_DOWNLOAD = "chart_download"           // tải lá số xuống ảnh

    // Cài đặt
    const val LANGUAGE_CHANGE = "language_change"         // người dùng đổi ngôn ngữ (biết thị trường)

    // Màn Home
    const val HOME_TILE_CLICK = "home_tile_click"         // bấm 1 trong 3 nút tính năng ở Home

    // Trình duyệt
    const val BROWSER_NAVIGATE = "browser_navigate"       // người dùng search / mở URL trên thanh địa chỉ

    // Tham số
    const val P_SUCCESS = "success"         // boolean
    const val P_ERROR = "error_reason"      // tên loại lỗi
    const val P_TILE = "tile"               // "tuvi" | "browser" | "calendar"
    const val P_LANGUAGE = "language"       // mã locale: "vi" | "en" | "zh" ...

    // Tham số trình duyệt
    const val P_NAV_TYPE = "nav_type"           // "search" | "url"
    const val P_SEARCH_QUERY = "search_query"   // nội dung tìm kiếm (chỉ khi nav_type = "search")
    const val P_NAV_URL = "nav_url"             // URL đích (đã chuẩn hoá)
}
