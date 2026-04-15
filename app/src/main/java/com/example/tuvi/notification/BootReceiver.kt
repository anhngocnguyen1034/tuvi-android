package com.example.tuvi.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.tuvi.data.local.TuViDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Sau khi điện thoại khởi động lại, AlarmManager bị xóa.
 * Receiver này lấy tất cả sự kiện còn hiệu lực và đặt lại alarm.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        CoroutineScope(Dispatchers.IO).launch {
            val db  = TuViDatabase.getInstance(context)
            val now = System.currentTimeMillis()
            db.suKienDao().getAll().first()
                .filter { it.alarmEpoch > now }
                .forEach { AlarmHelper.schedule(context, it) }
        }
    }
}
