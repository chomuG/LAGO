package com.lago.app.data.remote

import com.lago.app.data.remote.dto.TermDto
import retrofit2.http.GET

interface StudyApiService {
    
    @GET("api/study/term")
    suspend fun getTerms(): List<TermDto>
}