package com.example.tuvi.ui.theme

import android.content.Context
import android.content.res.Configuration
import androidx.compose.ui.graphics.Color
import com.example.tuvi.R

/**
 * Toàn bộ màu Compose đọc từ [colors.xml].
 * [Color] là inline class — không dùng [lateinit] trực tiếp; lưu trong [AppColorPalette].
 */
object TuViComposeColors {

    @Volatile
    var ready: Boolean = false
        private set

    private var palette: AppColorPalette? = null
    private var lastNightUi: Boolean? = null

    val TuViNavy: Color get() = palette!!.tuViNavy
    val TuViNavyLight: Color get() = palette!!.tuViNavyLight
    val TuViNavyCard: Color get() = palette!!.tuViNavyCard
    val TuViGold: Color get() = palette!!.tuViGold
    val TuViGoldLight: Color get() = palette!!.tuViGoldLight
    val TuViGoldDark: Color get() = palette!!.tuViGoldDark
    val TuViRed: Color get() = palette!!.tuViRed
    val TuViRedLight: Color get() = palette!!.tuViRedLight
    val TuViIvory: Color get() = palette!!.tuViIvory
    val TuViIvoryDim: Color get() = palette!!.tuViIvoryDim
    val TuViDivider: Color get() = palette!!.tuViDivider

    val InputBgGradientBottom: Color get() = palette!!.inputBgGradientBottom
    val InputDatePickerSurface: Color get() = palette!!.inputDatePickerSurface
    val InputChartRed: Color get() = palette!!.inputChartRed
    val HomeBgGradientTop: Color get() = palette!!.homeBgGradientTop
    val HomeBgGradientBottom: Color get() = palette!!.homeBgGradientBottom
    val HomeCardGradientStart: Color get() = palette!!.homeCardGradientStart
    val HomeCardGradientMid: Color get() = palette!!.homeCardGradientMid
    val HomeCardGradientEnd: Color get() = palette!!.homeCardGradientEnd

    val ChartDeepBg: Color get() = palette!!.chartDeepBg
    val ChartNavy: Color get() = palette!!.chartNavy
    val ChartCardBg: Color get() = palette!!.chartCardBg
    val ChartGold: Color get() = palette!!.chartGold
    val ChartGoldDim: Color get() = palette!!.chartGoldDim
    val ChartIvory: Color get() = palette!!.chartIvory
    val ChartIvoryDim: Color get() = palette!!.chartIvoryDim
    val ChartRed: Color get() = palette!!.chartRed
    val ChartBorderGold: Color get() = palette!!.chartBorderGold
    val ChartLabelWeekOther: Color get() = palette!!.chartLabelWeekOther

    val HanhThuy: Color get() = palette!!.hanhThuy
    val HanhHoa: Color get() = palette!!.hanhHoa
    val HanhKim: Color get() = palette!!.hanhKim
    val HanhMoc: Color get() = palette!!.hanhMoc
    val HanhTho: Color get() = palette!!.hanhTho

    val IncognitoBg: Color get() = palette!!.incognitoBg
    val IncognitoCard: Color get() = palette!!.incognitoCard
    val IncognitoEmphasis: Color get() = palette!!.incognitoEmphasis
    val IncognitoMuted: Color get() = palette!!.incognitoMuted
    val IncognitoDivider: Color get() = palette!!.incognitoDivider
    val IncognitoDimDark: Color get() = palette!!.incognitoDimDark

    fun initIfNeeded(context: Context) {
        val app = context.applicationContext
        val isNight =
            (app.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
        synchronized(this) {
            if (ready && lastNightUi == isNight) return
            val c = app
            fun col(id: Int) = Color(c.getColor(id))

            palette = AppColorPalette(
                tuViNavy = col(R.color.tuvi_navy),
                tuViNavyLight = col(R.color.tuvi_navy_light),
                tuViNavyCard = col(R.color.tuvi_navy_card),
                tuViGold = col(R.color.tuvi_gold),
                tuViGoldLight = col(R.color.tuvi_gold_light),
                tuViGoldDark = col(R.color.tuvi_gold_dark),
                tuViRed = col(R.color.tuvi_red),
                tuViRedLight = col(R.color.tuvi_red_light),
                tuViIvory = col(R.color.tuvi_ivory),
                tuViIvoryDim = col(R.color.tuvi_ivory_dim),
                tuViDivider = col(R.color.tuvi_divider),
                inputBgGradientBottom = col(R.color.input_bg_gradient_bottom),
                inputDatePickerSurface = col(R.color.input_date_picker_surface),
                inputChartRed = col(R.color.input_chart_red),
                homeBgGradientTop = col(R.color.home_bg_gradient_top),
                homeBgGradientBottom = col(R.color.home_bg_gradient_bottom),
                homeCardGradientStart = col(R.color.home_card_gradient_start),
                homeCardGradientMid = col(R.color.home_card_gradient_mid),
                homeCardGradientEnd = col(R.color.home_card_gradient_end),
                chartDeepBg = col(R.color.chart_deep_bg),
                chartNavy = col(R.color.chart_navy),
                chartCardBg = col(R.color.chart_card_bg),
                chartGold = col(R.color.chart_gold),
                chartGoldDim = col(R.color.chart_gold_dim),
                chartIvory = col(R.color.chart_ivory),
                chartIvoryDim = col(R.color.chart_ivory_dim),
                chartRed = col(R.color.chart_red),
                chartBorderGold = col(R.color.chart_border_gold),
                chartLabelWeekOther = col(R.color.chart_label_week_other),
                hanhThuy = col(R.color.hanh_thuy),
                hanhHoa = col(R.color.hanh_hoa),
                hanhKim = col(R.color.hanh_kim),
                hanhMoc = col(R.color.hanh_moc),
                hanhTho = col(R.color.hanh_tho),
                incognitoBg = col(R.color.incognito_bg),
                incognitoCard = col(R.color.incognito_card),
                incognitoEmphasis = col(R.color.incognito_emphasis),
                incognitoMuted = col(R.color.incognito_muted),
                incognitoDivider = col(R.color.incognito_divider),
                incognitoDimDark = col(R.color.incognito_dim_dark),
            )
            lastNightUi = isNight
            ready = true
        }
    }
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
