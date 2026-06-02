package com.example.tuvi.ads

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.ColorUtils
import com.example.tuvi.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

private const val TAG = "NativeAd"

/** Màu lấy từ [MaterialTheme.colorScheme] để tô native ad theo theme dark/light. */
private data class NativeAdColors(
    val surface: Int,
    val onSurface: Int,
    val onSurfaceVariant: Int,
    val primary: Int,
    val onPrimary: Int,
)

/**
 * Native ad dạng card cho vị trí [adName], tự load 1 lần khi vào màn và hủy khi rời màn.
 * Màu nền + chữ + nút lấy từ [MaterialTheme.colorScheme] nên tự đổi theo theme dark/light
 * cho dễ nhìn. Tắt ads / load fail thì không chiếm chỗ (composable rỗng).
 */
@Composable
fun NativeAdCard(adName: String, modifier: Modifier = Modifier) {
    if (!RemoteConfigManager.adsEnabled()) return

    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    val cs = MaterialTheme.colorScheme
    val colors = remember(cs.surface, cs.onSurface, cs.primary, cs.onPrimary) {
        NativeAdColors(
            surface = cs.surface.toArgb(),
            onSurface = cs.onSurface.toArgb(),
            onSurfaceVariant = cs.onSurface.copy(alpha = 0.7f).toArgb(),
            primary = cs.primary.toArgb(),
            onPrimary = cs.onPrimary.toArgb(),
        )
    }

    DisposableEffect(adName) {
        val loader = AdLoader.Builder(context.applicationContext, RemoteConfigManager.nativeAdUnitId(adName))
            .forNativeAd { loaded ->
                nativeAd = loaded
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "load failed: ${error.message}")
                }
            })
            .build()
        loader.loadAd(AdRequest.Builder().build())

        onDispose {
            nativeAd?.destroy()
            nativeAd = null
        }
    }

    val ad = nativeAd ?: return
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            LayoutInflater.from(ctx).inflate(R.layout.native_ad_card, null) as NativeAdView
        },
        update = { adView -> bindNativeAd(adView, ad, colors) }
    )
}

private fun bindNativeAd(adView: NativeAdView, ad: NativeAd, colors: NativeAdColors) {
    val headline = adView.findViewById<TextView>(R.id.ad_headline)
    val advertiser = adView.findViewById<TextView>(R.id.ad_advertiser)
    val body = adView.findViewById<TextView>(R.id.ad_body)
    val icon = adView.findViewById<ImageView>(R.id.ad_app_icon)
    val media = adView.findViewById<MediaView>(R.id.ad_media)
    val cta = adView.findViewById<Button>(R.id.ad_call_to_action)
    val adBadge = adView.findViewById<TextView>(R.id.ad_badge)

    applyTheme(adView, headline, advertiser, body, cta, adBadge, colors)

    headline.text = ad.headline
    adView.headlineView = headline

    val advertiserText = ad.advertiser ?: ad.store
    if (advertiserText.isNullOrEmpty()) {
        advertiser.visibility = android.view.View.GONE
    } else {
        advertiser.visibility = android.view.View.VISIBLE
        advertiser.text = advertiserText
        adView.advertiserView = advertiser
    }

    if (ad.body.isNullOrEmpty()) {
        body.visibility = android.view.View.GONE
    } else {
        body.visibility = android.view.View.VISIBLE
        body.text = ad.body
        adView.bodyView = body
    }

    val iconImage = ad.icon
    if (iconImage == null) {
        icon.visibility = android.view.View.GONE
    } else {
        icon.visibility = android.view.View.VISIBLE
        icon.setImageDrawable(iconImage.drawable)
        adView.iconView = icon
    }

    adView.mediaView = media
    ad.mediaContent?.let { media.mediaContent = it }

    if (ad.callToAction.isNullOrEmpty()) {
        cta.visibility = android.view.View.GONE
    } else {
        cta.visibility = android.view.View.VISIBLE
        cta.text = ad.callToAction
        adView.callToActionView = cta
    }

    adView.setNativeAd(ad)
}

/** Tô card + chữ + nút theo theme hiện tại. */
private fun applyTheme(
    adView: NativeAdView,
    headline: TextView,
    advertiser: TextView,
    body: TextView,
    cta: Button,
    adBadge: TextView?,
    colors: NativeAdColors,
) {
    val ctx = adView.context

    // Nền card bo góc + viền mảnh theo màu primary.
    adView.background = GradientDrawable().apply {
        cornerRadius = dp(ctx, 16f)
        setColor(colors.surface)
        setStroke(dp(ctx, 1f).toInt(), ColorUtils.setAlphaComponent(colors.primary, 40))
    }

    headline.setTextColor(colors.onSurface)
    advertiser.setTextColor(colors.onSurfaceVariant)
    body.setTextColor(colors.onSurfaceVariant)

    // Badge "Ad": nền primary, chữ onPrimary.
    adBadge?.setBackgroundColor(colors.primary)
    adBadge?.setTextColor(colors.onPrimary)

    // Nút CTA: nền primary bo góc, chữ onPrimary.
    cta.background = GradientDrawable().apply {
        cornerRadius = dp(ctx, 8f)
        setColor(colors.primary)
    }
    cta.setTextColor(colors.onPrimary)
}

private fun dp(context: Context, value: Float): Float =
    value * context.resources.displayMetrics.density
