package com.lago.app.domain.usecase

import com.lago.app.domain.entity.Quiz
import com.lago.app.domain.repository.StudyRepository
import javax.inject.Inject

class GetDailyQuizUseCase @Inject constructor(
    private val studyRepository: StudyRepository
) {
    suspend operator fun invoke(): Result<Quiz> {
        return studyRepository.getDailyQuiz()
    }
}