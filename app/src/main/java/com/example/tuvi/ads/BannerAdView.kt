package com.example.tuvi.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * Banner ad (adaptive anchored) cho vị trí [adName], tự load khi vào màn và hủy khi rời màn.
 * Ad unit lấy theo tên qua Remote Config; tắt ads thì composable rỗng (không chiếm chỗ).
 */
@Composable
fun BannerAdView(adName: String, modifier: Modifier = Modifier) {
    if (!RemoteConfigManager.adsEnabled()) return

    val context = LocalContext.current
    val widthDp = LocalConfiguration.current.screenWidthDp

    val adView = remember(adName, widthDp) {
        AdView(context).apply {
            setAdSize(
                AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp)
            )
            adUnitId = RemoteConfigManager.bannerAdUnitId(adName)
        }
    }

    DisposableEffect(adView) {
        adView.loadAd(AdRequest.Builder().build())
        onDispose { adView.destroy() }
    }

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { adView },
    )
}
