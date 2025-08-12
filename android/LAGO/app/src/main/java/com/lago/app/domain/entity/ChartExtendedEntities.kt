package com.lago.app.domain.entity

// Chart Indicator Data Container
data class ChartIndicatorData(
    val sma5: List<LineData>,
    val sma20: List<LineData>,
    val sma60: List<LineData>,
    val sma120: List<LineData>,
    val rsi: List<LineData>,
    val macd: MACDResult?,
    val bollingerBands: BollingerBandsResult?
)

// Trading History Page
data class TradingHistoryPage(
    val content: List<TradingItem>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)

// User Holdings/Trading Items (already referenced in ChartViewModel)
data class HoldingItem(
    val stockCode: String,
    val stockName: String,
    val quantity: Int,
    val averagePrice: Float,
    val currentPrice: Float,
    val profitLoss: Float,
    val profitLossPercent: Float,
    val totalValue: Float
)

data class TradingItem(
    val transactionId: String,
    val stockCode: String,
    val stockName: String,
    val actionType: String, // "BUY" or "SELL"
    val quantity: Int,
    val price: Float,
    val totalAmount: Float,
    val createdAt: String
)

// Pattern Analysis Result (already referenced in ChartViewModel)
data class PatternAnalysisResult(
    val patterns: List<PatternItem>,
    val analysisTime: String,
    val confidenceScore: Float
)

data class PatternItem(
    val patternName: String,
    val description: String,
    val confidence: Float,
    val recommendation: String?
)