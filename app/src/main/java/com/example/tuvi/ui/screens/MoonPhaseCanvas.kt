package com.example.tuvi.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * Tính fullness dựa trên ngày âm lịch.
 * Ngày 1 → 0.0 (trăng non), ngày 15 → 1.0 (trăng tròn), ngày 30 → 0.0.
 */
fun calculateMoonFullness(lunarDay: Int): Float {
    val day = lunarDay.coerceIn(1, 30)
    return 1f - (abs(day - 15) / 15f)
}

/**
 * Vẽ trăng theo độ đầy [fullness] từ 0.0 (khuyết hoàn toàn) đến 1.0 (tròn xoe).
 *
 * Thuật toán:
 * - Dùng cung tròn bên phải của vòng ngoài làm ranh giới sáng.
 * - Dùng cung ellipse (terminator) làm ranh giới bóng:
 *     crescent (f≤0.5): cung phải ellipse ngược chiều kim đồng hồ → tạo lưỡi liềm
 *     gibbous  (f>0.5): cung trái ellipse cùng chiều kim đồng hồ  → tạo gần tròn
 */
fun DrawScope.drawCustomMoon(center: Offset, radius: Float, color: Color, fullness: Float) {
    // Giới hạn 0.88 để trăng rằm (ngày 15) vẫn có bóng mờ ở cạnh trái, không bao giờ tròn xoe.
    val f = fullness.coerceIn(0f, 0.88f)

    if (f < 0.03f) {
        drawCircle(color.copy(alpha = 0.15f), radius, center)
        return
    }

    // x-radius của ellipse terminator: co lại từ r→0 (new→quarter) rồi mở ra 0→r (quarter→full nhưng ngược chiều)
    val xR = radius * abs(1f - 2f * f)

    val bigRect = Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius)
    val ellipseRect = Rect(center.x - xR, center.y - radius, center.x + xR, center.y + radius)

    val path = Path().apply {
        // Cung phải vòng tròn lớn: đỉnh → phải → đáy (CW +180°)
        arcTo(bigRect, -90f, 180f, false)
        if (f <= 0.5f) {
            // Lưỡi liềm: cung phải ellipse hẹp ngược chiều kim (đáy → phải → đỉnh)
            arcTo(ellipseRect, 90f, -180f, false)
        } else {
            // Gần tròn: cung trái ellipse hẹp cùng chiều kim (đáy → trái → đỉnh)
            arcTo(ellipseRect, 90f, 180f, false)
        }
        close()
    }
    drawPath(path, color)
}

/**
 * Composable vẽ icon trăng với pha tương ứng với ngày âm lịch.
 */
@Composable
fun MoonPhaseCanvas(
    lunarDay: Int,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    val fullness = calculateMoonFullness(lunarDay)
    Canvas(modifier = modifier.size(size)) {
        val r = minOf(this.size.width, this.size.height) / 2f * 0.85f
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        drawCustomMoon(center, r, color, fullness)
    }
}
