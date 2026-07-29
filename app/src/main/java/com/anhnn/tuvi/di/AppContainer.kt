package com.anhnn.tuvi.di

import android.content.Context
import com.anhnn.language.LanguageDataSource
import com.anhnn.tuvi.BuildConfig
import com.anhnn.tuvi.data.billing.BillingManager
import com.anhnn.tuvi.data.local.TuViDatabase
import com.anhnn.tuvi.data.preferences.UserPreferencesRepository
import com.anhnn.tuvi.data.remote.AiGateInterceptor
import com.anhnn.tuvi.data.remote.PlayIntegrityProvider
import com.anhnn.tuvi.data.remote.TuViApiService
import com.anhnn.tuvi.data.repository.QuoteRepositoryImpl
import com.anhnn.tuvi.data.repository.SavedChartRepositoryImpl
import com.anhnn.tuvi.data.repository.TuViRepositoryImpl
import com.anhnn.tuvi.domain.usecase.DeleteSavedChartUseCase
import com.anhnn.tuvi.domain.usecase.GetAllGroupsUseCase
import com.anhnn.tuvi.domain.usecase.GetQuotesUseCase
import com.anhnn.tuvi.domain.usecase.GetAllSavedChartsUseCase
import com.anhnn.tuvi.domain.usecase.GetChartsByGroupUseCase
import com.anhnn.tuvi.domain.usecase.GetSavedChartByIdUseCase
import com.anhnn.tuvi.domain.usecase.GetIapProductsUseCase
import com.anhnn.tuvi.domain.usecase.GetQuotaUseCase
import com.anhnn.tuvi.domain.usecase.GetTuViChartUseCase
import com.anhnn.tuvi.domain.usecase.GetTuViHoiUseCase
import com.anhnn.tuvi.domain.usecase.VerifyPurchaseUseCase
import com.anhnn.tuvi.domain.usecase.GetTuViInterpretUseCase
import com.anhnn.tuvi.domain.usecase.GetTuViVanHanUseCase
import com.anhnn.tuvi.domain.usecase.SaveChartUseCase
import com.anhnn.tuvi.domain.usecase.SearchSavedChartsUseCase
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object AppContainer {

    private const val BASE_URL = "https://tainhanh.io.vn/"
    private const val CLOUD_PROJECT_NUMBER = 0L

    lateinit var app: android.app.Application
        private set
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    /**
     * Mã ngôn ngữ UI hiện tại (vi / en / zh...) dùng cho header Accept-Language.
     * null = collector ở [init] chưa kịp phát giá trị đầu → interceptor tự đọc một lần.
     */
    @Volatile
    private var currentLanguageTag: String? = null

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
                val tag = currentLanguageTag
                    ?: runBlocking { LanguageDataSource(app).languageCode.first() }
                        .ifBlank { UserPreferencesRepository.LOCALE_VI }
                        .also { currentLanguageTag = it }
                chain.proceed(
                    chain.request().newBuilder()
                        .header("Accept-Language", tag)
                        .build()
                )
            }
            .apply {
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
        // Ngôn ngữ UI (module anhnn-language) là nguồn duy nhất cho header Accept-Language —
        // backend trả tên sao / luận giải theo header này. Theo dõi liên tục để đổi ngôn ngữ
        // xong là request sau dùng ngay ngôn ngữ mới.
        CoroutineScope(Dispatchers.IO).launch {
            LanguageDataSource(app).languageCode.collect { code ->
                if (code.isNotBlank()) currentLanguageTag = code
            }
        }
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
