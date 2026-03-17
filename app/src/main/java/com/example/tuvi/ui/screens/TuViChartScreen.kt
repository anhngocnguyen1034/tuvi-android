package com.example.tuvi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tuvi.model.Cung
import com.example.tuvi.model.Sao
import com.example.tuvi.model.ThienBan
import com.example.tuvi.model.TuViResponse

// ─── Bảng màu Tử Vi (khớp với InputScreen) ───────────────────────────────────
private val ChartDeepBg    = Color(0xFF0F0510)
private val ChartNavy      = Color(0xFF12082A)
private val ChartCardBg    = Color(0xFF1C0D30)
private val ChartGold      = Color(0xFFD4AF37)
private val ChartGoldDim   = Color(0xFF8B7020)
private val ChartIvory     = Color(0xFFF5E6C8)
private val ChartIvoryDim  = Color(0xFFBBA080)
private val ChartRed       = Color(0xFF8B0000)
private val ChartBorderGold= Color(0xFF5C3D0A)

// Hung tinh → đỏ sẫm, Cát tinh → vàng gold, mặc định → kem nhạt
fun getSaoColor(tenSao: String): Color = when {
    tenSao.contains("Kình", ignoreCase = true) || tenSao.contains("Đà", ignoreCase = true)  ||
    tenSao.contains("Hoả", ignoreCase = true)  || tenSao.contains("Linh", ignoreCase = true) ||
    tenSao.contains("Không", ignoreCase = true)|| tenSao.contains("Kiếp", ignoreCase = true) ||
    tenSao.contains("Kị", ignoreCase = true)   -> Color(0xFFFF4444)

    tenSao.contains("Khoa", ignoreCase = true) || tenSao.contains("Quyền", ignoreCase = true)||
    tenSao.contains("Lộc", ignoreCase = true)  || tenSao.contains("Tử Vi", ignoreCase = true)||
    tenSao.contains("Thiên Phủ", ignoreCase = true) -> ChartGold

    else -> ChartIvory
}

// ─── Màn hình chính ───────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuViChartScreen(
    data  : TuViResponse,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = ChartDeepBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "LÁ SỐ TỬ VI",
                            color      = ChartGold,
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 3.sp
                        )
                        Text(
                            data.thien_ban.ten,
                            color    = ChartIvoryDim,
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = ChartGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ChartNavy
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(ChartNavy, ChartDeepBg, ChartDeepBg))
                )
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Lưới 4×4 lá số ──
            ChartGrid(data)

            Spacer(Modifier.height(16.dp))

            // ── Footer ──
            Text(
                "✦ Tử Vi By AnhNN ✦",
                color     = ChartGoldDim,
                fontSize  = 11.sp,
                textAlign = TextAlign.Center,
                fontStyle = FontStyle.Italic,
                modifier  = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )
        }
    }
}

// ─── Lưới 4×4 cung ───────────────────────────────────────────────────────────
@Composable
fun ChartGrid(data: TuViResponse) {
    // index cung → (row, col) trong lưới 4x4
    val gridMapping = mapOf(
        0  to (3 to 2), 1  to (3 to 1), 2  to (3 to 0), 3  to (2 to 0),
        4  to (1 to 0), 5  to (0 to 0), 6  to (0 to 1), 7  to (0 to 2),
        8  to (0 to 3), 9  to (1 to 3), 10 to (2 to 3), 11 to (3 to 3)
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .aspectRatio(0.62f) // Tỷ lệ nhỏ để ô cung cao dọc hơn, nhiều không gian chứa sao
            .shadow(8.dp, RoundedCornerShape(4.dp))
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(listOf(ChartGold, ChartGoldDim, ChartGold)),
                shape = RoundedCornerShape(4.dp)
            )
            .clip(RoundedCornerShape(4.dp))
            .background(ChartCardBg)
    ) {
        val cellW = maxWidth  / 4
        val cellH = maxHeight / 4

        // 12 cung xung quanh
        data.dia_ban.forEachIndexed { index, cung ->
            val (row, col) = gridMapping[index] ?: (0 to 0)
            Box(
                modifier = Modifier
                    .offset(x = cellW * col, y = cellH * row)
                    .size(width = cellW, height = cellH)
                    .border(0.5.dp, ChartGoldDim.copy(alpha = 0.5f))
            ) {
                PalaceView(cung)
            }
        }

        // Thiên bàn trung tâm (2×2 ô giữa)
        Box(
            modifier = Modifier
                .offset(x = cellW, y = cellH)
                .size(width = cellW * 2, height = cellH * 2)
                .border(
                    width = 0.8.dp,
                    brush = Brush.linearGradient(listOf(ChartGold, ChartGoldDim, ChartGold)),
                    shape = androidx.compose.ui.graphics.RectangleShape
                )
                .background(ChartNavy.copy(alpha = 0.85f))
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            ThienBanCenterContent(data.thien_ban)
        }

        // Vẽ Tuần/Triệt ở ranh giới 2 cung
        DrawTuanTriet(data.dia_ban, gridMapping, cellW, cellH)
    }
}

