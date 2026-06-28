package com.example.tuvi.widget

import android.content.Context

/**
 * Lưu id câu danh ngôn đang được ghim cho widget.
 * [RANDOM] = không ghim câu nào -> widget hiển thị ngẫu nhiên (và chạm để đổi).
 */
object QuoteWidgetStore {
    private const val PREFS = "quote_widget_prefs"
    private const val KEY_QUOTE_ID = "quote_id"
    const val RANDOM = -1

    fun getQuoteId(context: Context): Int =
        prefs(context).getInt(KEY_QUOTE_ID, RANDOM)

    fun setQuoteId(context: Context, quoteId: Int) {
        // commit() (đồng bộ) để chắc chắn giá trị đã lưu trước khi widget render lại đọc ra.
        prefs(context).edit().putInt(KEY_QUOTE_ID, quoteId).commit()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
