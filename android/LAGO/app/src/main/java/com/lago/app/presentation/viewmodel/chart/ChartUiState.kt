package com.lago.app.presentation.viewmodel.chart

import com.lago.app.domain.entity.*

data class ChartUiState(
    val currentStock: ChartStockInfo = ChartStockInfo(
        code = "005930",
        name = "삼성전자",
        currentPrice = 74200f,
        priceChange = 800f,
        priceChangePercent = 1.09f,
        previousDay = null
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
    val chartLoadingStage: ChartLoadingStage = ChartLoadingStage.INITIAL,
    val errorMessage: String? = null,
    val isFavorite: Boolean = false,
    val holdingItems: List<HoldingItem> = emptyList(),
    val tradingHistory: List<TradingItem> = emptyList(),
    val selectedBottomTab: Int = 0,
    val patternAnalysisCount: Int = 1,
    val maxPatternAnalysisCount: Int = 3,
    val patternAnalysis: com.lago.app.domain.entity.PatternAnalysisResult? = null,
    val isPatternAnalyzing: Boolean = false,
    val patternAnalysisError: String? = null,
    val showIndicatorSettings: Boolean = false,
    val tradingSignals: List<TradingSignal> = emptyList(),
    val showUserTradingSignals: Boolean = false,
    val selectedAI: SignalSource? = null,
    val isTrading: Boolean = false,
    val tradeMessage: String? = null,
    val lastTradeResult: com.lago.app.domain.entity.MockTradeResult? = null,
    val accountType: Int = 0, // 0=실시간모의투자, 1=역사챌린지
    val accountBalance: Long = 0L, // 보유 현금
    val profitRate: Float = 0f // 수익률
)

enum class ChartLoadingStage {
    INITIAL,        // 초기 상태
    DATA_LOADING,   // 데이터 로딩 중
    WEBVIEW_LOADING,// WebView HTML 로딩 중
    JS_READY,       // JavaScript 준비 완료
    CHART_READY     // 차트 렌더링 완료
}

data class HoldingItem(
    val name: String,
    val quantity: String,
    val value: Int,
    val change: Float,
    val stockCode: String
)

data class TradingItem(
    val type: String,
    val quantity: String,
    val amount: Int,
    val date: String,
    val stockCode: String = "005930" // 기본값으로 삼성전자 추가
)

