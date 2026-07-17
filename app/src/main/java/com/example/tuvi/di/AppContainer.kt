package com.example.tuvi.di

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.example.tuvi.BuildConfig
import com.example.tuvi.data.billing.BillingManager
import com.example.tuvi.data.local.TuViDatabase
import com.example.tuvi.data.preferences.UserPreferencesRepository
import com.example.tuvi.data.remote.AiGateInterceptor
import com.example.tuvi.data.remote.PlayIntegrityProvider
import com.example.tuvi.data.remote.TuViApiService
import com.example.tuvi.data.repository.QuoteRepositoryImpl
import com.example.tuvi.data.repository.SavedChartRepositoryImpl
import com.example.tuvi.data.repository.TuViRepositoryImpl
import com.example.tuvi.domain.usecase.DeleteSavedChartUseCase
import com.example.tuvi.domain.usecase.GetAllGroupsUseCase
import com.example.tuvi.domain.usecase.GetQuotesUseCase
import com.example.tuvi.domain.usecase.GetAllSavedChartsUseCase
import com.example.tuvi.domain.usecase.GetChartsByGroupUseCase
import com.example.tuvi.domain.usecase.GetSavedChartByIdUseCase
import com.example.tuvi.domain.usecase.GetIapProductsUseCase
import com.example.tuvi.domain.usecase.GetQuotaUseCase
import com.example.tuvi.domain.usecase.GetTuViChartUseCase
import com.example.tuvi.domain.usecase.GetTuViHoiUseCase
import com.example.tuvi.domain.usecase.VerifyPurchaseUseCase
import com.example.tuvi.domain.usecase.GetTuViInterpretUseCase
import com.example.tuvi.domain.usecase.GetTuViVanHanUseCase
import com.example.tuvi.domain.usecase.SaveChartUseCase
import com.example.tuvi.domain.usecase.SearchSavedChartsUseCase
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.Locale
import java.util.concurrent.TimeUnit

object AppContainer {

    private const val BASE_URL = "http://192.168.0.103:8000"
    private const val CLOUD_PROJECT_NUMBER = 0L

    lateinit var app: android.app.Application
        private set
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    /** Play Integrity provider — chỉ tạo khi đã cấu hình Cloud Project Number. */
    private val playIntegrityProvider: PlayIntegrityProvider? by lazy {
        if (CLOUD_PROJECT_NUMBER > 0L) PlayIntegrityProvider(app, CLOUD_PROJECT_NUMBER) else null
    }

    /** Gắn X-Device-Id + X-Integrity-Token cho riêng endpoint AI. */
    private val aiGateInterceptor by lazy {
        AiGateInterceptor(
            deviceIdProvider = { userPreferencesRepository.deviceId },
            integrityProvider = playIntegrityProvider,
        )
    }

    /** Default OkHttp read timeout is 10s; `/api/interpret` (Gemini) often needs much longer. */
    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(aiGateInterceptor)
            .addInterceptor { chain ->
                val locales = AppCompatDelegate.getApplicationLocales()
                val tag = (if (!locales.isEmpty) locales[0] else Locale.getDefault())
                    ?.toLanguageTag() ?: "vi"
                chain.proceed(
                    chain.request().newBuilder()
                        .header("Accept-Language", tag)
                        .build()
                )
            }
            .apply {
                // Body chứa PII (tên, ngày/giờ sinh, giới tính) — chỉ log trong debug build.
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                }
            }
            .build()
    }

    val apiService: TuViApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(TuViApiService::class.java)
    }

    private val repository by lazy { TuViRepositoryImpl(apiService) }

    val getTuViChartUseCase by lazy { GetTuViChartUseCase(repository) }
    val getTuViInterpretUseCase by lazy { GetTuViInterpretUseCase(repository) }
    val getTuViVanHanUseCase by lazy { GetTuViVanHanUseCase(repository) }
    val getTuViHoiUseCase by lazy { GetTuViHoiUseCase(repository) }
    val getQuotaUseCase by lazy { GetQuotaUseCase(repository) }
    val getIapProductsUseCase by lazy { GetIapProductsUseCase(repository) }
    val verifyPurchaseUseCase by lazy { VerifyPurchaseUseCase(repository) }

    /** Google Play Billing — chỉ tạo khi cần (màn cửa hàng). */
    val billingManager by lazy { BillingManager(app) }

    private lateinit var database: TuViDatabase
    lateinit var userPreferencesRepository: UserPreferencesRepository
        private set

    fun init(context: Context) {
        app = context.applicationContext as android.app.Application
        database = TuViDatabase.getInstance(context)
        userPreferencesRepository = UserPreferencesRepository(app)
    }

    private val savedChartRepository by lazy {
        SavedChartRepositoryImpl(database.savedChartDao())
    }

    val historyDao by lazy { database.historyDao() }
    val bookmarkDao by lazy { database.bookmarkDao() }
    val tabSessionDao by lazy { database.tabSessionDao() }
    val suKienDao by lazy { database.suKienDao() }

    val getAllSavedChartsUseCase by lazy { GetAllSavedChartsUseCase(savedChartRepository) }
    val searchSavedChartsUseCase by lazy { SearchSavedChartsUseCase(savedChartRepository) }
    val getChartsByGroupUseCase by lazy { GetChartsByGroupUseCase(savedChartRepository) }
    val getAllGroupsUseCase by lazy { GetAllGroupsUseCase(savedChartRepository) }
    val saveChartUseCase by lazy { SaveChartUseCase(savedChartRepository) }
    val deleteSavedChartUseCase by lazy { DeleteSavedChartUseCase(savedChartRepository) }
    val getSavedChartByIdUseCase by lazy { GetSavedChartByIdUseCase(savedChartRepository) }

    private val quoteRepository by lazy { QuoteRepositoryImpl(app, json) }
    val getQuotesUseCase by lazy { GetQuotesUseCase(quoteRepository) }

    /** Serializer dùng chung để encode/decode TuViChart và TuViChartInput khi lưu DB */
    val appJson: Json get() = json
}
