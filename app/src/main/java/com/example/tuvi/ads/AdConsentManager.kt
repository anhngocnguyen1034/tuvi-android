package com.example.tuvi.ads

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

/**
 * Quản lý consent (UMP / Google User Messaging Platform) — bắt buộc cho AdMob, nhất là user EEA/UK.
 *
 * Luồng chuẩn của Google: thu thập consent TRƯỚC, rồi mới khởi tạo Mobile Ads SDK.
 * Gọi [gather] ở `MainActivity.onCreate`; khi xong (dù consent hay lỗi) sẽ chạy [onReady]
 * để bên ngoài init `MobileAds` + preload ad.
 */
object AdConsentManager {

    private const val TAG = "AdConsent"

    /** true nếu đã đủ điều kiện request ad (consent obtained / không bắt buộc). */
    fun canRequestAds(activity: Activity): Boolean =
        UserMessagingPlatform.getConsentInformation(activity).canRequestAds()

    /**
     * Cập nhật trạng thái consent và hiện form nếu cần. [onReady] luôn được gọi đúng 1 lần
     * (kể cả khi lỗi) để không chặn flow khởi động ads.
     */
    fun gather(activity: Activity, onReady: () -> Unit) {
        val params = ConsentRequestParameters.Builder()
            // Debug: ép EEA + chỉ định test device để thử form.
            // .setConsentDebugSettings(
            //     ConsentDebugSettings.Builder(activity)
            //         .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            //         .addTestDeviceHashedId("TEST-DEVICE-HASHED-ID")
            //         .build()
            // )
            .build()

        val consentInfo: ConsentInformation =
            UserMessagingPlatform.getConsentInformation(activity)

        var done = false
        val finishOnce = {
            if (!done) {
                done = true
                onReady()
            }
        }

        consentInfo.requestConsentInfoUpdate(
            activity,
            params,
            {
                // Hiện form nếu bắt buộc; callback chạy sau khi form đóng (hoặc không cần form).
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w(TAG, "form error ${formError.errorCode}: ${formError.message}")
                    }
                    finishOnce()
                }
            },
            { requestError ->
                Log.w(TAG, "consent update failed ${requestError.errorCode}: ${requestError.message}")
                finishOnce()
            }
        )
    }
}
