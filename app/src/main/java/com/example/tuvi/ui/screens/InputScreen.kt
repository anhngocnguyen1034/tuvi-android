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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.zIndex
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tuvi.ui.theme.*
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import com.example.tuvi.R

private fun inputScreenBgBrush() = Brush.verticalGradient(listOf(TuViNavy, InputBgGradientBottom))
private val CardBorder = Brush.linearGradient(listOf(TuViGold, TuViGoldDark, TuViGold))



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
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
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
                Icon(
                    painter = painterResource(
                        if (expanded) R.drawable.ic_drop_down else R.drawable.ic_drop
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
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

/** Dialog chọn ngày âm lịch — custom dialog có style giống TuViDatePickerDialog. */
@Composable
private fun LunarDatePickerDialog(
    selectedDay: Int,
    selectedMonth: Int,
    selectedYear: Int,
    minYear: Int,
    maxYear: Int,
    onDateSelected: (day: Int, month: Int, year: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var pickerDay by remember { mutableIntStateOf(selectedDay) }
    var pickerMonth by remember { mutableIntStateOf(selectedMonth) }
    var pickerYear by remember { mutableIntStateOf(selectedYear) }

    val days = remember { (1..30).toList() }
    val months = remember { (1..12).toList() }
    val years = remember(minYear, maxYear) { (maxYear downTo minYear).toList() }

    val dayState = rememberLazyListState(initialFirstVisibleItemIndex = (selectedDay - 1).coerceAtLeast(0))
    val monthState = rememberLazyListState(initialFirstVisibleItemIndex = (selectedMonth - 1).coerceAtLeast(0))
    val yearState = rememberLazyListState(initialFirstVisibleItemIndex = (maxYear - selectedYear).coerceAtLeast(0))

    @Composable
    fun <T> PickerColumn(
        items: List<T>,
        selected: T,
        state: LazyListState,
        label: String,
        display: (T) -> String,
        onSelect: (T) -> Unit
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                label,
                color = TuViIvoryDim,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            LazyColumn(
                state = state,
                modifier = Modifier
                    .width(88.dp)
                    .height(200.dp)
            ) {
                items(items, key = { it.hashCode() }) { item ->
                    val isSelected = item == selected
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) TuViGold.copy(alpha = 0.22f) else androidx.compose.ui.graphics.Color.Transparent)
                            .clickable { onSelect(item) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = display(item),
                            color = if (isSelected) TuViGold else TuViIvory,
                            fontSize = 16.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(InputDatePickerSurface)
                .fillMaxWidth()
        ) {
            // Title
            Text(
                stringResource(R.string.input_lunar_picker_title),
                modifier = Modifier.padding(start = 24.dp, top = 20.dp),
                color = TuViGold,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp
            )
            Text(
                text = "%02d / %02d / %d".format(pickerDay, pickerMonth, pickerYear),
                modifier = Modifier.padding(start = 24.dp, top = 4.dp, bottom = 12.dp),
                color = TuViGold,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            HorizontalDivider(color = TuViGold.copy(alpha = 0.2f), thickness = 1.dp)
            // Picker columns
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PickerColumn(
                    items = days,
                    selected = pickerDay,
                    state = dayState,
                    label = stringResource(R.string.input_picker_day),
                    display = { "%02d".format(it) },
                    onSelect = { pickerDay = it }
                )
                PickerColumn(
                    items = months,
                    selected = pickerMonth,
                    state = monthState,
                    label = stringResource(R.string.input_picker_month),
                    display = { "%02d".format(it) },
                    onSelect = { pickerMonth = it }
                )
                PickerColumn(
                    items = years,
                    selected = pickerYear,
                    state = yearState,
                    label = stringResource(R.string.input_picker_year),
                    display = { it.toString() },
                    onSelect = { pickerYear = it }
                )
            }
            HorizontalDivider(color = TuViGold.copy(alpha = 0.2f), thickness = 1.dp)
            // Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.btn_cancel), color = TuViIvoryDim, fontSize = 13.sp)
                }
                TextButton(onClick = {
                    onDateSelected(pickerDay, pickerMonth, pickerYear)
                    onDismiss()
                }) {
                    Text(stringResource(R.string.btn_select), color = TuViGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

/** Dialog chọn năm xem — tránh ExposedDropdown trong verticalScroll (hay bị khựng / chạm lỗi). */
@Composable
private fun ViewYearPickerDialog(
    selectedYear: Int,
    minYear: Int,
    maxYear: Int,
    onYearSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val years = remember(minYear, maxYear) { (maxYear downTo minYear).toList() }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TuViNavyCard,
        titleContentColor = TuViGold,
        textContentColor = TuViIvory,
        title = {
            Text(
                stringResource(R.string.input_view_year_dialog_title),
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                letterSpacing = 0.5.sp
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
            ) {
                items(years, key = { it }) { y ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (y == selectedYear) TuViGold.copy(alpha = 0.22f) else TuViNavyLight
                            )
                            .clickable {
                                onYearSelected(y)
                                onDismiss()
                            }
                    ) {
                        Text(
                            text = y.toString(),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            color = if (y == selectedYear) TuViGold else TuViIvory,
                            fontSize = 17.sp,
                            fontWeight = if (y == selectedYear) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_close), color = TuViIvoryDim)
            }
        }
    )
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
                Text(stringResource(R.string.btn_select), color = TuViGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel), color = TuViIvoryDim, fontSize = 13.sp)
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = InputDatePickerSurface,
            titleContentColor = TuViGold,
            headlineContentColor = TuViGold,
            weekdayContentColor = TuViIvoryDim,
            subheadContentColor = TuViIvoryDim,
            navigationContentColor = TuViGold,
            yearContentColor = TuViIvory,
            currentYearContentColor = TuViGold,
            selectedYearContentColor = InputDatePickerSurface,
            selectedYearContainerColor = TuViGold,
            dayContentColor = TuViIvory,
            selectedDayContentColor = InputDatePickerSurface,
            selectedDayContainerColor = TuViGold,
            todayContentColor = TuViGold,
            todayDateBorderColor = TuViGold,
            disabledDayContentColor = TuViIvoryDim.copy(alpha = 0.35f),
            dayInSelectionRangeContainerColor = TuViGold.copy(alpha = 0.2f),
            dayInSelectionRangeContentColor = TuViIvory,
        ),
        shape = RoundedCornerShape(20.dp),
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    stringResource(R.string.input_solar_picker_title),
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
                containerColor = InputDatePickerSurface,
                titleContentColor = TuViGold,
                headlineContentColor = TuViGold,
                weekdayContentColor = TuViIvoryDim,
                subheadContentColor = TuViIvoryDim,
                navigationContentColor = TuViGold,
                yearContentColor = TuViIvory,
                currentYearContentColor = TuViGold,
                selectedYearContentColor = InputDatePickerSurface,
                selectedYearContainerColor = TuViGold,
                dayContentColor = TuViIvory,
                selectedDayContentColor = InputDatePickerSurface,
                selectedDayContainerColor = TuViGold,
                todayContentColor = TuViGold,
                todayDateBorderColor = TuViGold,
                disabledDayContentColor = TuViIvoryDim.copy(alpha = 0.35f),
                dayInSelectionRangeContainerColor = TuViGold.copy(alpha = 0.2f),
                dayInSelectionRangeContentColor = TuViIvory,
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
    val minViewYear = 1900
    val maxViewYear = 2050
    var name by remember { mutableStateOf("Anhnn") }
    var day by remember { mutableIntStateOf(10) }
    var month by remember { mutableIntStateOf(3) }
    var year by remember { mutableIntStateOf(2004) }
    var viewYear by remember {
        mutableIntStateOf(currentYear.coerceIn(minViewYear, maxViewYear))
    }
    var hour by remember { mutableIntStateOf(7) }
    var minute by remember { mutableIntStateOf(30) }
    var gender by remember { mutableIntStateOf(1) }
    var duongLich by remember { mutableStateOf(true) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showLunarDatePicker by remember { mutableStateOf(false) }

    val zodiacHours = listOf(
        stringResource(R.string.input_zodiac_ty),
        stringResource(R.string.input_zodiac_suu),
        stringResource(R.string.input_zodiac_dan),
        stringResource(R.string.input_zodiac_mao),
        stringResource(R.string.input_zodiac_thin),
        stringResource(R.string.input_zodiac_ty2),
        stringResource(R.string.input_zodiac_ngo),
        stringResource(R.string.input_zodiac_mui),
        stringResource(R.string.input_zodiac_than),
        stringResource(R.string.input_zodiac_dau),
        stringResource(R.string.input_zodiac_tuat),
        stringResource(R.string.input_zodiac_hoi),
    )
    val zodiacIndex = ((hour + 1) / 2) % 12
    val zodiacLabel = zodiacHours[zodiacIndex]

    var hourExpanded by remember { mutableStateOf(false) }
    var minExpanded by remember { mutableStateOf(false) }
    var showViewYearPicker by remember { mutableStateOf(false) }

    // Dialog chọn ngày dương lịch
    if (showDatePicker && duongLich) {
        TuViDatePickerDialog(
            initialYear = year,
            initialMonth = month,
            initialDay = day,
            onDateSelected = { y, m, d -> year = y; month = m; day = d },
            onDismiss = { showDatePicker = false }
        )
    }
    // Dialog chọn ngày âm lịch
    if (showLunarDatePicker && !duongLich) {
        LunarDatePickerDialog(
            selectedDay = day,
            selectedMonth = month,
            selectedYear = year,
            minYear = minBirthYear,
            maxYear = maxBirthYear,
            onDateSelected = { d, m, y -> day = d; month = m; year = y },
            onDismiss = { showLunarDatePicker = false }
        )
    }
    if (showViewYearPicker) {
        ViewYearPickerDialog(
            selectedYear = viewYear,
            minYear = minViewYear,
            maxYear = maxViewYear,
            onYearSelected = { viewYear = it },
            onDismiss = { showViewYearPicker = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = inputScreenBgBrush())
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 76.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SectionCard {
                FieldLabel(stringResource(R.string.input_label_name))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = {
                        Text(
                            stringResource(R.string.input_hint_name),
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
            }

            // ── Ngày sinh (dương / âm) ──
            SectionCard {
                FieldLabel(stringResource(R.string.input_label_birthday))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = duongLich,
                        onClick = { duongLich = true },
                        label = { Text(stringResource(R.string.input_solar_calendar), fontSize = 12.sp) },
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
                        label = { Text(stringResource(R.string.input_lunar_calendar), fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TuViGold.copy(alpha = 0.35f),
                            selectedLabelColor = TuViGold,
                            labelColor = TuViIvoryDim,
                            containerColor = TuViNavyLight
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
                Button(
                    onClick = { if (duongLich) showDatePicker = true else showLunarDatePicker = true },
                    colors = ButtonDefaults.buttonColors(containerColor = TuViNavyLight),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        TuViGold.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "%02d  /  %02d  /  %d".format(day, month, year),
                            color = TuViIvory,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp
                        )
                        Icon(
                            painter = painterResource(R.drawable.ic_edit_dob),
                            contentDescription = stringResource(R.string.input_label_birthday),
                            tint = TuViGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Giờ sinh
            SectionCard {
                FieldLabel(stringResource(R.string.input_label_birth_time))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        TuViDropdownBox(
                            label = stringResource(R.string.input_hour_label),
                            value = stringResource(R.string.input_hour_item, "%02d".format(hour)),
                            expanded = hourExpanded,
                            onExpandedChange = { hourExpanded = it }
                        ) {
                            (0..23).forEach { h ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            stringResource(R.string.input_hour_item, "%02d".format(h)),
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
                            label = stringResource(R.string.input_minute_label),
                            value = stringResource(R.string.input_minute_item, "%02d".format(minute)),
                            expanded = minExpanded,
                            onExpandedChange = { minExpanded = it }
                        ) {
                            (0..59).forEach { m ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            stringResource(R.string.input_minute_item, "%02d".format(m)),
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
                    Icon(
                        painter = painterResource(id = R.drawable.ic_time),
                        contentDescription = null,
                        tint = TuViGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.input_earthly_branch_hour),
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

            SectionCard {
                FieldLabel(stringResource(R.string.input_label_gender))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GenderButton(
                        label = stringResource(R.string.input_gender_male),
                        icon = R.drawable.ic_male,
                        selected = gender == 1,
                        onClick = { gender = 1 },
                        modifier = Modifier.weight(1f)
                    )
                    GenderButton(
                        label = stringResource(R.string.input_gender_female),
                        icon = R.drawable.ic_female,
                        selected = gender == -1,
                        onClick = { gender = -1 },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Năm xem ──
            SectionCard {
                FieldLabel(stringResource(R.string.input_label_view_year))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = viewYear.toString(),
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text(
                                stringResource(R.string.input_hint_view_year),
                                color = TuViIvoryDim,
                                fontSize = 12.sp
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = false)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TuViIvory,
                            unfocusedTextColor = TuViIvory,
                            focusedBorderColor = TuViGold,
                            unfocusedBorderColor = TuViDivider,
                            focusedLabelColor = TuViGold,
                            focusedTrailingIconColor = TuViGold,
                            unfocusedTrailingIconColor = TuViIvoryDim
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showViewYearPicker = true }
                    )
                }
            }

            Button(
                onClick = {
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
                    text = stringResource(R.string.input_btn_view_chart),
                    color = TuViNavy,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            }


            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .height(64.dp)
                .zIndex(1f)
                .background(inputScreenBgBrush())
                .padding(top = 8.dp, start = 4.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                androidx.compose.material3.IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = stringResource(R.string.settings_back),
                        tint = TuViGold,
                    )
                }
            } else {
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = stringResource(R.string.input_screen_title),
                color = TuViGold,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
        }
    }
}

private val baguaTrigramLines = listOf(
    listOf(true, true, true),   // Càn  (Trời)
    listOf(true, true, false),  // Đoài (Đầm)
    listOf(true, false, true),  // Ly   (Lửa)
    listOf(true, false, false), // Chấn (Sấm)
    listOf(false, true, true),  // Tốn  (Gió)
    listOf(false, true, false), // Khảm (Nước)
    listOf(false, false, true), // Cấn  (Núi)
    listOf(false, false, false) // Khôn (Đất)
)

private val baguaNames = listOf("Càn", "Đoài", "Ly", "Chấn", "Tốn", "Khảm", "Cấn", "Khôn")

@Composable
fun BaguaDecoration(modifier: Modifier = Modifier) {
    val goldColor = TuViGold
    val goldDimColor = TuViGoldDark
    val bgColor = TuViNavy
    val redColor = InputChartRed

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val outerR = size.minDimension / 2f
        val innerR = outerR * 0.58f
        val lineAreaW = outerR * 0.28f
        val lineR = (innerR + outerR) / 2f

        // 1. Vòng ngoài viền vàng
        drawCircle(
            color = goldColor,
            radius = outerR - 1f,
            center = Offset(cx, cy),
            style = Stroke(width = 2.5f)
        )

        // 2. Vòng trong phân cách
        drawCircle(
            color = goldDimColor.copy(alpha = 0.5f),
            radius = innerR,
            center = Offset(cx, cy),
            style = Stroke(width = 1.2f)
        )

        // 3. Nền vòng giữa (giữa innerR và outerR)
        drawCircle(
            color = redColor.copy(alpha = 0.12f),
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
                color = goldDimColor.copy(alpha = 0.45f),
                start = Offset(cx + innerR * cos, cy + innerR * sin),
                end = Offset(cx + outerR * cos, cy + outerR * sin),
                strokeWidth = 1f
            )
        }

        // 5. Vẽ 3 vạch của từng quẻ
        val lineLen = lineAreaW * 0.75f
        val gapHalf = lineLen * 0.18f
        val lineSpacing = lineAreaW * 0.22f
        val lineStroke = outerR * 0.04f

        for (i in 0 until 8) {
            val centerAngleDeg = i * 45.0 + 22.5
            val centerAngleRad = Math.toRadians(centerAngleDeg)
            val bx = (cx + lineR * cos(centerAngleRad)).toFloat()
            val by = (cy + lineR * sin(centerAngleRad)).toFloat()

            val lines = baguaTrigramLines[i]

            withTransform({
                translate(bx, by)
                rotate(degrees = centerAngleDeg.toFloat() + 90f, pivot = Offset(0f, 0f))
            }) {
                for (j in 0..2) {
                    val yOff = (j - 1) * lineSpacing
                    if (lines[j]) {
                        drawLine(
                            color = goldColor,
                            start = Offset(-lineLen, yOff),
                            end = Offset(lineLen, yOff),
                            strokeWidth = lineStroke
                        )
                    } else {
                        drawLine(
                            color = goldColor,
                            start = Offset(-lineLen, yOff),
                            end = Offset(-gapHalf, yOff),
                            strokeWidth = lineStroke
                        )
                        drawLine(
                            color = goldColor,
                            start = Offset(gapHalf, yOff),
                            end = Offset(lineLen, yOff),
                            strokeWidth = lineStroke
                        )
                    }
                }
            }
        }

        // 6. Vòng tròn âm dương ở giữa — vẽ bằng nativeCanvas để dùng drawText
        val yinYangR = innerR * 0.62f
        val halfR = yinYangR / 2f
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

@Composable
private fun GenderButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Int
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                modifier = Modifier.size(18.dp),
                tint = textColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 15.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                letterSpacing = 0.5.sp
            )
        }
    }
}
