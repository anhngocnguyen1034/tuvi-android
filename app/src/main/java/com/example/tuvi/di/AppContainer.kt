package com.example.tuvi.di

import android.content.Context
import com.example.tuvi.BuildConfig
import com.example.tuvi.data.local.TuViDatabase
import com.example.tuvi.data.remote.AuthInterceptor
import com.example.tuvi.data.remote.TuViApiService
import com.example.tuvi.data.repository.AuthRepositoryImpl
import com.example.tuvi.data.repository.SavedChartRepositoryImpl
import com.example.tuvi.data.repository.TuViRepositoryImpl
import com.example.tuvi.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.tuvi.domain.usecase.DeleteSavedChartUseCase
import com.example.tuvi.domain.usecase.GetAllGroupsUseCase
import com.example.tuvi.domain.usecase.GetAllSavedChartsUseCase
import com.example.tuvi.domain.usecase.GetChartsByGroupUseCase
import com.example.tuvi.domain.usecase.GetSavedChartByIdUseCase
import com.example.tuvi.domain.usecase.GetTuViChartUseCase
import com.example.tuvi.domain.usecase.GetTuViInterpretUseCase
import com.example.tuvi.domain.usecase.SaveChartUseCase
import com.example.tuvi.domain.usecase.SearchSavedChartsUseCase
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object AppContainer {

    private const val BASE_URL = "http://160.250.181.238:8000"
//    private const val BASE_URL = "http://192.168.1.17:8000"
    lateinit var app: android.app.Application
        private set

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val firebaseAuth: FirebaseAuth by lazy { Firebase.auth }

    /** Default OkHttp read timeout is 10s; `/api/interpret` (Gemini) often needs much longer. */
    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(firebaseAuth))
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

    private lateinit var database: TuViDatabase

    fun init(context: Context) {
        app = context.applicationContext as android.app.Application
        database = TuViDatabase.getInstance(context)
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

    /** Serializer dùng chung để encode/decode TuViChart và TuViChartInput khi lưu DB */
    val appJson: Json get() = json

    val authRepository: AuthRepository by lazy { AuthRepositoryImpl(firebaseAuth, apiService) }
}
