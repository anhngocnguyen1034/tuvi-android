package com.anhnn.tuvi

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.anhnn.ads.AdFormat
import com.anhnn.ads.Ads
import com.anhnn.ads.AdsConfig
import com.anhnn.analytics.Analytics
import com.anhnn.language.LanguageDataSource
import com.microsoft.clarity.Clarity
import com.microsoft.clarity.ClarityConfig
import com.anhnn.tuvi.data.preferences.UserPreferencesRepository
import com.anhnn.tuvi.ads.AdNames
import com.anhnn.tuvi.ads.RemoteConfigManager
import com.anhnn.iap.IapConfig
import com.anhnn.iap.IapListener
import com.anhnn.iap.IapManager
import com.anhnn.tuvi.billing.BillingProducts
import com.anhnn.tuvi.di.AppContainer
import com.anhnn.tuvi.ui.theme.TuViComposeColors
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class TuViApplication : Application() {

    private companion object {
        const val CLARITY_PROJECT_ID = "xfjrc544e8"
    }

    lateinit var userPreferencesRepository: UserPreferencesRepository
        private set

    var initialDark: Boolean = false
        private set
    var initialOnboardingDone: Boolean = false
        private set

    override fun onCreate() {
        super.onCreate()
        userPreferencesRepository = UserPreferencesRepository(this)
        val savedDark = runBlocking(Dispatchers.IO) {
            val dark = userPreferencesRepository.initialThemeDark()
            AppCompatDelegate.setDefaultNightMode(
                if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
            initialOnboardingDone = userPreferencesRepository.isOnboardingDone()
            if (!initialOnboardingDone && userPreferencesRepository.consumeDefaultLanguageSeed()) {
                LanguageDataSource(this@TuViApplication)
                    .setLanguageCode(UserPreferencesRepository.LOCALE_VI)
            }
            val langCode = LanguageDataSource(this@TuViApplication).languageCode.first()
                .ifBlank { UserPreferencesRepository.LOCALE_VI }
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(langCode))
            dark
        }
        initialDark = savedDark
        TuViComposeColors.setDark(savedDark)
        AppContainer.init(this)

        RemoteConfigManager.init(this)

        Firebase.crashlytics.isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG

        if (FeatureFlags.ADS_ENABLED) {
            IapManager.init(
                this,
                IapConfig(nonConsumableIds = listOf(BillingProducts.REMOVE_ADS)),
            )
            IapManager.addListener(object : IapListener {
                override fun onPremiumChanged(isPremium: Boolean) {
                    if (isPremium) Ads.clear()
                }
            })
        }

        // Analytics: Firebase Analytics (đã có google-services.json). Event cụ thể khai báo ở Events.
        Analytics.init(this)


        Clarity.initialize(applicationContext, ClarityConfig(CLARITY_PROJECT_ID))

        Ads.init(
            AdsConfig(
                adsEnabled = {
                    FeatureFlags.ADS_ENABLED &&
                        RemoteConfigManager.adsEnabled() &&
                        !IapManager.isPremium.value
                },
                adUnitId = { name ->
                    when (AdNames.formatOf(name)) {
                        AdFormat.INTERSTITIAL -> RemoteConfigManager.interAdUnitId(name)
                        AdFormat.NATIVE -> RemoteConfigManager.nativeAdUnitId(name)
                        AdFormat.BANNER -> RemoteConfigManager.bannerAdUnitId(name)
                        AdFormat.APP_OPEN -> RemoteConfigManager.appOpenAdUnitId(name)
                        // App chưa dùng rewarded — không khai báo placement nào ở AdNames.
                        AdFormat.REWARDED, null -> ""
                    }
                },
                adFormat = { name -> AdNames.formatOf(name) },
                interCooldownMs = { RemoteConfigManager.interMinIntervalMs() },
            )
        )

        // Không đăng ký observer app-open khi build không có quảng cáo.
        if (FeatureFlags.ADS_ENABLED) {
            Ads.setupAppOpen(this, AdNames.APP_OPEN_RESUME)
        }

    }
}
