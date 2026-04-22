package com.example.tuvi.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.tuvi.MainActivity
import com.example.tuvi.R

class SuKienReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val tieuDe = intent.getStringExtra(EXTRA_TIEU_DE) ?: "Sự kiện"
        val ghiChu = intent.getStringExtra(EXTRA_GHI_CHU) ?: ""
        val id     = intent.getLongExtra(EXTRA_ID, 0L).toInt()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureChannel(nm)

        val tap = PendingIntent.getActivity(
            context, id,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(tieuDe)
            .setContentText(ghiChu.ifBlank { "Hôm nay có sự kiện trong lịch của bạn" })
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(ghiChu.ifBlank { "Hôm nay có sự kiện trong lịch của bạn" }))
            .setContentIntent(tap)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        nm.notify(id, notif)
    }

    private fun ensureChannel(nm: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                nm.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID,
                        "Sự kiện lịch",
                        NotificationManager.IMPORTANCE_HIGH,
                    ).apply { description = "Nhắc nhở sự kiện trong lịch âm dương" }
                )
            }
        }
    }

    companion object {
        const val CHANNEL_ID   = "su_kien_channel"
        const val EXTRA_ID     = "extra_id"
        const val EXTRA_TIEU_DE = "extra_tieu_de"
        const val EXTRA_GHI_CHU = "extra_ghi_chu"
    }
}
