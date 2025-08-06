package com.lago.app.data.repository

import com.lago.app.data.remote.NewsApiService
import com.lago.app.data.remote.dto.NewsDto
import com.lago.app.domain.entity.News
import com.lago.app.domain.repository.NewsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepositoryImpl @Inject constructor(
    private val newsApiService: NewsApiService
) : NewsRepository {
    
    override suspend fun getNews(): Result<List<News>> {
        return try {
            val response = newsApiService.getNews()
            val newsList = response.map { it.toDomain() }
            Result.success(newsList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getInterestNews(): Result<List<News>> {
        return try {
            val response = newsApiService.getInterestNews()
            val newsList = response.map { it.toDomain() }
            Result.success(newsList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private fun NewsDto.toDomain(): News {
    return News(
        newsId = this.newsId,
        title = this.title,
        sentiment = this.sentiment,
        publishedAt = this.publishedAt
    )
}