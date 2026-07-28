@file:Suppress("UNUSED_PARAMETER")

package com.anhnn.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Shim no-op thay cho module `anhnn-components-ads`.
 *
 * Bản đầu lên CH Play không có quảng cáo, và dependency `anhnn-components-ads` /
 * `play-services-ads` / `user-messaging-platform` đã bị bỏ khỏi `app/build.gradle.kts` để APK
 * không còn Mobile Ads SDK. File này khai báo đúng phần API mà app đang gọi (cùng package
 * `com.anhnn.ads`) nên toàn bộ call-site ở `MainActivity`, `TuViApplication` và các screen giữ
 * nguyên, không phải sửa.
 *
 * BẬT LẠI QUẢNG CÁO — làm đủ 3 bước, thiếu bước 1 sẽ lỗi "duplicate class":
 *  1. Xoá file này.
 *  2. Trả lại 3 dòng dependency trong `app/build.gradle.kts` (xem comment ở khối AdMob).
 *  3. Đổi `FeatureFlags.ADS_ENABLED` thành `true`.
 *
 * Mọi hàm ở đây phải giữ nguyên chữ ký của module thật — đổi chữ ký ở đây là che mất lỗi
 * biên dịch sẽ xuất hiện khi bật lại.
 */
object Ads {
    fun init(config: AdsConfig) = Unit

    fun start(activity: Activity, onReady: () -> Unit = {}) = Unit

    fun preload(context: Context, vararg adNames: String) = Unit

    fun isInterstitialReady(adName: String): Boolean = false

    /** Callback luôn chạy ngay — giữ đúng hợp đồng của module thật (không chặn điều hướng). */
    fun showInterstitial(activity: Activity, adName: String, onDone: () -> Unit) = onDone()

    fun isRewardedReady(adName: String): Boolean = false

    fun showRewarded(
        activity: Activity,
        adName: String,
        onReward: () -> Unit,
        onDone: () -> Unit = {},
    ) = onDone()

    fun isAppOpenReady(adName: String): Boolean = false

    fun showAppOpen(activity: Activity, adName: String, onDone: () -> Unit = {}) = onDone()

    fun setupAppOpen(app: Application, adName: String) = Unit

    fun canRequestAds(activity: Activity): Boolean = false

    fun clear() = Unit
}

enum class AdFormat { INTERSTITIAL, NATIVE, BANNER, APP_OPEN, REWARDED }

enum class NativeAdSize { SMALL, MEDIUM }

class AdsConfig(
    val adsEnabled: () -> Boolean = { false },
    val adUnitId: (String) -> String,
    val adFormat: (String) -> AdFormat?,
    val interCooldownMs: () -> Long = { 0L },
)

/** Không chiếm chỗ: layout các screen giữ nguyên như khi quảng cáo bị tắt. */
@Composable
fun BannerAd(adName: String, modifier: Modifier = Modifier) = Unit

@Composable
fun NativeAd(
    adName: String,
    size: NativeAdSize = NativeAdSize.MEDIUM,
    modifier: Modifier = Modifier,
) = Unit
