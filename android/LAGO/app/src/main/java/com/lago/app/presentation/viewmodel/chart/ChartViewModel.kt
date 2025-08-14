package com.lago.app.presentation.viewmodel.chart

// import androidx.compose.runtime.snapshotFlow - ViewModel에서는 사용 안함
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lago.app.domain.entity.*
import com.lago.app.domain.repository.ChartRepository
import com.lago.app.domain.usecase.AnalyzeChartPatternUseCase
import com.lago.app.data.local.prefs.UserPreferences
import com.lago.app.data.remote.websocket.SmartStockWebSocketService
import com.lago.app.data.scheduler.SmartUpdateScheduler
import com.lago.app.domain.entity.ScreenType
import com.lago.app.data.remote.dto.WebSocketConnectionState
import com.lago.app.data.cache.ChartMemoryCache
import com.lago.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlin.time.Duration.Companion.milliseconds
import javax.inject.Inject

@HiltViewModel
class ChartViewModel @Inject constructor(
    private val chartRepository: ChartRepository,
    private val analyzeChartPatternUseCase: AnalyzeChartPatternUseCase,
    private val userPreferences: UserPreferences,
    private val smartWebSocketService: SmartStockWebSocketService,
    private val smartUpdateScheduler: SmartUpdateScheduler,
    private val memoryCache: ChartMemoryCache,
    private val realTimeCache: com.lago.app.data.cache.RealTimeStockCache
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChartUiState())
    val uiState: StateFlow<ChartUiState> = _uiState.asStateFlow()
    
    private val _uiEvent = MutableSharedFlow<ChartUiEvent>()
    
    // 안전 타임아웃을 위한 Job
    private var chartLoadingTimeoutJob: Job? = null
    
    
    init {
        loadInitialData()
        // 웹소켓은 SmartStockWebSocketService에서 통합 관리
        observeRealTimePrice()
    }
    
    private fun observeRealTimePrice() {
        viewModelScope.launch {
            // 현재 차트 종목의 실시간 데이터 구독
            _uiState
                .map { it.currentStock.code }
                .filter { it.isNotBlank() }
                .distinctUntilChanged()
                .flatMapLatest { stockCode ->
                    android.util.Log.d("ChartViewModel", "📊 차트 종목 변경: $stockCode")
                    // 해당 종목의 Flow를 구독
                    realTimeCache.symbolFlow(stockCode)
                        .sample(100.milliseconds) // 차트는 100ms마다 업데이트
                }
                .collect { realTimeData ->
                    _uiState.update { state ->
                        state.copy(
                            currentStock = state.currentStock.copy(
                                currentPrice = realTimeData.price.toFloat(),
                                priceChange = realTimeData.priceChange.toFloat(),
                                priceChangePercent = realTimeData.priceChangePercent.toFloat()
                            )
                        )
                    }
                    android.util.Log.d("ChartViewModel", "📈 차트 가격 업데이트: ${realTimeData.stockCode} = ${realTimeData.price.toInt()}원")
                }
        }
    }
    
    fun onEvent(event: ChartUiEvent) {
        when (event) {
            is ChartUiEvent.ChangeStock -> changeStock(event.stockCode)
            is ChartUiEvent.ChangeStockWithInfo -> changeStockWithInfo(event.stockCode, event.stockInfo)
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
            is ChartUiEvent.ToggleIndicatorSettings -> toggleIndicatorSettings()
            is ChartUiEvent.LoadTradingSignals -> loadTradingSignals()
            is ChartUiEvent.ToggleUserTradingSignals -> toggleUserTradingSignals(event.show)
            is ChartUiEvent.SelectAITradingSignals -> selectAITradingSignals(event.aiSource)
            is ChartUiEvent.ClearError -> clearErrorMessage()
        }
    }
    
    private fun loadInitialData() {
        // 저장된 설정 불러오기
        val savedTimeFrame = userPreferences.getChartTimeFrame()
        val savedIndicators = userPreferences.getChartIndicators()
        
        // ChartIndicators 객체 생성
        val chartIndicators = ChartIndicators(
            sma5 = savedIndicators.contains("sma5"),
            sma20 = savedIndicators.contains("sma20"),
            sma60 = savedIndicators.contains("sma60"),
            sma120 = savedIndicators.contains("sma120"),
            rsi = savedIndicators.contains("rsi"),
            macd = savedIndicators.contains("macd"),
            bollingerBands = savedIndicators.contains("bollingerBands"),
            volume = savedIndicators.contains("volume")
        )
        
        // 초기 상태에 저장된 설정 적용
        _uiState.update { currentState ->
            currentState.copy(
                config = currentState.config.copy(
                    timeFrame = savedTimeFrame,
                    indicators = chartIndicators
                )
            )
        }
        
        val defaultStockCode = "005930" // Samsung Electronics
        changeStock(defaultStockCode)
        loadUserHoldings()
        loadTradingHistory()
    }
    
    private fun changeStock(stockCode: String) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isLoading = true, 
                    chartLoadingStage = ChartLoadingStage.DATA_LOADING,
                    errorMessage = null
                ) 
            }
            
            // 실제 서버에서 주식 정보 가져오기
            try {
                chartRepository.getStockInfo(stockCode).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            resource.data?.let { serverStockInfo ->
                                _uiState.update { 
                                    it.copy(
                                        currentStock = serverStockInfo,
                                        config = it.config.copy(stockCode = stockCode),
                                        chartLoadingStage = ChartLoadingStage.DATA_LOADING,
                                        isLoading = false
                                    )
                                }
                                
                                // 스마트 웹소켓에 차트 종목 변경 알림 (HOT 우선순위)
                                smartWebSocketService.updateChartStock(stockCode)
                                
                                // 주식 정보 로드 후 차트 데이터 로드
                                loadChartData(stockCode, _uiState.value.config.timeFrame)
                                checkFavoriteStatus(stockCode)
                                
                                // 실시간 데이터 구독은 SmartStockWebSocketService에서 자동 관리됨
                            }
                        }
                        is Resource.Error -> {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    errorMessage = "주식 정보를 불러올 수 없습니다: ${resource.message}"
                                )
                            }
                        }
                        is Resource.Loading -> {
                            _uiState.update { 
                                it.copy(
                                    isLoading = true,
                                    chartLoadingStage = ChartLoadingStage.DATA_LOADING
                                ) 
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "네트워크 연결 실패: ${e.localizedMessage}"
                    )
                }
            }
        }
    }
    
    private fun changeStockWithInfo(stockCode: String, stockInfo: ChartStockInfo) {
        viewModelScope.launch {
            // 즉시 StockList에서 가져온 정보로 UI 업데이트
            _uiState.update { 
                it.copy(
                    currentStock = stockInfo,
                    config = it.config.copy(stockCode = stockCode),
                    isLoading = true,
                    chartLoadingStage = ChartLoadingStage.DATA_LOADING,
                    errorMessage = null
                ) 
            }
            
            // 스마트 웹소켓에 차트 종목 변경 알림 (HOT 우선순위)
            smartWebSocketService.updateChartStock(stockCode)
            
            // 차트 데이터는 여전히 서버에서 가져와야 함
            loadChartData(stockCode, _uiState.value.config.timeFrame)
            checkFavoriteStatus(stockCode)
            
            // 실시간 데이터 구독은 SmartStockWebSocketService에서 자동 관리됨
        }
    }
    
    private fun loadChartData(stockCode: String, timeFrame: String) {
        viewModelScope.launch {
            try {
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
                                    it.copy(
                                        errorMessage = "차트 데이터를 불러올 수 없습니다: ${resource.message}",
                                        isLoading = false
                                    )
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
                            is Resource.Error -> {
                                _uiState.update { 
                                    it.copy(
                                        errorMessage = "거래량 데이터를 불러올 수 없습니다: ${resource.message}",
                                        isLoading = false
                                    )
                                }
                            }
                            is Resource.Loading -> {}
                        }
                    }
                }
                
                // Load indicators
                loadIndicators(stockCode, timeFrame)
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        errorMessage = "차트 데이터 로드 실패: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
            }
            
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    chartLoadingStage = ChartLoadingStage.JS_READY
                ) 
            }
        }
    }
    
    private fun loadIndicators(stockCode: String, timeFrame: String) {
        viewModelScope.launch {
            try {
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
                            is Resource.Error -> {
                                // Fallback to mock calculation
                                calculateMockIndicators()
                            }
                            is Resource.Loading -> {}
                        }
                    }
                }
            } catch (e: Exception) {
                calculateMockIndicators()
            }
        }
    }
    
    private fun changeTimeFrame(timeFrame: String) {
        val stockCode = _uiState.value.currentStock.code
        
        _uiState.update { 
            it.copy(
                config = it.config.copy(timeFrame = timeFrame)
            )
        }
        
        // 설정 저장
        userPreferences.setChartTimeFrame(timeFrame)
        
        // Reload chart data with new timeframe
        loadChartData(stockCode, timeFrame)
        
        // 실시간 구독은 SmartStockWebSocketService에서 자동 관리됨
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
        
        // 설정 저장
        val indicatorSet = mutableSetOf<String>()
        if (updatedIndicators.sma5) indicatorSet.add("sma5")
        if (updatedIndicators.sma20) indicatorSet.add("sma20")
        if (updatedIndicators.sma60) indicatorSet.add("sma60")
        if (updatedIndicators.sma120) indicatorSet.add("sma120")
        if (updatedIndicators.rsi) indicatorSet.add("rsi")
        if (updatedIndicators.macd) indicatorSet.add("macd")
        if (updatedIndicators.bollingerBands) indicatorSet.add("bollingerBands")
        if (updatedIndicators.volume) indicatorSet.add("volume")
        userPreferences.setChartIndicators(indicatorSet)
        
        // Reload indicators with updated configuration
        loadIndicators(_uiState.value.currentStock.code, _uiState.value.config.timeFrame)
    }
    
    private fun loadUserHoldings() {
        viewModelScope.launch {
            try {
                chartRepository.getUserHoldings().collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            // Convert Domain HoldingItem to UI HoldingItem
                            val uiHoldings = resource.data?.map { domainItem ->
                                HoldingItem(
                                    name = domainItem.stockName,
                                    quantity = "${domainItem.quantity}주",
                                    value = domainItem.totalValue.toInt(),
                                    change = domainItem.profitLossPercent,
                                    stockCode = domainItem.stockCode
                                )
                            } ?: emptyList()
                            
                            _uiState.update { 
                                it.copy(holdingItems = uiHoldings)
                            }
                        }
                        is Resource.Error -> {
                            // Use mock data
                            loadMockHoldings()
                        }
                        is Resource.Loading -> {}
                    }
                }
            } catch (e: Exception) {
                loadMockHoldings()
            }
        }
    }
    
    private fun loadTradingHistory() {
        viewModelScope.launch {
            try {
                chartRepository.getTradingHistory().collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            // Convert Domain TradingItem to UI TradingItem
                            val uiTradings = resource.data?.content?.map { domainItem ->
                                TradingItem(
                                    type = if (domainItem.actionType == "BUY") "구매" else "판매",
                                    quantity = "${domainItem.quantity}주",
                                    amount = domainItem.totalAmount.toInt(),
                                    date = domainItem.createdAt
                                )
                            } ?: emptyList()
                            
                            _uiState.update { 
                                it.copy(tradingHistory = uiTradings)
                            }
                        }
                        is Resource.Error -> {
                            // Use mock data
                            loadMockTradingHistory()
                        }
                        is Resource.Loading -> {}
                    }
                }
            } catch (e: Exception) {
                loadMockTradingHistory()
            }
        }
    }
    
    private fun toggleFavorite() {
        val stockCode = _uiState.value.currentStock.code
        val isFavorite = _uiState.value.isFavorite
        
        viewModelScope.launch {
            try {
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
            } catch (e: Exception) {
                // Toggle locally as fallback
                _uiState.update { 
                    it.copy(isFavorite = !isFavorite)
                }
            }
        }
    }
    
    private fun checkFavoriteStatus(stockCode: String) {
        viewModelScope.launch {
            try {
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
            } catch (e: Exception) {
                // Ignore error for non-critical feature
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
        viewModelScope.launch {
            analyzeChartPatternUseCase(
                stockCode = _uiState.value.currentStock.code,
                timeFrame = _uiState.value.config.timeFrame
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { 
                            it.copy(
                                isPatternAnalyzing = true,
                                patternAnalysisError = null
                            )
                        }
                    }
                    is Resource.Success -> {
                        _uiState.update { 
                            it.copy(
                                isPatternAnalyzing = false,
                                patternAnalysis = resource.data,
                                patternAnalysisCount = it.patternAnalysisCount + 1,
                                patternAnalysisError = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { 
                            it.copy(
                                isPatternAnalyzing = false,
                                patternAnalysisError = resource.message
                            )
                        }
                    }
                }
            }
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
    
    private fun toggleIndicatorSettings() {
        _uiState.update { 
            it.copy(showIndicatorSettings = !it.showIndicatorSettings)
        }
    }
    
    private fun clearErrorMessage() {
        _uiState.update { 
            it.copy(errorMessage = null)
        }
    }
    
    // 3단계 로딩 시스템을 위한 새로운 함수들
    fun onChartLoadingChanged(isLoading: Boolean) {
        if (isLoading) {
            // 웹뷰 로딩 시작 시 안전 타임아웃 설정 (10초)
            startChartLoadingTimeout()
            _uiState.update { 
                it.copy(
                    isLoading = true,
                    chartLoadingStage = ChartLoadingStage.WEBVIEW_LOADING
                )
            }
        } else {
            // 로딩 종료 시 타임아웃 취소
            cancelChartLoadingTimeout()
        }
    }
    
    fun onChartReady() {
        // 차트 렌더링 완료 시 타임아웃 취소
        cancelChartLoadingTimeout()
        _uiState.update { 
            it.copy(
                isLoading = false,
                chartLoadingStage = ChartLoadingStage.CHART_READY
            )
        }
    }
    
    private fun startChartLoadingTimeout() {
        cancelChartLoadingTimeout()
        chartLoadingTimeoutJob = viewModelScope.launch {
            delay(5000) // 5초 타임아웃으로 단축
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    chartLoadingStage = ChartLoadingStage.CHART_READY,
                    errorMessage = "차트 로딩 시간이 초과되었습니다. 네트워크 상태를 확인해주세요."
                )
            }
        }
    }
    
    private fun cancelChartLoadingTimeout() {
        chartLoadingTimeoutJob?.cancel()
        chartLoadingTimeoutJob = null
    }
    
    // ===== WEBSOCKET METHODS =====
    
    /**
     * 웹소켓 초기화 및 실시간 데이터 구독
     */
    private fun initializeWebSocket() {
        viewModelScope.launch {
            // 웹소켓 연결
            smartWebSocketService.connect()

            // 연결 상태 모니터링
            smartWebSocketService.connectionState.collect { state ->
                android.util.Log.d("ChartViewModel", "WebSocket connection state: $state")
                // UI 상태에 연결 상태 반영 가능
            }
        }

        viewModelScope.launch {
            // SmartStockWebSocketService 연결 상태 모니터링
            smartWebSocketService.connectionState.collect { state ->
                android.util.Log.d("ChartViewModel", "WebSocket connection state: $state")
                when (state) {
                    WebSocketConnectionState.CONNECTED -> {
                        // 연결 성공 시 필요한 처리
                    }
                    WebSocketConnectionState.ERROR -> {
                        _uiState.update {
                            it.copy(errorMessage = "실시간 데이터 연결 오류")
                        }
                    }
                    else -> {
                        // 다른 상태 처리
                    }
                }
            }
        }
    }

    /**
     * 실시간 캔들스틱 데이터로 차트 업데이트
     */
    private fun updateRealtimeCandlestick(realtimeData: com.lago.app.data.remote.dto.RealtimeCandlestickDto) {
        val currentStockCode = _uiState.value.currentStock.code
        
        // 현재 보고 있는 주식의 데이터인지 확인
        if (realtimeData.symbol != currentStockCode) {
            return
        }
        
        val newCandlestick = CandlestickData(
            time = realtimeData.timestamp,
            open = realtimeData.open,
            high = realtimeData.high,
            low = realtimeData.low,
            close = realtimeData.close,
            volume = realtimeData.volume
        )
        
        // 메모리 캐시에 실시간 데이터 추가
        memoryCache.appendCandlestickData(realtimeData.symbol, realtimeData.timeframe, newCandlestick)
        
        // UI 상태 업데이트를 위해 캐시에서 최신 데이터 가져오기
        val updatedData = memoryCache.getCandlestickData(realtimeData.symbol, realtimeData.timeframe) 
            ?: _uiState.value.candlestickData
        
        _uiState.update { 
            it.copy(
                candlestickData = updatedData,
                // 거래량 데이터도 함께 업데이트
                volumeData = updatedData.map { candle ->
                    VolumeData(
                        time = candle.time,
                        value = candle.volume.toFloat(),
                        color = if (candle.close >= candle.open) "#ef5350" else "#26a69a"
                    )
                }
            )
        }
    }
    
    /**
     * 실시간 틱 데이터로 주식 정보 업데이트
     */
    private fun updateRealtimeTick(tickData: com.lago.app.data.remote.dto.RealtimeTickDto) {
        val currentStockCode = _uiState.value.currentStock.code
        
        // 현재 보고 있는 주식의 데이터인지 확인
        if (tickData.symbol != currentStockCode) {
            return
        }
        
        // 주식 정보 실시간 업데이트
        val updatedStock = _uiState.value.currentStock.copy(
            currentPrice = tickData.price,
            priceChange = tickData.change,
            priceChangePercent = tickData.changePercent
        )
        
        _uiState.update { 
            it.copy(currentStock = updatedStock)
        }
    }
    
    /**
     * 타임프레임에 따라 같은 시간대인지 판단
     */
    private fun isSameTimeframe(time1: Long, time2: Long, timeframe: String): Boolean {
        val diff = kotlin.math.abs(time1 - time2)
        
        return when (timeframe) {
            "1" -> diff < 60 * 1000L // 1분
            "3" -> diff < 3 * 60 * 1000L // 3분
            "5" -> diff < 5 * 60 * 1000L // 5분
            "15" -> diff < 15 * 60 * 1000L // 15분
            "30" -> diff < 30 * 60 * 1000L // 30분
            "60" -> diff < 60 * 60 * 1000L // 1시간
            "D" -> {
                val cal1 = java.util.Calendar.getInstance().apply { timeInMillis = time1 }
                val cal2 = java.util.Calendar.getInstance().apply { timeInMillis = time2 }
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR) &&
                cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR)
            }
            else -> false
        }
    }
    
    /**
     * 주식 변경 시 실시간 데이터 구독 업데이트
     */
