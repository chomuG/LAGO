package com.lago.app.data.remote.dto

import com.lago.app.domain.entity.Term

data class TermDto(
    val term_id: Int,
    val term: String,
    val description: String,
    val know: Boolean?
)

fun TermDto.toEntity(): Term {
    return Term(
        termId = term_id,
        term = term,
        description = description,
        know = know
    )
}