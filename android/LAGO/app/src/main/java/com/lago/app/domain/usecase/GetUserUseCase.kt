package com.lago.app.domain.usecase

import com.lago.app.domain.repository.UserProfile
import com.lago.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    
    suspend operator fun invoke(): Result<UserProfile> {
        return userRepository.getUserProfile()
    }
    
    fun invokeAsFlow(): Flow<Result<UserProfile>> = flow {
        emit(userRepository.getUserProfile())
    }
}

@Singleton
class UpdateUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    
    suspend operator fun invoke(profile: UserProfile): Result<Unit> {
        return if (isValidProfile(profile)) {
            userRepository.updateUserProfile(profile)
        } else {
            Result.failure(IllegalArgumentException("Invalid profile data"))
        }
    }
    
    private fun isValidProfile(profile: UserProfile): Boolean {
        return profile.email.isNotBlank() && 
               profile.name.isNotBlank() &&
               profile.name.length >= 2
    }
}

@Singleton
class LoginUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        return if (isValidCredentials(email, password)) {
            userRepository.login(email, password).map { Unit }
        } else {
            Result.failure(IllegalArgumentException("Invalid credentials"))
        }
    }
    
    private fun isValidCredentials(email: String, password: String): Boolean {
        return email.contains("@") && 
               email.contains(".") &&
               password.length >= 8
    }
}

@Singleton
class LogoutUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    
    suspend operator fun invoke(): Result<Unit> {
        return userRepository.logout()
    }
}

@Singleton
class CheckLoginStatusUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    
    suspend operator fun invoke(): Boolean {
        return userRepository.isUserLoggedIn()
    }
}