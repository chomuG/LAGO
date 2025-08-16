package com.lago.app.domain.usecase

import com.lago.app.domain.entity.News
import com.lago.app.domain.repository.NewsRepository
import javax.inject.Inject

class GetInterestNewsUseCase @Inject constructor(
    private val newsRepository: NewsRepository
) {
    suspend operator fun invoke(userId: Int): Result<List<News>> {
        return newsRepository.getInterestNews(userId)
    }
}