package com.example.tuvi.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tuvi.R
import com.example.tuvi.ui.components.TuViTopBar
import com.example.tuvi.ui.theme.TuViGold
import com.example.tuvi.ui.theme.TuViGoldDark
import com.example.tuvi.ui.theme.TuViNavy
import com.example.tuvi.ui.theme.TuViNavyCard
import com.example.tuvi.util.QrShareBackground
import com.example.tuvi.util.composeQrShareImage
import com.example.tuvi.util.loadQrShareBackgrounds
import com.example.tuvi.util.shareBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ShareQrScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    val storeUrl = remember(context) {
        "https://play.google.com/store/apps/details?id=${context.packageName}"
    }
    val backgrounds = remember(context) { loadQrShareBackgrounds(context) }
    var selectedId by remember { mutableStateOf(backgrounds.firstOrNull()?.id) }
    val selected = remember(selectedId, backgrounds) {
        backgrounds.firstOrNull { it.id == selectedId } ?: backgrounds.firstOrNull()
    }

    // Ảnh preview độ phân giải cao, dựng ngoài main thread.
    val preview by produceState<ImageBitmap?>(null, selected, storeUrl) {
        val bg = selected
        value = if (bg == null) null else withContext(Dispatchers.Default) {
            composeQrShareImage(context, bg, storeUrl, sizePx = 1080).asImageBitmap()
        }
    }

    // Thumbnail nhỏ cho từng mẫu.
    val thumbnails by produceState<Map<String, ImageBitmap>>(emptyMap(), backgrounds, storeUrl) {
        value = withContext(Dispatchers.Default) {
            backgrounds.associate { bg ->
                bg.id to composeQrShareImage(context, bg, storeUrl, sizePx = 240).asImageBitmap()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TuViNavy)
    ) {
        TuViTopBar(
            title = stringResource(R.string.share_qr_title),
            onBack = onBack,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(TuViNavyCard),
                contentAlignment = Alignment.Center
            ) {
                val p = preview
                if (p != null) {
                    Image(
                        bitmap = p,
                        contentDescription = stringResource(R.string.share_qr_title),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    CircularProgressIndicator(color = TuViGold)
                }
            }

            Text(
                text = stringResource(R.string.share_qr_pick_template),
                color = TuViGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
                modifier = Modifier.fillMaxWidth()
            )

            // Hàng chọn mẫu
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(backgrounds, key = { it.id }) { bg ->
                    TemplateThumb(
                        bitmap = thumbnails[bg.id],
                        selected = bg.id == selectedId,
                        onClick = { selectedId = bg.id }
                    )
                }
            }
        }

        // Nút chia sẻ ảnh
        Button(
            onClick = {
                val bg = selected ?: return@Button
                val bmp = composeQrShareImage(context, bg, storeUrl, sizePx = 1080)
                val caption = context.getString(R.string.qr_share_message, storeUrl)
                shareBitmap(context, bmp, caption)
            },
            enabled = preview != null,
            colors = ButtonDefaults.buttonColors(
                containerColor = TuViGold,
                contentColor = TuViNavy
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
                .height(52.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_qrcode),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = stringResource(R.string.share_qr_share_button),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun TemplateThumb(
    bitmap: ImageBitmap?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(84.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(TuViNavyCard)
            .border(
                width = if (selected) 2.5.dp else 1.dp,
                color = if (selected) TuViGold else TuViGoldDark.copy(alpha = 0.25f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
