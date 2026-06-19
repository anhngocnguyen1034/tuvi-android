package com.example.tuvi

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.anhnn.ads.AdFormat
import com.anhnn.ads.Ads
import com.anhnn.ads.AdsConfig
import com.anhnn.analytics.Analytics
import com.example.tuvi.data.preferences.UserPreferencesRepository
import com.example.tuvi.ads.AdNames
import com.example.tuvi.ads.RemoteConfigManager
import com.example.tuvi.di.AppContainer
import com.example.tuvi.ui.theme.TuViComposeColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class TuViApplication : Application() {

    lateinit var userPreferencesRepository: UserPreferencesRepository
        private set

    var initialDark: Boolean = true
        private set

    /** Đã xem intro chưa — đọc 1 lần lúc khởi động để chọn màn bắt đầu (splash → intro/home). */
    var initialOnboardingDone: Boolean = false
        private set

    override fun onCreate() {
        super.onCreate()
        userPreferencesRepository = UserPreferencesRepository(this)
        val savedDark = runBlocking(Dispatchers.IO) {
            val (dark, localeTag) = userPreferencesRepository.initialSnapshot()
            AppCompatDelegate.setDefaultNightMode(
                if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(localeTag))
            initialOnboardingDone = userPreferencesRepository.isOnboardingDone()
            dark
        }
        initialDark = savedDark
        TuViComposeColors.setDark(savedDark)
        AppContainer.init(this)

        RemoteConfigManager.init(this)

        // Analytics: Firebase Analytics (đã có google-services.json). Event cụ thể khai báo ở Events.
        Analytics.init(this)

        // Cấu hình module ads: bơm dữ liệu app (Remote Config) vào, module không phụ thuộc Firebase.
        // Ad unit fallback theo định dạng (test unit) đã nằm sẵn trong RemoteConfigManager.
        Ads.init(
            AdsConfig(
                adsEnabled = { RemoteConfigManager.adsEnabled() },
                adUnitId = { name ->
                    when (AdNames.formatOf(name)) {
                        AdFormat.INTERSTITIAL -> RemoteConfigManager.interAdUnitId(name)
                        AdFormat.NATIVE -> RemoteConfigManager.nativeAdUnitId(name)
                        AdFormat.BANNER -> RemoteConfigManager.bannerAdUnitId(name)
                        null -> ""
                    }
                },
                adFormat = { name -> AdNames.formatOf(name) },
                interCooldownMs = { RemoteConfigManager.interMinIntervalMs() },
            )
        )
        // Consent (UMP) + MobileAds.initialize chạy ở MainActivity qua Ads.start — theo luồng
        // chuẩn của Google: consent trước, init ads sau.
    }
}
