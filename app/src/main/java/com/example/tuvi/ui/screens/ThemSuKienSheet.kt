package com.example.tuvi.ui.screens

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.tuvi.ui.theme.*
import java.util.Calendar

/**
 * Bottom-sheet để thêm sự kiện vào một ngày.
 *
 * @param ngay / thang / nam  Ngày dương lịch đang chọn
 * @param thuText             Tên thứ trong tuần để hiển thị
 * @param onDismiss           Đóng sheet
 * @param onSave              (tieuDe, ghiChu, alarmEpoch) → lưu
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemSuKienSheet(
    ngay: Int,
    thang: Int,
    nam: Int,
    thuText: String,
    onDismiss: () -> Unit,
    onSave: (tieuDe: String, ghiChu: String, alarmEpoch: Long) -> Unit,
) {
    val context = LocalContext.current

    var tieuDe by remember { mutableStateOf("") }
    var ghiChu by remember { mutableStateOf("") }
    var nhacNho by remember { mutableStateOf(false) }
    // Giờ nhắc mặc định = 8:00 sáng của ngày sự kiện
    var alarmHour   by remember { mutableIntStateOf(8) }
    var alarmMinute by remember { mutableIntStateOf(0) }

    // Kiểm tra xem quyền thông báo đã được cấp chưa
    val hasNotifPerm = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
        } else true
    }
    var notifGranted by remember { mutableStateOf(hasNotifPerm) }

    // Launcher xin quyền POST_NOTIFICATIONS (Android 13+)
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notifGranted = granted
        nhacNho = true   // bật nhắc dù user từ chối (vẫn lưu, chỉ không có alarm)
    }

    fun requestNotifPerm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!notifGranted) {
                permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        nhacNho = true
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = TuViNavyCard,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // ── Tiêu đề sheet ──────────────────────────────────────────────
            Text(
                "Thêm sự kiện  $ngay/$thang/$nam ($thuText)",
                color = TuViGold,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )

            // ── Input tiêu đề ───────────────────────────────────────────────
            OutlinedTextField(
                value = tieuDe,
                onValueChange = { tieuDe = it },
                label = { Text("Tiêu đề *", color = TuViIvoryDim) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = outlinedTextFieldColors(),
            )

            // ── Input ghi chú ───────────────────────────────────────────────
            OutlinedTextField(
                value = ghiChu,
                onValueChange = { ghiChu = it },
                label = { Text("Ghi chú", color = TuViIvoryDim) },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
                colors = outlinedTextFieldColors(),
            )

            // ── Toggle nhắc nhở ─────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(TuViNavyLight)
                    .clickable {
                        if (!nhacNho) requestNotifPerm() else nhacNho = false
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = if (nhacNho) TuViGold else TuViIvoryDim,
                    )
                    Column {
                        Text(
                            "Nhắc nhở",
                            color = if (nhacNho) TuViIvory else TuViIvoryDim,
                            fontSize = 14.sp,
                        )
                        if (nhacNho && !notifGranted) {
                            Text(
                                "Quyền thông báo bị từ chối – sự kiện vẫn được lưu",
                                color = TuViRedLight,
                                fontSize = 11.sp,
                            )
                        }
                    }
                }
                Switch(
                    checked = nhacNho,
                    onCheckedChange = { on ->
                        if (on) requestNotifPerm() else nhacNho = false
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = TuViNavy,
                        checkedTrackColor = TuViGold,
                        uncheckedThumbColor = TuViIvoryDim,
                        uncheckedTrackColor = TuViNavy,
                    ),
                )
            }

            // ── Chọn giờ nhắc (chỉ hiện khi bật nhắc nhở) ─────────────────
            if (nhacNho) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(TuViNavyLight)
                        .clickable {
                            TimePickerDialog(
                                context,
                                { _, h, m -> alarmHour = h; alarmMinute = m },
                                alarmHour, alarmMinute, true,
                            ).show()
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Giờ nhắc", color = TuViIvory, fontSize = 14.sp)
                    Text(
                        "%02d:%02d".format(alarmHour, alarmMinute),
                        color = TuViGold,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                    )
                }
            }

            // ── Nút Lưu ────────────────────────────────────────────────────
            Button(
                onClick = {
                    if (tieuDe.isBlank()) return@Button
                    val epoch = if (nhacNho && notifGranted) {
                        Calendar.getInstance().apply {
                            set(nam, thang - 1, ngay, alarmHour, alarmMinute, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                    } else 0L
                    onSave(tieuDe, ghiChu, epoch)
                    onDismiss()
                },
                enabled = tieuDe.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TuViGold,
                    contentColor = TuViNavy,
                    disabledContainerColor = TuViNavyLight,
                    disabledContentColor = TuViIvoryDim,
                ),
            ) {
                Text("Lưu sự kiện", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = TuViGold,
    unfocusedBorderColor = TuViDivider,
    focusedTextColor     = TuViIvory,
    unfocusedTextColor   = TuViIvory,
    cursorColor          = TuViGold,
)
