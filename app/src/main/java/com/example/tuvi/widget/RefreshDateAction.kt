package com.example.tuvi.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback

/** Chạm vào widget lịch -> render lại (cập nhật ngày hôm nay + bốc câu danh ngôn ngẫu nhiên). */
class RefreshDateAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        QuoteWidgetStore.setQuoteId(context, QuoteWidgetStore.RANDOM)
        DateWidget().update(context, glanceId)
    }
}
