package com.lago.app.data.remote.dto

import com.lago.app.domain.entity.Quiz

data class QuizDto(
    val quiz_id: Int,
    val question: String,
    val answer: Boolean,
    val daily_date: String? = null,
    val category: String? = null,
    val explanation: String,
    val term_id: Int
)

fun QuizDto.toEntity(): Quiz {
    return Quiz(
        quizId = quiz_id,
        question = question,
        answer = answer,
        dailyDate = daily_date,
        category = category ?: "랜덤",
        explanation = explanation,
        termId = term_id
    )
}