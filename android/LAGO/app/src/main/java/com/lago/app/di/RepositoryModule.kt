package com.lago.app.di

import com.lago.app.data.repository.ChartRepositoryImpl
import com.lago.app.domain.repository.ChartRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindChartRepository(
        chartRepositoryImpl: ChartRepositoryImpl
    ): ChartRepository
}