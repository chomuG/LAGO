package com.lago.app.data.remote

import com.lago.app.data.remote.dto.NewsDto
import com.lago.app.data.remote.dto.NewsPageResponse
import com.lago.app.data.remote.dto.HistoryChallengeNewsDto
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
    suspend fun getInterestNews(
        @Query("userId") userId: Int,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): NewsPageResponse
    
    @GET("api/news/{newsId}")
    suspend fun getNewsDetail(
        @Path("newsId") newsId: Int
    ): NewsDto
    
    @GET("api/history-challenge/{challengeId}/news")
    suspend fun getHistoryChallengeNews(
        @Path("challengeId") challengeId: Int,
        @Query("pastDateTime") pastDateTime: String
    ): List<HistoryChallengeNewsDto>
    
    @GET("api/history-challenge/{challengeId}/news/{challengeNewsId}")
    suspend fun getHistoryChallengeNewsDetail(
        @Path("challengeId") challengeId: Int,
        @Path("challengeNewsId") challengeNewsId: Int
    ): HistoryChallengeNewsDto
}