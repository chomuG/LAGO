package com.lago.app.data.remote

import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteDataSource @Inject constructor(
    private val retrofit: Retrofit
) {
    
    private val apiService by lazy { retrofit.create(ApiService::class.java) }
    
    suspend fun login(email: String, password: String): ApiResponse<AuthResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            ApiResponse.Success(response)
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Unknown error occurred")
        }
    }
    
    suspend fun getUserProfile(userId: String): ApiResponse<UserResponse> {
        return try {
            val response = apiService.getUserProfile(userId)
            ApiResponse.Success(response)
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Unknown error occurred")
        }
    }
    
    suspend fun updateUserProfile(userId: String, profile: UpdateUserRequest): ApiResponse<UserResponse> {
        return try {
            val response = apiService.updateUserProfile(userId, profile)
            ApiResponse.Success(response)
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Unknown error occurred")
        }
    }
}

sealed class ApiResponse<out T> {
    data class Success<T>(val data: T) : ApiResponse<T>()
    data class Error(val message: String) : ApiResponse<Nothing>()
}

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val user: UserResponse
)

data class UserResponse(
    val id: String,
    val email: String,
    val name: String,
    val profileImageUrl: String?
)

data class UpdateUserRequest(
    val name: String,
    val profileImageUrl: String?
)