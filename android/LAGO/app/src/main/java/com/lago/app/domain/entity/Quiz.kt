package com.lago.app.domain.entity

data class Quiz(
    val quizId: Int,
    val question: String,
    val answer: Boolean,
    val dailyDate: String?,
    val category: String,
    val explanation: String,
    val termId: Int
)