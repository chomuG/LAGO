package com.lago.app.domain.usecase

import com.lago.app.domain.entity.ChartPattern
import com.lago.app.domain.repository.StudyRepository
import javax.inject.Inject

class GetChartPatternsUseCase @Inject constructor(
    private val studyRepository: StudyRepository
) {
    suspend operator fun invoke(): Result<List<ChartPattern>> {
        return studyRepository.getChartPatterns()
    }
}