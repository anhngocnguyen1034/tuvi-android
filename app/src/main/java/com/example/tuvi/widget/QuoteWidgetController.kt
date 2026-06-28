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
     * Gửi broadcast cập nhật trực tiếp tới từng receiver (mỗi cỡ widget là một provider riêng).
     * Cách này đáng tin cậy hơn `updateAll()` của Glance khi nhiều provider dùng chung một lớp widget.
     */
    private fun refreshAll(context: Context) {
        val app = context.applicationContext
        val manager = AppWidgetManager.getInstance(app)
        QuoteWidgetSize.entries.forEach { size ->
            val ids = manager.getAppWidgetIds(ComponentName(app, size.receiver))
            if (ids.isNotEmpty()) {
                val intent = Intent(app, size.receiver).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                app.sendBroadcast(intent)
            }
        }
    }
}
