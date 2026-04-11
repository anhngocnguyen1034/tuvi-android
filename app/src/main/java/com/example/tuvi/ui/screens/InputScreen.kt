package com.example.tuvi.presentation.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tuvi.ui.theme.*
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

// ── Palette shortcuts ──────────────────────────────────────────────────
private val BgGradient = Brush.verticalGradient(listOf(TuViNavy, Color(0xFF071330)))
private val CardBorder = Brush.linearGradient(listOf(TuViGold, TuViGoldDark, TuViGold))

// ── Helpers ────────────────────────────────────────────────────────────

@Composable
private fun GoldDivider() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = TuViDivider, thickness = 1.dp)
        Text(
            text = "  ✦  ☽  ✦  ",
            color = TuViGold.copy(alpha = 0.7f),
            fontSize = 12.sp,
            letterSpacing = 2.sp
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = TuViDivider, thickness = 1.dp)
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                brush = CardBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .background(TuViNavyCard)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
private fun FieldLabel(text: String, icon: ImageVector? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = TuViGold, modifier = Modifier.size(16.dp))
        }
        Text(
            text = text,
            color = TuViGoldLight,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TuViDropdownBox(
    label: String,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, color = TuViIvoryDim, fontSize = 12.sp) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TuViIvory,
                unfocusedTextColor = TuViIvory,
                focusedBorderColor = TuViGold,
                unfocusedBorderColor = TuViDivider,
                focusedLabelColor = TuViGold,
                cursorColor = TuViGold,
                focusedTrailingIconColor = TuViGold,
                unfocusedTrailingIconColor = TuViIvoryDim
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.background(TuViNavyCard)
        ) {
            content()
        }
    }
}

// ── Custom Tử Vi DatePickerDialog ──────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuViDatePickerDialog(
    initialYear: Int,
    initialMonth: Int, // 1-indexed
    initialDay: Int,
    onDateSelected: (year: Int, month: Int, day: Int) -> Unit,
    onDismiss: () -> Unit
) {
    // Tính millis cho ngày khởi tạo
    val initMillis = remember(initialYear, initialMonth, initialDay) {
        val c = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        c.set(initialYear, initialMonth - 1, initialDay, 0, 0, 0)
        c.set(Calendar.MILLISECOND, 0)
        c.timeInMillis
    }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        cal.timeInMillis = millis
                        onDateSelected(
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH) + 1,
                            cal.get(Calendar.DAY_OF_MONTH)
                        )
                    }
                    onDismiss()
                }
            ) {
                Text("✦ CHỌN", color = TuViGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("HUỶ", color = TuViIvoryDim, fontSize = 13.sp)
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor            = Color(0xFF12082A),          // nền dialog tím đen
            titleContentColor         = TuViGold,
            headlineContentColor      = TuViGold,
            weekdayContentColor       = TuViIvoryDim,
            subheadContentColor       = TuViIvoryDim,
            navigationContentColor    = TuViGold,
            yearContentColor          = TuViIvory,
            currentYearContentColor   = TuViGold,
            selectedYearContentColor  = Color(0xFF12082A),
            selectedYearContainerColor= TuViGold,
            dayContentColor           = TuViIvory,
            selectedDayContentColor   = Color(0xFF12082A),
            selectedDayContainerColor = TuViGold,
            todayContentColor         = TuViGold,
            todayDateBorderColor      = TuViGold,
            disabledDayContentColor   = TuViIvoryDim.copy(alpha = 0.35f),
            dayInSelectionRangeContainerColor = TuViGold.copy(alpha = 0.2f),
            dayInSelectionRangeContentColor   = TuViIvory,
        ),
        shape = RoundedCornerShape(20.dp),
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    "✦  CHỌN NGÀY SINH",
                    modifier = Modifier.padding(start = 24.dp, top = 20.dp),
                    color = TuViGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp
                )
            },
            headline = {
                // Hiển thị ngày đã chọn bằng định dạng d/M/yyyy
                val selMillis = datePickerState.selectedDateMillis
                val displayText = if (selMillis != null) {
                    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    cal.timeInMillis = selMillis
                    "%02d / %02d / %d".format(
                        cal.get(Calendar.DAY_OF_MONTH),
                        cal.get(Calendar.MONTH) + 1,
                        cal.get(Calendar.YEAR)
                    )
                } else "—"
                Text(
                    displayText,
                    modifier = Modifier.padding(start = 24.dp, bottom = 12.dp),
                    color = TuViGold,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            },
            showModeToggle = false,
            colors = DatePickerDefaults.colors(
                containerColor            = Color(0xFF12082A),
                titleContentColor         = TuViGold,
                headlineContentColor      = TuViGold,
                weekdayContentColor       = TuViIvoryDim,
                subheadContentColor       = TuViIvoryDim,
                navigationContentColor    = TuViGold,
                yearContentColor          = TuViIvory,
                currentYearContentColor   = TuViGold,
                selectedYearContentColor  = Color(0xFF12082A),
                selectedYearContainerColor= TuViGold,
                dayContentColor           = TuViIvory,
                selectedDayContentColor   = Color(0xFF12082A),
                selectedDayContainerColor = TuViGold,
                todayContentColor         = TuViGold,
                todayDateBorderColor      = TuViGold,
                disabledDayContentColor   = TuViIvoryDim.copy(alpha = 0.35f),
                dayInSelectionRangeContainerColor = TuViGold.copy(alpha = 0.2f),
                dayInSelectionRangeContentColor   = TuViIvory,
            )
        )
    }
}

