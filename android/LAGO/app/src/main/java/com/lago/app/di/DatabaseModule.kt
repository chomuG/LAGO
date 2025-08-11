package com.lago.app.di

import android.content.Context
import androidx.room.Room
import com.lago.app.data.local.LagoDatabase
import com.lago.app.data.local.dao.ChartCacheDao
import com.lago.app.data.local.cache.ChartCacheManager
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): LagoDatabase {
        return Room.databaseBuilder(
            context,
            LagoDatabase::class.java,
            "lago_database"
        )
        .fallbackToDestructiveMigration() // 개발 중이므로 DB 스키마 변경 시 재생성
        .build()
    }
    
    @Provides
    fun provideChartCacheDao(database: LagoDatabase): ChartCacheDao {
        return database.chartCacheDao()
    }
    
    @Provides
    @Singleton
    fun provideChartCacheManager(
        chartCacheDao: ChartCacheDao,
        gson: Gson
    ): ChartCacheManager {
        return ChartCacheManager(chartCacheDao, gson)
    }
    
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }
}