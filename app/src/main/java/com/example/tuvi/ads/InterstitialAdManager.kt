package com.example.tuvi.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object InterstitialAdManager {

    // Google sample test unit — KHÔNG dùng cho production
    private const val TEST_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val TAG = "InterstitialAd"

    // Fallback nếu Remote Config chưa sẵn sàng (cũng đã set default trong xml)
    private const val DEFAULT_COOLDOWN_MS = 30_000L

    private fun cooldownMs(): Long =
        RemoteConfigManager.interMinIntervalMs().takeIf { it > 0L } ?: DEFAULT_COOLDOWN_MS

    @Volatile private var ad: InterstitialAd? = null
    @Volatile private var loading: Boolean = false
    @Volatile private var lastShownAt: Long = 0L

    fun preload(context: Context) {
        if (!RemoteConfigManager.adsEnabled()) return
        if (ad != null || loading) return
        loading = true
        InterstitialAd.load(
            context.applicationContext,
            TEST_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(loaded: InterstitialAd) {
                    ad = loaded
                    loading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "load failed: ${error.message}")
                    ad = null
                    loading = false
                }
            }
        )
    }

    /**
     * Hiển thị ads nếu đã load sẵn, sau đó gọi [onClosed]. Nếu chưa có ads (đang load
     * hoặc load fail) thì gọi [onClosed] ngay để không chặn user, và preload lượt sau.
     */
    fun showThen(activity: Activity, onClosed: () -> Unit) {
        if (!RemoteConfigManager.adsEnabled()) {
            onClosed()
            return
        }
        val now = System.currentTimeMillis()
        val current = ad
        val inCooldown = now - lastShownAt < cooldownMs()
        if (current == null || inCooldown) {
            if (current == null) preload(activity)
            onClosed()
            return
        }
        current.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                ad = null
                lastShownAt = System.currentTimeMillis()
                preload(activity)
                onClosed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(TAG, "show failed: ${error.message}")
                ad = null
                preload(activity)
                onClosed()
            }
        }
        current.show(activity)
    }
}
