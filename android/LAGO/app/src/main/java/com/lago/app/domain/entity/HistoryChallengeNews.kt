package com.lago.app.domain.entity

data class HistoryChallengeNews(
    val challengeNewsId: Int,
    val challengeId: Int,
    val title: String,
    val content: String,
    val publishedAt: String
)