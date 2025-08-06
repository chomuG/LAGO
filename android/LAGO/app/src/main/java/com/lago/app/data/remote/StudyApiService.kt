package com.lago.app.data.remote

import com.lago.app.data.remote.dto.ChartPatternDto
import com.lago.app.data.remote.dto.QuizDto
import com.lago.app.data.remote.dto.TermDto
import retrofit2.http.GET

interface StudyApiService {
    
    @GET("api/study/term")
    suspend fun getTerms(): List<TermDto>
    
    @GET("api/study/chart")
    suspend fun getChartPatterns(): List<ChartPatternDto>
    
    @GET("api/study/quiz/random")
    suspend fun getRandomQuiz(): QuizDto
    
    @GET("api/study/quiz/daily")
    suspend fun getDailyQuiz(): QuizDto
}