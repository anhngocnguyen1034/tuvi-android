package com.example.tuvi.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
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

@Composable
fun AiReadingScreen(
    selectedCung: CungSlug?,
    aiReadings: Map<CungSlug, String>,
    loading: Boolean,
    aiUsed: Boolean,
    vanHanReading: String?,
    hoiReading: String?,
    remaining: Int?,
    onSelectCung: (CungSlug) -> Unit,
    onRequest: () -> Unit,
    onRequestVanHan: () -> Unit,
    onAskQuestion: (String) -> Unit,
    onBuyCredits: () -> Unit,
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
            QuotaHeader(remaining = remaining, onBuyCredits = onBuyCredits)

            CungPicker(
                selected = selectedCung,
                aiReadings = aiReadings,
                onSelect = onSelectCung,
            )

            ActionPanel(
                selectedCung = selectedCung,
                hasReadingForSelected = currentReading != null,
                loading = loading,
                aiUsed = aiUsed,
                onRequest = onRequest,
            )

            VanHanPanel(
                hasReading = vanHanReading != null,
                loading = loading,
                aiUsed = aiUsed,
                onRequest = onRequestVanHan,
            )

            HoiPanel(
                hasReading = hoiReading != null,
                loading = loading,
                aiUsed = aiUsed,
                onAsk = onAskQuestion,
            )

            // 1 lượt free dùng chung cho cung / vận hạn / câu hỏi → chỉ một loại có kết quả.
            val readingToShow = currentReading ?: vanHanReading ?: hoiReading
            if (readingToShow != null) {
                Spacer(Modifier.height(20.dp))
                val bodyText = readingToShow.takeIf { it.isNotBlank() }
                    ?: stringResource(R.string.chart_ai_reading_empty)
                AiReadingSection(bodyText = bodyText)
            } else if (!loading) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = if (aiUsed) stringResource(R.string.ai_already_used_hint)
                    else stringResource(R.string.ai_reading_hint_single_cung),
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
                painter = painterResource(R.drawable.ic_back),
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
    aiUsed: Boolean,
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
            aiUsed -> stringResource(R.string.ai_reading_used_btn)
            cungName == null -> stringResource(R.string.chart_ai_request_btn_free)
            hasReadingForSelected -> stringResource(
                R.string.ai_reading_refresh_btn_for_cung_free, cungName
            )
            else -> stringResource(R.string.ai_reading_request_btn_for_cung_free, cungName)
        }
        Button(
            onClick = onRequest,
            enabled = !loading && selectedCung != null && !aiUsed,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp),
            shape = RoundedCornerShape(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp,
                vertical = 10.dp,
            ),
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
                maxLines = 2,
                softWrap = true,
                textAlign = TextAlign.Center,
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

@Composable
private fun VanHanPanel(
    hasReading: Boolean,
    loading: Boolean,
    aiUsed: Boolean,
    onRequest: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val btnText = when {
            aiUsed -> stringResource(R.string.ai_reading_used_btn)
            hasReading -> stringResource(R.string.ai_reading_van_han_refresh_btn)
            else -> stringResource(R.string.ai_reading_van_han_btn)
        }
        Button(
            onClick = onRequest,
            enabled = !loading && !aiUsed,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp),
            shape = RoundedCornerShape(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp,
                vertical = 10.dp,
            ),
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
                maxLines = 2,
                softWrap = true,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Hàng đầu màn AI: số lượt còn lại + nút nạp thêm (mở màn cửa hàng). */
@Composable
private fun QuotaHeader(remaining: Int?, onBuyCredits: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (remaining != null)
                stringResource(R.string.ai_reading_remaining, remaining)
            else "",
            color = ChartIvory,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = BeVietnamProFamily,
            modifier = Modifier.weight(1f),
        )
        Button(
            onClick = onBuyCredits,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ChartNavy.copy(alpha = 0.5f),
                contentColor = ChartGold,
            ),
            border = BorderStroke(1.dp, ChartGold.copy(alpha = 0.75f)),
        ) {
            Text(
                text = stringResource(R.string.ai_reading_buy_btn),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                fontFamily = BeVietnamProFamily,
            )
        }
    }
}

/**
 * Ô hỏi – đáp tự do: người dùng nhập câu hỏi rồi gửi sang /api/interpret/hoi.
 * Chung 1 lượt AI miễn phí với cung & vận hạn → disable khi [aiUsed] hoặc [loading].
 */
@Composable
private fun HoiPanel(
    hasReading: Boolean,
    loading: Boolean,
    aiUsed: Boolean,
    onAsk: (String) -> Unit,
) {
    var question by rememberSaveable { mutableStateOf("") }
    val enabled = !loading && !aiUsed && !hasReading

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.ai_reading_hoi_hint),
            color = ChartIvoryDim,
            fontSize = 12.sp,
            fontFamily = BeVietnamProFamily,
        )

        OutlinedTextField(
            value = question,
            onValueChange = { question = it },
            enabled = enabled,
            placeholder = {
                Text(
                    text = stringResource(R.string.ai_reading_hoi_placeholder),
                    color = ChartIvoryDim,
                    fontSize = 14.sp,
                    fontFamily = BeVietnamProFamily,
                )
            },
            textStyle = androidx.compose.ui.text.TextStyle(
                color = ChartIvory,
                fontSize = 14.sp,
                fontFamily = BeVietnamProFamily,
            ),
            minLines = 2,
            maxLines = 5,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ChartGold.copy(alpha = 0.75f),
                unfocusedBorderColor = ChartGold.copy(alpha = 0.35f),
                disabledBorderColor = ChartGoldDim.copy(alpha = 0.25f),
                cursorColor = ChartGold,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = { onAsk(question.trim()) },
            enabled = enabled && question.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp),
            shape = RoundedCornerShape(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp,
                vertical = 10.dp,
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = ChartNavy.copy(alpha = 0.45f),
                contentColor = ChartGold,
                disabledContainerColor = ChartNavy.copy(alpha = 0.25f),
                disabledContentColor = ChartGoldDim.copy(alpha = 0.45f),
            ),
            border = BorderStroke(1.dp, ChartGold.copy(alpha = 0.75f)),
        ) {
            Text(
                text = stringResource(R.string.ai_reading_hoi_btn),
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                fontFamily = BeVietnamProFamily,
                maxLines = 2,
                softWrap = true,
                textAlign = TextAlign.Center,
            )
        }
    }
}

