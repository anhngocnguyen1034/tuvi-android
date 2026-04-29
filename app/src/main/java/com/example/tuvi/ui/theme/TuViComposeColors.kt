package com.example.tuvi.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * Hai palette dark/light hardcode trong Kotlin.
 * [current] là Compose MutableState — khi [setDark] thay đổi palette,
 * mọi Composable đang đọc màu từ đây sẽ tự recompose mà không cần restart app.
 */
object TuViComposeColors {

    private val darkPalette = AppColorPalette(
        tuViNavy             = Color(0xFF0D1B3E),
        tuViNavyLight        = Color(0xFF162450),
        tuViNavyCard         = Color(0xFF1C2D5E),
        tuViGold             = Color(0xFFD4A843),
        tuViGoldLight        = Color(0xFFF5CB6A),
        tuViGoldDark         = Color(0xFFAA8020),
        tuViRed              = Color(0xFFC0392B),
        tuViRedLight         = Color(0xFFE57369),
        tuViIvory            = Color(0xFFF5EED8),
        tuViIvoryDim         = Color(0xFFC5BAA0),
        tuViDivider          = Color(0xFF2E4080),
        inputBgGradientBottom    = Color(0xFF071330),
        inputDatePickerSurface   = Color(0xFF12082A),
        inputChartRed            = Color(0xFF8B0000),
        homeBgGradientTop        = Color(0xFF060E24),
        homeBgGradientBottom     = Color(0xFF0A1535),
        homeCardGradientStart    = Color(0xFF1E2D5E),
        homeCardGradientMid      = Color(0xFF0F1B3E),
        homeCardGradientEnd      = Color(0xFF1A2650),
        chartDeepBg          = Color(0xFF0F0510),
        chartNavy            = Color(0xFF12082A),
        chartCardBg          = Color(0xFF1C0D30),
        chartGold            = Color(0xFFD4AF37),
        chartGoldDim         = Color(0xFF8B7020),
        chartIvory           = Color(0xFFF5E6C8),
        chartIvoryDim        = Color(0xFFBBA080),
        chartRed             = Color(0xFF8B0000),
        chartBorderGold      = Color(0xFF5C3D0A),
        chartLabelWeekOther  = Color(0xFF2E1B6B),
        hanhThuy             = Color(0xFF4A90D9),
        hanhHoa              = Color(0xFFE84040),
        hanhKim              = Color(0xFFE8D5A3),
        hanhMoc              = Color(0xFF4CAF50),
        hanhTho              = Color(0xFFD4A017),
        incognitoBg          = Color(0xFF0D0D0D),
        incognitoCard        = Color(0xFF1C1C1C),
        incognitoEmphasis    = Color(0xFFE0E0E0),
        incognitoMuted       = Color(0xFF9E9E9E),
        incognitoDivider     = Color(0xFF2C2C2C),
        incognitoDimDark     = Color(0xFF757575),
    )

    private val lightPalette = AppColorPalette(
        tuViNavy             = Color(0xFFF2EFE8),
        tuViNavyLight        = Color(0xFFE8E4DA),
        tuViNavyCard         = Color(0xFFFFFFFF),
        tuViGold             = Color(0xFFB8860B),
        tuViGoldLight        = Color(0xFFD4A843),
        tuViGoldDark         = Color(0xFF8B6914),
        tuViRed              = Color(0xFFC0392B),
        tuViRedLight         = Color(0xFFE57369),
        tuViIvory            = Color(0xFF1C2238),
        tuViIvoryDim         = Color(0xFF4A5168),
        tuViDivider          = Color(0xFFC8C2B8),
        inputBgGradientBottom    = Color(0xFFE5E0D6),
        inputDatePickerSurface   = Color(0xFFFFFFFF),
        inputChartRed            = Color(0xFF8B0000),
        homeBgGradientTop        = Color(0xFFF8F4EC),
        homeBgGradientBottom     = Color(0xFFE8E2D6),
        homeCardGradientStart    = Color(0xFFFFFFFF),
        homeCardGradientMid      = Color(0xFFF0EBE3),
        homeCardGradientEnd      = Color(0xFFE4DDD2),
        chartDeepBg          = Color(0xFFF0ECF5),
        chartNavy            = Color(0xFFF5F1FA),
        chartCardBg          = Color(0xFFFFFFFF),
        chartGold            = Color(0xFFB8860B),
        chartGoldDim         = Color(0xFF8B6914),
        chartIvory           = Color(0xFF1E1A24),
        chartIvoryDim        = Color(0xFF5A5660),
        chartRed             = Color(0xFF8B0000),
        chartBorderGold      = Color(0xFFD4C4A8),
        chartLabelWeekOther  = Color(0xFFE0D8F0),
        hanhThuy             = Color(0xFF2E6BB5),
        hanhHoa              = Color(0xFFD32F2F),
        hanhKim              = Color(0xFFB8956A),
        hanhMoc              = Color(0xFF2E7D32),
        hanhTho              = Color(0xFFB28704),
        incognitoBg          = Color(0xFFF2F2F2),
        incognitoCard        = Color(0xFFFFFFFF),
        incognitoEmphasis    = Color(0xFF212121),
        incognitoMuted       = Color(0xFF616161),
        incognitoDivider     = Color(0xFFE0E0E0),
        incognitoDimDark     = Color(0xFF9E9E9E),
    )

