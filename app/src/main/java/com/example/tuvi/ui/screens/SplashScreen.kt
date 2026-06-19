package com.example.tuvi.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tuvi.R
import com.example.tuvi.ui.theme.TuViGold
import com.example.tuvi.ui.theme.TuViGoldDark
import com.example.tuvi.ui.theme.TuViGoldLight
import com.example.tuvi.ui.theme.TuViIvory
import com.example.tuvi.ui.theme.TuViIvoryDim
import com.example.tuvi.ui.theme.TuViNavy
import com.example.tuvi.ui.theme.TuViNavyLight
import com.example.tuvi.ui.theme.TuViTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay

private const val SPLASH_DURATION_MS = 1600L

// Sau thời lượng tối thiểu, đợi thêm tối đa ngần này cho quảng cáo splash load xong.
private const val MAX_AD_WAIT_MS = 4000L

private const val TWO_PI = (2 * PI).toFloat()

/** Một ngôi sao lấp lánh trên nền splash (tọa độ tỉ lệ 0..1, phase để lệch nhịp nhấp nháy). */
private data class TwinkleStar(val x: Float, val y: Float, val radius: Float, val phase: Float)

/**
 * Màn splash: logo + tên app trên nền gradient navy/gold, fade + scale khi xuất hiện.
 * Trang trí thêm bầu trời sao nhấp nháy, vầng sáng vàng thở quanh logo và 2 vòng "địa bàn"
 * 12 Cung quay ngược chiều nhau quanh logo.
 * Hiển thị tối thiểu [SPLASH_DURATION_MS], sau đó đợi quảng cáo sẵn sàng ([isAdReady], tối đa
 * [MAX_AD_WAIT_MS]) rồi mới gọi [onFinish] để vào Home — tránh việc splash xong mà ad chưa kịp load.
 */
