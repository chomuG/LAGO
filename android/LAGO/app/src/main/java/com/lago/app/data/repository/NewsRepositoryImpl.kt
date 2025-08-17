package com.lago.app.data.repository

import com.lago.app.data.remote.NewsApiService
import com.lago.app.data.remote.dto.NewsDto
import com.lago.app.data.remote.dto.HistoryChallengeNewsDto
import com.lago.app.domain.entity.News
import com.lago.app.domain.entity.HistoryChallengeNews
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
    
    override suspend fun getInterestNews(userId: Int): Result<List<News>> {
        return try {
            android.util.Log.d("NewsRepository", "📰 관심뉴스 API 호출 시작 - userId: $userId, page: 0, size: 20")
            val response = newsApiService.getInterestNews(userId = userId, page = 0, size = 20)
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
    
    override suspend fun getHistoryChallengeNews(challengeId: Int, pastDateTime: String): Result<List<HistoryChallengeNews>> {
        return try {
            android.util.Log.d("NewsRepository", "📰 역사적 챌린지 뉴스 API 호출 시작 - challengeId: $challengeId, pastDateTime: $pastDateTime")
            val response = newsApiService.getHistoryChallengeNews(challengeId, pastDateTime)
            android.util.Log.d("NewsRepository", "📰 역사적 챌린지 뉴스 API 응답: 뉴스 개수=${response.size}")
            val newsList = response.map { it.toDomain() }
            Result.success(newsList)
        } catch (e: Exception) {
            android.util.Log.e("NewsRepository", "📰 역사적 챌린지 뉴스 로드 실패: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getHistoryChallengeNewsDetail(challengeId: Int, challengeNewsId: Int): Result<HistoryChallengeNews> {
        return try {
            android.util.Log.d("NewsRepository", "📰 역사적 챌린지 뉴스 상세 API 호출 시작 - challengeId: $challengeId, challengeNewsId: $challengeNewsId")
            val response = newsApiService.getHistoryChallengeNewsDetail(challengeId, challengeNewsId)
            android.util.Log.d("NewsRepository", "📰 역사적 챌린지 뉴스 상세 API 응답: title=${response.title}")
            val news = response.toDomain()
            Result.success(news)
        } catch (e: Exception) {
            android.util.Log.e("NewsRepository", "📰 역사적 챌린지 뉴스 상세 로드 실패: ${e.localizedMessage}", e)
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

private fun HistoryChallengeNewsDto.toDomain(): HistoryChallengeNews {
    return HistoryChallengeNews(
        challengeNewsId = this.challengeNewsId,
        challengeId = this.challengeId,
        title = this.title,
        content = this.content,
        publishedAt = this.publishedAt
    )
}