// ─── Vẽ Tuần / Triệt gộp ─────────────────────────────────────────────────────
@Composable
private fun DrawTuanTriet(
    diaBan: List<Cung>,
    mapping: Map<Int, Pair<Int, Int>>,
    cellW: androidx.compose.ui.unit.Dp,
    cellH: androidx.compose.ui.unit.Dp
) {
    // Thu thập các cặp cung liền nhau cùng có Tuần / Triệt
    val tuanPairs = mutableListOf<Pair<Int, Int>>()
    val trietPairs = mutableListOf<Pair<Int, Int>>()

    for (i in 0..11) {
        val next = if (i == 11) 0 else i + 1
        if (diaBan[i].tuan && diaBan[next].tuan) tuanPairs.add(i to next)
        if (diaBan[i].triet && diaBan[next].triet) trietPairs.add(i to next)
    }

    // Hàm phụ để tính toán vị trí trung bình giữa 2 ô
    @Composable
    fun SharedLabel(label: String, pair: Pair<Int, Int>, yOfft: Float) {
        val (i1, i2) = pair
        val pos1 = mapping[i1] ?: return
        val pos2 = mapping[i2] ?: return

        // Tâm của ô 1
        val cx1 = pos1.second + 0.5f
        val cy1 = pos1.first + 0.5f
        // Tâm của ô 2
        val cx2 = pos2.second + 0.5f
        val cy2 = pos2.first + 0.5f

        // Vị trí gộp chung (trung bình cộng của tọa độ lưới)
        val midX = (cx1 + cx2) / 2f
        val midY = (cy1 + cy2) / 2f

        // Vẽ nhãn tại vị trí trung bình
        Box(
            modifier = Modifier
                .offset(
                    x = cellW * midX - 12.dp,       // Căn giữa ngang xấp xỉ
                    y = cellH * midY - 6.dp + yOfft.dp // yOfft để tách Tuần, Triệt nếu trùng
                )
                .background(
                    color = if (label == "Tuần") ChartRed else Color(0xFF2E1B6B),
                    shape = RoundedCornerShape(3.dp)
                )
                .border(0.5.dp, ChartIvoryDim.copy(alpha = 0.6f), RoundedCornerShape(3.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                label,
                color = ChartIvory,
                fontSize = 6.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }

    // Vẽ
    tuanPairs.forEach { p ->
        // Nếu cặp này vừa có Tuần vừa Triệt -> Đặt song song
        val hasTrietToo = trietPairs.contains(p) || trietPairs.contains(p.second to p.first)
        SharedLabel("Tuần", p, if (hasTrietToo) -8f else 0f)
    }

    trietPairs.forEach { p ->
        val hasTuanToo = tuanPairs.contains(p) || tuanPairs.contains(p.second to p.first)
        SharedLabel("Triệt", p, if (hasTuanToo) 8f else 0f)
    }
}

// ─── Thiên Bàn trung tâm ─────────────────────────────────────────────────────
@Composable
private fun ThienBanCenterContent(tb: ThienBan) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(2.dp)
    ) {
        Text(
            "✦ LÁ SỐ TỬ VI ✦",
            fontWeight    = FontWeight.Bold,
            fontSize      = 8.sp,
            color         = ChartGold,
            letterSpacing = 1.sp,
            textAlign     = TextAlign.Center
        )
        Spacer(Modifier.height(2.dp))

        CenterLine("",        tb.ten,       valueColor = ChartGold, valueBold = true)
        CenterLine("",        tb.gioi_tinh)
        CenterLine("Dương",   tb.ngay_duong)
        CenterLine("Âm",      tb.ngay_am)
        tb.ngayAmLichTen?.let { CenterLine("", it, fontSize = 7.sp) }
        tb.gioSinh?.let       { CenterLine("Giờ", it) }

        if (tb.canNam != null || tb.chiNam != null)
            CenterLine("Năm", "${tb.canNam ?: ""} ${tb.chiNam ?: ""}".trim())
        if (tb.canThang != null || tb.chiThang != null)
            CenterLine("Tháng", "${tb.canThang ?: ""} ${tb.chiThang ?: ""}".trim())
        if (tb.canNgay != null || tb.chiNgay != null)
            CenterLine("Ngày", "${tb.canNgay ?: ""} ${tb.chiNgay ?: ""}".trim())

        tb.amDuongNamSinh?.let { CenterLine("Âm/Dương", it) }
        tb.amDuongMenh?.let    { CenterLine("", it, fontSize = 7.sp) }
        tb.menh?.let           { CenterLine("Mệnh", it, valueColor = ChartGold) }
        tb.banMenh?.let        { CenterLine("Bản Mệnh", it) }
        tb.cuc?.let            { CenterLine("Cục", it) }
        tb.menhChu?.let        { CenterLine("M.Chủ", it) }
        tb.thanChu?.let        { CenterLine("Th.Chủ", it) }
        tb.sinhKhac?.let       { CenterLine("Sinh/Khắc", it) }
    }
}

@Composable
private fun CenterLine(
    label     : String,
    value     : String,
    fontSize  : TextUnit  = 8.sp,
    valueColor: Color     = ChartIvory,
    valueBold : Boolean   = false
) {
    if (value.isBlank()) return
    Text(
        text       = if (label.isBlank()) value else "$label: $value",
        fontSize   = fontSize,
        color      = valueColor,
        fontWeight = if (valueBold) FontWeight.Bold else FontWeight.Normal,
        maxLines   = 1,
        overflow   = TextOverflow.Ellipsis,
        textAlign  = TextAlign.Center,
        lineHeight = fontSize * 1.3f
    )
}

// ─── Ô cung ───────────────────────────────────────────────────────────────────
@Composable
fun PalaceView(cung: Cung) {
    val chinhTinhNames = listOf(
        "Tử Vi", "Thiên Cơ", "Thái Dương", "Vũ Khúc", "Thiên Đồng", "Liêm Trinh",
        "Thiên Phủ", "Thái Âm", "Tham Lang", "Cự Môn",
        "Thiên Tướng", "Thiên Lương", "Thất Sát", "Phá Quân"
    )
    val (chinhTinhs, phuTinhs) = cung.sao.partition { sao ->
        chinhTinhNames.any { sao.ten.contains(it, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                // Cung có chính tinh → nền viền đỏ nhẹ/transparent
                if (chinhTinhs.isNotEmpty())
                    Brush.verticalGradient(listOf(ChartRed.copy(alpha = 0.15f), Color.Transparent))
                else
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
            )
            .padding(horizontal = 2.dp, vertical = 2.dp)
    ) {
        // ── Hàng 1: Can Chi | Tên Cung | Đại Hạn ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Căn trái (Để trống cho Cung Tên chiếm ưu thế hoặc sau này thêm info)
            Spacer(modifier = Modifier.weight(1f))
            // Tên Cung (Giữa)
            Text(
                cung.cungTen.uppercase(),
                fontSize   = 6.5.sp,
                fontWeight = FontWeight.Bold,
                color      = ChartGold,
                maxLines   = 1,
                overflow   = TextOverflow.Clip,
                modifier   = Modifier.weight(1.5f),
                textAlign  = TextAlign.Center
            )
            // Đại Hạn (Căn phải)
            Text(
                cung.daiHan?.toString() ?: "",
                fontSize = 5.5.sp,
                color    = ChartIvoryDim,
                maxLines = 1,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }

        // ── Hàng 2: Chính Tinh (căn giữa, in đậm, to hơn) ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 1.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            chinhTinhs.forEach { sao ->
                Text(
                    text       = "${sao.ten}${if (!sao.dac_tinh.isNullOrEmpty()) " (${sao.dac_tinh})" else ""}",
                    fontSize   = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    color      = getSaoColor(sao.ten),
                    lineHeight = 9.sp,
                    textAlign  = TextAlign.Center
                )
            }
        }

        // ── Hàng 3: Phụ Tinh (2 cột, chiếm phần còn lại) ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 1.dp)
        ) {
            val mid        = (phuTinhs.size + 1) / 2
            val leftStars  = phuTinhs.take(mid)
            val rightStars = phuTinhs.drop(mid)
            Column(Modifier.weight(1f)) { leftStars.forEach  { StarText(it) } }
            Column(Modifier.weight(1f)) { rightStars.forEach { StarText(it) } }
        }

        // ── Hàng 4: Cung Chủ | — | Tháng ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.Bottom
        ) {
            // Địa chi (Căn trái dưới)
            Text(
                cung.hanhCung.take(3),
                fontSize = 5.5.sp,
                color    = ChartIvoryDim,
                maxLines = 1,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )
            // Để trống giữa
            Spacer(modifier = Modifier.weight(0.5f))
            // Cung Chủ (Căn phải dưới)
            Text(
                cung.cungChu,
                fontSize = 5.sp,
                color = ChartGoldDim,
                maxLines = 1,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }
    }
}

// ─── Nhãn Tuần / Triệt ───────────────────────────────────────────────────────
@Composable
fun BoxScope.TuKhoiView(text: String, alignment: Alignment) {
    Box(
        modifier = Modifier
            .align(alignment)
            .background(
                color = if (text == "Tuần") ChartRed else Color(0xFF2E1B6B),
                shape = RoundedCornerShape(2.dp)
            )
            .padding(horizontal = 3.dp, vertical = 1.dp)
    ) {
        Text(
            text,
            color      = ChartIvory,
            fontSize   = 5.5.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ─── Sao phụ ─────────────────────────────────────────────────────────────────
@Composable
fun StarText(sao: Sao) {
    Text(
        text       = "${sao.ten}${if (!sao.dac_tinh.isNullOrEmpty()) "(${sao.dac_tinh})" else ""}",
        fontSize   = 5.5.sp,
        lineHeight = 7.sp,
        color      = getSaoColor(sao.ten).copy(alpha = 0.9f),
        maxLines   = 1,
        overflow   = TextOverflow.Clip
    )
}
