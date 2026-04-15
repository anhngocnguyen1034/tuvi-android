package com.example.tuvi.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.tuvi.data.local.SuKienEntity

object AlarmHelper {

    /**
     * Lên lịch báo thức cho một sự kiện.
     * Nếu [entity.alarmEpoch] == 0 hoặc đã qua → không đặt.
     */
    fun schedule(context: Context, entity: SuKienEntity) {
        if (entity.alarmEpoch <= 0 || entity.alarmEpoch <= System.currentTimeMillis()) return
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = buildPendingIntent(context, entity)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!am.canScheduleExactAlarms()) {
                // Fallback: dùng inexact alarm
                am.set(AlarmManager.RTC_WAKEUP, entity.alarmEpoch, pi)
                return
            }
        }
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, entity.alarmEpoch, pi)
    }

    /** Hủy báo thức đã đặt cho sự kiện. */
    fun cancel(context: Context, entity: SuKienEntity) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(buildPendingIntent(context, entity))
    }

    private fun buildPendingIntent(context: Context, entity: SuKienEntity): PendingIntent {
        val intent = Intent(context, SuKienReceiver::class.java).apply {
            putExtra(SuKienReceiver.EXTRA_ID,      entity.id)
            putExtra(SuKienReceiver.EXTRA_TIEU_DE, entity.tieuDe)
            putExtra(SuKienReceiver.EXTRA_GHI_CHU, entity.ghiChu)
        }
        return PendingIntent.getBroadcast(
            context,
            entity.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
