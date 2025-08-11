package com.lago.app.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class StockRealTimeData(
    val stockCode: String,
    val currentPrice: Double,
    val priceChange: Double,
    val priceChangePercent: Double,
    val volume: Long,
    val timestamp: Long = System.currentTimeMillis()
)

data class PortfolioReturn(
    val stockCode: String,
    val stockName: String,
    val quantity: Int,
    val buyPrice: Double,
    val currentPrice: Double,
    val profit: Double,
    val returnRate: Double,
    val totalValue: Double
)

data class TotalPortfolioSummary(
    val totalInvestment: Double,
    val totalCurrentValue: Double,
    val totalProfit: Double,
    val totalReturnRate: Double,
    val stockReturns: List<PortfolioReturn>
)

enum class StockPriority {
    HOT,    // 현재 보고있는 종목 (차트 화면)
    WARM,   // 포트폴리오 + 관심종목
    COLD    // 전체 목록에서 가끔 보는 종목
}

enum class ScreenType {
    CHART,
    STOCK_LIST,
    PORTFOLIO,
    SUMMARY,
    NEWS
}