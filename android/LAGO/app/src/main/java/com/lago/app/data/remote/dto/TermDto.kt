package com.lago.app.data.remote.dto

import com.lago.app.domain.entity.Term

data class TermDto(
    val termId: Int,
    val term: String,
    val definition: String,
    val description: String,
    val knowStatus: Boolean?
)

fun TermDto.toEntity(): Term {
    return Term(
        termId = termId,
        term = term,
        definition = definition,
        description = description,
        knowStatus = knowStatus
    )
}