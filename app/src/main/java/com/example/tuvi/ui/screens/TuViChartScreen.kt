package com.example.tuvi.presentation.screens

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.min
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.text.withStyle
import com.example.tuvi.domain.model.CungInfo
import com.example.tuvi.domain.model.SaoInfo
import com.example.tuvi.domain.model.ThienBanInfo
import com.example.tuvi.domain.model.TuViChart
import java.io.File
import java.io.FileOutputStream
import com.example.tuvi.R
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

// ─── Màu Ngũ Hành ─────────────────────────────────────────────────────────────
private val HanhThuy = Color(0xFF4A90D9)   // Thủy  – xanh dương
private val HanhHoa  = Color(0xFFE84040)   // Hỏa   – đỏ
private val HanhKim  = Color(0xFFE8D5A3)   // Kim   – vàng kim / trắng ngà
private val HanhMoc  = Color(0xFF4CAF50)   // Mộc   – xanh lá
private val HanhTho  = Color(0xFFD4A017)   // Thổ   – vàng nâu

// ─── Ánh xạ tên sao → Ngũ Hành ───────────────────────────────────────────────
private val thuySet = setOf(
    // Chính tinh
    "Thái Âm", "Thiên Đồng", "Phá Quân", "Cự Môn", "Tham Lang",
    // Phụ tinh
    "Văn Khúc", "Hóa Khoa", "Hóa Kỵ", "Thiên Hỷ", "Thanh Long",
    "Long Đức", "Đào Hoa", "Thiên Giải", "Địa Giải", "Hồng Loan"
)

private val hoaSet = setOf(
    // Chính tinh
    "Thái Dương", "Liêm Trinh",
    // Phụ tinh
    "Hỏa Tinh", "Linh Tinh", "Địa Không", "Địa Kiếp",
    "Đường Phù", "Thiên Mã", "Thiên Khốc", "Thiên Hư"
)

private val kimSet = setOf(
    // Chính tinh
    "Vũ Khúc", "Thất Sát", "Thiên Lương",
    // Phụ tinh
    "Văn Xương", "Thiên Việt", "Cô Thần", "Quả Tú", "Đại Hao", "Tiểu Hao",
    "Kình Dương", "Đà La"
)

private val mocSet = setOf(
    // Chính tinh
    "Thiên Cơ",
    // Phụ tinh
    "Tả Phụ", "Hóa Lộc", "Hóa Quyền", "Thiên Khôi", "Thiên Phúc",
    "Thiên Quan", "Thiếu Dương", "Thiếu Âm"
)

private val thoSet = setOf(
    // Chính tinh
    "Tử Vi", "Thiên Phủ", "Thiên Tướng",
    // Phụ tinh
    "Hữu Bật", "Lộc Tồn", "Thiên Hình", "Thiên Thọ",
    "Long Trì", "Phượng Các", "Mộ", "Thiên La", "Địa Võng",
    // Vòng Thái Tuế
    "Tang Môn", "Phúc Đức", "Điếu Khách", "Trực Phù", "Quan Phù", "Tử Phù", "Tuế Phá"
)

private val kimSet_extra = setOf(
    // Vòng Thái Tuế
    "Bạch Hổ"
)

// Tên 12 vị trí Vòng Tràng Sinh – dùng để lọc sao hiển thị ở footer cung
private val vongTrangSinhNames = setOf(
    "Trường Sinh", "Mộc Dục", "Quan Đới", "Lâm Quan", "Đế Vượng",
    "Suy", "Bệnh", "Tử", "Mộ", "Tuyệt", "Thai", "Dưỡng"
)

private fun normalizeSpaces(s: String): String {
    return s.replace('\u00A0', ' ').replace(Regex("\\s+"), " ").trim()
}

private fun normalizeSaoNameForColor(tenSao: String): String {
    val raw = tenSao.replace('\u00A0', ' ')
    return normalizeSpaces(
        raw
            .trim()
            .removePrefix("L.")
            .removePrefix("Lưu ")
            .removePrefix("Lưu")
            .removePrefix("LƯU ")
            .removePrefix("LƯU")
    )
}

