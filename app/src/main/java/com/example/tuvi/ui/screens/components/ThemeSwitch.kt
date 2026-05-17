package com.example.tuvi.ui.screens.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
    val trackColorAnim by animateColorAsState(
        targetValue = if (isDarkTheme) Color(0xFF2D2D2D) else Color(0xFF00E5FF),
        label = "track"
    )
    val thumbColorAnim by animateColorAsState(
        targetValue = if (isDarkTheme) Color(0xFFDDDDDD) else Color(0xFFFFE500),
        label = "thumbColor"
    )
    val starAlphaAnim by animateFloatAsState(
        targetValue = if (isDarkTheme) 1f else 0f,
        label = "star"
    )

    val borderColor = if (isDarkTheme) Color(0xFF444444) else Color(0xFFE0E0E0)

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

        drawRoundRect(
            color = trackColorAnim,
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            style = Fill
        )
        drawRoundRect(
            color = borderColor,
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            style = Stroke(width = 1.dp.toPx())
        )

        if (starAlphaAnim > 0f) {
            val starColor = Color.White.copy(alpha = starAlphaAnim)
            drawSparkle(Offset(size.width * 0.25f, size.height * 0.35f), 3.dp.toPx(), starColor)
            drawSparkle(Offset(size.width * 0.5f, size.height * 0.7f), 2.dp.toPx(), starColor)
        }

        val thumbX = thumbOffsetAnim.toPx() + (size.height / 2)
        val thumbCenter = Offset(thumbX, size.height / 2)
        val thumbRadius = (size.height / 2) - 4.dp.toPx()

        if (isDarkTheme) {
            drawCrescentMoon(thumbCenter, thumbRadius, thumbColorAnim)
        } else {
            drawSun(
                center = thumbCenter,
                coreRadius = thumbRadius * 0.6f,
                rayLength = thumbRadius * 0.35f,
                rayWidth = 2.dp.toPx(),
                rayGap = 2.dp.toPx(),
                color = thumbColorAnim,
                rotationDegrees = 0f
            )
        }
    }
}

private fun DrawScope.drawCrescentMoon(center: Offset, radius: Float, color: Color) {
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
}

private fun DrawScope.drawSun(
    center: Offset,
    coreRadius: Float,
    rayLength: Float,
    rayWidth: Float,
    rayGap: Float,
    color: Color,
    numRays: Int = 8,
    rotationDegrees: Float
) {
    rotate(rotationDegrees, center) {
        drawCircle(color, coreRadius, center)
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
