package com.lago.app.domain.entity

data class StockItem(
    val code: String,
    val name: String,
    val market: String, // "KOSPI", "KOSDAQ"
    val currentPrice: Int,
    val priceChange: Int,
    val priceChangePercent: Double,
    val volume: Long,
    val marketCap: Long?,
    val sector: String?,
    val isFavorite: Boolean = false,
    val updatedAt: String
)

data class StockListPage(
    val content: List<StockItem>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)