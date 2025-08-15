package com.lago.app.data.remote

import com.lago.app.data.remote.dto.NewsDto
import com.lago.app.data.remote.dto.NewsPageResponse
import retrofit2.http.GET

interface NewsApiService {
    
    @GET("api/news")
    suspend fun getNews(): NewsPageResponse
    
    @GET("api/news/watchlist")
    suspend fun getInterestNews(): NewsPageResponse
}