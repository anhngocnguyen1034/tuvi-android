package com.example.tuvi.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/** Widget lịch (dương lịch + âm lịch + danh ngôn), co giãn được. */
class DateWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DateWidget()
}
