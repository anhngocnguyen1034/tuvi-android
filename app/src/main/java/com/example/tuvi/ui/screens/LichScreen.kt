package com.example.tuvi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tuvi.R
import com.example.tuvi.data.local.SuKienEntity
import com.example.tuvi.data.remote.dto.NgayInfoDto
import com.example.tuvi.data.remote.dto.ThangLichDto
import com.example.tuvi.presentation.LichUiState
import com.example.tuvi.presentation.LichViewModel
import com.example.tuvi.presentation.SuKienViewModel
import com.example.tuvi.ui.theme.*
import java.util.Calendar


@Composable
fun LichScreen(
    onBack: () -> Unit,
    lichVm: LichViewModel   = viewModel(factory = LichViewModel.Factory),
    suKienVm: SuKienViewModel = viewModel(factory = SuKienViewModel.Factory),
) {
    val uiState     by lichVm.uiState.collectAsStateWithLifecycle()
    val selectedDay by lichVm.selectedDay.collectAsStateWithLifecycle()
    val currentMonth by lichVm.currentMonth.collectAsStateWithLifecycle()
    val currentYear  by lichVm.currentYear.collectAsStateWithLifecycle()

    // Đồng bộ tháng/năm với SuKienViewModel để lấy sự kiện tháng
    LaunchedEffect(currentMonth, currentYear) {
        suKienVm.setThang(currentMonth, currentYear)
    }
    val suKienThang by suKienVm.suKienThang.collectAsStateWithLifecycle()

    // Sự kiện của ngày đang chọn
    val suKienNgay by remember(selectedDay) {
        if (selectedDay != null)
            suKienVm.getSuKienNgay(selectedDay!!.ngayDuong, selectedDay!!.thangDuong, selectedDay!!.namDuong)
        else kotlinx.coroutines.flow.flowOf(emptyList())
    }.collectAsStateWithLifecycle(emptyList())

    var showAddSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TuViNavy)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.settings_back), tint = TuViGold)
            }
            Text(
                stringResource(R.string.calendar_screen_title),
                color = TuViGold,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = LoraFontFamily,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.size(48.dp))
        }

        HorizontalDivider(color = TuViDivider, thickness = 0.5.dp)

        // ── Month navigator ──────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = { lichVm.prevMonth() }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = TuViGold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(R.string.calendar_month_year, currentMonth, currentYear),
                    color = TuViIvory, fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
                    fontFamily = LoraFontFamily,
                )
                if (uiState is LichUiState.Success) {
                    val data = (uiState as LichUiState.Success).data
                    Text("${data.canChiThang} – ${data.canChiNam}", color = TuViGoldLight, fontSize = 12.sp)
                }
            }
            IconButton(onClick = { lichVm.nextMonth() }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TuViGold)
            }
        }

        when (val state = uiState) {
            is LichUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TuViGold)
            }
            is LichUiState.Error   -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.message, color = TuViRedLight, textAlign = TextAlign.Center)
            }
            is LichUiState.Success -> {
                // Set sự kiện có trong tháng (để đánh dấu chấm trên ô)
                val daysWithEvent = suKienThang.map { it.ngayDuong }.toSet()

                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    CalendarGrid(
                        data            = state.data,
                        selectedDay     = selectedDay,
                        daysWithEvent   = daysWithEvent,
                        onDayClick      = { lichVm.selectDay(it) },
                    )

                    selectedDay?.let { day ->
                        DayDetail(
                            day        = day,
                            suKienList = suKienNgay,
                            onAddClick = { showAddSheet = true },
                            onDelete   = { suKienVm.xoa(it) },
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }

    // ── Bottom sheet thêm sự kiện ────────────────────────────────────────────
    if (showAddSheet && selectedDay != null) {
        val day = selectedDay!!
        ThemSuKienSheet(
            ngay    = day.ngayDuong,
            thang   = day.thangDuong,
            nam     = day.namDuong,
            thuText = day.thu,
            onDismiss = { showAddSheet = false },
            onSave  = { tieuDe, ghiChu, epoch ->
                suKienVm.them(tieuDe, ghiChu, day.ngayDuong, day.thangDuong, day.namDuong, epoch)
            },
        )
    }
}

// ── Calendar grid ────────────────────────────────────────────────────────────

@Composable
private fun CalendarGrid(
    data: ThangLichDto,
    selectedDay: NgayInfoDto?,
    daysWithEvent: Set<Int>,
    onDayClick: (NgayInfoDto) -> Unit,
) {
    val today = Calendar.getInstance()
    val isCurrentMonth = data.thang == today.get(Calendar.MONTH) + 1
            && data.nam == today.get(Calendar.YEAR)
    val todayNum = today.get(Calendar.DAY_OF_MONTH)

    val daysOfWeek = listOf(
        stringResource(R.string.calendar_day_sun),
        stringResource(R.string.calendar_day_mon),
        stringResource(R.string.calendar_day_tue),
        stringResource(R.string.calendar_day_wed),
        stringResource(R.string.calendar_day_thu),
        stringResource(R.string.calendar_day_fri),
        stringResource(R.string.calendar_day_sat),
    )
    // Day-of-week header
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        daysOfWeek.forEachIndexed { i, label ->
            Text(
                label,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (i == 0) TuViRed else TuViIvoryDim,
            )
        }
    }

    val firstDayOfWeek = if (data.ngay.isNotEmpty()) thuToIndex(data.ngay.first().thu) else 0
    val cells = mutableListOf<NgayInfoDto?>()
    repeat(firstDayOfWeek) { cells.add(null) }
    cells.addAll(data.ngay)
    while (cells.size % 7 != 0) cells.add(null)

    cells.chunked(7).forEach { row ->
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp)) {
            row.forEachIndexed { colIdx, dayInfo ->
                Box(
                    modifier = Modifier.weight(1f).aspectRatio(1f).padding(2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (dayInfo != null) {
                        val isToday    = isCurrentMonth && dayInfo.ngayDuong == todayNum
                        val isSelected = selectedDay?.ngayDuong == dayInfo.ngayDuong
                        val hasApiEvent= dayInfo.leDuongLich != null || dayInfo.leAmLich != null
                        val hasMyEvent = dayInfo.ngayDuong in daysWithEvent
                        val isSunday   = colIdx == 0

                        val bgColor = when {
                            isSelected -> TuViGold
                            isToday    -> TuViNavyLight
                            else       -> Color.Transparent
                        }
                        val solarColor = when {
                            isSelected -> TuViNavy
                            isSunday   -> TuViRed
                            else       -> TuViIvory
                        }
                        val lunarColor = when {
                            isSelected -> TuViNavy
                            hasApiEvent -> TuViGold
                            else       -> TuViIvoryDim
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(6.dp))
                                .background(bgColor)
                                .then(
                                    if (isToday && !isSelected)
                                        Modifier.border(1.dp, TuViGold, RoundedCornerShape(6.dp))
                                    else Modifier
                                )
                                .clickable { onDayClick(dayInfo) },
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    "${dayInfo.ngayDuong}",
                                    color = solarColor,
                                    fontSize = 14.sp,
                                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                )
                                Text("${dayInfo.ngayAm}", color = lunarColor, fontSize = 9.sp)
                            }
                            // Chấm xanh nếu có sự kiện cá nhân
                            if (hasMyEvent) {
                                Box(
                                    Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(2.dp)
                                        .size(5.dp)
                                        .background(TuViGoldLight, CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Day detail panel ─────────────────────────────────────────────────────────

@Composable
private fun DayDetail(
    day: NgayInfoDto,
    suKienList: List<SuKienEntity>,
    onAddClick: () -> Unit,
    onDelete: (SuKienEntity) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(TuViNavyCard, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Tiêu đề ngày
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    "${day.thu}, ${day.ngayDuong}/${day.thangDuong}/${day.namDuong}",
                    color = TuViGold, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                )
                Text(
                    stringResource(R.string.calendar_lunar_prefix, day.amLichText) +
                        if (day.thangNhuan) " ${stringResource(R.string.calendar_intercalary)}" else "",
                    color = TuViIvory, fontSize = 13.sp,
                )
            }
            IconButton(
                onClick = onAddClick,
                modifier = Modifier
                    .size(36.dp)
                    .background(TuViGold, CircleShape),
            ) {
                Icon(Icons.Default.Add, stringResource(R.string.calendar_cd_add_event), tint = TuViNavy)
            }
        }

        HorizontalDivider(color = TuViDivider, thickness = 0.5.dp)
        DetailRow(stringResource(R.string.calendar_label_day),          day.canChiNgay)
        DetailRow(stringResource(R.string.calendar_label_lunar_month),  day.canChiThang)
        DetailRow(stringResource(R.string.calendar_label_lunar_year),   day.canChiNam)

        // Trực, Lục Nhâm, Giờ Hoàng Đạo
        if (day.truc != null || day.lucNham != null || !day.gioHoangDao.isNullOrEmpty()) {
            HorizontalDivider(color = TuViDivider, thickness = 0.5.dp)
            day.truc?.let { truc ->
                val trucColor = if (truc.tot) TuViGold else TuViRedLight
                val trucLabel = if (truc.tot) stringResource(R.string.calendar_truc_good) else stringResource(R.string.calendar_truc_bad)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.calendar_label_truc), color = TuViIvoryDim, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(truc.ten, color = TuViIvory, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Box(
                            modifier = Modifier
                                .background(trucColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(trucLabel, color = trucColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            day.lucNham?.let { ln ->
                val lnColor = if (ln.hoangDao) TuViGold else TuViRedLight
                val lnLabel = if (ln.hoangDao) stringResource(R.string.calendar_hoang_dao) else stringResource(R.string.calendar_hac_dao)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.calendar_luc_nham_label), color = TuViIvoryDim, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(ln.ten, color = lnColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Box(
                            modifier = Modifier
                                .background(lnColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(lnLabel, color = lnColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            if (!day.gioHoangDao.isNullOrEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.calendar_hoang_dao_hours_label), color = TuViIvoryDim, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    day.gioHoangDao.forEach { gio ->
                        Box(
                            modifier = Modifier
                                .background(TuViGold.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(gio, color = TuViGold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Lễ cố định
        if (!day.leDuongLich.isNullOrBlank() || !day.leAmLich.isNullOrBlank()) {
            HorizontalDivider(color = TuViDivider, thickness = 0.5.dp)
            if (!day.leDuongLich.isNullOrBlank()) {
                EventChip(text = day.leDuongLich, dotColor = TuViRedLight)
            }
            if (!day.leAmLich.isNullOrBlank()) {
                EventChip(text = day.leAmLich, dotColor = TuViGold)
            }
        }

        // Sự kiện cá nhân
        if (suKienList.isNotEmpty()) {
            HorizontalDivider(color = TuViDivider, thickness = 0.5.dp)
            Text(stringResource(R.string.calendar_your_events), color = TuViGoldLight, fontSize = 12.sp)
            suKienList.forEach { sk ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TuViNavyLight, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (sk.alarmEpoch > 0) {
                                Icon(Icons.Default.Notifications, null, tint = TuViGold, modifier = Modifier.size(14.dp))
                            }
                            Text(sk.tieuDe, color = TuViIvory, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                        if (sk.ghiChu.isNotBlank()) {
                            Text(sk.ghiChu, color = TuViIvoryDim, fontSize = 11.sp)
                        }
                        if (sk.alarmEpoch > 0) {
                            val cal = java.util.Calendar.getInstance().apply { timeInMillis = sk.alarmEpoch }
                            Text(
                                stringResource(
                                    R.string.add_event_remind_at,
                                    "%02d".format(cal.get(java.util.Calendar.HOUR_OF_DAY)),
                                    "%02d".format(cal.get(java.util.Calendar.MINUTE)),
                                ),
                                color = TuViGoldLight, fontSize = 11.sp,
                            )
                        }
                    }
                    IconButton(onClick = { onDelete(sk) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, stringResource(R.string.btn_delete), tint = TuViRedLight, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

private val leadingEmojiRe = Regex("^[^\\p{L}\\p{N}\\p{P}]+\\s*")

@Composable
private fun EventChip(text: String, dotColor: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(6.dp).background(dotColor, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(leadingEmojiRe.replace(text, ""), color = dotColor, fontSize = 13.sp)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TuViIvoryDim,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = TuViIvory,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun thuToIndex(thu: String): Int = when (thu) {
    "Chủ nhật" -> 0; "Thứ hai" -> 1; "Thứ ba" -> 2; "Thứ tư" -> 3
    "Thứ năm"  -> 4; "Thứ sáu" -> 5; "Thứ bảy" -> 6; else -> 0
}
