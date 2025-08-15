package com.lago.app.data.remote

import com.lago.app.data.remote.dto.NewsDto
import com.lago.app.data.remote.dto.NewsPageResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NewsApiService {
    
    @GET("api/news")
    suspend fun getNews(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): NewsPageResponse
    
    @GET("api/news/watchlist")
    suspend fun getInterestNews(): NewsPageResponse
    
    @GET("api/news/{newsId}")
    suspend fun getNewsDetail(
        @Path("newsId") newsId: Int
    ): NewsDto
}