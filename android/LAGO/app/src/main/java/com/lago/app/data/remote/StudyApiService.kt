package com.lago.app.data.remote

import com.lago.app.data.remote.dto.ChartPatternDto
import com.lago.app.data.remote.dto.DailyQuizResponse
import com.lago.app.data.remote.dto.DailyQuizSolveRequest
import com.lago.app.data.remote.dto.DailyQuizSolveResponse
import com.lago.app.data.remote.dto.QuizDto
import com.lago.app.data.remote.dto.RandomQuizSolveRequest
import com.lago.app.data.remote.dto.RandomQuizSolveResponse
import com.lago.app.data.remote.dto.TermDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface StudyApiService {
    
    @GET("api/study/term")
    suspend fun getTerms(): List<TermDto>
    
    @GET("api/study/term")
    suspend fun getTerms(@Query("userId") userId: Int): List<TermDto>
    
    @GET("api/study/chart")
    suspend fun getChartPatterns(): List<ChartPatternDto>
    
    @GET("api/study/quiz/random")
    suspend fun getRandomQuiz(@Query("quiz_id") excludeQuizId: Int = 0): QuizDto
    
    @POST("api/study/quiz/random/solve")
    suspend fun solveRandomQuiz(@Body request: RandomQuizSolveRequest): RandomQuizSolveResponse
    
    @GET("api/study/daily-quiz")
    suspend fun getDailyQuiz(@Query("userId") userId: Int): DailyQuizResponse
    
    @POST("api/study/daily-quiz/solve")
    suspend fun solveDailyQuiz(@Body request: DailyQuizSolveRequest): DailyQuizSolveResponse
}