package com.example.tuvi.widget

import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.sin

/**
 * Chuyển đổi Dương lịch -> Âm lịch (offline) theo thuật toán Hồ Ngọc Đức.
 * Dùng cho widget vì widget không nên phụ thuộc mạng/backend.
 *
 * Múi giờ mặc định = 7.0 (Việt Nam).
 */
object LunarCalendar {

    private const val TIME_ZONE = 7.0

    private val CAN = arrayOf(
        "Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý"
    )
    private val CHI = arrayOf(
        "Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi"
    )

    /** Kết quả âm lịch. [isLeapMonth] = tháng nhuận. */
    data class Lunar(
        val day: Int,
        val month: Int,
        val year: Int,
        val isLeapMonth: Boolean,
    ) {
        /** Can Chi của năm âm lịch, ví dụ "Ất Tỵ". */
        val yearCanChi: String
            get() = "${CAN[(year + 6) % 10]} ${CHI[(year + 8) % 12]}"
    }

    /**
     * Ngày tốt (Hoàng đạo) hay xấu (Hắc đạo) theo can chi ngày và tháng âm lịch.
     *
     * 12 vị thần luân phiên theo Địa Chi của ngày; điểm khởi (Thanh Long) phụ thuộc
     * tháng âm. Mẫu tốt/xấu 12 bước bắt đầu từ Thanh Long:
     * Thanh Long(+), Minh Đường(+), Thiên Hình(-), Chu Tước(-), Kim Quỹ(+), Bảo Quang(+),
     * Bạch Hổ(-), Ngọc Đường(+), Thiên Lao(-), Huyền Vũ(-), Tư Mệnh(+), Câu Trần(-).
     */
    fun isAuspicious(dd: Int, mm: Int, yy: Int): Boolean {
        val jd = jdFromDate(dd, mm, yy)
        val lunarMonth = fromSolar(dd, mm, yy).month
        val dayChi = (jd + 1) % 12                       // 0 = Tý
        val thanhLongChi = ((lunarMonth - 1) % 6) * 2    // Chi khởi của Thanh Long
        val offset = ((dayChi - thanhLongChi) % 12 + 12) % 12
        return HOANG_DAO_PATTERN[offset]
    }

    private val HOANG_DAO_PATTERN = booleanArrayOf(
        true, true, false, false, true, true, false, true, false, false, true, false
    )

    /** Chuyển ngày dương [dd]/[mm]/[yy] sang âm lịch. */
    fun fromSolar(dd: Int, mm: Int, yy: Int): Lunar {
        val dayNumber = jdFromDate(dd, mm, yy)
        val k = floor((dayNumber - 2415021.076998695) / 29.530588853).toInt()
        var monthStart = newMoonDay(k + 1)
        if (monthStart > dayNumber) monthStart = newMoonDay(k)

        var a11 = lunarMonth11(yy)
        var b11 = a11
        val lunarYearBase: Int
        if (a11 >= monthStart) {
            lunarYearBase = yy
            a11 = lunarMonth11(yy - 1)
        } else {
            lunarYearBase = yy + 1
            b11 = lunarMonth11(yy + 1)
        }

        val lunarDay = dayNumber - monthStart + 1
        val diff = floor((monthStart - a11) / 29.0).toInt()
        var lunarLeap = false
        var lunarMonth = diff + 11
        if (b11 - a11 > 365) {
            val leapMonthDiff = leapMonthOffset(a11)
            if (diff >= leapMonthDiff) {
                lunarMonth = diff + 10
                if (diff == leapMonthDiff) lunarLeap = true
            }
        }
        if (lunarMonth > 12) lunarMonth -= 12
        var lunarYear = lunarYearBase
        if (lunarMonth >= 11 && diff < 4) lunarYear -= 1

        return Lunar(lunarDay, lunarMonth, lunarYear, lunarLeap)
    }

