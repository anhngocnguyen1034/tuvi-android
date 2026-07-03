package com.example.tuvi.ui.screens

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tuvi.R
import com.example.tuvi.presentation.SettingsViewModel
import com.example.tuvi.ui.components.GenZThemeSwitch
import com.example.tuvi.ui.components.TuViTopBar
import com.example.tuvi.ui.theme.TuViGold
import com.example.tuvi.ui.theme.TuViGoldDark
import com.example.tuvi.ui.theme.TuViGoldLight
import com.example.tuvi.ui.theme.TuViIvory
import com.example.tuvi.ui.theme.TuViIvoryDim
import com.example.tuvi.ui.theme.TuViNavy
import com.example.tuvi.ui.theme.TuViNavyCard
import com.example.tuvi.ui.theme.TuViNavyLight
import com.example.tuvi.ui.theme.TuViRed

private enum class NotificationPreference {
    Holiday,
    Lunar,
}

private fun hasPostNotificationPermission(context: Context): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
}

private fun shouldShowPostNotificationRationale(context: Context): Boolean {
    val activity = context as? Activity ?: return false
    return ActivityCompat.shouldShowRequestPermissionRationale(
        activity,
        Manifest.permission.POST_NOTIFICATIONS
    )
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenSaved: () -> Unit = {},
    onOpenLanguage: () -> Unit = {},
    onOpenPrivacy: () -> Unit = {},
    onOpenFeedback: () -> Unit = {},
    onRateApp: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel(
        factory = AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scroll = rememberScrollState()
    val notifPermPrefs = remember(context) {
        context.getSharedPreferences("notif_perm_prefs", Context.MODE_PRIVATE)
    }

    var notifGranted by remember { mutableStateOf(hasPostNotificationPermission(context)) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var pendingNotifTarget by remember { mutableStateOf<NotificationPreference?>(null) }
    var openedNotificationSettings by remember { mutableStateOf(false) }

    fun applyNotificationPreference(target: NotificationPreference, enabled: Boolean) {
        when (target) {
            NotificationPreference.Holiday -> viewModel.setNotifHoliday(enabled)
            NotificationPreference.Lunar -> viewModel.setNotifLunar(enabled)
        }
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val granted = hasPostNotificationPermission(context)
                notifGranted = granted
                if (openedNotificationSettings) {
                    openedNotificationSettings = false
                    val target = pendingNotifTarget
                    if (granted && target != null) {
                        applyNotificationPreference(target, true)
                    }
                    pendingNotifTarget = null
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notifGranted = granted
        notifPermPrefs.edit().putBoolean("asked", true).apply()
        val target = pendingNotifTarget
        if (granted && target != null) {
            applyNotificationPreference(target, true)
            pendingNotifTarget = null
        } else if (!granted) {
            val rationale = shouldShowPostNotificationRationale(context)
            if (!rationale) {
                showSettingsDialog = true
            } else {
                pendingNotifTarget = null
            }
        }
    }

    fun dismissNotificationSettingsDialog() {
        showSettingsDialog = false
        pendingNotifTarget = null
    }

    fun setNotificationPreference(target: NotificationPreference, enabled: Boolean) {
        if (!enabled) {
            applyNotificationPreference(target, false)
            return
        }

        if (hasPostNotificationPermission(context)) {
            notifGranted = true
            applyNotificationPreference(target, true)
            return
        }

        pendingNotifTarget = target
        val askedBefore = notifPermPrefs.getBoolean("asked", false)
        val rationale = shouldShowPostNotificationRationale(context)
        if (askedBefore && !rationale) {
            showSettingsDialog = true
            return
        }

        permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = ::dismissNotificationSettingsDialog,
            containerColor = TuViNavyCard,
            titleContentColor = TuViGold,
            textContentColor = TuViIvory,
            title = {
                Text(
                    text = stringResource(R.string.notif_perm_blocked_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.notif_perm_blocked_message),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showSettingsDialog = false
                    openedNotificationSettings = true
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }) {
                    Text(
                        text = stringResource(R.string.notif_perm_open_settings),
                        color = TuViGold,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = ::dismissNotificationSettingsDialog) {
                    Text(
                        text = stringResource(R.string.notif_perm_cancel),
                        color = TuViIvoryDim
                    )
                }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TuViNavy)
    ) {
        TuViTopBar(
            title = stringResource(R.string.settings_title),
            onBack = onBack,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scroll)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_theme),
                color = TuViGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp
            )
            ThemeSwitchRow(
                isDark = state.themeDark,
                onToggle = { viewModel.setThemeDark(it) }
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.settings_language),
                color = TuViGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp
            )
            LanguageRow(onClick = onOpenLanguage)

            Text(
                text = stringResource(R.string.settings_notification_section),
                color = TuViGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp
            )
            NotificationToggleRow(
                title = stringResource(R.string.settings_notif_holiday_title),
                desc = stringResource(R.string.settings_notif_holiday_desc),
                checked = state.notifHoliday && notifGranted,
                onCheckedChange = { setNotificationPreference(NotificationPreference.Holiday, it) }
            )
            NotificationToggleRow(
                title = stringResource(R.string.settings_notif_lunar_title),
                desc = stringResource(R.string.settings_notif_lunar_desc),
                checked = state.notifLunar && notifGranted,
                onCheckedChange = { setNotificationPreference(NotificationPreference.Lunar, it) }
            )

            Text(
                text = stringResource(R.string.settings_data_section),
                color = TuViGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp
            )
            SavedChartsRow(onClick = onOpenSaved)

            Text(
                text = stringResource(R.string.settings_about_section),
                color = TuViGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp
            )
            PrivacyPolicyRow(onClick = onOpenPrivacy)
            AboutActionRow(
                iconRes = R.drawable.ic_favorite,
                title = stringResource(R.string.settings_rate_title),
                desc = stringResource( R.string.settings_rate_desc),
                onClick = onRateApp
            )
            AboutActionRow(
                iconRes = R.drawable.ic_feedback,
                title = stringResource(R.string.settings_feedback_title),
                desc = stringResource(R.string.settings_feedback_desc),
                onClick = onOpenFeedback
            )

        }
    }
}

