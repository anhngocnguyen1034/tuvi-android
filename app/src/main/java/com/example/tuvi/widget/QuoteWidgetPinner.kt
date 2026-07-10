package com.example.tuvi.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build

object QuoteWidgetPinner {

    /** Launcher hiện tại có hỗ trợ ghim widget từ trong app không (cần API 26+). */
    fun isSupported(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        return AppWidgetManager.getInstance(context).isRequestPinAppWidgetSupported
    }

    /**
     * Yêu cầu hệ thống ghim widget lịch (âm dương + danh ngôn) ra màn hình chính. Android sẽ hiển
     * thị hộp xác nhận; người dùng đồng ý mới thực sự thêm. Trả về false nếu thiết bị/launcher không hỗ trợ.
     */
    fun pin(context: Context): Boolean {
        if (!isSupported(context)) return false
        val provider = ComponentName(context, DateWidgetReceiver::class.java)
        return AppWidgetManager.getInstance(context)
            .requestPinAppWidget(provider, null, null)
    }
}
