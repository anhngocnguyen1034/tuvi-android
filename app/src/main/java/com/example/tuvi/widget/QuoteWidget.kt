package com.example.tuvi.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.LocalSize
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
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.tuvi.R
import com.example.tuvi.di.AppContainer
import com.example.tuvi.domain.model.Quote
import kotlin.random.Random

/**
 * Widget màn hình chính hiển thị một câu danh ngôn ngẫu nhiên lấy từ `assets/quotes.json`.
 * Chạm vào widget để đổi sang câu khác.
 */
class QuoteWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val quote = loadQuote(context)
        provideContent {
            GlanceTheme {
                QuoteContent(quote)
            }
        }
    }

    /**
     * Nếu người dùng đã ghim một câu (từ trong app) -> hiển thị đúng câu đó;
     * ngược lại (RANDOM) -> bốc ngẫu nhiên. Câu ghim không tìm thấy thì fallback ngẫu nhiên.
     */
    private suspend fun loadQuote(context: Context): Quote? {
        val quotes = AppContainer.getQuotesUseCase().getOrNull().orEmpty()
        if (quotes.isEmpty()) return null
        val pinnedId = QuoteWidgetStore.getQuoteId(context)
        return quotes.firstOrNull { it.id == pinnedId } ?: quotes[Random.nextInt(quotes.size)]
    }
}

private val White = ColorProvider(Color.White)
private val WhiteDim = ColorProvider(Color(0xCCFFFFFF))

@androidx.compose.runtime.Composable
private fun QuoteContent(quote: Quote?) {
    val height = LocalSize.current.height
    val compact = height < 130.dp
    val bodyLines = when {
        height < 130.dp -> 3
        height < 200.dp -> 6
        else -> 10
    }
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProviderBackground)
            .clickable(actionRunCallback<NextQuoteAction>())
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (quote == null) {
            Text(
                text = "Chạm để tải danh ngôn",
                style = TextStyle(color = White, fontSize = 15.sp, textAlign = TextAlign.Center),
            )
        } else {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "“${quote.noiDung}”",
                    maxLines = bodyLines,
                    style = TextStyle(
                        color = White,
                        fontSize = if (compact) 14.sp else 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                    ),
                )
                quote.tacGia?.takeIf { !compact }?.let { author ->
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Text(
                        text = "— $author",
                        maxLines = 1,
                        style = TextStyle(
                            color = WhiteDim,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center,
                        ),
                    )
                }
            }
        }
    }
}

private val ImageProviderBackground
    get() = androidx.glance.ImageProvider(R.drawable.quote_widget_background)
