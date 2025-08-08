package com.lago.app.domain.usecase

import com.lago.app.domain.entity.PatternAnalysisResult
import com.lago.app.domain.repository.ChartRepository
import com.lago.app.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AnalyzeChartPatternUseCase @Inject constructor(
    private val chartRepository: ChartRepository
) {
    suspend operator fun invoke(
        stockCode: String,
        timeFrame: String,
        startTime: String? = null,
        endTime: String? = null
    ): Flow<Resource<PatternAnalysisResult>> {
        return chartRepository.analyzeChartPattern(
            stockCode = stockCode,
            timeFrame = timeFrame,
            startTime = startTime,
            endTime = endTime
        )
    }
}