private fun inSetIgnoreCase(set: Set<String>, value: String): Boolean {
    return set.any { it.equals(value, ignoreCase = true) }
}

/** Trả về màu sao theo Ngũ Hành. Ưu tiên dùng ngu_hanh từ API, fallback hardcode. */
fun getSaoColor(sao: SaoInfo, hasTuLinh: Boolean = false): Color {
    // Ưu tiên ngu_hanh từ API nếu có
    val colorFromApi = when (sao.nguHanh?.trim()?.uppercase()) {
        "T"  -> HanhThuy
        "H"  -> HanhHoa
        "K"  -> HanhKim
        "M"  -> HanhMoc
        "TH" -> HanhTho
        else -> null
    }
    if (colorFromApi != null) return colorFromApi

    // Fallback: tra theo tên sao (hardcode)
    val baseName = normalizeSaoNameForColor(sao.ten)
    return when {
        inSetIgnoreCase(thuySet,     baseName) -> HanhThuy
        inSetIgnoreCase(hoaSet,      baseName) -> HanhHoa
        inSetIgnoreCase(kimSet,      baseName) -> HanhKim
        inSetIgnoreCase(kimSet_extra,baseName) -> HanhKim
        inSetIgnoreCase(mocSet,      baseName) -> HanhMoc
        inSetIgnoreCase(thoSet,      baseName) -> HanhTho
        else -> ChartIvory
    }
}

// Back-compat: một số chỗ gọi theo String cũ (nếu còn sót)
fun getSaoColor(tenSao: String): Color {
    val fake = SaoInfo(ten = tenSao, dacTinh = null, vongTrangSinh = 0)
    return getSaoColor(fake)
}

/** Phân nhóm sao phụ: đặt các sao Hỏa (hung) sang cột phải. */
private fun isHungSao(sao: SaoInfo, hasTuLinh: Boolean): Boolean {
    val baseName = normalizeSaoNameForColor(sao.ten)
    return inSetIgnoreCase(hoaSet, baseName) ||
           inSetIgnoreCase(kimSet, baseName) && (
               baseName.equals("Kình Dương", ignoreCase = true) ||
               baseName.equals("Đà La",      ignoreCase = true) ||
               baseName.equals("Đại Hao",    ignoreCase = true) ||
               baseName.equals("Tiểu Hao",   ignoreCase = true) ||
               baseName.equals("Cô Thần",    ignoreCase = true) ||
               baseName.equals("Quả Tú",     ignoreCase = true)
           )
}

// ─── Lưu bitmap vào thư viện ảnh ─────────────────────────────────────────────
private fun saveBitmapToGallery(context: android.content.Context, bitmap: Bitmap, name: String): Boolean {
    return try {
        val filename = "TuVi_${name}_${System.currentTimeMillis()}.png"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/TuVi")
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { os ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
                }
                true
            } ?: false
        } else {
            @Suppress("DEPRECATION")
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "TuVi")
            dir.mkdirs()
            val file = File(dir, filename)
            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            android.media.MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null, null)
            true
        }
    } catch (e: Exception) {
        false
    }
}

