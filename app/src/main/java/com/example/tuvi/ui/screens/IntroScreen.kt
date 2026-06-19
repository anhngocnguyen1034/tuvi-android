package com.example.tuvi.ui.screens

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tuvi.R
import com.example.tuvi.ui.theme.TuViGold
import com.example.tuvi.ui.theme.TuViIvory
import com.example.tuvi.ui.theme.TuViIvoryDim
import com.example.tuvi.ui.theme.TuViNavy
import com.example.tuvi.ui.theme.TuViNavyLight
import com.example.tuvi.ui.theme.TuViTheme
import kotlinx.coroutines.launch

/** Một trang giới thiệu: chỗ ảnh để trống (chưa có asset) + tiêu đề + mô tả. */
private data class IntroPage(
    @StringRes val title: Int,
    @StringRes val desc: Int,
)

private val INTRO_PAGES = listOf(
    IntroPage(R.string.intro_1_title, R.string.intro_1_desc),
    IntroPage(R.string.intro_2_title, R.string.intro_2_desc),
    IntroPage(R.string.intro_3_title, R.string.intro_3_desc),
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
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                IntroPageContent(INTRO_PAGES[page])
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
                    .padding(bottom = 36.dp)
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
    }
}

@Composable
private fun IntroPageContent(page: IntroPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IntroImageSlot()

        Spacer(Modifier.height(40.dp))

        Text(
            text = stringResource(page.title),
            color = TuViGold,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = stringResource(page.desc),
            color = TuViIvoryDim,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center
        )
    }
}

/** Chỗ đặt ảnh minh hoạ — hiện để TRỐNG (chưa có asset). Đặt Image() vào đây khi có ảnh. */
@Composable
private fun IntroImageSlot() {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(TuViIvory.copy(alpha = 0.04f))
            .border(1.dp, TuViGold.copy(alpha = 0.25f), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Placeholder rỗng: 1 sao mờ cho đỡ trống, thay bằng Image khi có asset.
        Text(text = "✦", color = TuViGold.copy(alpha = 0.30f), fontSize = 48.sp)
    }
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
