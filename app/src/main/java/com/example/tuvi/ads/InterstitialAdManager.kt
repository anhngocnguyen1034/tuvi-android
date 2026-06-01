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
import java.util.concurrent.ConcurrentHashMap

object InterstitialAdManager {

    private const val TAG = "InterstitialAd"

    // Fallback nếu Remote Config chưa sẵn sàng (cũng đã set default trong xml)
    private const val DEFAULT_COOLDOWN_MS = 30_000L

    private fun cooldownMs(): Long =
        RemoteConfigManager.interMinIntervalMs().takeIf { it > 0L } ?: DEFAULT_COOLDOWN_MS

    /** Mỗi ad_name có 1 ad đã load + cờ loading riêng. */
    private class Slot {
        @Volatile var ad: InterstitialAd? = null
        @Volatile var loading: Boolean = false
    }

    private val slots = ConcurrentHashMap<String, Slot>()

    // Cooldown dùng chung toàn app: 2 interstitial bất kỳ không hiện quá sát nhau.
    @Volatile private var lastShownAt: Long = 0L

    /** Tải sẵn quảng cáo cho [adName] nếu chưa có. */
    fun preload(context: Context, adName: String) {
        if (!RemoteConfigManager.adsEnabled()) return
        val slot = slots.getOrPut(adName) { Slot() }
        if (slot.ad != null || slot.loading) return
        slot.loading = true
        InterstitialAd.load(
            context.applicationContext,
            RemoteConfigManager.interAdUnitId(adName),
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(loaded: InterstitialAd) {
                    slot.ad = loaded
                    slot.loading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "[$adName] load failed: ${error.message}")
                    slot.ad = null
                    slot.loading = false
                }
            }
        )
    }

    /**
     * Hiển thị quảng cáo [adName] nếu đã load sẵn, sau đó gọi [onClosed]. Nếu chưa có
     * (đang load / load fail / đang trong cooldown) thì gọi [onClosed] ngay để không
     * chặn user, và preload cho lượt sau.
     */
    fun showThen(activity: Activity, adName: String, onClosed: () -> Unit) {
        if (!RemoteConfigManager.adsEnabled()) {
            onClosed()
            return
        }
        val slot = slots.getOrPut(adName) { Slot() }
        val now = System.currentTimeMillis()
        val current = slot.ad
        val inCooldown = now - lastShownAt < cooldownMs()
        if (current == null || inCooldown) {
            if (current == null) preload(activity, adName)
            onClosed()
            return
        }
        current.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                slot.ad = null
                lastShownAt = System.currentTimeMillis()
                preload(activity, adName)
                onClosed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(TAG, "[$adName] show failed: ${error.message}")
                slot.ad = null
                preload(activity, adName)
                onClosed()
            }
        }
        current.show(activity)
    }
}
