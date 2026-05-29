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
import androidx.compose.ui.graphics.painter.Painter
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
import com.example.tuvi.ui.theme.LoraFontFamily
import com.example.tuvi.ui.theme.TuViRed

@Composable
fun HomeScreen(
    onOpenTuVi: () -> Unit,
    onOpenBrowser: () -> Unit = {},
    onOpenCalendar: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
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
            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(onClick = onOpenSettings) {
                    Icon(
                        painter = painterResource(R.drawable.ic_settings),
                        contentDescription = stringResource(R.string.content_desc_settings),
                        tint = TuViGold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Bát Quái xoay ──
            BaguaDecoration(
                modifier = Modifier
                    .size(180.dp)
                    .rotate(rotation)
            )

            Spacer(Modifier.height(20.dp))

            // ── Tên app ──
            Text(
                text = stringResource(R.string.home_app_title),
                color = TuViGold,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = LoraFontFamily,
            )
            Spacer(Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Box(Modifier.weight(1f).height(1.dp).background(TuViGoldDark.copy(alpha = 0.5f)))
                Text(
                    "  —  ",
                    color = TuViGold.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
                Box(Modifier.weight(1f).height(1.dp).background(TuViGoldDark.copy(alpha = 0.5f)))
            }

            Spacer(Modifier.height(6.dp))

            MainFeatureCard(
                icon = painterResource(R.drawable.property_1_linear),
                title = stringResource(R.string.home_tuvi_title),
                description = stringResource(R.string.home_tuvi_desc),
                onClick = onOpenTuVi
            )

            Spacer(Modifier.height(16.dp))

            SecondaryFeatureCard(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.home_browser_title),
                description = stringResource(R.string.home_browser_desc),
                onClick = onOpenBrowser,
                icon = painterResource(R.drawable.ic_browser),
                iconTint = Color(0xFF2DD4BF)
            )

            Spacer(Modifier.height(14.dp))

            SecondaryFeatureCard(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.home_calendar_title),
                description = stringResource(R.string.home_calendar_desc),
                onClick = onOpenCalendar,
                icon  = painterResource(R.drawable.ic_calendar)
            )
        }

    }
}

@Composable
private fun MainFeatureCard(
    icon: Painter,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = TuViGold.copy(alpha = 0.4f),
                ambientColor = TuViGold.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        HomeCardGradientStart.copy(alpha = 0.85f),
                        HomeCardGradientMid.copy(alpha = 0.75f),
                        HomeCardGradientEnd.copy(alpha = 0.85f)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    listOf(
                        TuViGold.copy(alpha = 0.8f),
                        TuViGoldDark.copy(alpha = 0.3f),
                        TuViGold.copy(alpha = 0.6f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
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
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = TuViGold,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TuViGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = LoraFontFamily,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = description,
                    color = TuViIvoryDim,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
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
    onClick: () -> Unit,
    icon: Painter? = null,
    iconTint: Color = TuViGold,
) {
    val borderColor = if (enabled)
        Brush.linearGradient(listOf(TuViGold.copy(alpha = 0.6f), TuViGoldDark.copy(alpha = 0.25f), TuViGold.copy(alpha = 0.5f)))
    else
        Brush.linearGradient(listOf(TuViDivider.copy(alpha = 0.3f), TuViDivider.copy(alpha = 0.15f)))
    val bgStart = if (enabled) TuViNavyLight.copy(alpha = 0.82f) else TuViNavy.copy(alpha = 0.7f)
    val bgEnd = if (enabled) TuViNavyCard.copy(alpha = 0.75f) else TuViNavy.copy(alpha = 0.65f)
    val textColor = if (enabled) TuViIvory else TuViIvoryDim.copy(alpha = 0.45f)
    val subColor = if (enabled) TuViIvoryDim else TuViIvoryDim.copy(alpha = 0.3f)

    Box(
        modifier = modifier
            .shadow(
                elevation = if (enabled) 8.dp else 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = if (enabled) TuViGold.copy(alpha = 0.25f) else Color.Transparent
            )
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(bgStart, bgEnd)))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = description,
                    color = subColor,
                    fontSize = 12.sp
                )
            }
        }
    }
}
