package com.lago.app.di

import android.content.Context
import androidx.room.Room
import com.lago.app.data.local.LagoDatabase
import com.lago.app.data.cache.ChartMemoryCache
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
    @Singleton
    fun provideChartMemoryCache(): ChartMemoryCache {
        return ChartMemoryCache()
    }
}