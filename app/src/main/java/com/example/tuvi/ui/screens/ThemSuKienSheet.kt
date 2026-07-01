package com.example.tuvi.ui.screens

import android.Manifest
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.tuvi.R
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
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Refresh trạng thái khi quay lại từ màn Settings hệ thống
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                            PackageManager.PERMISSION_GRANTED
                } else true
                notifGranted = granted
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notifPermPrefs = remember {
        context.getSharedPreferences("notif_perm_prefs", Context.MODE_PRIVATE)
    }

    // Launcher xin quyền POST_NOTIFICATIONS (Android 13+)
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notifGranted = granted
        nhacNho = granted   // chỉ bật khi được cấp quyền
        notifPermPrefs.edit().putBoolean("asked", true).apply()
        if (!granted) {
            val act = context as? Activity
            val rationale = act != null && ActivityCompat
                .shouldShowRequestPermissionRationale(act, Manifest.permission.POST_NOTIFICATIONS)
            // Sau khi launcher trả về mà cả granted=false và rationale=false ⇒ "Don't ask again"
            if (!rationale) showSettingsDialog = true
        }
    }

    fun requestNotifPerm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notifGranted) {
            val act = context as? Activity
            val askedBefore = notifPermPrefs.getBoolean("asked", false)
            val rationale = act != null && ActivityCompat
                .shouldShowRequestPermissionRationale(act, Manifest.permission.POST_NOTIFICATIONS)
            // Đã hỏi trước đó, hệ thống không cho phép xin lại (Don't ask again / quá số lần)
            if (askedBefore && !rationale) {
                showSettingsDialog = true
                return
            }
            permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
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
                stringResource(R.string.add_event_sheet_title, ngay, thang, nam, thuText),
                color = TuViGold,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )

            // ── Input tiêu đề ───────────────────────────────────────────────
            OutlinedTextField(
                value = tieuDe,
                onValueChange = { tieuDe = it },
                label = { Text(stringResource(R.string.add_event_label_title), color = TuViIvoryDim) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = outlinedTextFieldColors(),
            )

            // ── Input ghi chú ───────────────────────────────────────────────
            OutlinedTextField(
                value = ghiChu,
                onValueChange = { ghiChu = it },
                label = { Text(stringResource(R.string.add_event_label_note), color = TuViIvoryDim) },
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
                        painter = painterResource(R.drawable.ic_notification),
                        contentDescription = null,
                        tint = if (nhacNho) TuViGold else TuViIvoryDim,
                    )
                    Text(
                        stringResource(R.string.add_event_reminder),
                        color = if (nhacNho) TuViIvory else TuViIvoryDim,
                        fontSize = 14.sp,
                    )
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
                    Text(stringResource(R.string.add_event_alarm_time), color = TuViIvory, fontSize = 14.sp)
                    Text(
                        "%02d:%02d".format(alarmHour, alarmMinute),
                        color = TuViGold,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                    )
                }
            }

            // ── Nút Lưu ────────────────────────────────────────────────────
            if (showSettingsDialog) {
                AlertDialog(
                    onDismissRequest = { showSettingsDialog = false },
                    containerColor = TuViNavyCard,
                    titleContentColor = TuViGold,
                    textContentColor = TuViIvory,
                    title = { Text(stringResource(R.string.notif_perm_blocked_title), fontWeight = FontWeight.Bold) },
                    text = { Text(stringResource(R.string.notif_perm_blocked_message), fontSize = 14.sp) },
                    confirmButton = {
                        TextButton(onClick = {
                            showSettingsDialog = false
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }) {
                            Text(stringResource(R.string.notif_perm_open_settings), color = TuViGold, fontWeight = FontWeight.SemiBold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSettingsDialog = false }) {
                            Text(stringResource(R.string.notif_perm_cancel), color = TuViIvoryDim)
                        }
                    },
                )
            }

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
                Text(stringResource(R.string.add_event_btn_save), fontWeight = FontWeight.Bold)
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
