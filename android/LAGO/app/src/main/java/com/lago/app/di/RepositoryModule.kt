package com.lago.app.di

//import com.lago.app.data.repository.LagoRepositoryImpl
//import com.lago.app.domain.repository.LagoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

//    @Binds
//    @Singleton
//    abstract fun bindLagoRepository(
//        lagoRepositoryImpl: LagoRepositoryImpl
//    ): LagoRepository
}