    private var current by mutableStateOf(darkPalette)

    fun setDark(dark: Boolean) {
        current = if (dark) darkPalette else lightPalette
    }

    val TuViNavy: Color get() = current.tuViNavy
    val TuViNavyLight: Color get() = current.tuViNavyLight
    val TuViNavyCard: Color get() = current.tuViNavyCard
    val TuViGold: Color get() = current.tuViGold
    val TuViGoldLight: Color get() = current.tuViGoldLight
    val TuViGoldDark: Color get() = current.tuViGoldDark
    val TuViRed: Color get() = current.tuViRed
    val TuViRedLight: Color get() = current.tuViRedLight
    val TuViIvory: Color get() = current.tuViIvory
    val TuViIvoryDim: Color get() = current.tuViIvoryDim
    val TuViDivider: Color get() = current.tuViDivider

    val InputBgGradientBottom: Color get() = current.inputBgGradientBottom
    val InputDatePickerSurface: Color get() = current.inputDatePickerSurface
    val InputChartRed: Color get() = current.inputChartRed
    val HomeBgGradientTop: Color get() = current.homeBgGradientTop
    val HomeBgGradientBottom: Color get() = current.homeBgGradientBottom
    val HomeCardGradientStart: Color get() = current.homeCardGradientStart
    val HomeCardGradientMid: Color get() = current.homeCardGradientMid
    val HomeCardGradientEnd: Color get() = current.homeCardGradientEnd

    val ChartDeepBg: Color get() = current.chartDeepBg
    val ChartNavy: Color get() = current.chartNavy
    val ChartCardBg: Color get() = current.chartCardBg
    val ChartGold: Color get() = current.chartGold
    val ChartGoldDim: Color get() = current.chartGoldDim
    val ChartIvory: Color get() = current.chartIvory
    val ChartIvoryDim: Color get() = current.chartIvoryDim
    val ChartRed: Color get() = current.chartRed
    val ChartBorderGold: Color get() = current.chartBorderGold
    val ChartLabelWeekOther: Color get() = current.chartLabelWeekOther

    val HanhThuy: Color get() = current.hanhThuy
    val HanhHoa: Color get() = current.hanhHoa
    val HanhKim: Color get() = current.hanhKim
    val HanhMoc: Color get() = current.hanhMoc
    val HanhTho: Color get() = current.hanhTho

    val IncognitoBg: Color get() = current.incognitoBg
    val IncognitoCard: Color get() = current.incognitoCard
    val IncognitoEmphasis: Color get() = current.incognitoEmphasis
    val IncognitoMuted: Color get() = current.incognitoMuted
    val IncognitoDivider: Color get() = current.incognitoDivider
    val IncognitoDimDark: Color get() = current.incognitoDimDark
}

private data class AppColorPalette(
    val tuViNavy: Color,
    val tuViNavyLight: Color,
    val tuViNavyCard: Color,
    val tuViGold: Color,
    val tuViGoldLight: Color,
    val tuViGoldDark: Color,
    val tuViRed: Color,
    val tuViRedLight: Color,
    val tuViIvory: Color,
    val tuViIvoryDim: Color,
    val tuViDivider: Color,
    val inputBgGradientBottom: Color,
    val inputDatePickerSurface: Color,
    val inputChartRed: Color,
    val homeBgGradientTop: Color,
    val homeBgGradientBottom: Color,
    val homeCardGradientStart: Color,
    val homeCardGradientMid: Color,
    val homeCardGradientEnd: Color,
    val chartDeepBg: Color,
    val chartNavy: Color,
    val chartCardBg: Color,
    val chartGold: Color,
    val chartGoldDim: Color,
    val chartIvory: Color,
    val chartIvoryDim: Color,
    val chartRed: Color,
    val chartBorderGold: Color,
    val chartLabelWeekOther: Color,
    val hanhThuy: Color,
    val hanhHoa: Color,
    val hanhKim: Color,
    val hanhMoc: Color,
    val hanhTho: Color,
    val incognitoBg: Color,
    val incognitoCard: Color,
    val incognitoEmphasis: Color,
    val incognitoMuted: Color,
    val incognitoDivider: Color,
    val incognitoDimDark: Color,
)
