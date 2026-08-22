package com.anhnn.tuvi.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anhnn.tuvi.data.remote.dto.ThangHanData
import com.anhnn.tuvi.ui.theme.ChartBorderGold
import com.anhnn.tuvi.ui.theme.ChartGold
import com.anhnn.tuvi.ui.theme.ChartGoldDim
import com.anhnn.tuvi.ui.theme.ChartIvory
import com.anhnn.tuvi.ui.theme.ChartIvoryDim
import com.anhnn.tuvi.ui.theme.ChartRed
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/** Số vòng lưới đồng tâm (mỗi vòng = 2 điểm trên thang 0..10). */
private const val RING_COUNT = 5
private const val MAX_SCORE = 10f

/**
 * Biểu đồ màng nhện (radar) 12 trục — mỗi trục là một tháng của năm xem hạn.
 *
 * Chạm vào bất kỳ vùng nào của biểu đồ sẽ chọn trục tháng gần nhất theo góc;
 * đỉnh tháng đang chọn được phóng to và nối với tâm bằng một tia sáng.
 */
@Composable
fun VanHanRadarChart(
    timeline: List<ThangHanData>,
    selectedIndex: Int,
    onMonthSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (timeline.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val onSelect by rememberUpdatedState(onMonthSelected)
    val haptic = LocalHapticFeedback.current

    val gridColor = ChartBorderGold
    val axisColor = ChartGoldDim
    val webColor = ChartGold
    val labelColor = ChartIvoryDim
    val labelSelectedColor = ChartIvory
    val worstColor = ChartRed

    // Hiệu ứng "bung" màng nhện từ tâm ra khi dữ liệu về
    val grow = remember { Animatable(0f) }
    LaunchedEffect(timeline) {
        grow.snapTo(0f)
        grow.animateTo(1f, tween(durationMillis = 700))
    }
    val growValue = grow.value

    // Đỉnh/đáy chỉ phụ thuộc dữ liệu — tính một lần, không tính lại mỗi frame
    val bestScore = remember(timeline) { timeline.maxOf { it.normalizedScore } }
    val worstScore = remember(timeline) { timeline.minOf { it.normalizedScore } }

    // Đo nhãn tháng một lần cho mỗi trạng thái (thường / đang chọn):
    // draw scope chạy lại mỗi frame của animation, đo text ở đó rất tốn kém.
    val labels = remember(timeline, labelColor, labelSelectedColor, textMeasurer) {
        timeline.map { data ->
            textMeasurer.measure(
                text = "T${data.thang}",
                style = TextStyle(
                    color = labelColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            )
        }
    }
    val labelsSelected = remember(timeline, labelSelectedColor, textMeasurer) {
        timeline.map { data ->
            textMeasurer.measure(
                text = "T${data.thang}",
                style = TextStyle(
                    color = labelSelectedColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }

    val selected = timeline.getOrNull(selectedIndex)
    Box(
        modifier = modifier.semantics {
            contentDescription = selected?.let {
                "Biểu đồ vận hạn 12 tháng. Đang chọn ${it.thangTen}, " +
                    "điểm ${"%.1f".format(java.util.Locale.US, it.normalizedScore)} trên 10."
            } ?: "Biểu đồ vận hạn 12 tháng"
        }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(timeline, selectedIndex) {
                    detectTapGestures { tap ->
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val dx = tap.x - cx
                        val dy = tap.y - cy
                        // Chạm sát tâm thì không đổi lựa chọn (góc không xác định)
                        if (hypot(dx, dy) < 12f) return@detectTapGestures
                        // Góc 0° ở đỉnh trên (12h), tăng theo chiều kim đồng hồ
                        var angle = Math.toDegrees(atan2(dx.toDouble(), -dy.toDouble())).toFloat()
                        if (angle < 0f) angle += 360f
                        val step = 360f / timeline.size
                        val next = (angle / step).roundToInt() % timeline.size
                        if (next != selectedIndex) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        onSelect(next)
                    }
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            // Chừa lề cho nhãn tháng nằm ngoài lưới
            val radius = min(size.width, size.height) / 2f - 22.dp.toPx()
            if (radius <= 0f) return@Canvas

            val count = timeline.size
            val angleStep = 2 * PI / count

            fun vertex(index: Int, ratio: Float): Offset {
                val a = -PI / 2 + angleStep * index
                return Offset(
                    x = center.x + (cos(a) * radius * ratio).toFloat(),
                    y = center.y + (sin(a) * radius * ratio).toFloat()
                )
            }

            // 1. Lưới đa giác đồng tâm
            for (ring in 1..RING_COUNT) {
                val ratio = ring / RING_COUNT.toFloat()
                val ringPath = Path()
                for (i in 0 until count) {
                    val p = vertex(i, ratio)
                    if (i == 0) ringPath.moveTo(p.x, p.y) else ringPath.lineTo(p.x, p.y)
                }
                ringPath.close()
                drawPath(
                    path = ringPath,
                    color = gridColor.copy(alpha = if (ring == RING_COUNT) 0.85f else 0.35f),
                    style = Stroke(width = if (ring == RING_COUNT) 1.4.dp.toPx() else 0.8.dp.toPx())
                )
            }

            // 2. Nan hoa từ tâm ra từng tháng
            val dashed = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))
            for (i in 0 until count) {
                val isSelected = i == selectedIndex
                drawLine(
                    color = if (isSelected) webColor.copy(alpha = 0.9f) else axisColor.copy(alpha = 0.4f),
                    start = center,
                    end = vertex(i, 1f),
                    strokeWidth = if (isSelected) 1.6.dp.toPx() else 0.8.dp.toPx(),
                    pathEffect = if (isSelected) null else dashed
                )
            }

            // 3. Vùng điểm vận hạn (đa giác dữ liệu)
            val dataPath = Path()
            val points = ArrayList<Offset>(count)
            timeline.forEachIndexed { i, data ->
                val ratio = (data.normalizedScore / MAX_SCORE).coerceIn(0f, 1f) * growValue
                // Giữ tối thiểu một chút để đa giác không sụp hẳn về tâm khi điểm = 0
                val p = vertex(i, max(ratio, 0.02f * growValue))
                points += p
                if (i == 0) dataPath.moveTo(p.x, p.y) else dataPath.lineTo(p.x, p.y)
            }
            dataPath.close()

            drawPath(
                path = dataPath,
                brush = Brush.radialGradient(
                    colors = listOf(webColor.copy(alpha = 0.45f), webColor.copy(alpha = 0.08f)),
                    center = center,
                    radius = radius
                )
            )
            drawPath(
                path = dataPath,
                color = webColor,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // 4. Đỉnh từng tháng + highlight tháng đang chọn
            points.forEachIndexed { i, p ->
                val score = timeline[i].normalizedScore
                val isSelected = i == selectedIndex
                val dotColor = when {
                    isSelected -> webColor
                    score >= bestScore -> webColor.copy(alpha = 0.9f)
                    score <= worstScore -> worstColor
                    else -> webColor.copy(alpha = 0.7f)
                }
                if (isSelected) {
                    drawCircle(color = dotColor.copy(alpha = 0.22f), radius = 11.dp.toPx(), center = p)
                    drawLine(
                        color = dotColor.copy(alpha = 0.7f),
                        start = center,
                        end = p,
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
                drawCircle(color = dotColor, radius = if (isSelected) 5.5.dp.toPx() else 3.dp.toPx(), center = p)
                if (isSelected) {
                    drawCircle(color = labelSelectedColor, radius = 2.dp.toPx(), center = p)
                }
            }

            // 5. Nhãn tháng quanh vành ngoài (dùng layout đã đo sẵn)
            for (i in 0 until count) {
                val layout = if (i == selectedIndex) labelsSelected[i] else labels[i]
                val anchor = vertex(i, 1.13f)
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(
                        x = anchor.x - layout.size.width / 2f,
                        y = anchor.y - layout.size.height / 2f
                    )
                )
            }
        }
    }
}
