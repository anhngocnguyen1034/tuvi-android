package com.example.tuvi.ui.screens

import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tuvi.R
import com.example.tuvi.domain.model.AuthUser
import com.example.tuvi.presentation.SettingsViewModel
import com.example.tuvi.ui.components.GenZThemeSwitch
import com.example.tuvi.ui.theme.TuViGold
import com.example.tuvi.ui.theme.TuViGoldDark
import com.example.tuvi.ui.theme.TuViGoldLight
import com.example.tuvi.ui.theme.TuViIvory
import com.example.tuvi.ui.theme.TuViIvoryDim
import com.example.tuvi.ui.theme.TuViNavy
import com.example.tuvi.ui.theme.TuViNavyCard
import com.example.tuvi.ui.theme.TuViNavyLight
import com.example.tuvi.ui.theme.TuViRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    authUser: AuthUser? = null,
    onSignOut: () -> Unit = {},
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
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scroll = rememberScrollState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = TuViNavy,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.settings_title),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    scrolledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                    titleContentColor = TuViIvory,
                    navigationIconContentColor = TuViGold
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                checked = state.notifHoliday,
                onCheckedChange = { viewModel.setNotifHoliday(it) }
            )
            NotificationToggleRow(
                title = stringResource(R.string.settings_notif_lunar_title),
                desc = stringResource(R.string.settings_notif_lunar_desc),
                checked = state.notifLunar,
                onCheckedChange = { viewModel.setNotifLunar(it) }
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
                iconRes = R.drawable.ic_rate_app,
                title = stringResource(R.string.settings_rate_title),
                desc = stringResource(R.string.settings_rate_desc),
                onClick = onRateApp
            )
            AboutActionRow(
                iconRes = R.drawable.ic_feedback,
                title = stringResource(R.string.settings_feedback_title),
                desc = stringResource(R.string.settings_feedback_desc),
                onClick = onOpenFeedback
            )

            Text(
                text = stringResource(R.string.settings_footer_hint),
                color = TuViIvoryDim,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                modifier = Modifier.padding(top = 12.dp)
            )

            if (authUser != null) {
                Spacer(Modifier.height(8.dp))
                LogoutButton(onClick = onSignOut)
            }
        }
    }
}

@Composable
private fun LogoutButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = TuViRed.copy(alpha = 0.18f),
            contentColor = TuViRed,
        ),
        border = BorderStroke(1.dp, TuViRed.copy(alpha = 0.55f)),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_logout),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = stringResource(R.string.settings_logout),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )
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
                    painter = painterResource(R.drawable.ic_saved),
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
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
            tint = TuViGoldLight,
            modifier = Modifier
                .size(18.dp)
                .rotate(180f)
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
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
            tint = TuViGoldLight,
            modifier = Modifier
                .size(18.dp)
                .rotate(180f)
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
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
            tint = TuViGoldLight,
            modifier = Modifier
                .size(18.dp)
                .rotate(180f)
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

