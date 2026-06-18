package com.example.tuvi.analytics

/**
 * Danh sách event analytics RIÊNG của app Tử Vi (module `anhnn-components-analytics` chỉ lo hạ
 * tầng, không biết event cụ thể). Gọi qua `Analytics.logEvent(Events.X, mapOf(...))`.
 *
 * ⚠️ KHÔNG log dữ liệu sinh (tên/ngày/giờ) — chỉ enum/boolean phi danh tính.
 */
object Events {
    // Funnel lá số
    const val CHART_CREATE_SUBMIT = "chart_create_submit" // bấm tạo lá số
    const val CHART_VIEW_SUCCESS = "chart_view_success"   // tính lá số thành công
    const val CHART_VIEW_ERROR = "chart_view_error"       // tính lá số lỗi
    const val CHART_SAVE = "chart_save"                   // lưu lá số vào thư viện
    const val CHART_UNSAVE = "chart_unsave"               // bỏ lưu
    const val CHART_DOWNLOAD = "chart_download"           // tải lá số xuống ảnh

    // Tham số
    const val P_GENDER = "gender"           // 0/1
    const val P_LICH_TYPE = "lich_type"     // "duong" | "am"
    const val P_VIEW_YEAR = "view_year"
    const val P_HAS_NAME = "has_name"       // boolean (không log tên thật)
    const val P_SUCCESS = "success"         // boolean
    const val P_ERROR = "error_reason"      // tên loại lỗi
}
