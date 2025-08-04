package com.lago.app.di

import com.lago.app.data.service.NewsServiceImpl
import com.lago.app.domain.service.NewsService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {

    @Binds
    @Singleton
    abstract fun bindNewsService(
        newsServiceImpl: NewsServiceImpl
    ): NewsService
}