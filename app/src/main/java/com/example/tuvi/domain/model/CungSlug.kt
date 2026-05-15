package com.example.tuvi.domain.model

/**
 * 12 cung của lá số Tử Vi, dùng làm tham số `cung` cho POST /api/interpret.
 * [slug] là giá trị backend yêu cầu; [displayName] dùng cho UI.
 */
enum class CungSlug(val slug: String, val displayName: String) {
    MENH("menh", "Mệnh"),
    PHU_MAU("phu_mau", "Phụ Mẫu"),
    PHUC_DUC("phuc_duc", "Phúc Đức"),
    DIEN_TRACH("dien_trach", "Điền Trạch"),
    QUAN_LOC("quan_loc", "Quan Lộc"),
    TAI_BACH("tai_bach", "Tài Bạch"),
    NO_BOC("no_boc", "Nô Bộc"),
    THIEN_DI("thien_di", "Thiên Di"),
    TAT_ACH("tat_ach", "Tật Ách"),
    TU_TUC("tu_tuc", "Tử Tức"),
    PHU_THE("phu_the", "Phu Thê"),
    HUYNH_DE("huynh_de", "Huynh Đệ"),
}
