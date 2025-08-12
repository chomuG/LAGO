package com.lago.app.data.remote.dto

import com.lago.app.domain.entity.ChartPattern

data class ChartPatternDto(
    val pattern_id: Int,
    val name: String,
    val description: String,
    val chart_img: String
)

fun ChartPatternDto.toEntity(): ChartPattern {
    return ChartPattern(
        patternId = pattern_id,
        name = name,
        description = description,
        chartImg = chart_img
    )
}