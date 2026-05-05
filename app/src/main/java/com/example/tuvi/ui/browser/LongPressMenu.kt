package com.example.tuvi.ui.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tuvi.R
import com.example.tuvi.presentation.LongPressTarget
import com.example.tuvi.ui.theme.TuViGold
import com.example.tuvi.ui.theme.TuViIvory
import com.example.tuvi.ui.theme.TuViNavy
import com.example.tuvi.ui.theme.TuViNavyCard

@Composable
fun LongPressMenu(
    target: LongPressTarget,
    onOpenInCurrentTab: (String) -> Unit,
    onOpenInNewTab: (String) -> Unit,
    onDownload: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val screenW = with(density) { config.screenWidthDp.dp.toPx() }
    val menuW = with(density) { 220.dp.toPx() }

    val clampedX = target.x.coerceIn(8f, screenW - menuW - 8f)
    // Show menu above touch point; if near top, show below
    val aboveY = target.y - with(density) { 160.dp.toPx() }
    val clampedY = if (aboveY < 80f) target.y + with(density) { 8.dp.toPx() } else aboveY

    Box(modifier = Modifier.fillMaxSize()) {
        // Dismiss overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onDismiss
                )
        )

        Column(
            modifier = Modifier
                .offset { androidx.compose.ui.unit.IntOffset(clampedX.toInt(), clampedY.toInt()) }
                .shadow(8.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(TuViNavyCard)
                .width(220.dp)
        ) {
            // URL preview
            Text(
                text = target.url,
                color = TuViGold,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TuViNavy)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            )
            HorizontalDivider(color = TuViNavy)
            when (target) {
                is LongPressTarget.Link -> {
                    MenuRow(stringResource(R.string.browser_ctx_open_link)) {
                        onOpenInCurrentTab(target.url); onDismiss()
                    }
                    HorizontalDivider(color = TuViNavy)
                    MenuRow(stringResource(R.string.browser_ctx_open_new_tab)) {
                        onOpenInNewTab(target.url); onDismiss()
                    }
                }
                is LongPressTarget.Image -> {
                    MenuRow(stringResource(R.string.browser_ctx_download_image)) {
                        onDownload(target.url); onDismiss()
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuRow(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        color = TuViIvory,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp)
    )
}