@Composable
private fun ThemeSwitchRow(isDark: Boolean, onToggle: (Boolean) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(TuViNavyLight.copy(alpha = 0.9f), TuViNavyCard.copy(alpha = 0.85f))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(TuViGold.copy(alpha = 0.35f), TuViGoldDark.copy(alpha = 0.15f))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isDark) stringResource(R.string.settings_theme_dark)
                    else stringResource(R.string.settings_theme_light),
                    color = TuViIvory,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (isDark) stringResource(R.string.settings_dark_mode_desc) else stringResource(
                        R.string.settings_light_mode_desc
                    ),
                    color = TuViIvoryDim,
                    fontSize = 12.sp
                )
            }
            GenZThemeSwitch(
                isDarkTheme = isDark,
                onToggle = { onToggle(!isDark) }
            )
        }
    }
}

@Composable
private fun SavedChartsRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(TuViNavyLight.copy(alpha = 0.9f), TuViNavyCard.copy(alpha = 0.85f))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(TuViGold.copy(alpha = 0.35f), TuViGoldDark.copy(alpha = 0.15f))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.radialGradient(listOf(TuViGold.copy(alpha = 0.15f), TuViNavyCard))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_folder),
                    contentDescription = null,
                    tint = TuViGold,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.size(14.dp))
            Column {
                Text(
                    text = stringResource(R.string.settings_saved_title),
                    color = TuViIvory,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.settings_saved_desc),
                    color = TuViIvoryDim,
                    fontSize = 12.sp
                )
            }
        }
        Icon(
            painter = painterResource(R.drawable.ic_right_forward),
            contentDescription = null,
            tint = TuViGoldLight,
            modifier = Modifier
                .size(18.dp)

        )
    }
}

@Composable
private fun NotificationToggleRow(
    title: String,
    desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(TuViNavyLight.copy(alpha = 0.9f), TuViNavyCard.copy(alpha = 0.85f))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(TuViGold.copy(alpha = 0.35f), TuViGoldDark.copy(alpha = 0.15f))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TuViIvory,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = desc,
                    color = TuViIvoryDim,
                    fontSize = 12.sp
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = TuViNavy,
                    checkedTrackColor = TuViGold,
                    uncheckedThumbColor = TuViIvoryDim,
                    uncheckedTrackColor = TuViNavyLight,
                    uncheckedBorderColor = TuViGold.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
private fun LanguageRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(TuViNavyLight.copy(alpha = 0.9f), TuViNavyCard.copy(alpha = 0.85f))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(TuViGold.copy(alpha = 0.35f), TuViGoldDark.copy(alpha = 0.15f))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.radialGradient(listOf(TuViGold.copy(alpha = 0.15f), TuViNavyCard))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_change_language),
                    contentDescription = null,
                    tint = TuViGold,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.size(14.dp))
            Column {
                Text(
                    text = stringResource(R.string.settings_language),
                    color = TuViIvory,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.settings_language_desc),
                    color = TuViIvoryDim,
                    fontSize = 12.sp
                )
            }
        }
        Icon(
            painter = painterResource(R.drawable.ic_right_forward),
            contentDescription = null,
            tint = TuViGoldLight,
            modifier = Modifier
                .size(18.dp)
        )
    }
}

@Composable
private fun AboutActionRow(
    iconRes: Int,
    title: String,
    desc: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(TuViNavyLight.copy(alpha = 0.9f), TuViNavyCard.copy(alpha = 0.85f))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(TuViGold.copy(alpha = 0.35f), TuViGoldDark.copy(alpha = 0.15f))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.radialGradient(listOf(TuViGold.copy(alpha = 0.15f), TuViNavyCard))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = TuViGold,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.size(14.dp))
            Column {
                Text(
                    text = title,
                    color = TuViIvory,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = desc,
                    color = TuViIvoryDim,
                    fontSize = 12.sp
                )
            }
        }
        Icon(
            painter = painterResource(R.drawable.ic_right_forward),
            contentDescription = null,
            tint = TuViGoldLight,
            modifier = Modifier
                .size(18.dp)
        )
    }
}

@Composable
private fun PrivacyPolicyRow(onClick: () -> Unit) {
    AboutActionRow(
        iconRes = R.drawable.ic_privacy_policy,
        title = stringResource(R.string.settings_privacy_title),
        desc = stringResource(R.string.settings_privacy_desc),
        onClick = onClick
    )
}
