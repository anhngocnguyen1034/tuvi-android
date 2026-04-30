package com.example.tuvi.ui.screens

import android.app.Application
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
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
                // Sun/Moon icon
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
                        text = if (isDark) stringResource(R.string.settings_dark_icon)
                               else stringResource(R.string.settings_light_icon),
                        fontSize = 22.sp
                    )
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

            GenZThemeSwitch(
                isDarkTheme = isDark,
                onToggle = { onToggle(!isDark) }
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

@Composable
private fun GenZThemeSwitch(
    isDarkTheme: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 64.dp,
    height: Dp = 36.dp
) {
    val thumbOffsetAnim by animateDpAsState(
        targetValue = if (isDarkTheme) width - height else 0.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label = "thumb"
    )
    val trackColorAnim by animateColorAsState(
        targetValue = if (isDarkTheme) Color(0xFF2D2D2D) else Color(0xFF00E5FF),
        label = "track"
    )
    val thumbColorAnim by animateColorAsState(
        targetValue = if (isDarkTheme) Color(0xFFDDDDDD) else Color(0xFFFFE500),
        label = "thumbColor"
    )
    val starAlphaAnim by animateFloatAsState(
        targetValue = if (isDarkTheme) 1f else 0f,
        label = "star"
    )

    val borderColor = if (isDarkTheme) Color(0xFF444444) else Color(0xFFE0E0E0)

    Canvas(
        modifier = modifier
            .size(width, height)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            )
    ) {
        val cornerRadius = size.height / 2

        drawRoundRect(
            color = trackColorAnim,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
            style = Fill
        )
        drawRoundRect(
            color = borderColor,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
            style = Stroke(width = 1.dp.toPx())
        )

        if (starAlphaAnim > 0f) {
            val starColor = Color.White.copy(alpha = starAlphaAnim)
            drawSparkle(Offset(size.width * 0.25f, size.height * 0.35f), 3.dp.toPx(), starColor)
            drawSparkle(Offset(size.width * 0.5f, size.height * 0.7f), 2.dp.toPx(), starColor)
        }

        val thumbX = thumbOffsetAnim.toPx() + (size.height / 2)
        val thumbCenter = Offset(thumbX, size.height / 2)
        val thumbRadius = (size.height / 2) - 4.dp.toPx()

        if (isDarkTheme) {
            drawCrescentMoon(thumbCenter, thumbRadius, thumbColorAnim)
        } else {
            drawSun(
                center = thumbCenter,
                coreRadius = thumbRadius * 0.6f,
                rayLength = thumbRadius * 0.35f,
                rayWidth = 2.dp.toPx(),
                rayGap = 2.dp.toPx(),
                color = thumbColorAnim,
                rotationDegrees = 0f
            )
        }
    }
}

private fun DrawScope.drawCrescentMoon(center: Offset, radius: Float, color: Color) {
    val path = Path().apply {
        addOval(Rect(center, radius))
        val cutPath = Path().apply {
            addOval(
                Rect(
                    center = Offset(center.x + radius * 0.35f, center.y - radius * 0.1f),
                    radius = radius
                )
            )
        }
        op(this, cutPath, PathOperation.Difference)
    }
    drawPath(path, color)
}

private fun DrawScope.drawSun(
    center: Offset,
    coreRadius: Float,
    rayLength: Float,
    rayWidth: Float,
    rayGap: Float,
    color: Color,
    numRays: Int = 8,
    rotationDegrees: Float
) {
    rotate(rotationDegrees, center) {
        drawCircle(color, coreRadius, center)
        val step = 360f / numRays
        repeat(numRays) { i ->
            rotate(i * step, center) {
                drawLine(
                    color = color,
                    start = Offset(center.x, center.y - (coreRadius + rayGap)),
                    end = Offset(center.x, center.y - (coreRadius + rayGap + rayLength)),
                    strokeWidth = rayWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

private fun DrawScope.drawSparkle(center: Offset, size: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y - size)
        quadraticTo(center.x, center.y, center.x + size, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + size)
        quadraticTo(center.x, center.y, center.x - size, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - size)
    }
    drawPath(path, color)
}
