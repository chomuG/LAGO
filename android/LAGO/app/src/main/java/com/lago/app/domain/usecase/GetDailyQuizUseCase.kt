package com.lago.app.domain.usecase

import com.lago.app.domain.entity.DailyQuizStatus
import com.lago.app.domain.repository.StudyRepository
import javax.inject.Inject

class GetDailyQuizUseCase @Inject constructor(
    private val studyRepository: StudyRepository
) {
    suspend operator fun invoke(userId: Int): Result<DailyQuizStatus> {
        return studyRepository.getDailyQuiz(userId)
    }
}