package com.example.tuvi.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.StrokeCap
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tuvi.R
import com.example.tuvi.ui.theme.TuViGold
import com.example.tuvi.ui.theme.TuViGoldDark
import com.example.tuvi.ui.theme.TuViIvory
import com.example.tuvi.ui.theme.TuViIvoryDim
import com.example.tuvi.ui.theme.TuViNavy
import com.example.tuvi.ui.theme.TuViNavyCard
import com.example.tuvi.ui.theme.TuViNavyLight
import kotlinx.coroutines.delay

private const val SPLASH_DURATION_MS = 1600L

// Sau thời lượng tối thiểu, đợi thêm tối đa ngần này cho quảng cáo splash load xong.
private const val MAX_AD_WAIT_MS = 4000L

/**
 * Màn splash: logo + tên app trên nền gradient navy/gold, fade + scale khi xuất hiện.
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .graphicsLayer { this.alpha = alpha }
                .scale(scale)
        ) {
            Box(
                modifier = Modifier
                    .size(124.dp)
                    .clip(CircleShape)
                    .background(TuViNavyCard)
                    .border(2.dp, TuViGold.copy(alpha = 0.55f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(112.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.app_name).uppercase(),
                color = TuViGold,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.splash_tagline),
                color = TuViIvoryDim,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .fillMaxWidth(0.55f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .graphicsLayer { this.alpha = alpha },
            color = TuViGold,
            trackColor = TuViGoldDark.copy(alpha = 0.25f),
            strokeCap = StrokeCap.Round
        )
    }
}
