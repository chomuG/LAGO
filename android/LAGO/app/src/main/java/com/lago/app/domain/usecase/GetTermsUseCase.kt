package com.lago.app.domain.usecase

import com.lago.app.domain.entity.Term
import com.lago.app.domain.repository.StudyRepository
import javax.inject.Inject

class GetTermsUseCase @Inject constructor(
    private val studyRepository: StudyRepository
) {
    suspend operator fun invoke(): Result<List<Term>> {
        return studyRepository.getTerms()
    }
}