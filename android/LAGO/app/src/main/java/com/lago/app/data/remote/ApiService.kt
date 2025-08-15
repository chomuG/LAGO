package com.lago.app.data.remote

import com.lago.app.data.remote.dto.TransactionDto
import com.lago.app.data.remote.dto.UserCurrentStatusDto
import com.lago.app.data.remote.dto.RankingDto
import com.lago.app.data.remote.dto.HistoryChallengeDto
import retrofit2.http.*

interface ApiService {
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse
    
    @GET("users/{userId}")
    suspend fun getUserProfile(@Path("userId") userId: String): UserResponse
    
    @PUT("users/{userId}")
    suspend fun updateUserProfile(
        @Path("userId") userId: String,
        @Body request: UpdateUserRequest
    ): UserResponse
    
    @POST("auth/logout")
    suspend fun logout(): Unit

    @GET("api/accounts/{userId}/transactions")
    suspend fun getTransactions(@Path("userId") userId: Long): List<TransactionDto>

    @GET("api/accounts/{userId}/history")
    suspend fun getHistoryTransactions(@Path("userId") userId: Long): List<TransactionDto>

    @GET("api/users/{userId}/current-status")
    suspend fun getUserCurrentStatus(
        @Path("userId") userId: Int,
        @Query("type") type: Int = 0
    ): UserCurrentStatusDto

    @GET("api/ranking")
    suspend fun getRanking(): List<RankingDto>

    @GET("api/history-challenge")
    suspend fun getHistoryChallenge(): HistoryChallengeDto
}