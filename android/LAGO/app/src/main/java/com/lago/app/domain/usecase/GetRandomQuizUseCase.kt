package com.lago.app.domain.usecase

import com.lago.app.domain.entity.Quiz
import com.lago.app.domain.repository.StudyRepository
import javax.inject.Inject

class GetRandomQuizUseCase @Inject constructor(
    private val repository: StudyRepository
) {
    suspend operator fun invoke(excludeQuizId: Int = 0): Result<Quiz> = repository.getRandomQuiz(excludeQuizId)
}