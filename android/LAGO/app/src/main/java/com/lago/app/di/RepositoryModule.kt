package com.lago.app.di

import com.lago.app.data.repository.ChartRepositoryImpl
import com.lago.app.data.repository.StockListRepositoryImpl
import com.lago.app.domain.repository.ChartRepository
import com.lago.app.domain.repository.StockListRepository
import com.lago.app.data.repository.NewsRepositoryImpl
import com.lago.app.data.repository.StudyRepositoryImpl
import com.lago.app.domain.repository.NewsRepository
import com.lago.app.domain.repository.StudyRepository
import com.lago.app.data.repository.HistoryChallengeRepositoryImpl
import com.lago.app.domain.repository.HistoryChallengeRepository
import com.lago.app.data.repository.MockTradeRepositoryImpl
import com.lago.app.domain.repository.MockTradeRepository
import com.lago.app.data.repository.UserRepositoryImpl
import com.lago.app.domain.repository.UserRepository
import com.lago.app.data.repository.RankingRepositoryImpl
import com.lago.app.domain.repository.RankingRepository
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
    abstract fun bindNewsRepository(
        newsRepositoryImpl: NewsRepositoryImpl
    ): NewsRepository

    @Binds
    @Singleton
    abstract fun bindStudyRepository(
        studyRepositoryImpl: StudyRepositoryImpl
    ): StudyRepository
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

    @Binds
    @Singleton
    abstract fun bindHistoryChallengeRepository(
        historyChallengeRepositoryImpl: HistoryChallengeRepositoryImpl
    ): HistoryChallengeRepository

    @Binds
    @Singleton
    abstract fun bindMockTradeRepository(
        mockTradeRepositoryImpl: MockTradeRepositoryImpl
    ): MockTradeRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
    
    @Binds
    @Singleton
    abstract fun bindRankingRepository(
        rankingRepositoryImpl: RankingRepositoryImpl
    ): RankingRepository
}