//    private fun updateRealtimeSubscription(stockCode: String, timeframe: String) {
//        smartWebSocketService.subscribeToCandlestickData(stockCode, timeframe)
//        smartWebSocketService.subscribeToTickData(stockCode)
//    }
//
    override fun onCleared() {
        super.onCleared()
        // 웹소켓 리소스 정리
        smartWebSocketService.disconnect()
        smartWebSocketService.cleanup()
        
        // 메모리 캐시 정리
        memoryCache.clearExpired()
    }
    
    // ===== MOCK DATA METHODS (Fallback) =====
    
    private fun loadMockData() {
        loadMockCandlestickData()
        loadMockVolumeData()
        calculateMockIndicators()
        loadMockHoldings()
        loadMockTradingHistory()
    }
    
    private fun loadMockCandlestickData() {
        val mockData = generateMockCandlestickData()
        _uiState.update { 
            it.copy(candlestickData = mockData)
        }
    }
    
    private fun loadMockVolumeData() {
        val mockData = generateMockVolumeData()
        _uiState.update { 
            it.copy(volumeData = mockData)
        }
    }
    
    private fun calculateMockIndicators() {
        val candlestickData = _uiState.value.candlestickData
        if (candlestickData.isEmpty()) return
        
        val config = _uiState.value.config.indicators
        
        _uiState.update { state ->
            state.copy(
                sma5Data = if (config.sma5) calculateSMA(candlestickData, 5) else emptyList(),
                sma20Data = if (config.sma20) calculateSMA(candlestickData, 20) else emptyList(),
                rsiData = if (config.rsi) calculateRSI(candlestickData) else emptyList(),
                macdData = if (config.macd) {
                    val macd = calculateMACD(candlestickData)
                    android.util.Log.d("LAGO_CHART", "MACD calculated: ${macd?.macdLine?.size ?: 0} points")
                    macd
                } else {
                    android.util.Log.d("LAGO_CHART", "MACD disabled in config")
                    null
                },
                bollingerBands = if (config.bollingerBands) calculateBollingerBands(candlestickData) else null
            )
        }
    }
    
    private fun loadMockHoldings() {
        val mockHoldings = listOf(
            HoldingItem("삼성전자", "10주", 742000, 1.09f, "005930"),
            HoldingItem("SK하이닉스", "5주", 675000, -1.46f, "000660"),
            HoldingItem("NAVER", "3주", 555000, 0.82f, "035420")
        )
        _uiState.update { 
            it.copy(holdingItems = mockHoldings)
        }
    }
    
    private fun loadMockTradingHistory() {
        val mockHistory = listOf(
            TradingItem("구매", "10주", 742000, "2024-01-15"),
            TradingItem("판매", "5주", 371000, "2024-01-14"),
            TradingItem("구매", "15주", 1113000, "2024-01-13")
        )
        _uiState.update { 
            it.copy(tradingHistory = mockHistory)
        }
    }
    
    private fun generateMockCandlestickData(): List<CandlestickData> {
        // Generate 100 days of mock data
        val data = mutableListOf<CandlestickData>()
        var currentPrice = 74200f
        val startTime = System.currentTimeMillis() - (100 * 24 * 60 * 60 * 1000L)
        
        for (i in 0 until 100) {
            val time = startTime + (i * 24 * 60 * 60 * 1000L)
            val change = (Math.random() * 0.1 - 0.05).toFloat()
            val open = currentPrice
            val close = currentPrice * (1 + change)
            val high = maxOf(open, close) * (1 + Math.random().toFloat() * 0.02f)
            val low = minOf(open, close) * (1 - Math.random().toFloat() * 0.02f)
            val volume = (1000000 + Math.random() * 2000000).toLong()
            
            data.add(CandlestickData(time, open, high, low, close, volume))
            currentPrice = close
        }
        
        return data
    }
    
    private fun generateMockVolumeData(): List<VolumeData> {
        return _uiState.value.candlestickData.map { candle ->
            VolumeData(
                time = candle.time,
                value = candle.volume.toFloat(),
                color = if (candle.close >= candle.open) "#ef5350" else "#26a69a"
            )
        }
    }
    
    private fun calculateSMA(data: List<CandlestickData>, period: Int): List<LineData> {
        if (data.size < period) return emptyList()
        
        val smaData = mutableListOf<LineData>()
        for (i in period - 1 until data.size) {
            val sum = (i - period + 1..i).sumOf { data[it].close.toDouble() }
            val average = (sum / period).toFloat()
            smaData.add(LineData(data[i].time, average))
        }
        return smaData
    }
    
    private fun calculateRSI(data: List<CandlestickData>, period: Int = 14): List<LineData> {
        if (data.size < period + 1) return emptyList()
        
        val rsiData = mutableListOf<LineData>()
        val gains = mutableListOf<Float>()
        val losses = mutableListOf<Float>()
        
        // Calculate initial gains and losses
        for (i in 1 until data.size) {
            val change = data[i].close - data[i - 1].close
            gains.add(maxOf(change, 0f))
            losses.add(maxOf(-change, 0f))
        }
        
        // Calculate RSI
        for (i in period - 1 until gains.size) {
            val avgGain = gains.subList(i - period + 1, i + 1).average().toFloat()
            val avgLoss = losses.subList(i - period + 1, i + 1).average().toFloat()
            
            val rs = if (avgLoss != 0f) avgGain / avgLoss else 0f
            val rsi = 100f - (100f / (1f + rs))
            
            rsiData.add(LineData(data[i + 1].time, rsi))
        }
        
        return rsiData
    }
    
    private fun calculateMACD(data: List<CandlestickData>): MACDResult? {
        android.util.Log.d("LAGO_CHART", "MACD calculation start - data size: ${data.size}")
        if (data.size < 26) {
            android.util.Log.d("LAGO_CHART", "MACD failed - insufficient data (need 26, got ${data.size})")
            return null
        }
        
        val ema12 = calculateEMA(data, 12)
        val ema26 = calculateEMA(data, 26)
        
        android.util.Log.d("LAGO_CHART", "EMA calculated - EMA12: ${ema12.size}, EMA26: ${ema26.size}")
        
        if (ema12.isEmpty() || ema26.isEmpty()) {
            android.util.Log.d("LAGO_CHART", "MACD failed - EMA data is empty")
            return null
        }
        
        val macdLine = mutableListOf<LineData>()
        // EMA26이 더 늦게 시작하므로 EMA26 크기에 맞춰서 계산
        val startOffset = ema12.size - ema26.size
        for (i in ema26.indices) {
            val ema12Index = i + startOffset
            if (ema12Index < ema12.size) {
                macdLine.add(LineData(ema26[i].time, ema12[ema12Index].value - ema26[i].value))
            }
        }
        
        val signalLine = calculateEMAFromLineData(macdLine, 9)
        
        val histogram = mutableListOf<VolumeData>()
        for (i in signalLine.indices) {
            val histValue = macdLine[i + (macdLine.size - signalLine.size)].value - signalLine[i].value
            histogram.add(VolumeData(signalLine[i].time, histValue))
        }
        
        return MACDResult(macdLine, signalLine, histogram)
    }
    
    private fun calculateEMA(data: List<CandlestickData>, period: Int): List<LineData> {
        if (data.isEmpty() || data.size < period) return emptyList()
        
        val emaData = mutableListOf<LineData>()
        val multiplier = 2.0 / (period + 1)
        
        // 첫 번째 EMA는 SMA로 초기화
        var sma = data.take(period).map { it.close.toDouble() }.average()
        emaData.add(LineData(data[period - 1].time, sma.toFloat()))
        
        // EMA 계산
        for (i in period until data.size) {
            sma = (data[i].close * multiplier) + (sma * (1 - multiplier))
            emaData.add(LineData(data[i].time, sma.toFloat()))
        }
        
        return emaData
    }
    
    private fun calculateEMAFromLineData(data: List<LineData>, period: Int): List<LineData> {
        if (data.isEmpty()) return emptyList()
        
        val emaData = mutableListOf<LineData>()
        val multiplier = 2.0 / (period + 1)
        var ema = data[0].value.toDouble()
        
        emaData.add(LineData(data[0].time, ema.toFloat()))
        
        for (i in 1 until data.size) {
            ema = (data[i].value * multiplier) + (ema * (1 - multiplier))
            emaData.add(LineData(data[i].time, ema.toFloat()))
        }
        
        return emaData.drop(period - 1)
    }
    
    private fun calculateBollingerBands(data: List<CandlestickData>, period: Int = 20, multiplier: Float = 2.0f): BollingerBandsResult? {
        if (data.size < period) return null
        
        val upperBand = mutableListOf<LineData>()
        val middleBand = mutableListOf<LineData>()
        val lowerBand = mutableListOf<LineData>()
        
        for (i in period - 1 until data.size) {
            val prices = (i - period + 1..i).map { data[it].close }
            val sma = prices.average().toFloat()
            
            val variance = prices.map { (it - sma) * (it - sma) }.average()
            val standardDeviation = kotlin.math.sqrt(variance).toFloat()
            
            val upper = sma + (standardDeviation * multiplier)
            val lower = sma - (standardDeviation * multiplier)
            
            val time = data[i].time
            upperBand.add(LineData(time, upper))
            middleBand.add(LineData(time, sma))
            lowerBand.add(LineData(time, lower))
        }
        
        return BollingerBandsResult(upperBand, middleBand, lowerBand)
    }
    
    // ======================== 매매 내역 관련 함수들 ========================
    
    private fun loadTradingSignals() {
        viewModelScope.launch {
            try {
                // 실제로는 repository에서 매매 내역을 가져올 것
                val signals = generateMockTradingSignals()
                _uiState.update { 
                    it.copy(tradingSignals = signals) 
                }
            } catch (e: Exception) {
                android.util.Log.e("LAGO_CHART", "매매 내역 로드 실패", e)
                _uiState.update { 
                    it.copy(errorMessage = "매매 내역을 불러올 수 없습니다.") 
                }
            }
        }
    }
    
    private fun toggleUserTradingSignals(show: Boolean) {
        _uiState.update { 
            it.copy(showUserTradingSignals = show) 
        }
        // WebView와 통신하여 사용자 매매 마커 업데이트
        updateChartMarkers()
    }
    
    private fun selectAITradingSignals(aiSource: SignalSource?) {
        _uiState.update { 
            it.copy(selectedAI = aiSource) 
        }
        // WebView와 통신하여 AI 매매 마커 업데이트
        updateChartMarkers()
    }
    
    private fun updateChartMarkers() {
        val currentState = _uiState.value
        val markersToShow = mutableListOf<TradingSignal>()
        
        // 사용자 매매 내역 표시
        if (currentState.showUserTradingSignals) {
            markersToShow.addAll(
                currentState.tradingSignals.filter { it.signalSource == SignalSource.USER }
            )
        }
        
        // 선택된 AI 매매 내역 표시
        currentState.selectedAI?.let { selectedAI ->
            markersToShow.addAll(
                currentState.tradingSignals.filter { it.signalSource == selectedAI }
            )
        }
        
        // TODO: WebView와 통신하여 실제 마커 업데이트
        android.util.Log.d("LAGO_CHART", "마커 업데이트: ${markersToShow.size}개")
    }
    
    private fun generateMockTradingSignals(): List<TradingSignal> {
        val calendar = java.util.Calendar.getInstance()
        val currentTime = calendar.time
        
        // 10일 전
        calendar.add(java.util.Calendar.DAY_OF_MONTH, -10)
        val time10DaysAgo = calendar.time
        
        // 5일 전  
        calendar.time = currentTime
        calendar.add(java.util.Calendar.DAY_OF_MONTH, -5)
        val time5DaysAgo = calendar.time
        
        // 8일 전
        calendar.time = currentTime
        calendar.add(java.util.Calendar.DAY_OF_MONTH, -8) 
        val time8DaysAgo = calendar.time
        
        // 3일 전
        calendar.time = currentTime
        calendar.add(java.util.Calendar.DAY_OF_MONTH, -3)
        val time3DaysAgo = calendar.time
        
        // 6일 전
        calendar.time = currentTime
        calendar.add(java.util.Calendar.DAY_OF_MONTH, -6)
        val time6DaysAgo = calendar.time
        
        return listOf(
            TradingSignal(
                id = "signal_1",
                stockCode = "005930",
                signalType = SignalType.BUY,
                signalSource = SignalSource.USER,
                timestamp = time10DaysAgo,
                price = 72000.0,
                message = "사용자 매수"
            ),
            TradingSignal(
                id = "signal_2", 
                stockCode = "005930",
                signalType = SignalType.SELL,
                signalSource = SignalSource.USER,
                timestamp = time5DaysAgo,
                price = 74500.0,
                message = "사용자 매도"
            ),
            TradingSignal(
                id = "signal_3",
                stockCode = "005930", 
                signalType = SignalType.BUY,
                signalSource = SignalSource.AI_BLUE,
                timestamp = time8DaysAgo,
                price = 71500.0,
                message = "AI 파랑 매수"
            ),
            TradingSignal(
                id = "signal_4",
                stockCode = "005930",
                signalType = SignalType.SELL, 
                signalSource = SignalSource.AI_GREEN,
                timestamp = time3DaysAgo,
                price = 75000.0,
                message = "AI 초록 매도"
            ),
            TradingSignal(
                id = "signal_5",
                stockCode = "005930",
                signalType = SignalType.BUY,
                signalSource = SignalSource.AI_RED,
                timestamp = time6DaysAgo,
                price = 73200.0,
                message = "AI 빨강 매수"
            )
        )
    }
}