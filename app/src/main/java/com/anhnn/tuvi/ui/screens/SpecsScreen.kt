package com.anhnn.tuvi.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import com.anhnn.tuvi.data.remote.dto.ScoreBreakdown
import com.anhnn.tuvi.data.remote.dto.ThangHanData
import com.anhnn.tuvi.data.remote.dto.VongHanSummary
import com.anhnn.tuvi.domain.model.TuViChartInput
import com.anhnn.tuvi.ui.components.TuViTopBar
import com.anhnn.tuvi.ui.specs.SpecsViewModel
import com.anhnn.tuvi.ui.theme.ChartBorderGold
import com.anhnn.tuvi.ui.theme.ChartCardBg
import com.anhnn.tuvi.ui.theme.ChartDeepBg
import com.anhnn.tuvi.ui.theme.ChartGold
import com.anhnn.tuvi.ui.theme.ChartGoldDim
import com.anhnn.tuvi.ui.theme.ChartIvory
import com.anhnn.tuvi.ui.theme.ChartIvoryDim
import com.anhnn.tuvi.ui.theme.ChartNavy
import com.anhnn.tuvi.ui.theme.ChartRed
import com.anhnn.tuvi.ui.theme.HanhKim
import com.anhnn.tuvi.ui.theme.HanhMoc
import com.anhnn.tuvi.ui.theme.TuViTheme

@Composable
fun SpecsScreen(
    viewModel: SpecsViewModel,
    input: TuViChartInput,
    onBack: () -> Boolean
) {
    SpecsContent(
        namXem = input.namXem,
        timeline = viewModel.uiState?.timeline.orEmpty(),
        summary = viewModel.uiState?.summary,
        isLoading = viewModel.isLoading,
        errorMessage = viewModel.errorMessage,
        onBack = { onBack() }
    )
}

