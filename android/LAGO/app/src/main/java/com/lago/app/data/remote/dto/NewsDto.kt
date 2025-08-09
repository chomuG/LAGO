package com.lago.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class NewsDto(
    @SerializedName("news_id")
    val newsId: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("sentiment")
    val sentiment: String,
    @SerializedName("publishedAt")
    val publishedAt: String
)

data class NewsListResponse(
    val data: List<NewsDto>
)