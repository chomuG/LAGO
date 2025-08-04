package com.lago.app.domain.repository

import com.lago.app.domain.entity.ChartPattern
import com.lago.app.domain.entity.Term

interface StudyRepository {
    suspend fun getTerms(): Result<List<Term>>
    suspend fun getChartPatterns(): Result<List<ChartPattern>>
}