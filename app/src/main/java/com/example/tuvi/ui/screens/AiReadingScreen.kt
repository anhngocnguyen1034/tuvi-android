package com.example.tuvi.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tuvi.R
import com.example.tuvi.domain.model.CungSlug
import com.example.tuvi.presentation.screens.AiReadingSection
import com.example.tuvi.ui.theme.BeVietnamProFamily
import com.example.tuvi.ui.theme.ChartDeepBg
import com.example.tuvi.ui.theme.ChartGold
import com.example.tuvi.ui.theme.ChartGoldDim
import com.example.tuvi.ui.theme.ChartIvory
import com.example.tuvi.ui.theme.ChartIvoryDim
import com.example.tuvi.ui.theme.ChartNavy
import com.example.tuvi.ui.theme.TuViTheme

/**
 * Màn riêng để xem luận giải lá số bằng AI cho TỪNG CUNG.
 *
 * - Người dùng chọn 1 cung trong 12 cung → bấm "Luận giải cung X".
 * - Reading được cache theo cung trong [aiReadings]; chuyển cung khác mà đã có cache → hiển thị ngay,
 *   chưa có → hiển thị nút yêu cầu.
 */
@Composable
fun AiReadingScreen(
    selectedCung: CungSlug?,
    aiReadings: Map<CungSlug, String>,
    loading: Boolean,
    onSelectCung: (CungSlug) -> Unit,
    onRequest: () -> Unit,
    onBack: () -> Unit,
) {
    val currentReading = selectedCung?.let(aiReadings::get)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ChartNavy, ChartDeepBg, ChartDeepBg))),
    ) {
        TopBar(onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        ) {
            CungPicker(
                selected = selectedCung,
                aiReadings = aiReadings,
                onSelect = onSelectCung,
            )

            ActionPanel(
                selectedCung = selectedCung,
                hasReadingForSelected = currentReading != null,
                loading = loading,
                onRequest = onRequest,
            )

            if (currentReading != null) {
                Spacer(Modifier.height(20.dp))
                val bodyText = currentReading.takeIf { it.isNotBlank() }
                    ?: stringResource(R.string.chart_ai_reading_empty)
                AiReadingSection(bodyText = bodyText)
            } else if (!loading) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.ai_reading_hint_single_cung),
                    color = ChartIvoryDim,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = BeVietnamProFamily,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                )
            }
        }
    }
}

@Composable
private fun TopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 4.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
    ) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.settings_back),
                tint = ChartGold,
            )
        }
        Text(
            text = stringResource(R.string.chart_ai_reading_title),
            color = ChartGold,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CungPicker(
    selected: CungSlug?,
    aiReadings: Map<CungSlug, String>,
    onSelect: (CungSlug) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.ai_reading_cung_picker_label),
            color = ChartIvory,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = BeVietnamProFamily,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            CungSlug.entries.forEach { cung ->
                val cached = aiReadings.containsKey(cung)
                val label = if (cached) "${cung.displayName} ✓" else cung.displayName
                FilterChip(
                    selected = cung == selected,
                    onClick = { onSelect(cung) },
                    label = {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontFamily = BeVietnamProFamily,
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = ChartNavy.copy(alpha = 0.35f),
                        labelColor = ChartIvory,
                        selectedContainerColor = ChartGold.copy(alpha = 0.85f),
                        selectedLabelColor = ChartDeepBg,
                    ),
                )
            }
        }
    }
}

@Composable
private fun ActionPanel(
    selectedCung: CungSlug?,
    hasReadingForSelected: Boolean,
    loading: Boolean,
    onRequest: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val cungName = selectedCung?.displayName
        val btnText = when {
            cungName == null -> stringResource(R.string.chart_ai_request_btn)
            hasReadingForSelected -> stringResource(R.string.ai_reading_refresh_btn_for_cung, cungName)
            else -> stringResource(R.string.ai_reading_request_btn_for_cung, cungName)
        }
        Button(
            onClick = onRequest,
            enabled = !loading && selectedCung != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ChartNavy.copy(alpha = 0.45f),
                contentColor = ChartGold,
                disabledContainerColor = ChartNavy.copy(alpha = 0.25f),
                disabledContentColor = ChartGoldDim.copy(alpha = 0.45f),
            ),
            border = BorderStroke(1.dp, ChartGold.copy(alpha = 0.75f)),
        ) {
            Text(
                text = btnText,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                fontFamily = BeVietnamProFamily,
            )
        }
        if (loading) {
            Text(
                text = cungName?.let { stringResource(R.string.ai_reading_loading_cung, it) }
                    ?: stringResource(R.string.chart_loading_ai),
                color = ChartIvoryDim,
                fontSize = 12.sp,
            )
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = ChartGold,
                trackColor = ChartGoldDim.copy(alpha = 0.35f),
            )
        }
    }
}

@Preview(name = "AI reading empty - dark", showBackground = true)
@Composable
private fun AiReadingScreenEmptyPreview() {
    TuViTheme(darkTheme = true) {
        AiReadingScreen(
            selectedCung = null,
            aiReadings = emptyMap(),
            loading = false,
            onSelectCung = {},
            onRequest = {},
            onBack = {},
        )
    }
}

@Preview(name = "AI reading loading - dark", showBackground = true)
@Composable
private fun AiReadingScreenLoadingPreview() {
    TuViTheme(darkTheme = true) {
        AiReadingScreen(
            selectedCung = CungSlug.MENH,
            aiReadings = emptyMap(),
            loading = true,
            onSelectCung = {},
            onRequest = {},
            onBack = {},
        )
    }
}

@Preview(name = "AI reading content - dark", showBackground = true)
@Composable
private fun AiReadingScreenContentPreview() {
    TuViTheme(darkTheme = true) {
        AiReadingScreen(
            selectedCung = CungSlug.MENH,
            aiReadings = mapOf(
                CungSlug.MENH to "Mẫu luận giải: cung Mệnh vượng, đại vận hiện tại thuận lợi cho sự nghiệp.",
            ),
            loading = false,
            onSelectCung = {},
            onRequest = {},
            onBack = {},
        )
    }
}
