package com.example.tuvi.ui.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.example.tuvi.R
import com.example.tuvi.presentation.LongPressTarget
import com.example.tuvi.ui.theme.TuViGold
import com.example.tuvi.ui.theme.TuViIvory
import com.example.tuvi.ui.theme.TuViIvoryDim
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
    Popup(
        offset = IntOffset(target.x.toInt(), target.y.toInt()),
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .shadow(8.dp, RoundedCornerShape(12.dp))
                .background(TuViNavyCard, RoundedCornerShape(12.dp))
                .width(220.dp)
        ) {
            Text(
                text = target.url,
                color = TuViIvoryDim,
                fontSize = 10.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
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
