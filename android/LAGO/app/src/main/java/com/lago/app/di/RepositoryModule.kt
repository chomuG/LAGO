package com.lago.app.di

import com.lago.app.data.repository.ChartRepositoryImpl
import com.lago.app.data.repository.StockListRepositoryImpl
import com.lago.app.domain.repository.ChartRepository
import com.lago.app.domain.repository.StockListRepository
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

    @Binds
    @Singleton
    abstract fun bindStockListRepository(
        stockListRepositoryImpl: StockListRepositoryImpl
    ): StockListRepository
}