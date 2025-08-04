package com.lago.app.domain.repository

import com.lago.app.domain.entity.Term

interface StudyRepository {
    suspend fun getTerms(): Result<List<Term>>
}