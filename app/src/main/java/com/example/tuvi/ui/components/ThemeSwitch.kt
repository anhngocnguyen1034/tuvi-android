package com.example.tuvi.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ── Bảng màu hợp tông Tử Vi (navy + vàng kim) thay cho tông Gen-Z chói ──
private val DayTrackTop = Color(0xFFFFE9A8)   // bình minh nhạt
private val DayTrackBottom = Color(0xFFF2B43E) // vàng kim ấm
private val DaySun = Color(0xFFFFC23C)
private val DaySunCore = Color(0xFFFFD86B)

private val NightTrackTop = Color(0xFF24365E)  // navy sáng
private val NightTrackBottom = Color(0xFF0C152B) // navy thẳm
private val NightMoon = Color(0xFFF4ECD2)       // ngà
private val NightMoonShade = Color(0xFFCBBF97)

@Composable
fun GenZThemeSwitch(
    isDarkTheme: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 64.dp,
    height: Dp = 36.dp
) {
    val thumbOffsetAnim by animateDpAsState(
        targetValue = if (isDarkTheme) width - height else 0.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label = "thumb"
    )
    val trackTop by animateColorAsState(
        targetValue = if (isDarkTheme) NightTrackTop else DayTrackTop,
        animationSpec = tween(450), label = "trackTop"
    )
    val trackBottom by animateColorAsState(
        targetValue = if (isDarkTheme) NightTrackBottom else DayTrackBottom,
        animationSpec = tween(450), label = "trackBottom"
    )
    val thumbColorAnim by animateColorAsState(
        targetValue = if (isDarkTheme) NightMoon else DaySun,
        animationSpec = tween(450), label = "thumbColor"
    )
    val starAlphaAnim by animateFloatAsState(
        targetValue = if (isDarkTheme) 1f else 0f,
        animationSpec = tween(500), label = "star"
    )

    // Tia nắng xoay rất chậm cho mặt trời có sức sống.
    val sunSpin = rememberInfiniteTransition(label = "sunSpin")
    val sunRotation by sunSpin.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sunRotation"
    )
    // Sao lấp lánh nhẹ ở chế độ tối.
    val twinkle by sunSpin.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle"
    )

    val borderColor = if (isDarkTheme) Color(0xFF8A7B4F).copy(alpha = 0.55f)
    else Color(0xFFFFFFFF).copy(alpha = 0.6f)

    Canvas(
        modifier = modifier
            .size(width, height)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            )
    ) {
        val cornerRadius = size.height / 2

        // Track nền gradient (chiều dọc → cảm giác chiều sâu bầu trời).
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(trackTop, trackBottom)),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            style = Fill
        )
        // Highlight bóng mờ phía trên cho track bóng bẩy.
        drawRoundRect(
            brush = Brush.verticalGradient(
                0f to Color.White.copy(alpha = 0.18f),
                0.5f to Color.Transparent
            ),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            style = Fill
        )
        drawRoundRect(
            color = borderColor,
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            style = Stroke(width = 1.dp.toPx())
        )

        // Sao nền ở chế độ tối.
        if (starAlphaAnim > 0f) {
            val starColor = Color.White.copy(alpha = starAlphaAnim)
            drawSparkle(Offset(size.width * 0.22f, size.height * 0.30f), 3.2.dp.toPx(), starColor.copy(alpha = starAlphaAnim * twinkle))
            drawSparkle(Offset(size.width * 0.40f, size.height * 0.62f), 2.dp.toPx(), starColor.copy(alpha = starAlphaAnim * (1.5f - twinkle)))
            drawSparkle(Offset(size.width * 0.30f, size.height * 0.78f), 1.6.dp.toPx(), starColor.copy(alpha = starAlphaAnim * twinkle))
            drawCircle(starColor.copy(alpha = starAlphaAnim * 0.8f), 0.8.dp.toPx(), Offset(size.width * 0.13f, size.height * 0.62f))
        }

        val thumbX = thumbOffsetAnim.toPx() + (size.height / 2)
        val thumbCenter = Offset(thumbX, size.height / 2)
        val thumbRadius = (size.height / 2) - 4.dp.toPx()

        // Hào quang mềm quanh thumb.
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(thumbColorAnim.copy(alpha = 0.45f), Color.Transparent),
                center = thumbCenter,
                radius = thumbRadius * 2.1f
            ),
            radius = thumbRadius * 2.1f,
            center = thumbCenter
        )
        // Bóng đổ nhẹ dưới thumb cho cảm giác nổi khối.
        drawCircle(
            color = Color.Black.copy(alpha = 0.18f),
            radius = thumbRadius,
            center = thumbCenter.copy(y = thumbCenter.y + 1.2.dp.toPx())
        )

        if (isDarkTheme) {
            drawCrescentMoon(thumbCenter, thumbRadius, thumbColorAnim, NightMoonShade)
        } else {
            drawSun(
                center = thumbCenter,
                coreRadius = thumbRadius * 0.62f,
                rayLength = thumbRadius * 0.4f,
                rayWidth = 2.2.dp.toPx(),
                rayGap = 2.dp.toPx(),
                color = thumbColorAnim,
                coreColor = DaySunCore,
                rotationDegrees = sunRotation
            )
        }
    }
}

private fun DrawScope.drawCrescentMoon(
    center: Offset,
    radius: Float,
    color: Color,
    shadeColor: Color
) {
    val path = Path().apply {
        addOval(Rect(center, radius))
        val cutPath = Path().apply {
            addOval(
                Rect(
                    center = Offset(center.x + radius * 0.35f, center.y - radius * 0.1f),
                    radius = radius
                )
            )
        }
        op(this, cutPath, PathOperation.Difference)
    }
    drawPath(path, color)
    // Vài "miệng hố" mờ cho mặt trăng có chiều sâu.
    drawCircle(shadeColor.copy(alpha = 0.5f), radius * 0.16f, Offset(center.x - radius * 0.30f, center.y - radius * 0.18f))
    drawCircle(shadeColor.copy(alpha = 0.4f), radius * 0.11f, Offset(center.x - radius * 0.10f, center.y + radius * 0.30f))
}

private fun DrawScope.drawSun(
    center: Offset,
    coreRadius: Float,
    rayLength: Float,
    rayWidth: Float,
    rayGap: Float,
    color: Color,
    coreColor: Color,
    numRays: Int = 8,
    rotationDegrees: Float
) {
    rotate(rotationDegrees, center) {
        val step = 360f / numRays
        repeat(numRays) { i ->
            rotate(i * step, center) {
                drawLine(
                    color = color,
                    start = Offset(center.x, center.y - (coreRadius + rayGap)),
                    end = Offset(center.x, center.y - (coreRadius + rayGap + rayLength)),
                    strokeWidth = rayWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }
    // Lõi mặt trời với gradient ấm.
    drawCircle(color, coreRadius, center)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(coreColor, color),
            center = center.copy(x = center.x - coreRadius * 0.25f, y = center.y - coreRadius * 0.25f),
            radius = coreRadius * 1.3f
        ),
        radius = coreRadius,
        center = center
    )
}

private fun DrawScope.drawSparkle(center: Offset, size: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y - size)
        quadraticTo(center.x, center.y, center.x + size, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + size)
        quadraticTo(center.x, center.y, center.x - size, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - size)
    }
    drawPath(path, color)
}
