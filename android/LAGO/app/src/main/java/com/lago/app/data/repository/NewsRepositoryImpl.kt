package com.lago.app.data.repository

import com.lago.app.domain.entity.News
import com.lago.app.domain.repository.NewsRepository
import com.lago.app.domain.service.NewsService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepositoryImpl @Inject constructor(
    private val newsService: NewsService
) : NewsRepository {
    
    override suspend fun getNews(): Result<List<News>> {
        return newsService.getNews()
    }
    
    override suspend fun getInterestNews(): Result<List<News>> {
        return newsService.getInterestNews()
    }
}