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
            android.util.Log.d("NewsRepository", "📰 뉴스 API 응답: totalElements=${response.totalElements}, content 크기=${response.content.size}")
            // content 배열에서 실제 뉴스 데이터 추출
            val newsList = response.content.map { it.toDomain() }
            Result.success(newsList)
        } catch (e: Exception) {
            android.util.Log.e("NewsRepository", "📰 뉴스 로드 실패: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getInterestNews(): Result<List<News>> {
        return try {
            val response = newsApiService.getInterestNews()
            android.util.Log.d("NewsRepository", "📰 관심뉴스 API 응답: totalElements=${response.totalElements}, content 크기=${response.content.size}")
            // content 배열에서 실제 뉴스 데이터 추출
            val newsList = response.content.map { it.toDomain() }
            Result.success(newsList)
        } catch (e: Exception) {
            android.util.Log.e("NewsRepository", "📰 관심뉴스 로드 실패: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getNewsDetail(newsId: Int): Result<News> {
        return try {
            val response = newsApiService.getNewsDetail(newsId)
            android.util.Log.d("NewsRepository", "📰 뉴스 상세 API 응답: newsId=${response.newsId}, title=${response.title}")
            val news = response.toDomain()
            Result.success(news)
        } catch (e: Exception) {
            android.util.Log.e("NewsRepository", "📰 뉴스 상세 로드 실패: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }
}

private fun NewsDto.toDomain(): News {
    return News(
        newsId = this.newsId,
        title = this.title,
        content = this.content,
        summary = this.summary,
        sentiment = this.sentiment,
        publishedAt = this.publishedAt,
        type = this.type
    )
}