// ─── Màn hình chính ───────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuViChartScreen(
    data  : TuViChart,
    onBack: () -> Unit
) {
    val context      = LocalContext.current
    val scope        = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()

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
                            data.thienBan.ten,
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
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                            val saved = withContext(Dispatchers.IO) {
                                saveBitmapToGallery(context, bitmap, data.thienBan.ten)
                            }
                            Toast.makeText(
                                context,
                                if (saved) "Đã lưu vào Thư viện ảnh" else "Lỗi khi lưu ảnh",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_download),
                            contentDescription = "Tải xuống",
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
            ChartGrid(
                data = data,
                modifier = Modifier.drawWithContent {
                    graphicsLayer.record { this@drawWithContent.drawContent() }
                    drawContent()
                }
            )

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
fun ChartGrid(data: TuViChart, modifier: Modifier = Modifier) {
    // index cung → (row, col) trong lưới 4x4
    val gridMapping = mapOf(
        0  to (3 to 2), 1  to (3 to 1), 2  to (3 to 0), 3  to (2 to 0),
        4  to (1 to 0), 5  to (0 to 0), 6  to (0 to 1), 7  to (0 to 2),
        8  to (0 to 3), 9  to (1 to 3), 10 to (2 to 3), 11 to (3 to 3)
    )

    BoxWithConstraints(
        modifier = modifier
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
        data.diaBan.forEachIndexed { index, cung ->
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
            ThienBanCenterContent(data.thienBan)
        }

        // Vẽ Tuần/Triệt tại ranh giới giữa 2 cung kề nhau
        DrawTuanTriet(data.diaBan, gridMapping, cellW, cellH)
    }
}

// ─── Vẽ Tuần / Triệt gộp ─────────────────────────────────────────────────────
@Composable
private fun DrawTuanTriet(
    diaBan: List<CungInfo>,
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

    // Gộp nhãn theo cùng một ranh giới để tránh chồng chữ
    data class Boundary(val a: Int, val b: Int)
    val boundaryToLabels = linkedMapOf<Boundary, MutableList<String>>()

    fun addLabel(pair: Pair<Int, Int>, label: String) {
        val (i1, i2) = pair
        val key = if (i1 < i2) Boundary(i1, i2) else Boundary(i2, i1)
        boundaryToLabels.getOrPut(key) { mutableListOf() }.add(label)
    }

    tuanPairs.forEach { addLabel(it, "Tuần") }
    trietPairs.forEach { addLabel(it, "Triệt") }

    // Đặt text theo anchor point và canh giữa theo kích thước đo được (không trừ tay)
    @Composable
    fun AnchoredText(text: String, anchorX: androidx.compose.ui.unit.Dp, anchorY: androidx.compose.ui.unit.Dp) {
        Layout(
            modifier = Modifier.fillMaxSize(),
            content = {
                Text(
                    text = text,
                    color = ChartIvory,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }
        ) { measurables, constraints ->
            val loose = Constraints(
                minWidth = 0,
                minHeight = 0,
                maxWidth = Constraints.Infinity,
                maxHeight = Constraints.Infinity
            )
            val placeable = measurables.first().measure(loose)
            val xPx = anchorX.roundToPx()
            val yPx = anchorY.roundToPx()
            layout(constraints.maxWidth, constraints.maxHeight) {
                placeable.placeRelative(
                    x = xPx - placeable.width / 2,
                    y = yPx - placeable.height / 2
                )
            }
        }
    }

    boundaryToLabels.forEach { (boundary, labels) ->
        val pos1 = mapping[boundary.a] ?: return@forEach
        val pos2 = mapping[boundary.b] ?: return@forEach

        val r1 = pos1.first
        val c1 = pos1.second
        val r2 = pos2.first
        val c2 = pos2.second

        // Anchor tại trung điểm CẠNH CHUNG giữa 2 ô
        val (midX, midY) = when {
            r1 == r2 && abs(c1 - c2) == 1 -> (min(c1, c2) + 1).toFloat() to (r1 + 0.5f)
            c1 == c2 && abs(r1 - r2) == 1 -> (c1 + 0.5f) to (min(r1, r2) + 1).toFloat()
            else -> {
                val cx1 = c1 + 0.5f
                val cy1 = r1 + 0.5f
                val cx2 = c2 + 0.5f
                val cy2 = r2 + 0.5f
                ((cx1 + cx2) / 2f) to ((cy1 + cy2) / 2f)
            }
        }

        val text = labels.distinct().joinToString("/")
        AnchoredText(
            text = text,
            anchorX = cellW * midX,
            anchorY = cellH * midY
        )
    }
}

// ─── Thiên Bàn trung tâm ─────────────────────────────────────────────────────
@Composable
private fun ThienBanCenterContent(tb: ThienBanInfo) {
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
        CenterLine("",        tb.gioiTinh)
        CenterLine("Dương",   tb.ngayDuong)
        CenterLine("Âm",      tb.ngayAm)
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

/** Map code ngắn từ API → nhãn đầy đủ. */
private val dacTinhFullLabel = mapOf(
    "M" to "Miếu",
    "V" to "Vượng",
    "Đ" to "Đắc",
    "B" to "Bình",
    "H" to "Hãm"
)

/**
 * Trả về chữ viết tắt để hiển thị sau chính tinh.
 * Hỗ trợ cả code ngắn từ API ("M","V","Đ","B","H") lẫn tên đầy đủ cũ.
 */
private fun dacTinhVietTat(dacTinh: String): String = when {
    dacTinhFullLabel.containsKey(dacTinh) -> dacTinh   // code ngắn từ API
    dacTinh.equals("Đắc",   ignoreCase = true) -> "Đ"
    dacTinh.equals("Miếu",  ignoreCase = true) -> "M"
    dacTinh.equals("Vượng", ignoreCase = true) -> "V"
    dacTinh.equals("Bình",  ignoreCase = true) -> "B"
    dacTinh.equals("Hãm",   ignoreCase = true) -> "H"
    else -> ""
}

/**
 * Chuẩn hóa nhãn sao:
 * - Nếu `dac_tinh == "Lưu"` thì hiển thị theo format `L.<ten>` (vd: `L.Thái Tuế`).
 * - Nếu [chinhTinh] == true và dac_tinh là Đắc/Miếu/Vượng/Bình/Hãm thì dùng chữ viết tắt.
 * - Không hiển thị thêm "(Lưu)" để tránh lặp.
 */
private fun saoLabel(sao: SaoInfo, chinhTinh: Boolean = false): String {
    val dacTinh = sao.dacTinh?.trim().orEmpty()
    val isLuu = dacTinh.equals("Lưu", ignoreCase = true)
    return if (isLuu) {
        val tenRaw = sao.ten.trim()
        val tenNoLuu = tenRaw
            .removePrefix("L.")
            .removePrefix("Lưu ")
            .removePrefix("Lưu\u00A0") // non‑breaking space
            .removePrefix("LƯU ")
            .removePrefix("LƯU\u00A0")
            .trim()
        "L.$tenNoLuu"
    } else if (chinhTinh) {
        val vietTat = dacTinhVietTat(dacTinh)
        "${sao.ten}${if (vietTat.isNotEmpty()) " ($vietTat)" else ""}"
    } else {
        val vietTat = dacTinhVietTat(dacTinh)
        "${sao.ten}${if (vietTat.isNotEmpty()) " ($vietTat)" else ""}"
    }
}

// ─── Ô cung ───────────────────────────────────────────────────────────────────
@Composable
fun PalaceView(cung: CungInfo) {
    // Sao Vòng Tràng Sinh chỉ hiển thị 1 lần ở dưới giữa,
    // nên loại khỏi danh sách sao (chính tinh/phụ tinh) để không bị lặp.
    val saoKhongTrangSinh = cung.sao.filter { sao ->
        val baseName = normalizeSpaces(sao.ten.replace('\u00A0', ' '))
        !vongTrangSinhNames.any { baseName.equals(it, ignoreCase = true) }
    }

    val chinhTinhNames = listOf(
        "Tử Vi", "Thiên Cơ", "Thái Dương", "Vũ Khúc", "Thiên Đồng", "Liêm Trinh",
        "Thiên Phủ", "Thái Âm", "Tham Lang", "Cự Môn",
        "Thiên Tướng", "Thiên Lương", "Thất Sát", "Phá Quân"
    )
    val (chinhTinhs, phuTinhs) = saoKhongTrangSinh.partition { sao ->
        chinhTinhNames.any { sao.ten.contains(it, ignoreCase = true) }
    }

    // Dùng để phân biệt Bạch Hổ theo danh sách: Bạch Hổ chỉ là cát khi đi cùng bộ Tứ Linh.
    val saoTenSetForColor = cung.sao.map { normalizeSaoNameForColor(it.ten) }.toSet()
    val hasTuLinh = setOf("Long Trì", "Phượng Các", "Hoa Cái").all { it in saoTenSetForColor }

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
            // Cung chức (Giữa) - Mệnh, Phụ Mẫu, Phúc Đức...
            Box(
                modifier = Modifier.weight(1.5f),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = cung.cungChu.uppercase(),
                        fontSize = 6.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChartGold,
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                }
            }
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
                    text       = saoLabel(sao, chinhTinh = true),
                    fontSize   = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    color      = getSaoColor(sao, hasTuLinh),
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
            val badStars  = phuTinhs.filter { isHungSao(it, hasTuLinh) }
            val goodStars = phuTinhs.filter { !isHungSao(it, hasTuLinh) }

            fun List<SaoInfo>.luuLast() = sortedWith(compareBy { it.dacTinh?.trim().equals("Lưu", ignoreCase = true) })

            // Cột trái: sao tốt (cát + trung tính), sao Lưu xuống cuối
            Column(Modifier.weight(1f)) { goodStars.luuLast().forEach { StarText(it, hasTuLinh) } }
            // Cột phải: sao xấu (hung), sao Lưu xuống cuối
            Column(Modifier.weight(1f)) { badStars.luuLast().forEach { StarText(it, hasTuLinh) } }
        }

        // ── Hàng 4: Địa chi | Vòng Tràng Sinh | Tháng ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.Bottom
        ) {
            // Địa chi (Tý/Sửu/Dần/Mão...) (Căn trái dưới)
            Text(
                cung.cungTen,
                fontSize = 5.5.sp,
                color    = ChartIvoryDim,
                maxLines = 1,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )
            // Tr.Sinh: <vị trí>
            val trangSinhViTri = cung.sao.firstOrNull { sao ->
                val baseName = normalizeSpaces(sao.ten.replace('\u00A0', ' '))
                vongTrangSinhNames.any { baseName.equals(it, ignoreCase = true) }
            }?.ten
            val thangCung = cung.thang ?: thangTuChiCung(cung.cungTen)
            // Chỉ hiển thị đúng tên vị trí vòng Tràng Sinh (Đế vượng, Lâm quan, ...)
            Text(
                text = trangSinhViTri ?: "",
                fontSize = 6.sp,
                color = Color.DarkGray,
                maxLines = 1,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            // Tháng âm lịch theo cung
            Text(
                text = thangCung?.let { "Tháng $it" } ?: "Tháng",
                fontSize = 6.sp,
                color = Color.DarkGray,
                maxLines = 1,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }
    }
}

/** Ánh xạ tên chi trong `cungTen` sang tháng âm lịch (Dần=1, ... , Sửu=12). */
private fun thangTuChiCung(cungTen: String): Int? {
    val chiToThang = mapOf(
        "Dần" to 1,
        "Mão" to 2,
        "Thìn" to 3,
        "Tỵ" to 4,
        "Ngọ" to 5,
        "Mùi" to 6,
        "Thân" to 7,
        "Dậu" to 8,
        "Tuất" to 9,
        "Hợi" to 10,
        "Tý" to 11,
        "Sửu" to 12
    )
    return chiToThang.entries.firstOrNull { cungTen.contains(it.key) }?.value
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
fun StarText(sao: SaoInfo, hasTuLinh: Boolean) {
    Text(
        text       = saoLabel(sao),
        fontSize   = 5.5.sp,
        lineHeight = 7.sp,
        color      = getSaoColor(sao, hasTuLinh).copy(alpha = 0.9f),
        maxLines   = 1,
        overflow   = TextOverflow.Clip
    )
}
