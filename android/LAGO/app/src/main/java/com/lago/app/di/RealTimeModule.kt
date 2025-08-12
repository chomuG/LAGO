package com.lago.app.di

import com.lago.app.data.cache.RealTimeStockCache
import com.lago.app.data.remote.websocket.SmartStockWebSocketService
import com.lago.app.data.scheduler.SmartUpdateScheduler
import com.lago.app.data.local.prefs.UserPreferences
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RealTimeModule {
    // @Inject constructor 사용으로 자동 바인딩됨
}