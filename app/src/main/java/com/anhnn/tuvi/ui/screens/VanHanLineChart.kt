package com.anhnn.tuvi.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.anhnn.tuvi.data.remote.dto.ThangHanData
import kotlin.math.abs

@Composable
fun VanHanInteractiveLineChart(
    timeline: List<ThangHanData>,
    selectedIndex: Int,
    onMonthSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (timeline.isEmpty()) return

    val lineColor = Color(0xFF6200EE)
    val pointColor = Color(0xFFFFC107)
    val selectedPointColor = Color(0xFFFF5722) // Màu cam nổi bật cho tháng đang chọn

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(timeline) {
                    detectTapGestures { offset ->
                        val width = size.width
                        val stepX = width / (timeline.size - 1).coerceAtLeast(1)

                        // Tìm xem vị trí user chạm gần cột (index) nào nhất
                        var closestIndex = 0
                        var minDistance = Float.MAX_VALUE

                        timeline.forEachIndexed { index, _ ->
                            val x = index * stepX
                            val distance = abs(offset.x - x)
                            if (distance < minDistance) {
                                minDistance = distance
                                closestIndex = index
                            }
                        }
                        onMonthSelected(closestIndex)
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val stepX = width / (timeline.size - 1).coerceAtLeast(1)
            val maxY = 10f

            val path = Path()
            val points = mutableListOf<Offset>()

            timeline.forEachIndexed { index, data ->
                val x = index * stepX
                val y = height - (data.normalizedScore / maxY) * height
                points.add(Offset(x, y))

                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            // 1. Vẽ đường đồ thị sóng
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx())
            )

            // 2. Vẽ các điểm tròn tại từng tháng
            points.forEachIndexed { index, offset ->
                val isSelected = (index == selectedIndex)
                drawCircle(
                    color = if (isSelected) selectedPointColor else pointColor,
                    radius = if (isSelected) 8.dp.toPx() else 5.dp.toPx(),
                    center = offset
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = offset
                )
            }
        }
    }
}