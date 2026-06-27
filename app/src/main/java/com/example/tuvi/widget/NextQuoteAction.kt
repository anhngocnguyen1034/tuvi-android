package com.example.tuvi.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback

/** Chạm vào widget -> bỏ ghim (về RANDOM) rồi render lại để bốc câu danh ngôn ngẫu nhiên khác. */
class NextQuoteAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        QuoteWidgetStore.setQuoteId(context, QuoteWidgetStore.RANDOM)
        QuoteWidget().update(context, glanceId)
    }
}
