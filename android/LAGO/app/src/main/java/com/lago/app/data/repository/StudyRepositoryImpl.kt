package com.lago.app.data.repository

import com.lago.app.data.remote.StudyApiService
import com.lago.app.data.remote.dto.DailyQuizSolveRequest
import com.lago.app.data.remote.dto.RandomQuizSolveRequest
import com.lago.app.data.remote.dto.toEntity
import com.lago.app.domain.entity.ChartPattern
import com.lago.app.domain.entity.DailyQuizResult
import com.lago.app.domain.entity.DailyQuizStatus
import com.lago.app.domain.entity.DailyQuizStreak
import com.lago.app.domain.entity.Quiz
import com.lago.app.domain.entity.QuizResult
import com.lago.app.domain.entity.Term
import com.lago.app.domain.repository.StudyRepository
import javax.inject.Inject

class StudyRepositoryImpl @Inject constructor(
    private val studyApiService: StudyApiService
) : StudyRepository {
    
    override suspend fun getTerms(userId: Int?): Result<List<Term>> {
        return try {
            val response = if (userId != null) {
                studyApiService.getTerms(userId)
            } else {
                studyApiService.getTerms()
            }
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
    
    override suspend fun getRandomQuiz(excludeQuizId: Int): Result<Quiz> {
        return try {
            val response = studyApiService.getRandomQuiz(excludeQuizId)
            Result.success(response.toEntity())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun solveRandomQuiz(userId: Int, quizId: Int, userAnswer: Boolean): Result<QuizResult> {
        return try {
            val request = RandomQuizSolveRequest(userId, quizId, userAnswer)
            val response = studyApiService.solveRandomQuiz(request)
            Result.success(
                QuizResult(
                    correct = response.correct,
                    score = response.score,
                    explanation = response.explanation
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getDailyQuiz(userId: Int): Result<DailyQuizStatus> {
        return try {
            val response = studyApiService.getDailyQuiz(userId)
            Result.success(
                DailyQuizStatus(
                    alreadySolved = response.alreadySolved,
                    quiz = response.quiz?.toEntity(),
                    solvedAt = response.solvedAt,
                    score = response.score,
                    ranking = response.ranking
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun solveDailyQuiz(userId: Int, quizId: Int, userAnswer: Boolean, solvedTimeSeconds: Int): Result<DailyQuizResult> {
        return try {
            val request = DailyQuizSolveRequest(userId, quizId, userAnswer, solvedTimeSeconds)
            val response = studyApiService.solveDailyQuiz(request)
            Result.success(
                DailyQuizResult(
                    correct = response.correct,
                    score = response.score,
                    ranking = response.ranking,
                    bonusAmount = response.bonusAmount,
                    explanation = response.explanation
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getDailyQuizStreak(userId: Int): Result<DailyQuizStreak> {
        return try {
            val response = studyApiService.getDailyQuizStreak(userId)
            Result.success(
                DailyQuizStreak(
                    userId = response.userId,
                    streak = response.streak
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}