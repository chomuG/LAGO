package com.lago.app.domain.repository

import com.lago.app.domain.entity.ChartPattern
import com.lago.app.domain.entity.DailyQuizResult
import com.lago.app.domain.entity.DailyQuizStatus
import com.lago.app.domain.entity.Quiz
import com.lago.app.domain.entity.QuizResult
import com.lago.app.domain.entity.Term

interface StudyRepository {
    suspend fun getTerms(userId: Int?): Result<List<Term>>
    suspend fun getChartPatterns(): Result<List<ChartPattern>>
    suspend fun getRandomQuiz(excludeQuizId: Int = 0): Result<Quiz>
    suspend fun solveRandomQuiz(userId: Int, quizId: Int, userAnswer: Boolean): Result<QuizResult>
    suspend fun getDailyQuiz(userId: Int): Result<DailyQuizStatus>
    suspend fun solveDailyQuiz(userId: Int, quizId: Int, userAnswer: Boolean, solvedTimeSeconds: Int): Result<DailyQuizResult>
}