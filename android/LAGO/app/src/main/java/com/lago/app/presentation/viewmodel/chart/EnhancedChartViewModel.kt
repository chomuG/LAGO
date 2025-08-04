package com.lago.app.presentation.viewmodel.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lago.app.domain.entity.*
import com.lago.app.domain.repository.ChartRepository
import com.lago.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnhancedChartViewModel @Inject constructor(
    private val chartRepository: ChartRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChartUiState())
    val uiState: StateFlow<ChartUiState> = _uiState.asStateFlow()
    
    private val _uiEvent = MutableSharedFlow<ChartUiEvent>()
    
    init {
        loadInitialData()
    }
    
    fun onEvent(event: ChartUiEvent) {
        when (event) {
            is ChartUiEvent.ChangeStock -> changeStock(event.stockCode)
            is ChartUiEvent.ChangeTimeFrame -> changeTimeFrame(event.timeFrame)
            is ChartUiEvent.ToggleIndicator -> toggleIndicator(event.indicatorType, event.enabled)
            is ChartUiEvent.RefreshData -> refreshData()
            is ChartUiEvent.ToggleFavorite -> toggleFavorite()
            is ChartUiEvent.ChangeBottomTab -> changeBottomTab(event.tabIndex)
            is ChartUiEvent.AnalyzePattern -> analyzePattern()
            is ChartUiEvent.BackPressed -> handleBackPressed()
            is ChartUiEvent.BuyClicked -> handleBuyClicked()
            is ChartUiEvent.SellClicked -> handleSellClicked()
            is ChartUiEvent.ShowIndicatorSettings -> showIndicatorSettings()
            is ChartUiEvent.HideIndicatorSettings -> hideIndicatorSettings()
            is ChartUiEvent.ClearError -> clearErrorMessage()
        }
    }
    
    private fun loadInitialData() {
        val defaultStockCode = "005930" // Samsung Electronics
        changeStock(defaultStockCode)
        loadUserHoldings()
        loadTradingHistory()
    }
    
    private fun changeStock(stockCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            // Load stock info
            chartRepository.getStockInfo(stockCode).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.update { 
                            it.copy(
                                currentStock = resource.data ?: StockInfo("", "", 0f, 0f, 0f),
                                config = it.config.copy(stockCode = stockCode)
                            )
                        }
                        
                        // Load chart data after stock info is loaded
                        loadChartData(stockCode, _uiState.value.config.timeFrame)
                        checkFavoriteStatus(stockCode)
                    }
                    is Resource.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                errorMessage = resource.message
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }
    
    private fun loadChartData(stockCode: String, timeFrame: String) {
        viewModelScope.launch {
            // Load candlestick data
            launch {
                chartRepository.getCandlestickData(stockCode, timeFrame).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _uiState.update { 
                                it.copy(candlestickData = resource.data ?: emptyList())
                            }
                        }
                        is Resource.Error -> {
                            _uiState.update { 
                                it.copy(errorMessage = resource.message)
                            }
                        }
                        is Resource.Loading -> {}
                    }
                }
            }
            
            // Load volume data
            launch {
                chartRepository.getVolumeData(stockCode, timeFrame).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _uiState.update { 
                                it.copy(volumeData = resource.data ?: emptyList())
                            }
                        }
                        is Resource.Error -> {}
                        is Resource.Loading -> {}
                    }
                }
            }
            
            // Load indicators
            loadIndicators(stockCode, timeFrame)
            
            _uiState.update { it.copy(isLoading = false) }
        }
    }
    
    private fun loadIndicators(stockCode: String, timeFrame: String) {
        viewModelScope.launch {
            val enabledIndicators = mutableListOf<String>()
            val currentConfig = _uiState.value.config.indicators
            
            if (currentConfig.sma5) enabledIndicators.add("sma5")
            if (currentConfig.sma20) enabledIndicators.add("sma20")
            if (currentConfig.sma60) enabledIndicators.add("sma60")
            if (currentConfig.sma120) enabledIndicators.add("sma120")
            if (currentConfig.rsi) enabledIndicators.add("rsi")
            if (currentConfig.macd) enabledIndicators.add("macd")
            if (currentConfig.bollingerBands) enabledIndicators.add("bollinger_bands")
            
            if (enabledIndicators.isNotEmpty()) {
                chartRepository.getIndicators(stockCode, enabledIndicators, timeFrame).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            val data = resource.data
                            if (data != null) {
                                _uiState.update { 
                                    it.copy(
                                        sma5Data = data.sma5,
                                        sma20Data = data.sma20,
                                        rsiData = data.rsi,
                                        macdData = data.macd,
                                        bollingerBands = data.bollingerBands
                                    )
                                }
                            }
                        }
                        is Resource.Error -> {}
                        is Resource.Loading -> {}
                    }
                }
            }
        }
    }
    
    private fun changeTimeFrame(timeFrame: String) {
        _uiState.update { 
            it.copy(
                config = it.config.copy(timeFrame = timeFrame)
            )
        }
        
        // Reload chart data with new timeframe
        loadChartData(_uiState.value.currentStock.code, timeFrame)
    }
    
    private fun toggleIndicator(indicatorType: String, enabled: Boolean) {
        val currentConfig = _uiState.value.config
        val updatedIndicators = when (indicatorType) {
            "sma5" -> currentConfig.indicators.copy(sma5 = enabled)
            "sma20" -> currentConfig.indicators.copy(sma20 = enabled)
            "sma60" -> currentConfig.indicators.copy(sma60 = enabled)
            "sma120" -> currentConfig.indicators.copy(sma120 = enabled)
            "rsi" -> currentConfig.indicators.copy(rsi = enabled)
            "macd" -> currentConfig.indicators.copy(macd = enabled)
            "bollingerBands" -> currentConfig.indicators.copy(bollingerBands = enabled)
            "volume" -> currentConfig.indicators.copy(volume = enabled)
            else -> currentConfig.indicators
        }
        
        _uiState.update { 
            it.copy(
                config = currentConfig.copy(indicators = updatedIndicators)
            )
        }
        
        // Reload indicators with updated configuration
        loadIndicators(_uiState.value.currentStock.code, _uiState.value.config.timeFrame)
    }
    
    private fun loadUserHoldings() {
        viewModelScope.launch {
            chartRepository.getUserHoldings().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.update { 
                            it.copy(holdingItems = resource.data ?: emptyList())
                        }
                    }
                    is Resource.Error -> {
                        // Handle silently for non-critical data
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }
    
    private fun loadTradingHistory() {
        viewModelScope.launch {
            chartRepository.getTradingHistory().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.update { 
                            it.copy(tradingHistory = resource.data?.content ?: emptyList())
                        }
                    }
                    is Resource.Error -> {
                        // Handle silently for non-critical data
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }
    
    private fun toggleFavorite() {
        val stockCode = _uiState.value.currentStock.code
        val isFavorite = _uiState.value.isFavorite
        
        viewModelScope.launch {
            val resource = if (isFavorite) {
                chartRepository.removeFromFavorites(stockCode)
            } else {
                chartRepository.addToFavorites(stockCode)
            }
            
            resource.collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update { 
                            it.copy(isFavorite = !isFavorite)
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { 
                            it.copy(errorMessage = result.message)
                        }
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }
    
    private fun checkFavoriteStatus(stockCode: String) {
        viewModelScope.launch {
            chartRepository.isFavorite(stockCode).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.update { 
                            it.copy(isFavorite = resource.data == true)
                        }
                    }
                    is Resource.Error -> {}
                    is Resource.Loading -> {}
                }
            }
        }
    }
    
    private fun refreshData() {
        val currentStock = _uiState.value.currentStock.code
        val currentTimeFrame = _uiState.value.config.timeFrame
        
        loadChartData(currentStock, currentTimeFrame)
        loadUserHoldings()
        loadTradingHistory()
    }
    
    private fun changeBottomTab(tabIndex: Int) {
        _uiState.update { 
            it.copy(selectedBottomTab = tabIndex)
        }
    }
    
    private fun analyzePattern() {
        // TODO: Implement pattern analysis logic
        // This could call an AI service or use local algorithms
        _uiState.update { 
            it.copy(
                patternAnalysis = PatternAnalysisResult(
                    patternType = "Bullish Flag",
                    confidence = 0.75f,
                    description = "상승 플래그 패턴이 감지되었습니다.",
                    recommendation = "매수 신호"
                )
            )
        }
    }
    
    private fun handleBackPressed() {
        // Handle back navigation logic
    }
    
    private fun handleBuyClicked() {
        // Handle buy button click - navigate to purchase screen
    }
    
    private fun handleSellClicked() {
        // Handle sell button click - navigate to purchase screen
    }
    
    private fun showIndicatorSettings() {
        _uiState.update { 
            it.copy(showIndicatorSettings = true)
        }
    }
    
    private fun hideIndicatorSettings() {
        _uiState.update { 
            it.copy(showIndicatorSettings = false)
        }
    }
    
    private fun clearErrorMessage() {
        _uiState.update { 
            it.copy(errorMessage = null)
        }
    }
}