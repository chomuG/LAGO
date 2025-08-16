package com.lago.app.domain.repository

import com.lago.app.domain.entity.News

interface NewsRepository {
    suspend fun getNews(): Result<List<News>>
    suspend fun getInterestNews(userId: Int): Result<List<News>>
    suspend fun getNewsDetail(newsId: Int): Result<News>
}