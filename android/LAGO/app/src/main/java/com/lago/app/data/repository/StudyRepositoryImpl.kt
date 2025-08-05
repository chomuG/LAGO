package com.lago.app.data.repository

import com.lago.app.data.remote.StudyApiService
import com.lago.app.data.remote.dto.toEntity
import com.lago.app.domain.entity.ChartPattern
import com.lago.app.domain.entity.Quiz
import com.lago.app.domain.entity.Term
import com.lago.app.domain.repository.StudyRepository
import javax.inject.Inject

class StudyRepositoryImpl @Inject constructor(
    private val studyApiService: StudyApiService
) : StudyRepository {
    
    override suspend fun getTerms(): Result<List<Term>> {
        return try {
            val response = studyApiService.getTerms()
            Result.success(response.map { it.toEntity() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getChartPatterns(): Result<List<ChartPattern>> {
        return try {
            val response = studyApiService.getChartPatterns()
            Result.success(response.map { it.toEntity() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getRandomQuiz(): Result<Quiz> {
        return try {
            val response = studyApiService.getRandomQuiz()
            Result.success(response.toEntity())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}