package com.lago.app.data.repository

import com.lago.app.data.local.LocalDataSource
import com.lago.app.data.local.prefs.UserPreferences
import com.lago.app.data.remote.ApiResponse
import com.lago.app.data.remote.RemoteDataSource
import com.lago.app.data.remote.UpdateUserRequest
import com.lago.app.data.remote.dto.UserCurrentStatusDto
import com.lago.app.data.remote.dto.HistoryChallengeDto
import com.lago.app.domain.repository.AuthToken
import com.lago.app.domain.repository.UserProfile
import com.lago.app.domain.repository.UserRepository
import com.lago.app.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
    private val userPreferences: UserPreferences
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
                    
                    // 개발용: userId를 5로 고정
                    userPreferences.saveUserId(5L)
                    android.util.Log.d("UserRepository", "Login successful - userId set to 5")
                    
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
            userPreferences.clearAllData() // userId도 함께 지움
            android.util.Log.d("UserRepository", "Logout successful - userId cleared")
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
    
    override suspend fun getUserCurrentStatus(userId: Int, type: Int): Flow<Resource<UserCurrentStatusDto>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = remoteDataSource.getUserCurrentStatus(userId, type)
            emit(Resource.Success(response))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "사용자 현재 상황 조회 실패"))
        }
    }
    
    override suspend fun getHistoryChallenge(): Flow<Resource<HistoryChallengeDto>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = remoteDataSource.getHistoryChallenge()
            emit(Resource.Success(response))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "역사챌린지 조회 실패"))
        }
    }
}