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
            // Ngôn ngữ mặc định khi mở app lần đầu là tiếng Việt: module anhnn-language fallback
            // "en" khi datastore của nó chưa có giá trị, nên gieo sẵn "vi" trước khi
            // MainActivity.attachBaseContext đọc. Chỉ chạy ở máy chưa qua onboarding (cài mới)
            // và đúng một lần → không ghi đè ngôn ngữ người dùng đã chọn.
            if (!initialOnboardingDone && userPreferencesRepository.consumeDefaultLanguageSeed()) {
                LanguageDataSource(this@TuViApplication)
                    .setLanguageCode(UserPreferencesRepository.LOCALE_VI)
            }
            // Áp ngôn ngữ đã chọn ở mức app (không phải KEY_LOCALE cũ, vốn luôn "vi" vì không
            // màn nào ghi vào): để context ngoài Activity — notification của SuKienReceiver /
            // BootReceiver — cũng đúng ngôn ngữ, khớp với attachBaseContext của MainActivity.
            val langCode = LanguageDataSource(this@TuViApplication).languageCode.first()
                .ifBlank { UserPreferencesRepository.LOCALE_VI }
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(langCode))
            dark
        }
        initialDark = savedDark
        TuViComposeColors.setDark(savedDark)
        AppContainer.init(this)

        RemoteConfigManager.init(this)

        // Crashlytics: chỉ gửi crash từ bản release để log debug không làm nhiễu dashboard.
        // KHÔNG set custom key chứa dữ liệu ngày sinh — chỉ enum/boolean (xem quy ước ở Events).
        Firebase.crashlytics.isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG

        // IAP: khởi tạo Billing sớm để biết trạng thái premium (isPremium đọc từ cache bền) —
        // dùng để tắt quảng cáo bên dưới. Thêm consumableIds khi làm "mua lượt AI".
        //
        // Build không có quảng cáo (FeatureFlags.ADS_ENABLED = false) thì không mở kết nối
        // Billing: sản phẩm duy nhất là "gỡ quảng cáo" và lối vào đã ẩn ở SettingsScreen.
        // isPremium giữ mặc định false, không ảnh hưởng adsEnabled bên dưới (đã false).
        if (FeatureFlags.ADS_ENABLED) {
            IapManager.init(
                this,
                IapConfig(nonConsumableIds = listOf(BillingProducts.REMOVE_ADS)),
            )
            // Mua gỡ quảng cáo xong → xoá ad đang cache để tắt quảng cáo tức thì (không cần restart).
            // Các request mới đã tự tắt qua adsEnabled ở dưới.
            IapManager.addListener(object : IapListener {
                override fun onPremiumChanged(isPremium: Boolean) {
                    if (isPremium) Ads.clear()
                }
            })
        }

        // Analytics: Firebase Analytics (đã có google-services.json). Event cụ thể khai báo ở Events.
        Analytics.init(this)

        // Microsoft Clarity: heatmap vùng chạm/scroll + session replay. Clarity tự thu thập
        // tương tác chạm nên không cần code thêm cho heatmap. Xem báo cáo tại clarity.microsoft.com.
        Clarity.initialize(applicationContext, ClarityConfig(CLARITY_PROJECT_ID))

        // Cấu hình module ads: bơm dữ liệu app (Remote Config) vào, module không phụ thuộc Firebase.
        // Ad unit fallback theo định dạng (test unit) đã nằm sẵn trong RemoteConfigManager.
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
