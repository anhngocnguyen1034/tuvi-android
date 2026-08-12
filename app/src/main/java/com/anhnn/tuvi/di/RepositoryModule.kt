package com.anhnn.tuvi.di

import com.anhnn.tuvi.data.repository.TuViRepositoryImpl
import com.anhnn.tuvi.domain.repository.TuViRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module cho các ViewModel mới (vd: SpecsViewModel).
 *
 * Tạm thời tái sử dụng Retrofit/OkHttp từ [AppContainer] (manual DI) — chưa migrate
 * toàn bộ sang Hilt. [AppContainer.init] luôn chạy trước trong [com.anhnn.tuvi.TuViApplication.onCreate].
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideTuViRepository(): TuViRepository = TuViRepositoryImpl(AppContainer.apiService)
}