// ── Main Screen ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputScreen(
    onViewChart: (String, Int, Int, Int, Int, Int, Int, Int, Boolean) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
    val minBirthYear = 1900
    val maxBirthYear = currentYear + 1
    var name by remember { mutableStateOf("Anhnn") }
    var day by remember { mutableIntStateOf(10) }
    var month by remember { mutableIntStateOf(3) }
    var year by remember { mutableIntStateOf(2004) }
    var viewYearText by remember { mutableStateOf(currentYear.toString()) }
    var hour by remember { mutableIntStateOf(7) }
    var minute by remember { mutableIntStateOf(30) }
    var gender by remember { mutableIntStateOf(1) }
    var duongLich by remember { mutableStateOf(true) }

    var showDatePicker by remember { mutableStateOf(false) }
    var lunarDayExpanded by remember { mutableStateOf(false) }
    var lunarMonthExpanded by remember { mutableStateOf(false) }
    var lunarYearExpanded by remember { mutableStateOf(false) }

    val zodiacHours = listOf(
        "Tý (23h–01h)", "Sửu (01h–03h)", "Dần (03h–05h)", "Mão (05h–07h)",
        "Thìn (07h–09h)", "Tỵ (09h–11h)", "Ngọ (11h–13h)", "Mùi (13h–15h)",
        "Thân (15h–17h)", "Dậu (17h–19h)", "Tuất (19h–21h)", "Hợi (21h–23h)"
    )
    val zodiacIndex = ((hour + 1) / 2) % 12
    val zodiacLabel = zodiacHours[zodiacIndex]

    var hourExpanded by remember { mutableStateOf(false) }
    var minExpanded by remember { mutableStateOf(false) }

    // Dialog chọn ngày (dương lịch)
    if (showDatePicker && duongLich) {
        TuViDatePickerDialog(
            initialYear  = year,
            initialMonth = month,
            initialDay   = day,
            onDateSelected = { y, m, d -> year = y; month = m; day = d },
            onDismiss    = { showDatePicker = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = BgGradient)
    ) {
        if (onBack != null) {
            androidx.compose.material3.IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 12.dp, start = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = TuViGold
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ── Header ──
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Hoạ tiết Bát Quái
                BaguaDecoration(
                    modifier = Modifier.size(160.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "TỬ VI BY ANHNN",
                    color = TuViGold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "NHẬP THÔNG TIN LÁ SỐ",
                    color = TuViIvoryDim,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp,
                    fontStyle = FontStyle.Italic
                )
                Spacer(modifier = Modifier.height(8.dp))
                GoldDivider()
            }

            // ── Họ và Tên ──
            SectionCard {
                FieldLabel("HỌ VÀ TÊN")
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = {
                        Text("Nhập họ tên đầy đủ…", color = TuViIvoryDim.copy(alpha = 0.5f), fontSize = 14.sp)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TuViIvory,
                        unfocusedTextColor = TuViIvory,
                        focusedBorderColor = TuViGold,
                        unfocusedBorderColor = TuViDivider,
                        focusedLabelColor = TuViGold,
                        cursorColor = TuViGold,
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ── Ngày sinh (dương / âm) ──
            SectionCard {
                FieldLabel("NGÀY SINH", Icons.Default.MoreVert)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = duongLich,
                        onClick = { duongLich = true },
                        label = { Text("Dương lịch", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TuViGold.copy(alpha = 0.35f),
                            selectedLabelColor = TuViGold,
                            labelColor = TuViIvoryDim,
                            containerColor = TuViNavyLight
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = !duongLich,
                        onClick = { duongLich = false },
                        label = { Text("Âm lịch", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TuViGold.copy(alpha = 0.35f),
                            selectedLabelColor = TuViGold,
                            labelColor = TuViIvoryDim,
                            containerColor = TuViNavyLight
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
                if (duongLich) {
                    Button(
                        onClick = { showDatePicker = true },
                        colors = ButtonDefaults.buttonColors(containerColor = TuViNavyLight),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TuViGold.copy(alpha = 0.7f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "  %02d  /  %02d  /  %d".format(day, month, year),
                            color = TuViIvory,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "Nhấn để chọn ngày dương lịch",
                        color = TuViIvoryDim.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            TuViDropdownBox(
                                label = "Ngày",
                                value = "%02d".format(day),
                                expanded = lunarDayExpanded,
                                onExpandedChange = { lunarDayExpanded = it }
                            ) {
                                (1..31).forEach { d ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "%02d".format(d),
                                                color = if (d == day) TuViGold else TuViIvory
                                            )
                                        },
                                        onClick = { day = d; lunarDayExpanded = false }
                                    )
                                }
                            }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            TuViDropdownBox(
                                label = "Tháng",
                                value = "%02d".format(month),
                                expanded = lunarMonthExpanded,
                                onExpandedChange = { lunarMonthExpanded = it }
                            ) {
                                (1..12).forEach { m ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Tháng %02d".format(m),
                                                color = if (m == month) TuViGold else TuViIvory
                                            )
                                        },
                                        onClick = { month = m; lunarMonthExpanded = false }
                                    )
                                }
                            }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            TuViDropdownBox(
                                label = "Năm",
                                value = year.toString(),
                                expanded = lunarYearExpanded,
                                onExpandedChange = { lunarYearExpanded = it }
                            ) {
                                (minBirthYear..maxBirthYear).reversed().forEach { y ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                y.toString(),
                                                color = if (y == year) TuViGold else TuViIvory
                                            )
                                        },
                                        onClick = { year = y; lunarYearExpanded = false }
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = "Nhập ngày / tháng / năm theo âm lịch (server quy đổi sang dương lịch)",
                        color = TuViIvoryDim.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // ── Giờ sinh ──
            SectionCard {
                FieldLabel("GIỜ SINH", Icons.Default.Edit)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        TuViDropdownBox(
                            label = "Giờ",
                            value = "%02dh".format(hour),
                            expanded = hourExpanded,
                            onExpandedChange = { hourExpanded = it }
                        ) {
                            (0..23).forEach { h ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "%02d giờ".format(h),
                                            color = if (h == hour) TuViGold else TuViIvory
                                        )
                                    },
                                    onClick = { hour = h; hourExpanded = false }
                                )
                            }
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        TuViDropdownBox(
                            label = "Phút",
                            value = "%02d phút".format(minute),
                            expanded = minExpanded,
                            onExpandedChange = { minExpanded = it }
                        ) {
                            (0..59).forEach { m ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "%02d phút".format(m),
                                            color = if (m == minute) TuViGold else TuViIvory
                                        )
                                    },
                                    onClick = { minute = m; minExpanded = false }
                                )
                            }
                        }
                    }
                }

                // Zodiac hint
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(TuViNavyLight)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("☽", color = TuViGold, fontSize = 18.sp)
                    Column {
                        Text(
                            text = "Giờ địa chi",
                            color = TuViIvoryDim,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = zodiacLabel,
                            color = TuViGoldLight,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // ── Giới tính ──
            SectionCard {
                FieldLabel("GIỚI TÍNH")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GenderButton(
                        label = "Nam ♂",
                        selected = gender == 1,
                        onClick = { gender = 1 },
                        modifier = Modifier.weight(1f)
                    )
                    GenderButton(
                        label = "Nữ ♀",
                        selected = gender == -1,
                        onClick = { gender = -1 },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Năm xem ──
            SectionCard {
                FieldLabel("NĂM XEM")
                OutlinedTextField(
                    value = viewYearText,
                    onValueChange = { input ->
                        viewYearText = input.filter { it.isDigit() }.take(4)
                    },
                    placeholder = {
                        Text(
                            "Năm hiện tại",
                            color = TuViIvoryDim.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TuViIvory,
                        unfocusedTextColor = TuViIvory,
                        focusedBorderColor = TuViGold,
                        unfocusedBorderColor = TuViDivider,
                        focusedLabelColor = TuViGold,
                        cursorColor = TuViGold,
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Mặc định năm hiện tại theo lịch máy",
                    color = TuViIvoryDim.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            GoldDivider()

            // ── Submit ──
            Button(
                onClick = {
                    val viewYear = viewYearText.toIntOrNull() ?: currentYear
                    onViewChart(name, day, month, year, viewYear, hour, minute, gender, duongLich)
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TuViGold,
                    disabledContainerColor = TuViGoldDark.copy(alpha = 0.35f)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = "✦  XEM LÁ SỐ TỬ VI  ✦",
                    color = TuViNavy,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            }

            Text(
                text = "Kết quả dựa trên Tử Vi Đẩu Số cổ truyền",
                color = TuViIvoryDim.copy(alpha = 0.45f),
                fontSize = 11.sp,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ── Bát Quái Decoration ───────────────────────────────────────────────
// 8 quẻ Bát Quái (Càn, Đoài, Ly, Chấn, Tốn, Khảm, Cấn, Khôn)
// Mỗi quẻ có 3 vạch: vạch liên (dương ─) hoặc vạch đứt (âm - -)
// true = vạch liên (dương), false = vạch đứt (âm)
private val baguaTrigramLines = listOf(
    listOf(true,  true,  true ),  // ☰ Càn   (Trời)
    listOf(true,  true,  false),  // ☱ Đoài  (Đầm)
    listOf(true,  false, true ),  // ☲ Ly    (Lửa)
    listOf(true,  false, false),  // ☳ Chấn  (Sấm)
    listOf(false, true,  true ),  // ☴ Tốn   (Gió)
    listOf(false, true,  false),  // ☵ Khảm  (Nước)
    listOf(false, false, true ),  // ☶ Cấn   (Núi)
    listOf(false, false, false)   // ☷ Khôn  (Đất)
)

private val baguaNames = listOf("☰", "☱", "☲", "☳", "☴", "☵", "☶", "☷")

@Composable
fun BaguaDecoration(modifier: Modifier = Modifier) {
    val goldColor   = TuViGold
    val goldDimColor = TuViGoldDark
    val bgColor     = TuViNavy
    val redColor    = Color(0xFF8B0000)

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val outerR   = size.minDimension / 2f
        val innerR   = outerR * 0.58f
        val lineAreaW = outerR * 0.28f  // vùng vẽ vạch quẻ (giữa innerR và outerR)
        val lineR     = (innerR + outerR) / 2f // tâm vùng vạch

        // 1. Vòng ngoài viền vàng
        drawCircle(
            color  = goldColor,
            radius = outerR - 1f,
            center = Offset(cx, cy),
            style  = Stroke(width = 2.5f)
        )

        // 2. Vòng trong phân cách
        drawCircle(
            color  = goldDimColor.copy(alpha = 0.5f),
            radius = innerR,
            center = Offset(cx, cy),
            style  = Stroke(width = 1.2f)
        )

        // 3. Nền vòng giữa (giữa innerR và outerR)
        drawCircle(
            color  = redColor.copy(alpha = 0.12f),
            radius = (innerR + outerR) / 2f + lineAreaW / 2f,
            center = Offset(cx, cy)
        )

        // 4. Tia phân cách 8 quẻ
        for (i in 0 until 8) {
            val angleDeg = i * 45.0
            val angleRad = Math.toRadians(angleDeg)
            val cos = cos(angleRad).toFloat()
            val sin = sin(angleRad).toFloat()
            drawLine(
                color       = goldDimColor.copy(alpha = 0.45f),
                start       = Offset(cx + innerR * cos, cy + innerR * sin),
                end         = Offset(cx + outerR * cos, cy + outerR * sin),
                strokeWidth = 1f
            )
        }

        // 5. Vẽ 3 vạch của từng quẻ
        val lineLen      = lineAreaW * 0.75f  // độ dài nửa vạch liên
        val gapHalf      = lineLen * 0.18f    // khoảng hở giữa 2 đoạn vạch đứt
        val lineSpacing  = lineAreaW * 0.22f  // khoảng cách giữa 3 vạch
        val lineStroke   = outerR * 0.04f

        for (i in 0 until 8) {
            val centerAngleDeg = i * 45.0 + 22.5  // giữa khe phân cách
            val centerAngleRad = Math.toRadians(centerAngleDeg)
            val bx = (cx + lineR * cos(centerAngleRad)).toFloat()
            val by = (cy + lineR * sin(centerAngleRad)).toFloat()

            val lines = baguaTrigramLines[i]

            withTransform({
                translate(bx, by)
                rotate(degrees = centerAngleDeg.toFloat() + 90f, pivot = Offset(0f, 0f))
            }) {
                for (j in 0..2) {
                    val yOff = (j - 1) * lineSpacing  // -1, 0, +1
                    if (lines[j]) {
                        // Vạch liên (dương) — một đường
                        drawLine(
                            color       = goldColor,
                            start       = Offset(-lineLen, yOff),
                            end         = Offset(lineLen, yOff),
                            strokeWidth = lineStroke
                        )
                    } else {
                        // Vạch đứt (âm) — hai đoạn
                        drawLine(
                            color       = goldColor,
                            start       = Offset(-lineLen, yOff),
                            end         = Offset(-gapHalf, yOff),
                            strokeWidth = lineStroke
                        )
                        drawLine(
                            color       = goldColor,
                            start       = Offset(gapHalf, yOff),
                            end         = Offset(lineLen, yOff),
                            strokeWidth = lineStroke
                        )
                    }
                }
            }
        }

        // 6. Vòng tròn âm dương ở giữa — vẽ bằng nativeCanvas để dùng drawText
        val yinYangR  = innerR * 0.62f
        val halfR     = yinYangR / 2f
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
        }

        // Nền trắng trên (dương)
        paint.color = android.graphics.Color.WHITE
        drawContext.canvas.nativeCanvas.drawArc(
            cx - yinYangR, cy - yinYangR, cx + yinYangR, cy + yinYangR,
            -90f, 180f, true, paint
        )
        // Nền đen dưới (âm)
        paint.color = android.graphics.Color.BLACK
        drawContext.canvas.nativeCanvas.drawArc(
            cx - yinYangR, cy - yinYangR, cx + yinYangR, cy + yinYangR,
            90f, 180f, true, paint
        )
        // Bán vòng nhỏ: trên phần trắng — hình bán đen
        paint.color = android.graphics.Color.BLACK
        drawContext.canvas.nativeCanvas.drawCircle(cx, cy - halfR, halfR, paint)
        // Bán vòng nhỏ: trên phần đen — hình bán trắng
        paint.color = android.graphics.Color.WHITE
        drawContext.canvas.nativeCanvas.drawCircle(cx, cy + halfR, halfR, paint)
        // Chấm nhỏ âm (trong vùng dương)
        paint.color = android.graphics.Color.BLACK
        drawContext.canvas.nativeCanvas.drawCircle(cx, cy - halfR, halfR * 0.32f, paint)
        // Chấm nhỏ dương (trong vùng âm)
        paint.color = android.graphics.Color.WHITE
        drawContext.canvas.nativeCanvas.drawCircle(cx, cy + halfR, halfR * 0.32f, paint)
        // Viền âm dương
        paint.color = goldColor.toArgb()
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 1.8f
        drawContext.canvas.nativeCanvas.drawCircle(cx, cy, yinYangR, paint)
    }
}

// ── Gender pill button ─────────────────────────────────────────────────
@Composable
private fun GenderButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) TuViGold else TuViNavyLight,
        animationSpec = tween(300), label = "genderBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) TuViNavy else TuViIvoryDim,
        animationSpec = tween(300), label = "genderText"
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = 1.dp,
                color = if (selected) TuViGold else TuViDivider,
                shape = RoundedCornerShape(10.dp)
            )
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            letterSpacing = 0.5.sp
        )
    }
}
