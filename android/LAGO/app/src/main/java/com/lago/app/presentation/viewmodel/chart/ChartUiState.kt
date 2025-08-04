package com.lago.app.presentation.viewmodel.chart

import com.lago.app.domain.entity.*

data class ChartUiState(
    val currentStock: StockInfo = StockInfo(
        code = "005930",
        name = "삼성전자",
        currentPrice = 74200f,
        priceChange = 800f,
        priceChangePercent = 1.09f
    ),
    val config: ChartConfig = ChartConfig(
        stockCode = "005930",
        timeFrame = "10",
        indicators = ChartIndicators()
    ),
    val candlestickData: List<CandlestickData> = emptyList(),
    val volumeData: List<VolumeData> = emptyList(),
    val rsiData: List<LineData> = emptyList(),
    val macdData: MACDResult? = null,
    val sma5Data: List<LineData> = emptyList(),
    val sma20Data: List<LineData> = emptyList(),
    val sma60Data: List<LineData> = emptyList(),
    val sma120Data: List<LineData> = emptyList(),
    val bollingerBands: BollingerBandsResult? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isFavorite: Boolean = false,
    val holdingItems: List<HoldingItem> = emptyList(),
    val tradingHistory: List<TradingItem> = emptyList(),
    val selectedBottomTab: Int = 0,
    val patternAnalysisCount: Int = 1,
    val maxPatternAnalysisCount: Int = 3,
    val patternAnalysis: PatternAnalysisResult? = null,
    val showIndicatorSettings: Boolean = false
)

data class HoldingItem(
    val name: String,
    val quantity: String,
    val value: Int,
    val change: Float
)

data class TradingItem(
    val type: String,
    val quantity: String,
    val amount: Int,
    val date: String
)

data class PatternAnalysisResult(
    val patternName: String,
    val description: String,
    val analysisTime: String
)