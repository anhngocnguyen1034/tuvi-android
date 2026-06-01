package com.example.tuvi.ads

import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.tuvi.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

private const val TAG = "NativeAd"

/**
 * Native ad dạng card cho vị trí [adName], tự load 1 lần khi vào màn và hủy khi rời màn.
 * Nếu Remote Config tắt ads hoặc ad load fail thì không chiếm chỗ (composable rỗng).
 */
@Composable
fun NativeAdCard(adName: String, modifier: Modifier = Modifier) {
    if (!RemoteConfigManager.adsEnabled()) return

    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

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
        update = { adView -> bindNativeAd(adView, ad) }
    )
}

private fun bindNativeAd(adView: NativeAdView, ad: NativeAd) {
    val headline = adView.findViewById<TextView>(R.id.ad_headline)
    val advertiser = adView.findViewById<TextView>(R.id.ad_advertiser)
    val body = adView.findViewById<TextView>(R.id.ad_body)
    val icon = adView.findViewById<ImageView>(R.id.ad_app_icon)
    val media = adView.findViewById<MediaView>(R.id.ad_media)
    val cta = adView.findViewById<Button>(R.id.ad_call_to_action)

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
