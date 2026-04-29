package com.example.tuvi.ui.screens

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tuvi.R
import com.example.tuvi.data.preferences.UserPreferencesRepository
import com.example.tuvi.presentation.SettingsViewModel
import com.example.tuvi.ui.theme.TuViGold
import com.example.tuvi.ui.theme.TuViGoldDark
import com.example.tuvi.ui.theme.TuViGoldLight
import com.example.tuvi.ui.theme.TuViIvory
import com.example.tuvi.ui.theme.TuViIvoryDim
import com.example.tuvi.ui.theme.TuViNavy
import com.example.tuvi.ui.theme.TuViNavyCard
import com.example.tuvi.ui.theme.TuViNavyLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
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
                    containerColor = TuViNavy,
                    titleContentColor = TuViIvory,
                    navigationIconContentColor = TuViGold
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .navigationBarsPadding()
                .fillMaxSize()
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
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ThemeChip(
                    label = stringResource(R.string.settings_lang_vi),
                    selected = state.localeTag == UserPreferencesRepository.LOCALE_VI,
                    onClick = { viewModel.setLocaleTag(UserPreferencesRepository.LOCALE_VI) }
                )
                ThemeChip(
                    label = stringResource(R.string.settings_lang_en),
                    selected = state.localeTag == UserPreferencesRepository.LOCALE_EN,
                    onClick = { viewModel.setLocaleTag(UserPreferencesRepository.LOCALE_EN) }
                )
            }

            Text(
                text = stringResource(R.string.settings_footer_hint),
                color = TuViIvoryDim,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                modifier = Modifier.padding(top = 12.dp)
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
            // Icon + label group
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Sun/Moon icon circle
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isDark)
                                Brush.radialGradient(
                                    listOf(TuViNavy.copy(alpha = 0.8f), TuViNavyCard)
                                )
                            else
                                Brush.radialGradient(
                                    listOf(TuViGold.copy(alpha = 0.25f), TuViNavyCard)
                                )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isDark) stringResource(R.string.settings_dark_icon) else stringResource(
                            R.string.settings_light_icon
                        ),
                        fontSize = 20.sp
                    )
                    Icon(painter = painterResource(R.drawable.ic_clock_light), null)
                }
                Spacer(Modifier.size(14.dp))
                Column {
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
            }

            Switch(
                checked = isDark,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = TuViNavy,
                    checkedTrackColor = TuViGold,
                    checkedBorderColor = TuViGoldDark,
                    uncheckedThumbColor = TuViGoldLight,
                    uncheckedTrackColor = TuViNavyLight,
                    uncheckedBorderColor = TuViGoldDark.copy(alpha = 0.4f)
                )
            )
        }
    }
}

@Composable
private fun ThemeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 13.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = TuViGold.copy(alpha = 0.35f),
            selectedLabelColor = TuViGold,
            labelColor = TuViIvoryDim,
            containerColor = TuViNavyLight
        )
    )
}
