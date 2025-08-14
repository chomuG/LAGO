package com.lago.app.data.remote

import com.lago.app.data.remote.dto.UserCurrentStatusDto
import com.lago.app.data.remote.dto.RankingDto
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
    
    @GET("api/users/{userId}/current-status")
    suspend fun getUserCurrentStatus(@Path("userId") userId: Int): UserCurrentStatusDto
    
    @GET("api/ranking")
    suspend fun getRanking(): List<RankingDto>
}