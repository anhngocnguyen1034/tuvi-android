package com.example.tuvi.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

object QuoteWidgetController {

    /**
     * Ghim [quoteId] làm câu hiển thị rồi cập nhật mọi widget danh ngôn đang đặt trên màn hình.
     * Gọi từ trong app khi người dùng bấm "Đặt làm widget".
     */
    fun setWidgetQuote(context: Context, quoteId: Int) {
        QuoteWidgetStore.setQuoteId(context, quoteId)
        refreshAll(context)
    }

    /**
     * Gửi broadcast cập nhật trực tiếp tới receiver — đáng tin cậy hơn `updateAll()` của Glance.
     */
    private fun refreshAll(context: Context) {
        val app = context.applicationContext
        val provider = ComponentName(app, QuoteWidgetReceiver::class.java)
        val ids = AppWidgetManager.getInstance(app).getAppWidgetIds(provider)
        if (ids.isEmpty()) return
        val intent = Intent(app, QuoteWidgetReceiver::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        app.sendBroadcast(intent)
    }
}
