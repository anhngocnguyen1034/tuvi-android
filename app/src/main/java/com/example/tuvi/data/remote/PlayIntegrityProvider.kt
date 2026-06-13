package com.example.tuvi.data.remote

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest

/**
 * Lấy Play Integrity token (Classic request) — khớp với backend dùng
 * decodeIntegrityToken. Trả token cho một nonce, hoặc null nếu thất bại
 * (để interceptor vẫn gửi request; backend quyết định chấp nhận hay 403).
 *
 * [cloudProjectNumber] là số project GCP đã link Play Console. Bắt buộc > 0.
 */
class PlayIntegrityProvider(
    context: Context,
    private val cloudProjectNumber: Long,
) {
    private val manager = IntegrityManagerFactory.create(context.applicationContext)

    /** Gọi blocking — chạy trong interceptor của OkHttp (đã ở background thread). */
    fun tokenForNonce(nonce: String): String? = try {
        val request = IntegrityTokenRequest.builder()
            .setNonce(nonce)
            .setCloudProjectNumber(cloudProjectNumber)
            .build()
        Tasks.await(manager.requestIntegrityToken(request)).token()
    } catch (e: Exception) {
        Log.w("PlayIntegrity", "requestIntegrityToken failed: ${e.message}")
        null
    }
}
