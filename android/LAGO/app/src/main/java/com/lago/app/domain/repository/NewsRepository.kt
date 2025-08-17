package com.lago.app.domain.repository

import com.lago.app.domain.entity.News
import com.lago.app.domain.entity.HistoryChallengeNews

interface NewsRepository {
    suspend fun getNews(): Result<List<News>>
    suspend fun getInterestNews(userId: Int): Result<List<News>>
    suspend fun getNewsDetail(newsId: Int): Result<News>
    suspend fun getHistoryChallengeNews(challengeId: Int, pastDateTime: String): Result<List<HistoryChallengeNews>>
    suspend fun getHistoryChallengeNewsDetail(challengeId: Int, challengeNewsId: Int): Result<HistoryChallengeNews>
}