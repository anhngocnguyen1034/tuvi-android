package com.example.tuvi.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.Image
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anhnn.ads.BannerAd
import com.example.tuvi.R
import com.example.tuvi.ads.AdNames
import com.example.tuvi.ui.theme.TuViGold
import com.example.tuvi.ui.theme.TuViIvoryDim
import com.example.tuvi.ui.theme.TuViNavy
import com.example.tuvi.ui.theme.TuViNavyLight
import com.example.tuvi.ui.theme.TuViTheme
import kotlinx.coroutines.launch

/** Ảnh giới thiệu full-bleed cho mỗi trang (tiêu đề + mô tả đã nằm sẵn trong ảnh). */
@DrawableRes
private val INTRO_PAGES = intArrayOf(
    R.drawable.intro1,
    R.drawable.intro2,
    R.drawable.intro3,
)

/**
 * Màn giới thiệu 3 phần, chỉ hiện ở lần mở app đầu tiên. Vuốt ngang qua từng phần;
 * "Bỏ qua" / "Bắt đầu" đều gọi [onFinish] (caller lưu cờ đã-xem rồi vào Home).
 *
 * Chỗ ảnh hiện để TRỐNG (placeholder bo góc) — khi có asset chỉ cần đặt Image vào [IntroImageSlot].
 */
@Composable
fun IntroScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { INTRO_PAGES.size })
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == INTRO_PAGES.lastIndex

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(TuViNavyLight, TuViNavy)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        IntroPageContent(INTRO_PAGES[page])
                    }

                    // Làm mờ dần đáy ảnh vào nền để liền mạch với phần điều khiển phía dưới
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, TuViNavy)
                                )
                            )
                    )
                }

                // Chỉ báo trang
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(INTRO_PAGES.size) { index ->
                        val selected = pagerState.currentPage == index
                        val width by animateDpAsState(if (selected) 22.dp else 8.dp, label = "dotWidth")
                        val color by animateColorAsState(
                            if (selected) TuViGold else TuViIvoryDim.copy(alpha = 0.4f),
                            label = "dotColor"
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                Button(
                    onClick = {
                        if (isLast) onFinish()
                        else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TuViGold,
                        contentColor = TuViNavy
                    )
                ) {
                    Text(
                        text = stringResource(if (isLast) R.string.intro_start else R.string.intro_next),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            BannerAd(adName = AdNames.INTRO_BANNER, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

/** Ảnh full-width, căn mép trên; phần dư phía dưới bị cắt (crop). */
@Composable
private fun IntroPageContent(@DrawableRes image: Int) {
    Image(
        painter = painterResource(image),
        contentDescription = null,
        contentScale = ContentScale.FillWidth,
        alignment = Alignment.TopCenter,
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
    )
}

@Preview(name = "Intro – Dark", showBackground = true)
@Composable
private fun IntroScreenDarkPreview() {
    TuViTheme(darkTheme = true) { IntroScreen(onFinish = {}) }
}

@Preview(name = "Intro – Light", showBackground = true)
@Composable
private fun IntroScreenLightPreview() {
    TuViTheme(darkTheme = false) { IntroScreen(onFinish = {}) }
}
