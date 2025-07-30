package com.lago.app.data.repository

import com.lago.app.data.local.LocalDataSource
import com.lago.app.data.remote.ApiResponse
import com.lago.app.data.remote.RemoteDataSource
import com.lago.app.data.remote.UpdateUserRequest
import com.lago.app.domain.repository.AuthToken
import com.lago.app.domain.repository.UserProfile
import com.lago.app.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource
) : UserRepository {
    
    override suspend fun getUserProfile(): Result<UserProfile> {
        return try {
            when (val response = remoteDataSource.getUserProfile("current")) {
                is ApiResponse.Success -> {
                    val userProfile = UserProfile(
                        id = response.data.id,
                        email = response.data.email,
                        name = response.data.name,
                        profileImageUrl = response.data.profileImageUrl
                    )
                    Result.success(userProfile)
                }
                is ApiResponse.Error -> {
                    Result.failure(Exception(response.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            val updateRequest = UpdateUserRequest(
                name = profile.name,
                profileImageUrl = profile.profileImageUrl
            )
            
            when (val response = remoteDataSource.updateUserProfile(profile.id, updateRequest)) {
                is ApiResponse.Success -> {
                    Result.success(Unit)
                }
                is ApiResponse.Error -> {
                    Result.failure(Exception(response.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun login(email: String, password: String): Result<AuthToken> {
        return try {
            when (val response = remoteDataSource.login(email, password)) {
                is ApiResponse.Success -> {
                    val authToken = AuthToken(
                        accessToken = response.data.accessToken,
                        refreshToken = response.data.refreshToken,
                        expiresIn = response.data.expiresIn
                    )
                    
                    // Save tokens locally
                    localDataSource.saveUserData(authToken.accessToken)
                    
                    Result.success(authToken)
                }
                is ApiResponse.Error -> {
                    Result.failure(Exception(response.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun logout(): Result<Unit> {
        return try {
            localDataSource.clearUserData()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun isUserLoggedIn(): Boolean {
        return try {
            localDataSource.getUserData() != null
        } catch (e: Exception) {
            false
        }
    }
}