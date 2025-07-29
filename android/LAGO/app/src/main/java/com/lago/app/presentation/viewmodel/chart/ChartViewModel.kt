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
            is ChartUiEvent.NotificationClicked -> handleNotificationClicked()
            is ChartUiEvent.SettingsClicked -> handleSettingsClicked()
            is ChartUiEvent.BuyClicked -> handleBuyClicked()
            is ChartUiEvent.SellClicked -> handleSellClicked()
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
        
        // 필요한 경우 데이터 다시 로드
        if (enabled) {
            loadIndicatorData(indicatorType)
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
        // TODO: 실제 SMA 계산 로직 구현
        val mockSma5 = generateMockLineData(50f)
        val mockSma20 = generateMockLineData(100f)
        
        _uiState.value = _uiState.value.copy(
            sma5Data = mockSma5,
            sma20Data = mockSma20
        )
    }
    
    private suspend fun loadRsiData() {
        // TODO: 실제 RSI 계산 로직 구현
        val mockRsi = generateMockRsiData()
        
        _uiState.value = _uiState.value.copy(
            rsiData = mockRsi
        )
    }
    
    private suspend fun loadMacdData() {
        // TODO: 실제 MACD 계산 로직 구현
        val mockMacd = MACDResult(
            macdLine = generateMockLineData(0f),
            signalLine = generateMockLineData(0f),
            histogram = generateMockVolumeData()
        )
        
        _uiState.value = _uiState.value.copy(
            macdData = mockMacd
        )
    }
    
    private suspend fun loadBollingerBandsData() {
        // TODO: 실제 볼린저 밴드 계산 로직 구현
        val mockBollinger = BollingerBandsResult(
            upperBand = generateMockLineData(200f),
            middleBand = generateMockLineData(0f),
            lowerBand = generateMockLineData(-200f)
        )
        
        _uiState.value = _uiState.value.copy(
            bollingerBands = mockBollinger
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
    
    private fun handleNotificationClicked() {
        // TODO: 알림 화면 이동
    }
    
    private fun handleSettingsClicked() {
        // TODO: 설정 화면 이동
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
}