@Composable
fun SplashScreen(
    onFinish: () -> Unit,
    isAdReady: () -> Boolean = { true },
) {
    var started by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "splashAlpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (started) 1f else 0.8f,
        animationSpec = tween(durationMillis = 700),
        label = "splashScale"
    )
    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = SPLASH_DURATION_MS.toInt(), easing = LinearEasing),
        label = "splashProgress"
    )

    val infinite = rememberInfiniteTransition(label = "splashFx")
    // Vòng địa bàn quay chậm 1 vòng / 20s; vòng trong quay ngược chiều (nhân hệ số âm khi vẽ).
    val ringAngle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 20_000, easing = LinearEasing)),
        label = "ringAngle"
    )
    // Vầng sáng vàng quanh logo "thở" nhẹ nhàng.
    val glowPulse by infinite.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1_600), RepeatMode.Reverse),
        label = "glowPulse"
    )
    // Pha chung cho hiệu ứng sao nhấp nháy; mỗi sao lệch pha riêng.
    val twinklePhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = TWO_PI,
        animationSpec = infiniteRepeatable(tween(durationMillis = 3_200, easing = LinearEasing)),
        label = "twinklePhase"
    )

    val stars = remember {
        val rnd = Random(seed = 2024)
        List(64) {
            TwinkleStar(
                x = rnd.nextFloat(),
                y = rnd.nextFloat(),
                radius = 0.6f + rnd.nextFloat() * 1.3f,
                phase = rnd.nextFloat() * TWO_PI
            )
        }
    }

    LaunchedEffect(Unit) {
        started = true
        delay(SPLASH_DURATION_MS)
        // Đợi quảng cáo splash load xong (tối đa MAX_AD_WAIT_MS) rồi mới vào Home.
        var waited = 0L
        while (!isAdReady() && waited < MAX_AD_WAIT_MS) {
            delay(100)
            waited += 100
        }
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(TuViNavyLight, TuViNavy))
            ),
        contentAlignment = Alignment.Center
    ) {
        // Bầu trời sao nhấp nháy phủ toàn màn, fade in cùng nội dung.
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { this.alpha = alpha }
        ) {
            stars.forEach { star ->
                val twinkle = (sin(twinklePhase + star.phase) + 1f) / 2f
                drawCircle(
                    color = TuViIvory,
                    radius = star.radius.dp.toPx(),
                    center = Offset(star.x * size.width, star.y * size.height),
                    alpha = 0.10f + 0.55f * twinkle
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .graphicsLayer { this.alpha = alpha }
                .scale(scale)
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Vầng sáng vàng tỏa sau logo, độ sáng "thở" theo glowPulse.
                Box(
                    modifier = Modifier
                        .size(216.dp)
                        .graphicsLayer { this.alpha = glowPulse }
                        .background(
                            Brush.radialGradient(
                                listOf(TuViGold.copy(alpha = 0.30f), Color.Transparent)
                            ),
                            CircleShape
                        )
                )

                // Vòng địa bàn ngoài: nét đứt + 12 chấm tượng trưng 12 Cung, quay chậm.
                Canvas(
                    modifier = Modifier
                        .size(180.dp)
                        .graphicsLayer { rotationZ = ringAngle }
                ) {
                    val ringRadius = size.minDimension / 2f - 2.dp.toPx()
                    drawCircle(
                        color = TuViGold.copy(alpha = 0.35f),
                        radius = ringRadius,
                        style = Stroke(
                            width = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(
                                floatArrayOf(4.dp.toPx(), 7.dp.toPx())
                            )
                        )
                    )
                    repeat(12) { i ->
                        val angle = i * 30f * (PI / 180f).toFloat()
                        drawCircle(
                            color = TuViGoldLight.copy(alpha = 0.85f),
                            radius = 1.8.dp.toPx(),
                            center = Offset(
                                center.x + ringRadius * cos(angle),
                                center.y + ringRadius * sin(angle)
                            )
                        )
                    }
                }

                // Vòng trong mảnh hơn, quay ngược chiều nhanh hơn một chút.
                Canvas(
                    modifier = Modifier
                        .size(150.dp)
                        .graphicsLayer { rotationZ = -ringAngle * 1.5f }
                ) {
                    drawCircle(
                        color = TuViGold.copy(alpha = 0.20f),
                        radius = size.minDimension / 2f - 1.dp.toPx(),
                        style = Stroke(
                            width = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(
                                floatArrayOf(2.dp.toPx(), 9.dp.toPx())
                            )
                        )
                    )
                }

                // Logo app đầy đủ (bản playstore), bo tròn + viền vàng.
                Image(
                    painter = painterResource(R.drawable.splash_logo),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(124.dp)
                        .clip(CircleShape)
                        .border(2.dp, TuViGold.copy(alpha = 0.55f), CircleShape)
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = stringResource(R.string.app_name).uppercase(),
                style = TextStyle(
                    brush = Brush.linearGradient(listOf(TuViGoldLight, TuViGold, TuViGoldLight)),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(Modifier.height(12.dp))

            // Hoa văn ngăn cách: gạch mờ dần — sao ✦ — gạch mờ dần.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, TuViGold.copy(alpha = 0.6f))
                            )
                        )
                )
                Text(
                    text = "✦",
                    color = TuViGold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(TuViGold.copy(alpha = 0.6f), Color.Transparent)
                            )
                        )
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.splash_tagline),
                color = TuViIvoryDim,
                fontSize = 13.sp,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .fillMaxWidth(0.5f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .graphicsLayer { this.alpha = alpha },
            color = TuViGold,
            trackColor = TuViGoldDark.copy(alpha = 0.25f),
            strokeCap = StrokeCap.Butt
        )
    }
}

@Preview(name = "Splash – Dark", showBackground = true)
@Composable
private fun SplashScreenDarkPreview() {
    TuViTheme(darkTheme = true) {
        SplashScreen(onFinish = {})
    }
}

@Preview(name = "Splash – Light", showBackground = true)
@Composable
private fun SplashScreenLightPreview() {
    TuViTheme(darkTheme = false) {
        SplashScreen(onFinish = {})
    }
}
