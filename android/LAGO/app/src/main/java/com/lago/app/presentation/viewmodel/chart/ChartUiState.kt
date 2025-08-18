package com.lago.app.presentation.viewmodel.chart

import com.lago.app.domain.entity.*

data class ChartUiState(
    val currentStock: ChartStockInfo = ChartStockInfo(
        code = "",
        name = "",
        currentPrice = 0f,
        priceChange = 0f,
        priceChangePercent = 0f,
        previousDay = null
    ),
    val config: ChartConfig = ChartConfig(
        stockCode = "",
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
    val isLoading: Boolean = true,
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
    val patternAnalysisStage: PatternAnalysisStage = PatternAnalysisStage.STAGE_3,
    val availablePatterns: List<ChartPattern> = getDefaultPatterns(),
    val selectedPattern: ChartPattern? = null,
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
    CHART_READY,    // 차트 렌더링 완료
    COMPLETED       // 🔥 모든 로딩 완료 (데이터 + 지표)
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
    val stockCode: String = ""
)

enum class PatternAnalysisStage {
    STAGE_3,  // 3개 패턴 표시
    STAGE_2,  // 2개 패턴 표시  
    STAGE_1   // 1개 패턴 표시 (최종)
}

data class ChartPattern(
    val name: String,
    val reason: String
)

/**
 * 기본 패턴 목록 반환
 */
fun getDefaultPatterns(): List<ChartPattern> {
    return listOf(
        ChartPattern("더블 바텀 패턴", "2025-07-29와 2025-07-29에 저점이 반복 형성되었으며, 아직 넥라인 돌파는 발생하지 않았습니다."),
        ChartPattern("페넌트 패턴", "패턴이 감지되었으나, 상세 정보를 생성할 수 없습니다."),
        ChartPattern("대칭 삼각형", "수렴형 삼각형 패턴으로, 고점과 저점이 점점 좁아지고 있습니다. 변동성 확대가 예상됩니다. (2025-08-06, 2025-08-07 기준)")
    )
}