@Composable
private fun SpecsContent(
    namXem: Int,
    timeline: List<ThangHanData>,
    summary: VongHanSummary?,
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit
) {
    // Index tháng đang chọn — giữ qua rotation / process death
    var selectedMonthIndex by rememberSaveable { mutableIntStateOf(0) }
    // Dữ liệu mới có thể ngắn hơn: kéo lựa chọn về trong khoảng hợp lệ
    LaunchedEffect(timeline.size) {
        if (timeline.isNotEmpty() && selectedMonthIndex > timeline.lastIndex) {
            selectedMonthIndex = 0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(ChartDeepBg, ChartNavy))
            )
    ) {
        TuViTopBar(title = "Thông số lá số $namXem", onBack = onBack)

        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = ChartGold)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Đang luận vận hạn 12 tháng…",
                        color = ChartIvoryDim,
                        fontSize = 13.sp
                    )
                }
            }

            errorMessage != null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Không lấy được dữ liệu vận hạn.\n$errorMessage",
                    color = ChartRed,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }

            timeline.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Chưa có dữ liệu vận hạn.", color = ChartIvoryDim, fontSize = 14.sp)
            }

            else -> {
                val selected = timeline[selectedMonthIndex.coerceIn(0, timeline.lastIndex)]

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    YearOverviewRow(timeline = timeline, summary = summary)

                    RadarCard(
                        timeline = timeline,
                        selectedIndex = selectedMonthIndex,
                        onMonthSelected = { selectedMonthIndex = it }
                    )

                    MonthStrip(
                        timeline = timeline,
                        selectedIndex = selectedMonthIndex,
                        onMonthSelected = { selectedMonthIndex = it }
                    )

                    MonthDetailCard(data = selected)

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

/** Ba ô tổng quan: điểm trung bình năm, tháng đẹp nhất, tháng cần cẩn trọng. */
@Composable
private fun YearOverviewRow(
    timeline: List<ThangHanData>,
    summary: VongHanSummary?
) {
    val avg = remember(timeline) {
        if (timeline.isEmpty()) 0f else timeline.map { it.normalizedScore }.average().toFloat()
    }
    val best = summary?.bestMonth ?: timeline.maxByOrNull { it.normalizedScore }?.thang
    val worst = summary?.worstMonth ?: timeline.minByOrNull { it.normalizedScore }?.thang

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatTile(
                modifier = Modifier.weight(1f),
                label = "Điểm TB năm",
                value = fmt1(avg),
                accent = scoreColorOf(avg)
            )
            StatTile(
                modifier = Modifier.weight(1f),
                label = "Tháng rực rỡ",
                value = best?.let { "T$it" } ?: "—",
                accent = HanhMoc
            )
            StatTile(
                modifier = Modifier.weight(1f),
                label = "Tháng cẩn trọng",
                value = worst?.let { "T$it" } ?: "—",
                accent = ChartRed
            )
        }

        // Biên độ điểm thô của năm — cho biết vận hạn năm nay dao động mạnh hay êm
        val minRaw = summary?.minRaw
        val maxRaw = summary?.maxRaw
        if (minRaw != null && maxRaw != null) {
            Text(
                text = "Biên độ điểm thô năm: ${fmt1(minRaw)} → ${fmt1(maxRaw)}",
                color = ChartIvoryDim,
                fontSize = 11.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(ChartCardBg)
            .border(1.dp, ChartBorderGold.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, color = accent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            color = ChartIvoryDim,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

/** Khung chứa biểu đồ màng nhện + chú giải thang điểm. */
@Composable
private fun RadarCard(
    timeline: List<ThangHanData>,
    selectedIndex: Int,
    onMonthSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ChartCardBg)
            .border(1.dp, ChartBorderGold.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Vòng vận hạn 12 tháng",
            color = ChartGold,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "Chạm vào một trục hoặc dải tháng bên dưới để xem chi tiết",
            color = ChartIvoryDim,
            fontSize = 11.sp
        )
        Spacer(Modifier.height(12.dp))

        VanHanRadarChart(
            timeline = timeline,
            selectedIndex = selectedIndex,
            onMonthSelected = onMonthSelected,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendDot(color = ChartGold, label = "Đỉnh cao")
            Spacer(Modifier.width(16.dp))
            LegendDot(color = ChartRed, label = "Đáy thấp")
            Spacer(Modifier.width(16.dp))
            Text(text = "Vòng ngoài = 10đ", color = ChartIvoryDim, fontSize = 11.sp)
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(5.dp))
        Text(text = label, color = ChartIvoryDim, fontSize = 11.sp)
    }
}

/** Định dạng 1 chữ số thập phân, luôn dùng dấu "." bất kể locale máy. */
private fun fmt1(value: Float): String = String.format(Locale.US, "%.1f", value)

/** Màu theo thang điểm 0..10 — dùng chung cho badge, dải tháng và nhãn. */
private fun scoreColorOf(score: Float): Color = when {
    score >= 7f -> HanhMoc
    score >= 4f -> ChartGold
    else -> ChartRed
}

/** Nhãn định tính cho điểm tháng. */
private fun scoreLabelOf(score: Float): String = when {
    score >= 8f -> "Đại cát"
    score >= 7f -> "Hanh thông"
    score >= 5.5f -> "Bình hoà"
    score >= 4f -> "Cần giữ thế"
    else -> "Cẩn trọng"
}

/**
 * Dải 12 tháng cuộn ngang — cách chọn tháng chính xác hơn so với chạm vào trục
 * radar, và cho phép so sánh điểm các tháng cạnh nhau bằng mắt.
 */
@Composable
private fun MonthStrip(
    timeline: List<ThangHanData>,
    selectedIndex: Int,
    onMonthSelected: (Int) -> Unit
) {
    val listState = rememberLazyListState()
    // Tự cuộn tới tháng đang chọn khi lựa chọn đổi từ biểu đồ radar
    LaunchedEffect(selectedIndex) {
        listState.animateScrollToItem(index = selectedIndex.coerceAtLeast(0))
    }

    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = timeline, key = { it.thang }) { data ->
            val index = data.thang - 1
            val isSelected = index == selectedIndex
            val accent = scoreColorOf(data.normalizedScore)
            MonthChip(
                month = data.thang,
                score = data.normalizedScore,
                accent = accent,
                isSelected = isSelected,
                onClick = { onMonthSelected(index) }
            )
        }
    }
}

