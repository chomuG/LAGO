package com.lago.app.domain.service

import com.lago.app.domain.entity.News

interface NewsService {
    suspend fun getNews(): Result<List<News>>
    suspend fun getInterestNews(): Result<List<News>>
}