package com.lago.app.domain.entity

data class News(
    val newsId: Int,
    val title: String,
    val sentiment: String,
    val publishedAt: String
)