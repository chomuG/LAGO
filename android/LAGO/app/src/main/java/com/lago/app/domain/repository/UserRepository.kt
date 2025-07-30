package com.lago.app.domain.repository

interface UserRepository {
    
    suspend fun getUserProfile(): Result<UserProfile>
    
    suspend fun updateUserProfile(profile: UserProfile): Result<Unit>
    
    suspend fun login(email: String, password: String): Result<AuthToken>
    
    suspend fun logout(): Result<Unit>
    
    suspend fun isUserLoggedIn(): Boolean
}

data class UserProfile(
    val id: String,
    val email: String,
    val name: String,
    val profileImageUrl: String? = null
)

data class AuthToken(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)