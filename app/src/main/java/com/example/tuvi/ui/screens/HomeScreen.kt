package com.example.tuvi.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tuvi.presentation.screens.BaguaDecoration
import com.example.tuvi.ui.theme.HomeBgGradientBottom
import com.example.tuvi.ui.theme.HomeBgGradientTop
import com.example.tuvi.ui.theme.HomeCardGradientEnd
import com.example.tuvi.ui.theme.HomeCardGradientMid
import com.example.tuvi.ui.theme.HomeCardGradientStart
import com.example.tuvi.ui.theme.TuViDivider
import com.example.tuvi.ui.theme.TuViGold
import com.example.tuvi.ui.theme.TuViGoldDark
import com.example.tuvi.ui.theme.TuViGoldLight
import com.example.tuvi.ui.theme.TuViIvory
import com.example.tuvi.ui.theme.TuViIvoryDim
import com.example.tuvi.ui.theme.TuViNavy
import com.example.tuvi.ui.theme.TuViNavyCard
import com.example.tuvi.ui.theme.TuViNavyLight
import com.example.tuvi.R
import com.example.tuvi.ui.theme.TuViRed

@Composable
fun HomeScreen(
    onOpenTuVi: () -> Unit,
    onOpenSaved: () -> Unit,
    onOpenBrowser: () -> Unit = {},
    onOpenCalendar: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val homeBg = remember {
        Brush.verticalGradient(listOf(HomeBgGradientTop, TuViNavy, HomeBgGradientBottom))
    }
    val infiniteTransition = rememberInfiniteTransition(label = "bagua_rotate")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 40_000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bagua_rotation"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2_500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(homeBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 8.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_settings),
                contentDescription = stringResource(R.string.content_desc_settings),
                tint = TuViGold
            )
        }

        // Ánh sáng nền phía sau Bát Quái
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.TopCenter)
                .padding(top = 60.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            TuViGold.copy(alpha = glowAlpha * 0.18f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))

            // ── Bát Quái xoay ──
            BaguaDecoration(
                modifier = Modifier
                    .size(180.dp)
                    .rotate(rotation)
            )

            Spacer(Modifier.height(20.dp))

            // ── Tên app ──
            Text(
                text = "TỬ VI",
                color = TuViGold,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 10.sp
            )
            Text(
                text = "BY ANHNN",
                color = TuViGoldLight.copy(alpha = 0.7f),
                fontSize = 13.sp,
                letterSpacing = 6.sp,
                fontWeight = FontWeight.Light
            )

            Spacer(Modifier.height(8.dp))

            // Divider trang trí
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Box(Modifier.weight(1f).height(1.dp).background(TuViGoldDark.copy(alpha = 0.5f)))
                Text(
                    "  ✦  ",
                    color = TuViGold.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
                Box(Modifier.weight(1f).height(1.dp).background(TuViGoldDark.copy(alpha = 0.5f)))
            }

            Spacer(Modifier.height(6.dp))

            Spacer(Modifier.height(40.dp))

            // ── Card chính: Lá số Tử Vi ──
            MainFeatureCard(
                emoji = "☯",
                title = "Lá Số Tử Vi",
                description = "Xem lá số cá nhân theo ngày sinh",
                onClick = onOpenTuVi
            )

            Spacer(Modifier.height(16.dp))

            // ── Card phụ ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SecondaryFeatureCard(
                    modifier = Modifier.weight(1f),
                    title = "Lá Số\nĐã Lưu",
                    description = "Xem lại & quản lý",
                    onClick = onOpenSaved
                )
                SecondaryFeatureCard(
                    modifier = Modifier.weight(1f),
                    title = "Trình\nDuyệt",
                    description = "Duyệt web & tài liệu",
                    onClick = onOpenBrowser
                )
            }

            Spacer(Modifier.height(14.dp))

            SecondaryFeatureCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Lịch Âm / Dương",
                description = "Xem nhanh lịch âm lịch và dương lịch",
                onClick = onOpenCalendar
            )
        }
    }
}

@Composable
private fun MainFeatureCard(
    emoji: String,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(HomeCardGradientStart, HomeCardGradientMid, HomeCardGradientEnd)
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    listOf(TuViGold, TuViGoldDark, TuViGold.copy(alpha = 0.4f))
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Biểu tượng
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(TuViGold.copy(alpha = 0.2f), TuViNavy.copy(alpha = 0.8f))
                        )
                    )
                    .border(1.dp, TuViGold.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 30.sp)
            }

            Spacer(Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TuViGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = description,
                    color = TuViIvoryDim,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(TuViGold)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Xem ngay",
                        color = TuViNavy,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SecondaryFeatureCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val borderColor = if (enabled) TuViGoldDark else TuViDivider.copy(alpha = 0.4f)
    val bgBrush = if (enabled)
        Brush.verticalGradient(listOf(TuViNavyLight, TuViNavyCard))
    else
        Brush.verticalGradient(listOf(TuViNavy.copy(alpha = 0.8f), TuViNavy))
    val textColor = if (enabled) TuViIvory else TuViIvoryDim.copy(alpha = 0.45f)
    val subColor = if (enabled) TuViGold.copy(alpha = 0.8f) else TuViIvoryDim.copy(alpha = 0.3f)

    Box(
        modifier = modifier
            .shadow(elevation = if (enabled) 6.dp else 2.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(bgBrush)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier)
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                color = subColor,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
