package com.lago.app.data.remote

import com.lago.app.data.remote.dto.NewsDto
import retrofit2.http.GET

interface NewsApiService {
    
    @GET("api/news")
    suspend fun getNews(): List<NewsDto>
    
    @GET("api/news/interest")
    suspend fun getInterestNews(): List<NewsDto>
}