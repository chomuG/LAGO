package com.lago.app.presentation.viewmodel.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lago.app.domain.entity.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ChartViewModel @Inject constructor(
    // TODO: Repository 주입
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChartUiState())
    val uiState: StateFlow<ChartUiState> = _uiState.asStateFlow()
    
    private val stockInfoMap = mapOf(
        "005930" to StockInfo("005930", "삼성전자", 74200f, 800f, 1.09f),
        "000660" to StockInfo("000660", "SK하이닉스", 135000f, -2000f, -1.46f),
        "035420" to StockInfo("035420", "NAVER", 185000f, 1500f, 0.82f),
        "035720" to StockInfo("035720", "카카오", 45300f, -700f, -1.52f),
        "373220" to StockInfo("373220", "LG에너지솔루션", 420000f, 8000f, 1.94f)
    )
    
    init {
        loadInitialData()
        loadMockHoldings()
        loadMockTradingHistory()
    }
    
    fun onEvent(event: ChartUiEvent) {
        when (event) {
            is ChartUiEvent.ChangeStock -> changeStock(event.stockCode)
            is ChartUiEvent.ChangeTimeFrame -> changeTimeFrame(event.timeFrame)
            is ChartUiEvent.ToggleIndicator -> toggleIndicator(event.indicatorType, event.enabled)
            is ChartUiEvent.RefreshData -> refreshData()
            is ChartUiEvent.ClearError -> clearErrorMessage()
            is ChartUiEvent.ToggleFavorite -> toggleFavorite()
            is ChartUiEvent.ChangeBottomTab -> changeBottomTab(event.tabIndex)
            is ChartUiEvent.AnalyzePattern -> analyzePattern()
            is ChartUiEvent.BackPressed -> handleBackPressed()
            is ChartUiEvent.BuyClicked -> handleBuyClicked()
            is ChartUiEvent.SellClicked -> handleSellClicked()
            is ChartUiEvent.ShowIndicatorSettings -> showIndicatorSettings()
            is ChartUiEvent.HideIndicatorSettings -> hideIndicatorSettings()
            is ChartUiEvent.ToggleIndicatorSettings -> toggleIndicatorSettings()
            // UpdatePanelSizes 이벤트 제거 - 단순화된 구조에서는 불필요
        }
    }
    
    private fun changeStock(stockCode: String) {
        val stockInfo = stockInfoMap[stockCode] ?: return
        
        _uiState.value = _uiState.value.copy(
            currentStock = stockInfo,
            config = _uiState.value.config.copy(stockCode = stockCode)
        )
        
        loadChartData()
    }
    
    private fun changeTimeFrame(timeFrame: String) {
        _uiState.value = _uiState.value.copy(
            config = _uiState.value.config.copy(timeFrame = timeFrame)
        )
        
        loadChartData()
    }
    
    private fun toggleIndicator(indicatorType: String, enabled: Boolean) {
        val currentIndicators = _uiState.value.config.indicators
        val newIndicators = when (indicatorType) {
            "sma5" -> currentIndicators.copy(sma5 = enabled)
            "sma20" -> currentIndicators.copy(sma20 = enabled)
            "sma" -> currentIndicators.copy(
                sma5 = enabled,
                sma20 = enabled
            )
            "bollinger" -> currentIndicators.copy(bollingerBands = enabled)
            "volume" -> currentIndicators.copy(volume = enabled)
            "macd" -> currentIndicators.copy(macd = enabled)
            "rsi" -> currentIndicators.copy(rsi = enabled)
            else -> currentIndicators
        }
        
        _uiState.value = _uiState.value.copy(
            config = _uiState.value.config.copy(indicators = newIndicators)
        )
        
        // 지표 상태 변경 시 항상 차트 새로고침 (켜거나 끌 때 모두)
        if (enabled) {
            loadIndicatorData(indicatorType)
        } else {
            // 지표를 끄는 경우에도 차트 새로고침
            loadChartData()
        }
    }
    
    private fun refreshData() {
        loadChartData()
    }
    
    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                loadChartData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "데이터 로딩 실패: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    private fun loadChartData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                // TODO: 실제 API 호출로 대체
                val mockCandlestickData = generateMockCandlestickData()
                val mockVolumeData = generateMockVolumeData()
                
                _uiState.value = _uiState.value.copy(
                    candlestickData = mockCandlestickData,
                    volumeData = mockVolumeData,
                    isLoading = false
                )
                
                // 활성화된 지표들 로드
                val indicators = _uiState.value.config.indicators
                if (indicators.sma5 || indicators.sma20) {
                    loadSmaData()
                }
                if (indicators.rsi) {
                    loadRsiData()
                }
                if (indicators.macd) {
                    loadMacdData()
                }
                if (indicators.bollingerBands) {
                    loadBollingerBandsData()
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "차트 데이터 로딩 실패: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    private fun loadIndicatorData(indicatorType: String) {
        viewModelScope.launch {
            try {
                when (indicatorType) {
                    "sma" -> loadSmaData()
                    "rsi" -> loadRsiData()
                    "macd" -> loadMacdData()
                    "bollinger" -> loadBollingerBandsData()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "지표 데이터 로딩 실패: ${e.message}"
                )
            }
        }
    }
    
    private suspend fun loadSmaData() {
        val candlestickData = _uiState.value.candlestickData
        if (candlestickData.isEmpty()) return
        
        val sma5Data = calculateSMA(candlestickData, 5)
        val sma20Data = calculateSMA(candlestickData, 20)
        
        _uiState.value = _uiState.value.copy(
            sma5Data = sma5Data,
            sma20Data = sma20Data
        )
    }
    
    private suspend fun loadRsiData() {
        val candlestickData = _uiState.value.candlestickData
        if (candlestickData.isEmpty()) return
        
        val rsiData = calculateRSI(candlestickData, 14)
        
        _uiState.value = _uiState.value.copy(
            rsiData = rsiData
        )
    }
    
    private suspend fun loadMacdData() {
        val candlestickData = _uiState.value.candlestickData
        if (candlestickData.isEmpty()) return
        
        val macdData = calculateMACD(candlestickData, 12, 26, 9)
        
        _uiState.value = _uiState.value.copy(
            macdData = macdData
        )
    }
    
    private suspend fun loadBollingerBandsData() {
        val candlestickData = _uiState.value.candlestickData
        if (candlestickData.isEmpty()) return
        
        val bollingerBands = calculateBollingerBands(candlestickData, 20, 2.0f)
        
        _uiState.value = _uiState.value.copy(
            bollingerBands = bollingerBands
        )
    }
    
    private fun generateMockCandlestickData(): List<CandlestickData> {
        val basePrice = _uiState.value.currentStock.currentPrice
        val calendar = Calendar.getInstance()
        
        return (0..29).map { i ->
            val tempCalendar = Calendar.getInstance()
            tempCalendar.add(Calendar.DAY_OF_MONTH, -(29 - i))
            val time = tempCalendar.timeInMillis
            
            val open = (basePrice + (Math.random() - 0.5) * 1000).toFloat()
            val close = (open + (Math.random() - 0.5) * 500).toFloat()
            val high = (maxOf(open, close) + Math.random() * 300).toFloat()
            val low = (minOf(open, close) - Math.random() * 300).toFloat()
            val volume = (Math.random() * 1000000).toLong()
            
            CandlestickData(time, open, high, low, close, volume)
        }
    }
    
    private fun generateMockVolumeData(): List<VolumeData> {
        return (0..29).map { i ->
            val tempCalendar = Calendar.getInstance()
            tempCalendar.add(Calendar.DAY_OF_MONTH, -(29 - i))
            val time = tempCalendar.timeInMillis
            val value = (Math.random() * 1000000).toFloat()
            VolumeData(time, value)
        }
    }
    
    private fun generateMockLineData(offset: Float): List<LineData> {
        val basePrice = _uiState.value.currentStock.currentPrice
        return (0..29).map { i ->
            val tempCalendar = Calendar.getInstance()
            tempCalendar.add(Calendar.DAY_OF_MONTH, -(29 - i))
            val time = tempCalendar.timeInMillis
            val value = (basePrice + offset + (Math.random() - 0.5) * 200).toFloat()
            LineData(time, value)
        }
    }
    
    private fun generateMockRsiData(): List<LineData> {
        return (0..29).map { i ->
            val tempCalendar = Calendar.getInstance()
            tempCalendar.add(Calendar.DAY_OF_MONTH, -(29 - i))
            val time = tempCalendar.timeInMillis
            val value = (30 + Math.random() * 40).toFloat() // RSI는 보통 30-70 범위
            LineData(time, value)
        }
    }
    
    private fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    private fun toggleFavorite() {
        _uiState.value = _uiState.value.copy(
            isFavorite = !_uiState.value.isFavorite
        )
    }
    
    private fun changeBottomTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(
            selectedBottomTab = tabIndex
        )
    }
    
    private fun analyzePattern() {
        if (_uiState.value.patternAnalysisCount < _uiState.value.maxPatternAnalysisCount) {
            _uiState.value = _uiState.value.copy(
                patternAnalysisCount = _uiState.value.patternAnalysisCount + 1,
                lastPatternAnalysis = PatternAnalysisResult(
                    patternName = "상승 삼각형",
                    description = "저항선을 여러 차례 돌파 시도했지만\n매번에 상승 가능성이 높습니다.",
                    analysisTime = "2025-07-28 오후 1시 35분"
                )
            )
        }
    }
    
    private fun handleBackPressed() {
        // TODO: Navigation 처리
    }
    
    
    private fun handleBuyClicked() {
        // TODO: 구매 화면 이동
    }
    
    private fun handleSellClicked() {
        // TODO: 판매 화면 이동
    }
    
    private fun loadMockHoldings() {
        val mockHoldings = listOf(
            HoldingItem("삼성전자", "10주", 1000400, 5.2f),
            HoldingItem("GS리테일", "10주", 1000400, 2.1f),
            HoldingItem("한화생명", "10주", 1000400, -1.5f),
            HoldingItem("LG전자", "10주", 1000400, 0.8f),
            HoldingItem("삼성전자", "10주", 1000400, 3.2f)
        )
        _uiState.value = _uiState.value.copy(holdings = mockHoldings)
    }
    
    private fun loadMockTradingHistory() {
        val mockHistory = listOf(
            TradingItem("구매", "10주", 712000, "2025-07-28 오전10:48"),
            TradingItem("구매", "10주", 712000, "2025-07-28 오전10:48"),
            TradingItem("구매", "10주", 712000, "2025-07-28 오전10:48"),
            TradingItem("구매", "10주", 712000, "2025-07-28 오전10:48"),
            TradingItem("구매", "10주", 712000, "2025-07-28 오전10:48")
        )
        _uiState.value = _uiState.value.copy(tradingHistory = mockHistory)
    }
    
    private fun showIndicatorSettings() {
        _uiState.value = _uiState.value.copy(showIndicatorSettings = true)
    }
    
    private fun hideIndicatorSettings() {
        _uiState.value = _uiState.value.copy(showIndicatorSettings = false)
    }
    
    private fun toggleIndicatorSettings() {
        _uiState.value = _uiState.value.copy(
            showIndicatorSettings = !_uiState.value.showIndicatorSettings
        )
    }
    
    // updatePanelSizes 함수 제거 - 단순화된 구조에서는 불필요
    
    // 기술적 지표 계산 함수들
    
    /**
     * Simple Moving Average (단순이동평균) 계산
     */
    private fun calculateSMA(candlestickData: List<CandlestickData>, period: Int): List<LineData> {
        if (candlestickData.size < period) return emptyList()
        
        val result = mutableListOf<LineData>()
        
        for (i in period - 1 until candlestickData.size) {
            val sum = candlestickData.subList(i - period + 1, i + 1).sumOf { it.close.toDouble() }
            val average = (sum / period).toFloat()
            
            result.add(LineData(candlestickData[i].time, average))
        }
        
        return result
    }
    
    /**
     * RSI (Relative Strength Index) 계산
     */
    private fun calculateRSI(candlestickData: List<CandlestickData>, period: Int): List<LineData> {
        if (candlestickData.size < period + 1) return emptyList()
        
        val result = mutableListOf<LineData>()
        val gains = mutableListOf<Float>()
        val losses = mutableListOf<Float>()
        
        // 첫 번째 기간의 gain/loss 계산
        for (i in 1 until candlestickData.size) {
            val change = candlestickData[i].close - candlestickData[i - 1].close
            gains.add(if (change > 0) change else 0f)
            losses.add(if (change < 0) -change else 0f)
        }
        
        // RSI 계산
        for (i in period - 1 until gains.size) {
            val avgGain = gains.subList(i - period + 1, i + 1).average().toFloat()
            val avgLoss = losses.subList(i - period + 1, i + 1).average().toFloat()
            
            val rs = if (avgLoss != 0f) avgGain / avgLoss else 100f
            val rsi = 100f - (100f / (1f + rs))
            
            result.add(LineData(candlestickData[i + 1].time, rsi))
        }
        
        return result
    }
    
    /**
     * MACD (Moving Average Convergence Divergence) 계산
     */
    private fun calculateMACD(candlestickData: List<CandlestickData>, fastPeriod: Int, slowPeriod: Int, signalPeriod: Int): MACDResult {
        if (candlestickData.size < slowPeriod) return MACDResult(emptyList(), emptyList(), emptyList())
        
        // EMA 계산 함수
        fun calculateEMA(data: List<Float>, period: Int): List<Float> {
            if (data.size < period) return emptyList()
            
            val result = mutableListOf<Float>()
            val multiplier = 2f / (period + 1f)
            
            // 첫 번째 EMA는 SMA로 시작
            var ema = data.subList(0, period).average().toFloat()
            result.add(ema)
            
            // 나머지 EMA 계산
            for (i in period until data.size) {
                ema = (data[i] * multiplier) + (ema * (1f - multiplier))
                result.add(ema)
            }
            
            return result
        }
        
        val prices = candlestickData.map { it.close }
        val fastEMA = calculateEMA(prices, fastPeriod)
        val slowEMA = calculateEMA(prices, slowPeriod)
        
        // MACD 라인 계산 (Fast EMA - Slow EMA)
        val macdLine = mutableListOf<LineData>()
        val macdValues = mutableListOf<Float>()
        
        val startIndex = slowPeriod - 1
        for (i in 0 until minOf(fastEMA.size, slowEMA.size)) {
            val macdValue = fastEMA[i + (fastPeriod - slowPeriod)] - slowEMA[i]
            macdValues.add(macdValue)
            macdLine.add(LineData(candlestickData[startIndex + i].time, macdValue))
        }
        
        // Signal 라인 계산 (MACD의 EMA)
        val signalEMA = calculateEMA(macdValues, signalPeriod)
        val signalLine = mutableListOf<LineData>()
        
        for (i in signalEMA.indices) {
            signalLine.add(LineData(candlestickData[startIndex + signalPeriod - 1 + i].time, signalEMA[i]))
        }
        
        // 히스토그램 계산 (MACD - Signal)
        val histogram = mutableListOf<VolumeData>()
        for (i in signalEMA.indices) {
            val histValue = macdValues[signalPeriod - 1 + i] - signalEMA[i]
            histogram.add(VolumeData(candlestickData[startIndex + signalPeriod - 1 + i].time, histValue))
        }
        
        return MACDResult(macdLine, signalLine, histogram)
    }
    
    /**
     * Bollinger Bands (볼린저 밴드) 계산
     */
    private fun calculateBollingerBands(candlestickData: List<CandlestickData>, period: Int, multiplier: Float): BollingerBandsResult {
        if (candlestickData.size < period) return BollingerBandsResult(emptyList(), emptyList(), emptyList())
        
        val upperBand = mutableListOf<LineData>()
        val middleBand = mutableListOf<LineData>()
        val lowerBand = mutableListOf<LineData>()
        
        for (i in period - 1 until candlestickData.size) {
            val prices = candlestickData.subList(i - period + 1, i + 1).map { it.close }
            
            // SMA (중간선)
            val sma = prices.average().toFloat()
            
            // 표준편차 계산
            val variance = prices.map { (it - sma) * (it - sma) }.average()
            val standardDeviation = kotlin.math.sqrt(variance).toFloat()
            
            // 밴드 계산
            val upper = sma + (standardDeviation * multiplier)
            val lower = sma - (standardDeviation * multiplier)
            
            val time = candlestickData[i].time
            upperBand.add(LineData(time, upper))
            middleBand.add(LineData(time, sma))
            lowerBand.add(LineData(time, lower))
        }
        
        return BollingerBandsResult(upperBand, middleBand, lowerBand)
    }
}