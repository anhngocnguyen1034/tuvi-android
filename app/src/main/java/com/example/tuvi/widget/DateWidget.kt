package com.example.tuvi.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.Row
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.tuvi.R
import com.example.tuvi.di.AppContainer
import com.example.tuvi.domain.model.Quote
import java.util.Calendar
import kotlin.random.Random

/**
 * Widget lịch: Dương lịch (thứ, ngày/tháng/năm) phía trên, Âm lịch phía dưới,
 * và một câu danh ngôn ở dưới cùng. Chạm để đổi câu / làm mới ngày.
 */
class DateWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val quote = loadQuote(context)
        val today = todaySnapshot()
        provideContent {
            GlanceTheme {
                DateContent(today, quote)
            }
        }
    }

    private suspend fun loadQuote(context: Context): Quote? {
        val quotes = AppContainer.getQuotesUseCase().getOrNull().orEmpty()
        if (quotes.isEmpty()) return null
        val pinnedId = QuoteWidgetStore.getQuoteId(context)
        return quotes.firstOrNull { it.id == pinnedId } ?: quotes[Random.nextInt(quotes.size)]
    }

    private fun todaySnapshot(): TodayInfo {
        val c = Calendar.getInstance()
        val day = c.get(Calendar.DAY_OF_MONTH)
        val month = c.get(Calendar.MONTH) + 1
        val year = c.get(Calendar.YEAR)
        val weekday = WEEKDAYS[c.get(Calendar.DAY_OF_WEEK) - 1]
        val lunar = LunarCalendar.fromSolar(day, month, year)
        val auspicious = LunarCalendar.isAuspicious(day, month, year)
        return TodayInfo(weekday, day, month, year, lunar, auspicious)
    }
}

private data class TodayInfo(
    val weekday: String,
    val day: Int,
    val month: Int,
    val year: Int,
    val lunar: LunarCalendar.Lunar,
    val auspicious: Boolean,
)

/** Thứ Hai..Chủ Nhật theo Calendar.DAY_OF_WEEK (1 = Chủ Nhật). */
private val WEEKDAYS = arrayOf(
    "Chủ Nhật", "Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy"
)

private val TextMain = ColorProvider(Color(0xFF1A1A2E))
private val TextDim = ColorProvider(Color(0xFF6B6B7B))
private val DividerColor = ColorProvider(Color(0x22000000))
private val GoodGreen = ColorProvider(Color(0xFF2E9E4F))
private val BadRed = ColorProvider(Color(0xFFE53935))

private val ImageProviderBackground
    get() = ImageProvider(R.drawable.date_widget_background)

@androidx.compose.runtime.Composable
private fun DateContent(info: TodayInfo, quote: Quote?) {
    val height = LocalSize.current.height
    val compact = height < 160.dp
    val showQuote = height >= 150.dp
    val quoteLines = when {
        height < 220.dp -> 2
        height < 300.dp -> 4
        else -> 6
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProviderBackground)
            .clickable(actionRunCallback<RefreshDateAction>())
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // --- Ngày tốt / xấu (Hoàng đạo / Hắc đạo) ---
        val quality = if (info.auspicious) GoodGreen else BadRed
        val qualityIcon = if (info.auspicious) R.drawable.ic_happy else R.drawable.ic_sad
        val qualityText = if (info.auspicious) "Ngày tốt" else "Ngày xấu"
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                provider = ImageProvider(qualityIcon),
                contentDescription = qualityText,
                colorFilter = ColorFilter.tint(quality),
                modifier = GlanceModifier.size(if (compact) 16.dp else 20.dp),
            )
            Spacer(GlanceModifier.width(6.dp))
            Text(
                text = qualityText,
                style = TextStyle(
                    color = quality,
                    fontSize = if (compact) 13.sp else 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
            )
        }
        Spacer(GlanceModifier.height(8.dp))

        // --- Dương lịch ---
        Text(
            text = info.weekday,
            style = TextStyle(
                color = TextDim,
                fontSize = if (compact) 13.sp else 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            ),
        )
        Spacer(GlanceModifier.height(2.dp))
        Text(
            text = "${pad(info.day)}/${pad(info.month)}/${info.year}",
            style = TextStyle(
                color = TextMain,
                fontSize = if (compact) 26.sp else 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            ),
        )

        Spacer(GlanceModifier.height(10.dp))
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DividerColor),
        ) {}
        Spacer(GlanceModifier.height(10.dp))

        // --- Âm lịch ---
        Text(
            text = "Âm lịch ${info.lunar.day}/${info.lunar.month}" +
                if (info.lunar.isLeapMonth) " (nhuận)" else "",
            style = TextStyle(
                color = TextMain,
                fontSize = if (compact) 14.sp else 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            ),
        )
        Text(
            text = "Năm ${info.lunar.yearCanChi}",
            style = TextStyle(
                color = TextDim,
                fontSize = if (compact) 12.sp else 13.sp,
                textAlign = TextAlign.Center,
            ),
        )

        // --- Danh ngôn ---
        if (showQuote && quote != null) {
            Spacer(GlanceModifier.height(12.dp))
            Text(
                text = "“${quote.noiDung}”",
                maxLines = quoteLines,
                style = TextStyle(
                    color = TextDim,
                    fontSize = if (compact) 12.sp else 13.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                ),
            )
        }
    }
}

private fun pad(n: Int): String = if (n < 10) "0$n" else "$n"
