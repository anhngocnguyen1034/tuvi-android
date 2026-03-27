package com.example.tuvi.domain.model

/** Input từ người dùng để tra lá số. */
data class TuViChartInput(
    val ten: String,
    val ngay: Int,
    val thang: Int,
    val nam: Int,
    val namXem: Int,
    val gio: Int,
    val phut: Int,
    val gioiTinh: Int   // 1 = Nam, -1 = Nữ
)

/** Domain entity – toàn bộ lá số Tử Vi (không có annotation mạng). */
data class TuViChart(
    val thienBan: ThienBanInfo,
    val diaBan: List<CungInfo>
)

data class ThienBanInfo(
    val ten: String,
    val gioiTinh: String,
    val ngayDuong: String,
    val ngayAm: String,
    val ngayAmLichTen: String? = null,
    val thangNhuan: Int? = null,
    val gioSinh: String? = null,
    val chiGioSinh: String? = null,
    val canNam: String? = null,
    val chiNam: String? = null,
    val canThang: String? = null,
    val chiThang: String? = null,
    val canNgay: String? = null,
    val chiNgay: String? = null,
    val amDuongNamSinh: String? = null,
    val amDuongMenh: String? = null,
    val menh: String? = null,
    val banMenh: String? = null,
    val cuc: String? = null,
    val hanhCuc: Int? = null,
    val menhChu: String? = null,
    val thanChu: String? = null,
    val sinhKhac: String? = null,
    val namXem: Int? = null,
    val tuoiAm: Int? = null
)

data class CungInfo(
    /**
     * **Chỉ số ô trên lưới** (1…12) khớp vòng `gridMapping` trong app — **không** gắn cứng “Tý luôn một ô”;
     * địa chi từng cung (`cung_ten`) thay đổi theo lá số (Mệnh, …). Engine backend phải map đúng ô hoặc
     * trả `dia_ban` theo thứ tự vòng khi không gửi `cung_so`.
     */
    val cungSo: Int? = null,
    /**
     * **Can theo vòng tháng** (cung Dần = tháng Giêng, cùng hệ với `timCuc` trong backend) — chỉ để **hiển thị** góc ô (Can Chi với [cungTen]).
     * Không thay cho bảng `thienCan[can].vitriDiaBan` dùng khi an Lộc Tồn / Kình Đà / … (hệ lookup 10 Can).
     */
    val thienCan: String? = null,
    /**
     * Một ký tự `chuCaiDau` (G,A,B,…) — map sang tên Can đầy đủ khi hiển thị; cùng nguồn nghĩa với [thienCan] (ưu tiên chuỗi đầy đủ nếu có).
     */
    val chuCaiDau: String? = null,
    val cungTen: String,
    val cungChu: String,
    val hanhCung: String,
    val daiHan: Int? = null,
    val thang: Int? = null,
    val sao: List<SaoInfo>,
    val tuan: Boolean = false,
    val triet: Boolean = false
)

data class SaoInfo(
    val ten: String,
    val dacTinh: String? = "",
    val nguHanh: String? = null,   // "T"=Thủy, "H"=Hỏa, "K"=Kim, "M"=Mộc, "TH"=Thổ
    val vongTrangSinh: Int? = 0,
    val isLuu: Boolean = false
)