@Composable
private fun MonthChip(
    month: Int,
    score: Float,
    accent: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg by animateColorAsState(
        targetValue = if (isSelected) accent.copy(alpha = 0.22f) else ChartCardBg,
        animationSpec = tween(220),
        label = "chip-bg-$month"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) accent else ChartBorderGold.copy(alpha = 0.5f),
        animationSpec = tween(220),
        label = "chip-border-$month"
    )

    Column(
        modifier = Modifier
            .widthIn(min = 56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "T$month",
            color = if (isSelected) ChartIvory else ChartIvoryDim,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = fmt1(score),
            color = accent,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Card bóc tách điểm của tháng đang chọn. */
@Composable
private fun MonthDetailCard(data: ThangHanData) {
    val score = data.normalizedScore
    val scoreColor by animateColorAsState(
        targetValue = scoreColorOf(score),
        animationSpec = tween(300),
        label = "score-color"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ChartCardBg)
            .border(1.dp, ChartBorderGold.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = data.thangTen,
                    color = ChartIvory,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${data.cungTen} · ${data.cungChu}",
                    color = ChartGoldDim,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = scoreLabelOf(score),
                    color = scoreColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(scoreColor.copy(alpha = 0.14f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
            ScoreBadge(score = score, color = scoreColor)
        }

        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(ChartBorderGold.copy(alpha = 0.5f))
        )
        Spacer(Modifier.height(14.dp))

        Text(
            text = "Bóc tách điểm",
            color = ChartGold,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(10.dp))

        val bd = data.breakdown
        val rows = listOf(
            Triple("Chính tinh", bd.chinhTinh, HanhKim),
            Triple("Cát tinh", bd.catTinh, HanhMoc),
            Triple("Sát tinh", bd.satTinh, ChartRed),
            Triple("Sao lưu", bd.saoLuu, ChartGold)
        )
        // Chuẩn hoá độ dài thanh theo giá trị tuyệt đối lớn nhất trong tháng
        val maxAbs = remember(bd) {
            maxOf(
                kotlin.math.abs(bd.chinhTinh),
                kotlin.math.abs(bd.catTinh),
                kotlin.math.abs(bd.satTinh),
                kotlin.math.abs(bd.saoLuu)
            ).coerceAtLeast(1f)
        }
        rows.forEach { (label, value, color) ->
            BreakdownBar(label = label, value = value, maxAbs = maxAbs, color = color)
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(4.dp))
        Text(
            text = "Hệ số cung: ${String.format(Locale.US, "%.2f", bd.heSo)}  ·  Điểm thô: ${fmt1(data.rawScore)}",
            color = ChartIvoryDim,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun ScoreBadge(score: Float, color: Color) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = fmt1(score),
            color = color,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(text = "/ 10", color = ChartIvoryDim, fontSize = 10.sp)
    }
}

/** Một dòng bóc tách: nhãn — thanh tỉ lệ (âm sang trái, dương sang phải) — giá trị. */
@Composable
private fun BreakdownBar(
    label: String,
    value: Float,
    maxAbs: Float,
    color: Color
) {
    val fraction by animateFloatAsState(
        targetValue = (kotlin.math.abs(value) / maxAbs).coerceIn(0f, 1f),
        animationSpec = tween(400),
        label = "breakdown-$label"
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            color = ChartIvoryDim,
            fontSize = 12.sp,
            modifier = Modifier.width(80.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(ChartBorderGold.copy(alpha = 0.25f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
        Text(
            text = String.format(Locale.US, if (value < 0f) "%.1f" else "+%.1f", value),
            color = if (value < 0f) ChartRed else ChartIvory,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(48.dp)
        )
    }
}

// ---------------------------------------------------------------- Previews

private fun previewTimeline(): List<ThangHanData> = List(12) { i ->
    val score = listOf(6.2f, 7.8f, 4.1f, 8.9f, 5.5f, 3.2f, 6.8f, 9.1f, 4.7f, 7.2f, 5.9f, 6.5f)[i]
    ThangHanData(
        thang = i + 1,
        thangTen = "Tháng ${i + 1}",
        cungSo = i,
        cungTen = "Cung Tý",
        cungChu = "Mệnh",
        rawScore = score * 3.4f,
        normalizedScore = score,
        breakdown = ScoreBreakdown(
            chinhTinh = 12.5f,
            catTinh = 8.0f,
            satTinh = -6.5f,
            saoLuu = 3.5f,
            heSo = 1.15f
        )
    )
}

@Preview(name = "Specs - Light", showBackground = true, heightDp = 1000)
@Composable
private fun SpecsScreenPreviewLight() {
    TuViTheme(darkTheme = false) {
        SpecsContent(
            namXem = 2026,
            timeline = previewTimeline(),
            summary = VongHanSummary(
                minRaw = 10.9f,
                maxRaw = 30.9f,
                bestMonth = 8,
                worstMonth = 6,
                avgRaw = 21.4f
            ),
            isLoading = false,
            errorMessage = null,
            onBack = {}
        )
    }
}

@Preview(name = "Specs - Dark", showBackground = true, heightDp = 1000)
@Composable
private fun SpecsScreenPreviewDark() {
    TuViTheme(darkTheme = true) {
        SpecsContent(
            namXem = 2026,
            timeline = previewTimeline(),
            summary = VongHanSummary(
                minRaw = 10.9f,
                maxRaw = 30.9f,
                bestMonth = 8,
                worstMonth = 6,
                avgRaw = 21.4f
            ),
            isLoading = false,
            errorMessage = null,
            onBack = {}
        )
    }
}
