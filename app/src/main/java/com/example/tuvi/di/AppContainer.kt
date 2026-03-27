package com.example.tuvi.di

import com.example.tuvi.data.remote.TuViApiService
import com.example.tuvi.data.repository.TuViRepositoryImpl
import com.example.tuvi.domain.usecase.GetTuViChartUseCase
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

/**
 * Composition root – khởi tạo và wiring toàn bộ dependency thủ công.
 * Đây là nơi DUY NHẤT biết về cả data layer và domain layer.
 */
object AppContainer {

    private const val BASE_URL = "http://192.168.1.19:8000"

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val apiService: TuViApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(TuViApiService::class.java)
    }

    private val repository by lazy { TuViRepositoryImpl(apiService) }

    val getTuViChartUseCase by lazy { GetTuViChartUseCase(repository) }
}
