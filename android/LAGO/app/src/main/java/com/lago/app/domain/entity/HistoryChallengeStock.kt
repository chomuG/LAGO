package com.lago.app.domain.entity

data class HistoryChallengeStock(
    val challengeId: Int,
    val stockCode: String,
    val stockName: String,
    val currentPrice: Float,
    val openPrice: Float,
    val highPrice: Float,
    val lowPrice: Float,
    val closePrice: Float,
    val fluctuationRate: Float,
    val tradingVolume: Long,
    val marketCap: Long? = null,
    val profitRate: Float? = null
)