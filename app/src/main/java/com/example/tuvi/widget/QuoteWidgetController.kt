package com.example.tuvi.widget

import android.content.Context
import androidx.glance.appwidget.updateAll

object QuoteWidgetController {

    /**
     * Ghim [quoteId] làm câu hiển thị rồi cập nhật mọi widget danh ngôn đang đặt trên màn hình.
     * Gọi từ trong app khi người dùng bấm "Đặt làm widget".
     */
    suspend fun setWidgetQuote(context: Context, quoteId: Int) {
        QuoteWidgetStore.setQuoteId(context, quoteId)
        QuoteWidget().updateAll(context.applicationContext)
    }
}