    /** Số ngày Julian của ngày dương lịch. */
    private fun jdFromDate(dd: Int, mm: Int, yy: Int): Int {
        val a = (14 - mm) / 12
        val y = yy + 4800 - a
        val m = mm + 12 * a - 3
        var jd = dd + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
        if (jd < 2299161) {
            jd = dd + (153 * m + 2) / 5 + 365 * y + y / 4 - 32083
        }
        return jd
    }

    /** Ngày Julian của điểm sóc (trăng non) thứ [k]. */
    private fun newMoonDay(k: Int): Int {
        val t = k / 1236.85
        val t2 = t * t
        val t3 = t2 * t
        val dr = PI / 180
        var jd1 = 2415020.75933 + 29.53058868 * k + 0.0001178 * t2 - 0.000000155 * t3
        jd1 += 0.00033 * sin((166.56 + 132.87 * t - 0.009173 * t2) * dr)
        val m = 359.2242 + 29.10535608 * k - 0.0000333 * t2 - 0.00000347 * t3
        val mpr = 306.0253 + 385.81691806 * k + 0.0107306 * t2 + 0.00001236 * t3
        val f = 21.2964 + 390.67050646 * k - 0.0016528 * t2 - 0.00000239 * t3
        var c1 = (0.1734 - 0.000393 * t) * sin(m * dr) + 0.0021 * sin(2 * dr * m)
        c1 += -0.4068 * sin(mpr * dr) + 0.0161 * sin(dr * 2 * mpr)
        c1 += -0.0004 * sin(dr * 3 * mpr)
        c1 += 0.0104 * sin(dr * 2 * f) - 0.0051 * sin(dr * (m + mpr))
        c1 += -0.0074 * sin(dr * (m - mpr)) + 0.0004 * sin(dr * (2 * f + m))
        c1 += -0.0004 * sin(dr * (2 * f - m)) - 0.0006 * sin(dr * (2 * f + mpr))
        c1 += 0.0010 * sin(dr * (2 * f - mpr)) + 0.0005 * sin(dr * (2 * mpr + m))
        val deltat = if (t < -11) {
            0.001 + 0.000839 * t + 0.0002261 * t2 - 0.00000845 * t3 - 0.000000081 * t * t3
        } else {
            -0.000278 + 0.000265 * t + 0.000262 * t2
        }
        val jdNew = jd1 + c1 - deltat
        return floor(jdNew + 0.5 + TIME_ZONE / 24).toInt()
    }

    /** Kinh độ mặt trời (chia thành 12 cung, trả 0..11). */
    private fun sunLongitude(jdn: Int): Int {
        val t = (jdn - 2451545.5 - TIME_ZONE / 24) / 36525
        val t2 = t * t
        val dr = PI / 180
        val m = 357.52910 + 35999.05030 * t - 0.0001559 * t2 - 0.00000048 * t * t2
        val l0 = 280.46645 + 36000.76983 * t + 0.0003032 * t2
        var dl = (1.914600 - 0.004817 * t - 0.000014 * t2) * sin(dr * m)
        dl += (0.019993 - 0.000101 * t) * sin(dr * 2 * m) + 0.000290 * sin(dr * 3 * m)
        var l = (l0 + dl) * dr
        l -= PI * 2 * floor(l / (PI * 2))
        return floor(l / PI * 6).toInt()
    }

    /** Ngày Julian bắt đầu tháng 11 âm lịch của năm dương [yy]. */
    private fun lunarMonth11(yy: Int): Int {
        val off = jdFromDate(31, 12, yy) - 2415021
        val k = floor(off / 29.530588853).toInt()
        var nm = newMoonDay(k)
        val sunLong = sunLongitude(nm)
        if (sunLong >= 9) nm = newMoonDay(k - 1)
        return nm
    }

    /** Xác định tháng nhuận nằm sau tháng 11 [a11]. */
    private fun leapMonthOffset(a11: Int): Int {
        val k = floor((a11 - 2415021.076998695) / 29.530588853 + 0.5).toInt()
        var last: Int
        var i = 1
        var arc = sunLongitude(newMoonDay(k + i))
        do {
            last = arc
            i++
            arc = sunLongitude(newMoonDay(k + i))
        } while (arc != last && i < 14)
        return i - 1
    }
}
