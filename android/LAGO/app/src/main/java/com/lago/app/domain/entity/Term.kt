package com.lago.app.domain.entity

data class Term(
    val termId: Int,
    val term: String,
    val description: String,
    val know: Boolean? // true: 안다, false: 모른다, null: 미학습
)