package com.example.tuvi.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Mỗi kích thước cố định là một provider riêng (resizeMode="none") nhưng dùng chung
 * phần render [QuoteWidget]. Người dùng chọn size trong app rồi ghim đúng provider tương ứng.
 */
class QuoteWidgetSmallReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuoteWidget()
}

class QuoteWidgetMediumReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuoteWidget()
}

class QuoteWidgetLargeReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuoteWidget()
}
