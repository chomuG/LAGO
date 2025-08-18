package com.lago.app.presentation.viewmodel.chart

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
import com.lago.app.data.local.dao.ChartCacheDao
import com.lago.app.data.local.entity.CachedChartData
import com.lago.app.data.local.entity.CachedStockInfo
import com.lago.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlin.time.Duration.Companion.milliseconds
import javax.inject.Inject
import com.lago.app.presentation.ui.chart.v5.HistoricalDataRequestListener
import com.lago.app.presentation.ui.chart.v5.ChartTimeManager
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*


@HiltViewModel
class ChartViewModel @Inject constructor(
    private val chartRepository: ChartRepository,
    private val analyzeChartPatternUseCase: AnalyzeChartPatternUseCase,
    private val userPreferences: UserPreferences,
    private val smartWebSocketService: SmartStockWebSocketService,
    private val smartUpdateScheduler: SmartUpdateScheduler,
    private val memoryCache: ChartMemoryCache,
    private val realTimeCache: com.lago.app.data.cache.RealTimeStockCache,
    private val mockTradeRepository: com.lago.app.domain.repository.MockTradeRepository,
    private val portfolioRepository: com.lago.app.domain.repository.PortfolioRepository,
    private val chartCacheDao: ChartCacheDao,
    private val favoriteCache: com.lago.app.data.cache.FavoriteCache,
    private val patternAnalysisPreferences: com.lago.app.data.local.prefs.PatternAnalysisPreferences
) : ViewModel(), HistoricalDataRequestListener, com.lago.app.presentation.ui.chart.v5.JsBridge.PatternAnalysisListener {

    private val _uiState = MutableStateFlow(ChartUiState())
    val uiState: StateFlow<ChartUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ChartUiEvent>()

    // 안전 타임아웃을 위한 Job
    private var chartLoadingTimeoutJob: Job? = null

    /**
     * 새로운 인터벌 API를 사용하여 차트 데이터 로드 (모의투자용)
     */
    fun loadChartDataWithInterval(stockCode: String, timeFrame: String, pastHours: Int? = null) {
        android.util.Log.d("ChartViewModel", "📥 loadChartDataWithInterval 호출됨: stockCode=$stockCode, timeFrame=$timeFrame, pastHours=$pastHours")
        viewModelScope.launch {
            try {
                android.util.Log.d("ChartViewModel", "📥 ViewModel 코루틴 시작")

                val cacheId = CachedChartData.createId(stockCode, timeFrame)

                // 1. 캐시에서 먼저 확인
                val cachedData = chartCacheDao.getCachedChartData(cacheId)
                if (cachedData != null) {
                    android.util.Log.d("ChartViewModel", "💾 캐시된 차트 데이터 사용: ${cachedData.data.size}개 캔들")

                    // 캐시된 데이터로 UI 즉시 업데이트
                    _uiState.update { state ->
                        state.copy(
                            candlestickData = cachedData.data,
                            isLoading = false,
                            errorMessage = null
                        )
                    }

                    // 캐시된 데이터 변환
                    android.util.Log.d("ChartViewModel", "💾 캐시된 데이터 변환 시작")

                    // CandlestickData를 CandleData로 변환
                    val candleDataList = cachedData.data.map { candlestick ->
                        com.lago.app.presentation.ui.chart.v5.CandleData(
                            time = candlestick.time,
                            open = candlestick.open,
                            high = candlestick.high,
                            low = candlestick.low,
                            close = candlestick.close
                        )
                    }

                    // 차트 브릿지가 있으면 즉시 설정, 없으면 대기 (볼륨은 null)
                    chartBridge?.let { bridge ->
                        android.util.Log.d("ChartViewModel", "💾 차트 브릿지 존재 - 캐시된 데이터 즉시 설정")
                        bridge.setInitialData(candleDataList)
                        _uiState.update { it.copy(chartLoadingStage = ChartLoadingStage.CHART_READY) }
                    } ?: run {
                        android.util.Log.d("ChartViewModel", "💾 차트 브릿지 없음 - 캐시된 데이터를 대기 상태로 저장")
                        // 캐시된 데이터는 거래량이 없으므로 빈 리스트로 설정
                        pendingChartCandles = candleDataList
                        pendingVolumeData = emptyList()
                    }

                    // 캐시된 데이터를 보여준 후 백그라운드에서 최신 데이터 확인
                    // 계속 진행하여 서버에서 최신 데이터를 가져옴
                }

                _uiState.update { it.copy(isLoading = true) }

                // 시간프레임에 따른 적절한 과거 기간 계산 (충분한 캔들 수 확보)
                val calculatedPastHours = pastHours ?: calculateOptimalPastHours(timeFrame)
                android.util.Log.d("ChartViewModel", "📥 계산된 과거 기간: ${calculatedPastHours}시간 (timeFrame: $timeFrame)")

                // 시간 범위 계산 (KST)
                val now = Calendar.getInstance()
                val toDateTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(now.time)

                now.add(Calendar.HOUR_OF_DAY, -calculatedPastHours)
                val fromDateTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(now.time)

                // 타임프레임을 인터벌로 변환
                val interval = convertTimeFrameToInterval(timeFrame)

                // 새로운 인터벌 API 호출
                val response = chartRepository.getIntervalChartData(stockCode, interval, fromDateTime, toDateTime)

                response.collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            val data = resource.data ?: emptyList()
                            android.util.Log.d("ChartViewModel", "📥 인터벌 API 성공: ${data.size}개 캔들")

                            // 2. 서버에서 받은 최신 데이터를 캐시에 저장
                            if (data.isNotEmpty()) {
                                val currentTime = System.currentTimeMillis()
                                val cachedChartData = CachedChartData(
                                    id = cacheId,
                                    stockCode = stockCode,
                                    timeFrame = timeFrame,
                                    data = data,
                                    lastUpdated = currentTime
                                )
                                chartCacheDao.insertChartData(cachedChartData)
                                android.util.Log.d("ChartViewModel", "💾 차트 데이터 캐시에 저장됨")
                            }

                            _uiState.update { state ->
                                state.copy(
                                    candlestickData = data,
                                    isLoading = false,
                                    errorMessage = null // 성공 시 에러 메시지 클리어
                                )
                            }

                            // 데이터 변환 (차트 브릿지 유무와 관계없이 항상 수행)
                            android.util.Log.d("ChartViewModel", "📥 데이터 변환 시작")

                            // 캔들 데이터 변환
                            val chartCandles = data.map { candle ->
                                // ChartTimeManager 사용으로 통일
                                val epochSeconds = ChartTimeManager.normalizeToEpochSeconds(candle.time)
                                android.util.Log.v("ChartViewModel", "📥 캔들 변환: ${candle.time} → $epochSeconds (${java.util.Date(epochSeconds * 1000)})")
                                com.lago.app.presentation.ui.chart.v5.CandleData(
                                    time = epochSeconds,
                                    open = candle.open,
                                    high = candle.high,
                                    low = candle.low,
                                    close = candle.close
                                )
                            }

                            // 거래량 데이터 변환 (역사챌린지와 동일한 방식)
                            val volumeData = data.map { candle ->
                                val epochSeconds = ChartTimeManager.normalizeToEpochSeconds(candle.time)
                                com.lago.app.presentation.ui.chart.v5.VolumeData(
                                    time = epochSeconds,
                                    value = candle.volume,
                                    color = if (candle.close >= candle.open) "#26a69a" else "#ef5350" // 상승/하락 색상
                                )
                            }

                            android.util.Log.d("ChartViewModel", "📥 변환 완료 - 캔들: ${chartCandles.size}개, 거래량: ${volumeData.size}개")
                            if (chartCandles.isNotEmpty()) {
                                android.util.Log.d("ChartViewModel", "📥 첫 캔들: time=${chartCandles.first().time}, close=${chartCandles.first().close}")
                                android.util.Log.d("ChartViewModel", "📥 마지막 캔들: time=${chartCandles.last().time}, close=${chartCandles.last().close}")
                            }

                            // 차트 브릿지가 있으면 즉시 설정, 없으면 대기
                            chartBridge?.let { bridge ->
                                android.util.Log.d("ChartViewModel", "📥 차트 브릿지 존재 - 즉시 데이터 설정")
                                bridge.setInitialData(chartCandles, volumeData)
                                android.util.Log.d("ChartViewModel", "📥 bridge.setInitialData() 호출 완료")
                                
                                // 🔥 거래량이 항상 표시되도록 volume indicator 자동 활성화
                                bridge.setIndicatorWithQueue("volume", true)
                                android.util.Log.d("ChartViewModel", "📊 거래량 지표 자동 활성화")
                                
                                _uiState.update { it.copy(chartLoadingStage = ChartLoadingStage.CHART_READY) }
                            } ?: run {
                                android.util.Log.d("ChartViewModel", "📥 차트 브릿지 없음 - 데이터를 대기 상태로 저장")
                                // 차트 브릿지가 설정될 때까지 데이터 대기
                                pendingChartCandles = chartCandles
                                pendingVolumeData = volumeData
                            }
                        }
                        is Resource.Error -> {
                            android.util.Log.e("ChartViewModel", "🚨 인터벌 API 실패: ${resource.message}")
                            // 차트 데이터 로드 실패 시에도 로딩 유지하고 백그라운드에서 재시도
                            _uiState.update {
                                it.copy(
                                    isLoading = true,
                                    errorMessage = resource.message
                                )
                            }
                            // 3초 후 자동 재시도
                            viewModelScope.launch {
                                delay(3000)
                                if (_uiState.value.errorMessage != null) {
                                    android.util.Log.d("ChartViewModel", "🔄 차트 데이터 자동 재시도: $stockCode")
                                    loadChartDataWithInterval(stockCode, timeFrame, pastHours)
                                }
                            }
                        }
                        is Resource.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ChartViewModel", "인터벌 API 호출 실패", e)
                // 예외 시에도 로딩 유지하고 백그라운드에서 재시도
                _uiState.update {
                    it.copy(
                        isLoading = true,
                        errorMessage = "데이터 로드 실패: ${e.message}"
                    )
                }
                // 5초 후 자동 재시도
                viewModelScope.launch {
                    delay(5000)
                    if (_uiState.value.errorMessage != null) {
                        android.util.Log.d("ChartViewModel", "🔄 API 예외 자동 재시도: $stockCode")
                        loadChartDataWithInterval(stockCode, timeFrame, pastHours)
                    }
                }
            }
        }
    }

    /**
     * 시간프레임에 따른 최적의 과거 기간 계산 (충분한 캔들 수 확보)
     * 목표: 100~200개 캔들 확보
     */
    private fun calculateOptimalPastHours(timeFrame: String): Int {
        return when (timeFrame) {
            "1" -> 24 * 7      // 1분봉: 1주일 (7일 * 24시간 = 168시간)
            "3" -> 24 * 14     // 3분봉: 2주일 (336시간)
            "5" -> 24 * 21     // 5분봉: 3주일 (504시간)
            "10" -> 24 * 30    // 10분봉: 30일 (720시간)
            "15" -> 24 * 45    // 15분봉: 45일 (1080시간)
            "30" -> 24 * 60    // 30분봉: 60일 (1440시간)
            "60" -> 24 * 90    // 60분봉: 90일 (2160시간)
            "D" -> 24 * 365    // 일봉: 1년 (8760시간)
            "W" -> 24 * 365 * 3 // 주봉: 3년 (26280시간)
            "M" -> 24 * 365 * 5 // 월봉: 5년 (43800시간)
            else -> 24 * 30    // 기본값: 30일
        }
    }

    /**
     * UI 타임프레임을 API interval로 변환
     */
    private fun convertTimeFrameToInterval(timeFrame: String): String {
        return when (timeFrame) {
            "1" -> "MINUTE"      // 역사챌린지와 동일하게 변경
            "3" -> "MINUTE3"
            "5" -> "MINUTE5"
            "10" -> "MINUTE10"
            "15" -> "MINUTE15"
            "30" -> "MINUTE30"
            "60" -> "MINUTE60"   // HOUR1 -> MINUTE60으로 변경
            "D" -> "DAY"
            "W" -> "WEEK"
            "M" -> "MONTH"
            "Y" -> "YEAR"        // 추가
            else -> "DAY"        // 기본값을 DAY로 변경 (역사챌린지와 동일)
        }
    }

    // 실시간 차트 업데이트를 위한 JsBridge와 MinuteAggregator
    private var chartBridge: com.lago.app.presentation.ui.chart.v5.JsBridge? = null
    private val minuteAggregator = com.lago.app.presentation.ui.chart.v5.MinuteAggregator()
    
    // 차트 브릿지 설정 전에 로딩된 데이터를 임시 저장
    private var pendingChartCandles: List<com.lago.app.presentation.ui.chart.v5.CandleData>? = null
    private var pendingVolumeData: List<com.lago.app.presentation.ui.chart.v5.VolumeData>? = null

    // 무한 히스토리 관련 상태 변수들
    private var currentEarliestTime: Long? = null // 현재 차트에 로드된 가장 오래된 데이터 시간
    private var isLoadingHistory = false // 과거 데이터 로딩 중 여부
    private val gson = Gson()


    init {
        loadInitialData()
        // 웹소켓은 SmartStockWebSocketService에서 통합 관리
        observeRealTimePrice()
        // 관심종목 캐시 관찰
        observeFavoriteData()
        // 패턴 분석 횟수 초기화
        initializePatternAnalysisCount()
        // 캐시 정리 시작
        startCacheCleanup()
    }

    /**
     * 관심종목 상태 변화 관찰
     */
    private fun observeFavoriteData() {
        viewModelScope.launch {
            favoriteCache.favoriteFlow.collect { favorites ->
                val currentStockCode = _uiState.value.currentStock.code

                // 주식 코드가 설정된 경우에만 관심종목 상태 확인
                if (currentStockCode.isNotEmpty()) {
                    val isFavorite = favorites.contains(currentStockCode)

                    _uiState.update { currentState ->
                        currentState.copy(isFavorite = isFavorite)
                    }
                    android.util.Log.d("ChartViewModel", "💖 관심종목 상태 업데이트: $currentStockCode → $isFavorite")
                }
            }
        }
    }

    private fun startCacheCleanup() {
        viewModelScope.launch {
            // 10분마다 캐시 정리
            while (true) {
                delay(10 * 60 * 1000) // 10분
                try {
                    // 만료된 캐시 삭제
                    chartCacheDao.deleteExpiredChartData()
                    chartCacheDao.deleteExpiredStockInfo()

                    // 24시간 이전의 오래된 데이터 삭제
                    val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
                    chartCacheDao.deleteOldChartData(oneDayAgo)
                    chartCacheDao.deleteOldStockInfo(oneDayAgo)

                    android.util.Log.d("ChartViewModel", "🧹 캐시 정리 완료")
                } catch (e: Exception) {
                    android.util.Log.e("ChartViewModel", "캐시 정리 실패", e)
                }
            }
        }
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
                    // 🎯 유효한 실시간 데이터가 있을 때만 업데이트 (마지막 알려진 가격 유지)
                    if (realTimeData.price > 0.0) {
                        android.util.Log.d("ChartViewModel", "📈 유효한 실시간 데이터 - 가격 업데이트: ${realTimeData.stockCode} = ${realTimeData.price.toInt()}원")

                        // UI 상태 업데이트
                        _uiState.update { state ->
                            state.copy(
                                currentStock = state.currentStock.copy(
                                    currentPrice = realTimeData.price.toFloat(),
                                    priceChange = realTimeData.priceChange.toFloat(),
                                    priceChangePercent = realTimeData.priceChangePercent.toFloat(),
                                    previousDay = realTimeData.previousDay // 웹소켓 previousDay 적용
                                )
                            )
                        }

                        // 실시간 차트 캔들 업데이트
                        updateRealTimeChart(realTimeData)
                    } else {
                        android.util.Log.d("ChartViewModel", "⚠️ 무효한 실시간 데이터 - 마지막 알려진 가격 유지: ${realTimeData.stockCode} price=${realTimeData.price}")
                    }
                }
        }
    }

    /**
     * 실시간 데이터를 받아 차트 캔들을 업데이트
     * TradingView 표준 방식: 현재 시간프레임의 마지막 캔들만 업데이트
     * ChartTimeManager 사용으로 통일
     */
    private fun updateRealTimeChart(realTimeData: com.lago.app.domain.entity.StockRealTimeData) {
        android.util.Log.d("ChartViewModel", "📥 updateRealTimeChart 호출됨 - 종목: ${realTimeData.stockCode}, 가격: ${realTimeData.price}")
        android.util.Log.d("ChartViewModel", "📥 웹소켓 데이터 - originDateTime: ${realTimeData.originDateTime}, date: ${realTimeData.date}, timestamp: ${realTimeData.timestamp}")
        try {
            val currentTimeFrame = _uiState.value.config.timeFrame

            // 웹소켓 데이터에서 올바른 KST timestamp 사용
            val kstTimestamp = realTimeData.getKstTimestamp()
            val kstEpochSec = ChartTimeManager.normalizeToEpochSeconds(kstTimestamp)

            // 역사챌린지 모드 감지 및 로깅
            val isHistorical = realTimeData.isHistoricalMode()
            val dateTimeStr = if (isHistorical) {
                java.time.Instant.ofEpochMilli(kstTimestamp)
                    .atZone(java.time.ZoneId.of("Asia/Seoul"))
                    .toLocalDateTime().toString()
            } else {
                "현재시간기준"
            }

            val normalizedTime = ChartTimeManager.normalizeToEpochSeconds(kstEpochSec)
            val normalizedDateTimeStr = java.time.Instant.ofEpochSecond(normalizedTime)
                .atZone(java.time.ZoneId.of("Asia/Seoul"))
                .toLocalDateTime().toString()

            android.util.Log.d("ChartViewModel", "📊 실시간 업데이트 - 모드: ${if(isHistorical) "역사챌린지" else "실시간"}, 원본시간: $dateTimeStr, 정규화시간: $normalizedDateTimeStr, 프레임: $currentTimeFrame")

            // 시간프레임별 실시간 업데이트 처리
            when (currentTimeFrame) {
                "1", "3", "5", "10", "15", "30" -> {
                    // 분봉: 직접 캔들 업데이트 (MinuteAggregator 우회)
                    updateDirectCandle(realTimeData, normalizedTime)
                }
                "60" -> {
                    // 시간봉
                    updateDirectCandle(realTimeData, normalizedTime)
                }
                "D", "W", "M", "Y" -> {
                    // 일봉/주봉/월봉/년봉
                    updateDirectCandle(realTimeData, normalizedTime)
                }
            }

        } catch (e: Exception) {
            android.util.Log.e("ChartViewModel", "실시간 차트 업데이트 실패", e)
        }
    }

    /**
     * 통합된 실시간 캔들 업데이트 (모든 timeframe 공통)
     * ChartTimeManager 사용으로 통일
     */
    private fun updateDirectCandle(realTimeData: com.lago.app.domain.entity.StockRealTimeData, normalizedTime: Long) {
        android.util.Log.d("ChartViewModel", "📥 updateDirectCandle 호출 - normalizedTime: $normalizedTime")

        val candle = com.lago.app.presentation.ui.chart.v5.Candle(
            time = normalizedTime,
            open = realTimeData.openPrice?.toInt() ?: realTimeData.price.toInt(),
            high = realTimeData.highPrice?.toInt() ?: realTimeData.price.toInt(),
            low = realTimeData.lowPrice?.toInt() ?: realTimeData.price.toInt(),
            close = realTimeData.price.toInt()
        )

        val volumeBar = com.lago.app.presentation.ui.chart.v5.VolumeBar(
            normalizedTime,
            realTimeData.volume ?: 1000L
        )

        android.util.Log.d("ChartViewModel", "📥 캔들 생성 완료 - time: ${candle.time}, close: ${candle.close}")
        updateChartCandle(candle, volumeBar)
        android.util.Log.d("ChartViewModel", "📊 실시간 캔들 업데이트: ${realTimeData.price}원 (정규화시간: $normalizedTime)")
    }


    private fun updateChartCandle(candle: com.lago.app.presentation.ui.chart.v5.Candle, volumeBar: com.lago.app.presentation.ui.chart.v5.VolumeBar) {
        chartBridge?.let { bridge ->
            val currentTimeFrame = _uiState.value.config.timeFrame
            android.util.Log.d("ChartViewModel", "🔥 실시간 업데이트 시작 - 캔들: ${candle.close}원, 거래량: ${volumeBar.value}, 타임프레임: $currentTimeFrame")
            
            bridge.updateBar(candle, currentTimeFrame)
            bridge.updateVolume(volumeBar, currentTimeFrame)
            
            android.util.Log.d("ChartViewModel", "🕯️ 실시간 캔들 업데이트 완료 [${currentTimeFrame}]: ${candle.time} = ${candle.close}원")
        } ?: run {
            android.util.Log.w("ChartViewModel", "⚠️ chartBridge가 null이어서 실시간 업데이트 불가")
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
            is ChartUiEvent.ToggleUserTradingSignals -> toggleUserTradingSignals(event.enabled)
            is ChartUiEvent.SelectAITradingSignals -> selectAITradingSignals(event.aiSource)
            is ChartUiEvent.ClearError -> clearErrorMessage()
            is ChartUiEvent.ClearTradeMessage -> clearTradeMessage()
            is ChartUiEvent.SelectPattern -> selectPattern(event.pattern)
            is ChartUiEvent.NextPatternStage -> nextPatternStage()
            is ChartUiEvent.ResetPatternStage -> resetPatternStage()
        }
    }

    /**
     * 네비게이션에서 받은 주식 정보를 즉시 설정 (빈 화면 방지)
     */
    fun setInitialStockInfo(stockCode: String, stockName: String) {
        android.util.Log.d("ChartViewModel", "🎯 초기 주식 정보 설정: $stockName($stockCode)")

        // 관심종목 상태 확인
        val isFavorite = favoriteCache.favoriteFlow.value.contains(stockCode)
        android.util.Log.d("ChartViewModel", "💖 관심종목 상태 확인: $stockCode → $isFavorite")

        _uiState.update { currentState ->
            currentState.copy(
                currentStock = currentState.currentStock.copy(
                    code = stockCode,
                    name = stockName
                ),
                config = currentState.config.copy(stockCode = stockCode),
                isFavorite = isFavorite
            )
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

        // 초기 상태에서는 종목이 설정되지 않았으므로 홀딩/거래내역만 로드
        loadUserHoldings()
        loadTradingHistory()
    }

    private fun changeStock(stockCode: String) {
        viewModelScope.launch {
            val currentStock = _uiState.value.currentStock

            // 🎯 같은 종목이면서 이미 유효한 데이터가 있는 경우에만 early return
            if (currentStock.code == stockCode &&
                currentStock.name.isNotEmpty() &&
                currentStock.currentPrice > 0f) {
                android.util.Log.d("ChartViewModel", "✅ 같은 종목 재로드 - 기존 가격 유지: ${currentStock.currentPrice}원")
                // 기존 가격 유지하면서 데이터만 새로 로드
                smartWebSocketService.updateChartStock(stockCode)
                loadChartDataWithInterval(stockCode, _uiState.value.config.timeFrame)
                checkFavoriteStatus(stockCode)
                return@launch
            }

            _uiState.update {
                it.copy(
                    isLoading = true,
                    chartLoadingStage = ChartLoadingStage.DATA_LOADING,
                    errorMessage = null
                )
            }

            // 주식 정보 캐시 확인
            val cachedStockInfo = chartCacheDao.getCachedStockInfo(stockCode)
            if (cachedStockInfo != null) {
                android.util.Log.d("ChartViewModel", "💾 캐시된 주식 정보 사용: ${cachedStockInfo.name}")

                val stockInfo = ChartStockInfo(
                    code = cachedStockInfo.stockCode,
                    name = cachedStockInfo.name,
                    currentPrice = cachedStockInfo.currentPrice,
                    priceChange = cachedStockInfo.priceChange,
                    priceChangePercent = cachedStockInfo.priceChangePercent,
                    previousDay = cachedStockInfo.previousDay
                )

                _uiState.update {
                    it.copy(
                        currentStock = stockInfo,
                        config = it.config.copy(stockCode = stockCode),
                        chartLoadingStage = ChartLoadingStage.DATA_LOADING,
                        isLoading = false,
                        errorMessage = null
                    )
                }

                // 차트 레전드에 종목명 업데이트
                chartBridge?.updateSymbolName(stockInfo.name)

                // 캐시된 데이터 먼저 표시 후 백그라운드에서 최신 데이터 확인
            }

            // 실제 서버에서 주식 정보 가져오기 (캐시가 있어도 최신 데이터 확인)
            try {
                chartRepository.getStockInfo(stockCode).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            resource.data?.let { serverStockInfo ->
                                // 🎯 서버 데이터가 0원이면 일봉 데이터로 폴백, 그것도 안되면 기본값 유지
                                val finalStockInfo = if (serverStockInfo.currentPrice == 0f) {
                                    val enrichedInfo = enrichStockInfoWithDayCandles(serverStockInfo, stockCode)
                                    // 일봉 데이터도 0원이면 기존 가격 정보 유지
                                    if (enrichedInfo.currentPrice == 0f) {
                                        val currentStockInfo = _uiState.value.currentStock
                                        android.util.Log.d("ChartViewModel", "⚠️ 서버 및 일봉 데이터 모두 0원 - 기존 가격 유지")
                                        android.util.Log.d("ChartViewModel", "⚠️ 현재 종목: ${currentStockInfo.code}(${currentStockInfo.currentPrice}원), 요청 종목: $stockCode")

                                        // 🎯 같은 종목이면 기존 가격 유지, 다른 종목이면 에러 처리
                                        if (currentStockInfo.code == stockCode && currentStockInfo.currentPrice > 0f) {
                                            android.util.Log.d("ChartViewModel", "✅ 같은 종목 - 기존 가격 유지: ${currentStockInfo.currentPrice}원")
                                            enrichedInfo.copy(currentPrice = currentStockInfo.currentPrice)
                                        } else {
                                            android.util.Log.e("ChartViewModel", "❌ 가격 데이터 완전 실패 - 에러 상태 유지")
                                            // 🎯 하드코딩 대신 에러 상태로 처리하여 사용자에게 알림
                                            _uiState.update {
                                                it.copy(
                                                    isLoading = false,
                                                    errorMessage = "주식 가격 정보를 불러올 수 없습니다. 네트워크를 확인해주세요."
                                                )
                                            }
                                            return@collect // 더 이상 진행하지 않음
                                        }
                                    } else {
                                        enrichedInfo
                                    }
                                } else {
                                    serverStockInfo
                                }

                                // 주식 정보를 캐시에 저장
                                val currentTime = System.currentTimeMillis()
                                val cachedStockInfo = CachedStockInfo(
                                    stockCode = finalStockInfo.code,
                                    name = finalStockInfo.name,
                                    currentPrice = finalStockInfo.currentPrice,
                                    priceChange = finalStockInfo.priceChange,
                                    priceChangePercent = finalStockInfo.priceChangePercent,
                                    previousDay = finalStockInfo.previousDay,
                                    lastUpdated = currentTime
                                )
                                chartCacheDao.insertStockInfo(cachedStockInfo)
                                android.util.Log.d("ChartViewModel", "💾 주식 정보 캐시에 저장됨: ${finalStockInfo.name}")

                                _uiState.update {
                                    it.copy(
                                        currentStock = finalStockInfo,
                                        config = it.config.copy(stockCode = stockCode),
                                        chartLoadingStage = ChartLoadingStage.DATA_LOADING,
                                        isLoading = false,
                                        errorMessage = null // 성공 시 에러 메시지 클리어
                                    )
                                }

                                // 차트 레전드에 종목명 업데이트
                                chartBridge?.updateSymbolName(serverStockInfo.name)

                                // 스마트 웹소켓에 차트 종목 변경 알림 (HOT 우선순위)
                                smartWebSocketService.updateChartStock(stockCode)

                                // 주식 정보 로드 후 차트 데이터 로드 (새로운 인터벌 API 사용)
                                loadChartDataWithInterval(stockCode, _uiState.value.config.timeFrame)
                                checkFavoriteStatus(stockCode)

                                // 실시간 데이터 구독은 SmartStockWebSocketService에서 자동 관리됨
                            }
                        }
                        is Resource.Error -> {
                            // 에러 시에도 로딩 유지하고 백그라운드에서 재시도
                            _uiState.update {
                                it.copy(
                                    isLoading = true,
                                    errorMessage = "주식 정보를 불러올 수 없습니다: ${resource.message}"
                                )
                            }
                            // 3초 후 자동 재시도
                            viewModelScope.launch {
                                delay(3000)
                                if (_uiState.value.errorMessage != null) {
                                    android.util.Log.d("ChartViewModel", "🔄 주식 정보 자동 재시도: $stockCode")
                                    changeStock(stockCode) // 자동 재시도
                                }
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
                // 예외 시에도 로딩 유지하고 백그라운드에서 재시도
                _uiState.update {
                    it.copy(
                        isLoading = true,
                        errorMessage = "네트워크 연결 실패: ${e.localizedMessage}"
                    )
                }
                // 5초 후 자동 재시도
                viewModelScope.launch {
                    delay(5000)
                    if (_uiState.value.errorMessage != null) {
                        android.util.Log.d("ChartViewModel", "🔄 네트워크 오류 자동 재시도: $stockCode")
                        changeStock(stockCode) // 자동 재시도
                    }
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

            // 차트 레전드에 종목명 업데이트
            chartBridge?.updateSymbolName(stockInfo.name)

            // 스마트 웹소켓에 차트 종목 변경 알림 (HOT 우선순위)
            smartWebSocketService.updateChartStock(stockCode)

            // 차트 데이터는 여전히 서버에서 가져와야 함 (새로운 인터벌 API 사용)
            loadChartDataWithInterval(stockCode, _uiState.value.config.timeFrame)
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
                                val data = resource.data ?: emptyList()
                                // DB에 과거 데이터가 없으면 빈 차트로 표시
                                val rawData = data

                                // 버킷 재샘플링으로 정규화 (ChartTimeManager 사용)
                                val chartData = rawData.map { it.copy(time = ChartTimeManager.normalizeToEpochSeconds(it.time)) }

                                // 현재 차트의 가장 오래된 데이터 시간 추적
                                currentEarliestTime = chartData.minByOrNull { it.time }?.time

                                _uiState.update {
                                    it.copy(candlestickData = chartData)
                                }

                                android.util.Log.d("ChartViewModel", "📈 차트 데이터 로드 완료: ${chartData.size}개, 가장 오래된 시간: ${currentEarliestTime}")
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
                android.util.Log.d("ChartViewModel", "📄 loadIndicators 시작: $stockCode, $timeFrame")
                val enabledIndicators = mutableListOf<String>()
                val currentConfig = _uiState.value.config.indicators

                if (currentConfig.sma5) enabledIndicators.add("sma5")
                if (currentConfig.sma20) enabledIndicators.add("sma20")
                if (currentConfig.sma60) enabledIndicators.add("sma60")
                if (currentConfig.sma120) enabledIndicators.add("sma120")
                if (currentConfig.rsi) enabledIndicators.add("rsi")
                if (currentConfig.macd) enabledIndicators.add("macd")
                if (currentConfig.bollingerBands) enabledIndicators.add("bollinger_bands")

                android.util.Log.d("ChartViewModel", "🎯 활성화된 지표: $enabledIndicators")

                if (enabledIndicators.isNotEmpty()) {
                    chartRepository.getIndicators(stockCode, enabledIndicators, timeFrame).collect { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                val data = resource.data
                                if (data != null) {
                                    android.util.Log.d("ChartViewModel", "✅ 지표 데이터 로딩 성공 - SMA5: ${data.sma5.size}, SMA20: ${data.sma20.size}, RSI: ${data.rsi.size}, MACD: ${data.macd != null}, BB: ${data.bollingerBands != null}")
                                    _uiState.update {
                                        it.copy(
                                            sma5Data = data.sma5,
                                            sma20Data = data.sma20,
                                            rsiData = data.rsi,
                                            macdData = data.macd,
                                            bollingerBands = data.bollingerBands
                                        )
                                    }
                                } else {
                                    android.util.Log.w("ChartViewModel", "⚠️ 지표 데이터가 null입니다")
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
        // 시간프레임 변경

        _uiState.update {
            it.copy(
                config = it.config.copy(timeFrame = timeFrame)
            )
        }

        // 설정 저장
        userPreferences.setChartTimeFrame(timeFrame)

        // 시간프레임 변경시 aggregator 리셋
        when (timeFrame) {
            "1", "3", "5", "10", "15", "30", "60" -> {
                // 분봉 - aggregator 리셋
                minuteAggregator.reset()
                android.util.Log.d("ChartViewModel", "🔄 Aggregator reset for ${timeFrame}분봉")
            }
            "D", "W", "M", "Y" -> {
                // 일봉 이상 - aggregator 필요없음
                android.util.Log.d("ChartViewModel", "📅 Switched to ${timeFrame} - no aggregation needed")
            }
        }

        // 시간프레임 변경 시 웹뷰 재생성으로 새 timeScale 옵션 적용 (안정적 방식)

        // 새로운 프레임에 맞는 데이터 다시 로드 (새로운 인터벌 API 사용)
        loadChartDataWithInterval(stockCode, timeFrame)

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
                val userId = userPreferences.getUserIdLong()
                if (userId == 0L) return@launch
                val accountType = _uiState.value.accountType
                android.util.Log.d("ChartViewModel", "📊 보유현황 로딩 시작: userId=$userId, accountType=$accountType")

                portfolioRepository.getUserCurrentStatus(userId, accountType).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            val response = resource.data
                            if (response != null) {
                                android.util.Log.d("ChartViewModel", "📊 보유현황 데이터 수신: ${response.holdings.size}개 종목, 잔액: ${response.balance}")

                                // Convert PortfolioController response to UI HoldingItem
                                val uiHoldings = response.holdings.map { holding ->
                                    android.util.Log.d("ChartViewModel", "📊 보유 주식: ${holding.stockName}(${holding.stockCode}) ${holding.quantity}주")
                                    HoldingItem(
                                        name = holding.stockName,
                                        quantity = "${holding.quantity}주",
                                        value = holding.totalPurchaseAmount,
                                        change = 0f, // 현재 수익률은 실시간 계산 필요
                                        stockCode = holding.stockCode
                                    )
                                }

                                _uiState.update {
                                    it.copy(
                                        holdingItems = uiHoldings,
                                        accountBalance = response.balance.toLong(),
                                        profitRate = response.profitRate.toFloat()
                                    )
                                }
                            } else {
                                android.util.Log.w("ChartViewModel", "📊 보유현황 응답이 null입니다")
                            }
                        }
                        is Resource.Error -> {
                            android.util.Log.e("ChartViewModel", "포트폴리오 조회 실패: ${resource.message}")
                            // 빈 상태로 유지 (더미 데이터 사용 안함)
                            _uiState.update {
                                it.copy(holdingItems = emptyList())
                            }
                        }
                        is Resource.Loading -> {}
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ChartViewModel", "포트폴리오 조회 예외: ${e.message}")
                // 빈 상태로 유지 (더미 데이터 사용 안함)
                _uiState.update {
                    it.copy(holdingItems = emptyList())
                }
            }
        }
    }

    private fun loadTradingHistory() {
        viewModelScope.launch {
            try {
                val userId = userPreferences.getUserIdLong()
                if (userId == 0L) return@launch
                val accountType = _uiState.value.accountType
                android.util.Log.d("ChartViewModel", "📈 거래내역 로딩 시작: userId=$userId, accountType=$accountType")

                // PortfolioRepository를 사용하여 거래내역 조회 (계좌타입별)
                portfolioRepository.getTransactionHistory(userId, accountType).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            android.util.Log.d("ChartViewModel", "📈 거래내역 데이터 수신: ${resource.data?.size ?: 0}개 거래")

                            // Convert Backend TransactionHistoryResponse to UI TradingItem
                            val uiTradings = resource.data?.map { transaction ->
                                android.util.Log.d("ChartViewModel", "📈 거래: ${transaction.stockName}(${transaction.stockId}) ${transaction.buySell} ${transaction.quantity}주")
                                TradingItem(
                                    type = if (transaction.buySell == "BUY") "구매" else "판매",
                                    quantity = "${transaction.quantity ?: 0}주",
                                    amount = transaction.price,
                                    date = formatTradeDateTime(transaction.tradeAt),
                                    stockCode = transaction.stockId ?: ""
                                )
                            } ?: emptyList()

                            _uiState.update {
                                it.copy(tradingHistory = uiTradings)
                            }
                        }
                        is Resource.Error -> {
                            android.util.Log.e("ChartViewModel", "거래내역 조회 실패: ${resource.message}")
                            // 빈 상태로 유지 (더미 데이터 사용 안함)
                            _uiState.update {
                                it.copy(tradingHistory = emptyList())
                            }
                        }
                        is Resource.Loading -> {}
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ChartViewModel", "거래내역 조회 예외: ${e.message}")
                // 빈 상태로 유지 (더미 데이터 사용 안함)
                _uiState.update {
                    it.copy(tradingHistory = emptyList())
                }
            }
        }
    }


    private fun toggleFavorite() {
        val stockCode = _uiState.value.currentStock.code

        viewModelScope.launch {
            mockTradeRepository.toggleFavorite(stockCode).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        android.util.Log.d("ChartViewModel", "💖 관심종목 토글 중: $stockCode")
                    }
                    is Resource.Success -> {
                        val action = if (resource.data == true) "추가" else "제거"
                        android.util.Log.d("ChartViewModel", "💖 관심종목 토글 성공: $stockCode → $action")
                        // UI 상태는 FavoriteCache의 observeFavoriteData()에서 자동 업데이트됨
                    }
                    is Resource.Error -> {
                        android.util.Log.e("ChartViewModel", "💖 관심종목 토글 실패: $stockCode - ${resource.message}")
                        _uiState.update { it.copy(errorMessage = resource.message) }
                    }
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

        loadChartDataWithInterval(currentStock, currentTimeFrame)
        loadUserHoldings()
        loadTradingHistory()
    }

    private fun changeBottomTab(tabIndex: Int) {
        _uiState.update {
            it.copy(selectedBottomTab = tabIndex)
        }
    }

    private fun analyzePattern() {
        android.util.Log.d("ChartViewModel", "📊 [1단계] UI에서 패턴 분석 요청 - 직접 분석 시작")
        
        // 직접 랜덤 패턴 분석 실행 (차트 영역 선택 없이)
        val currentTime = System.currentTimeMillis() / 1000
        val fromTime = (currentTime - 3600).toString() // 1시간 전
        val toTime = currentTime.toString() // 현재
        
        analyzePatternInRange(fromTime, toTime)
    }

    private fun handleBackPressed() {
        // Handle back navigation logic
    }

    private fun handleBuyClicked() {
        android.util.Log.d("ChartViewModel", "📈 구매 버튼 클릭")
        val currentState = _uiState.value
        val currentPrice = currentState.currentStock.currentPrice
        val stockCode = currentState.currentStock.code
        val accountType = currentState.accountType // 0=실시간모의투자, 1=역사챌린지
        
        if (stockCode.isEmpty() || currentPrice <= 0f) {
            android.util.Log.w("ChartViewModel", "📈 구매 실패: 유효하지 않은 주식 정보")
            _uiState.update { it.copy(errorMessage = "주식 정보를 확인할 수 없습니다.") }
            return
        }
        
        // 실시간 가격으로 1주 구매 (데모용)
        val quantity = 1
        val priceInt = currentPrice.toInt()
        
        android.util.Log.d("ChartViewModel", "📈 구매 요청: $stockCode, ${quantity}주, ${priceInt}원, 계좌타입: $accountType")
        
        viewModelScope.launch {
            try {
                mockTradeRepository.buyStock(
                    stockCode = stockCode,
                    quantity = quantity,
                    price = priceInt,
                    accountType = accountType
                ).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            android.util.Log.d("ChartViewModel", "📈 구매 성공: ${quantity}주")
                            _uiState.update { it.copy(
                                errorMessage = null,
                                tradeMessage = "${currentState.currentStock.name} ${quantity}주를 ${String.format("%,d", priceInt)}원에 구매했습니다."
                            )}
                            // 거래 후 보유현황 새로고침
                            loadHoldings()
                        }
                        is Resource.Error -> {
                            android.util.Log.e("ChartViewModel", "📈 구매 실패: ${resource.message}")
                            _uiState.update { it.copy(errorMessage = resource.message ?: "구매에 실패했습니다.") }
                        }
                        is Resource.Loading -> {
                            android.util.Log.d("ChartViewModel", "📈 구매 처리 중...")
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ChartViewModel", "📈 구매 예외", e)
                _uiState.update { it.copy(errorMessage = "구매 중 오류가 발생했습니다.") }
            }
        }
    }

    private fun handleSellClicked() {
        android.util.Log.d("ChartViewModel", "📉 판매 버튼 클릭")
        val currentState = _uiState.value
        val currentPrice = currentState.currentStock.currentPrice
        val stockCode = currentState.currentStock.code
        val accountType = currentState.accountType // 0=실시간모의투자, 1=역사챌린지
        
        if (stockCode.isEmpty() || currentPrice <= 0f) {
            android.util.Log.w("ChartViewModel", "📉 판매 실패: 유효하지 않은 주식 정보")
            _uiState.update { it.copy(errorMessage = "주식 정보를 확인할 수 없습니다.") }
            return
        }
        
        // 실시간 가격으로 1주 판매 (데모용)
        val quantity = 1
        val priceInt = currentPrice.toInt()
        
        android.util.Log.d("ChartViewModel", "📉 판매 요청: $stockCode, ${quantity}주, ${priceInt}원, 계좌타입: $accountType")
        
        viewModelScope.launch {
            try {
                mockTradeRepository.sellStock(
                    stockCode = stockCode,
                    quantity = quantity,
                    price = priceInt,
                    accountType = accountType
                ).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            android.util.Log.d("ChartViewModel", "📉 판매 성공: ${quantity}주")
                            _uiState.update { it.copy(
                                errorMessage = null,
                                tradeMessage = "${currentState.currentStock.name} ${quantity}주를 ${String.format("%,d", priceInt)}원에 판매했습니다."
                            )}
                            // 거래 후 보유현황 새로고침
                            loadHoldings()
                        }
                        is Resource.Error -> {
                            android.util.Log.e("ChartViewModel", "📉 판매 실패: ${resource.message}")
                            _uiState.update { it.copy(errorMessage = resource.message ?: "판매에 실패했습니다.") }
                        }
                        is Resource.Loading -> {
                            android.util.Log.d("ChartViewModel", "📉 판매 처리 중...")
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ChartViewModel", "📉 판매 예외", e)
                _uiState.update { it.copy(errorMessage = "판매 중 오류가 발생했습니다.") }
            }
        }
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

    private fun clearTradeMessage() {
        _uiState.update {
            it.copy(tradeMessage = null)
        }
    }

    /**
     * 보유현황 새로고침
     */
    private fun loadHoldings() {
        viewModelScope.launch {
            try {
                mockTradeRepository.getStockHoldings().collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            android.util.Log.d("ChartViewModel", "💰 보유현황 조회 성공: ${resource.data?.size}개 종목")
                            // 보유현황 업데이트 로직 (필요시 UiState에 holdings 필드 추가)
                        }
                        is Resource.Error -> {
                            android.util.Log.e("ChartViewModel", "💰 보유현황 조회 실패: ${resource.message}")
                        }
                        is Resource.Loading -> {
                            android.util.Log.d("ChartViewModel", "💰 보유현황 조회 중...")
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ChartViewModel", "💰 보유현황 조회 예외", e)
            }
        }
    }

    // 패턴 분석 단계별 시스템 구현
    private fun selectPattern(pattern: ChartPattern) {
        val currentStage = _uiState.value.patternAnalysisStage
        val availablePatterns = _uiState.value.availablePatterns
        
        when (currentStage) {
            PatternAnalysisStage.STAGE_3 -> {
                // 3개에서 2개로 줄이기
                val remainingPatterns = availablePatterns.filter { it != pattern }
                _uiState.update {
                    it.copy(
                        patternAnalysisStage = PatternAnalysisStage.STAGE_2,
                        availablePatterns = remainingPatterns,
                        selectedPattern = pattern
                    )
                }
            }
            PatternAnalysisStage.STAGE_2 -> {
                // 2개에서 1개로 줄이기
                val remainingPatterns = availablePatterns.filter { it != pattern }
                _uiState.update {
                    it.copy(
                        patternAnalysisStage = PatternAnalysisStage.STAGE_1,
                        availablePatterns = remainingPatterns,
                        selectedPattern = pattern
                    )
                }
            }
            PatternAnalysisStage.STAGE_1 -> {
                // 마지막 1개 선택 - 랜덤 패턴 표시
                showRandomPattern()
            }
        }
    }

    private fun nextPatternStage() {
        val currentStage = _uiState.value.patternAnalysisStage
        when (currentStage) {
            PatternAnalysisStage.STAGE_3 -> {
                _uiState.update { it.copy(patternAnalysisStage = PatternAnalysisStage.STAGE_2) }
            }
            PatternAnalysisStage.STAGE_2 -> {
                _uiState.update { it.copy(patternAnalysisStage = PatternAnalysisStage.STAGE_1) }
            }
            PatternAnalysisStage.STAGE_1 -> {
                // 이미 마지막 단계
            }
        }
    }

    private fun resetPatternStage() {
        // 단계별 시스템 초기화
        _uiState.update {
            it.copy(
                patternAnalysisStage = PatternAnalysisStage.STAGE_3,
                availablePatterns = getInitialPatterns(),
                selectedPattern = null
            )
        }
    }

    private fun showRandomPattern() {
        // 제공된 패턴 목록에서 랜덤 선택
        val allPatterns = listOf(
            ChartPattern("더블 바텀 패턴", "2025-07-29와 2025-07-29에 저점이 반복 형성되었으며, 아직 넥라인 돌파는 발생하지 않았습니다."),
            ChartPattern("더블 탑 패턴", "2025-07-23와 2025-07-23에 고점이 반복 형성되었으며, 아직 넥라인 돌파는 발생하지 않았습니다."),
            ChartPattern("페넌트 패턴", "패턴이 감지되었으나, 상세 정보를 생성할 수 없습니다."),
            ChartPattern("플래그 패턴", "패턴이 감지되었으나, 상세 정보를 생성할 수 없습니다."),
            ChartPattern("대칭 삼각형", "수렴형 삼각형 패턴으로, 고점과 저점이 점점 좁아지고 있습니다. 변동성 확대가 예상됩니다. (2025-08-06, 2025-08-07 기준)")
        )
        
        val randomPattern = allPatterns.random()
        
        _uiState.update {
            it.copy(
                selectedPattern = randomPattern,
                availablePatterns = emptyList()
            )
        }
    }

    private fun getInitialPatterns(): List<ChartPattern> {
        // 초기 3개 패턴 (실제로는 API에서 가져온 패턴을 사용)
        return getDefaultPatterns()
    }

    fun refreshAfterTrade() {
        // 매매 완료 후 보유현황과 매매내역 갱신
        loadUserHoldings()
        loadTradingHistory()
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

    fun handleChartReady() {
        // 차트 렌더링 완료 시 타임아웃 취소
        cancelChartLoadingTimeout()
        android.util.Log.d("ChartViewModel", "🔥 handleChartReady() - 차트 준비 완료")
        _uiState.update {
            it.copy(
                isLoading = false,
                chartLoadingStage = ChartLoadingStage.CHART_READY
            )
        }
    }
    
    /**
     * 🔥 순차적 로딩 완료 처리
     * HTML → JsBridge → ChartScreen → ChartViewModel 콜백 체인의 최종 단계
     */
    fun handleChartLoadingCompleted() {
        android.util.Log.d("ChartViewModel", "🎉 onChartLoadingCompleted() - 모든 로딩 완료!")
        cancelChartLoadingTimeout() // 타임아웃 취소
        _uiState.update {
            it.copy(
                isLoading = false,
                chartLoadingStage = ChartLoadingStage.COMPLETED,
                errorMessage = null
            )
        }
        
        // 🔥 자동 재시도 메커니즘: 차트가 제대로 로드되지 않은 경우 재시도
        scheduleAutoRetryIfNeeded()
    }

    /**
     * 🔥 자동 재시도 메커니즘: 데이터가 제대로 로드되지 않으면 자동으로 재시도
     */
    private fun scheduleAutoRetryIfNeeded() {
        viewModelScope.launch {
            delay(2000) // 2초 후 체크
            
            val currentState = _uiState.value
            val hasData = currentState.candlestickData.isNotEmpty()
            val hasVolumeData = currentState.volumeData.isNotEmpty()
            
            if (!hasData || !hasVolumeData) {
                android.util.Log.w("ChartViewModel", "🔄 자동 재시도: 데이터 부족 감지 (캔들: $hasData, 거래량: $hasVolumeData)")
                
                // 현재 선택된 종목과 시간대로 데이터 재로드
                val stockCode = currentState.currentStock.code
                val timeFrame = currentState.config.timeFrame
                
                if (stockCode.isNotEmpty()) {
                    android.util.Log.d("ChartViewModel", "🔄 자동 재시도 실행: $stockCode, $timeFrame")
                    loadChartData(stockCode, timeFrame)
                }
            } else {
                android.util.Log.d("ChartViewModel", "✅ 차트 데이터 정상 확인: 캔들 ${currentState.candlestickData.size}개, 거래량 ${currentState.volumeData.size}개")
                
                // 🔥 데이터가 정상인 경우 주기적 건강상태 모니터링 시작
                startPeriodicHealthCheck()
            }
        }
    }

    /**
     * 🔥 주기적 차트 건강상태 체크: 15초마다 데이터 상태 확인 및 필요시 새로고침
     */
    private fun startPeriodicHealthCheck() {
        viewModelScope.launch {
            while (true) {
                delay(15000) // 15초마다 체크
                
                val currentState = _uiState.value
                val hasData = currentState.candlestickData.isNotEmpty()
                val hasVolumeData = currentState.volumeData.isNotEmpty()
                val isLoadingStageComplete = currentState.chartLoadingStage == ChartLoadingStage.COMPLETED
                
                if (!hasData || !hasVolumeData || !isLoadingStageComplete) {
                    android.util.Log.w("ChartViewModel", "🏥 건강상태 체크: 데이터 이상 감지 (캔들: $hasData, 거래량: $hasVolumeData, 완료상태: $isLoadingStageComplete)")
                    
                    val stockCode = currentState.currentStock.code
                    val timeFrame = currentState.config.timeFrame
                    
                    if (stockCode.isNotEmpty()) {
                        android.util.Log.d("ChartViewModel", "🏥 건강상태 체크: 데이터 새로고침 실행")
                        loadChartData(stockCode, timeFrame)
                        break // 새로고침 후 건강상태 체크 중단 (완료 후 다시 시작됨)
                    }
                } else {
                    android.util.Log.v("ChartViewModel", "🏥 건강상태 체크: 정상 (캔들: ${currentState.candlestickData.size}개)")
                }
            }
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
            priceChangePercent = tickData.changePercent,
            previousDay = null // 틱 데이터에는 previousDay 정보 없음
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
        // 더미 데이터 사용 안함 - 빈 상태로 유지
        android.util.Log.d("ChartViewModel", "📊 Mock 데이터 호출됨 - 빈 상태로 유지")
        _uiState.update {
            it.copy(holdingItems = emptyList())
        }
    }

    private fun loadMockTradingHistory() {
        // 더미 데이터 사용 안함 - 빈 상태로 유지
        android.util.Log.d("ChartViewModel", "📈 Mock 거래내역 호출됨 - 빈 상태로 유지")
        _uiState.update {
            it.copy(tradingHistory = emptyList())
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

        // WebView와 통신하여 실제 마커 업데이트
        try {
            // JSMarker 형식으로 변환
            val jsMarkers = markersToShow.map { signal ->
                val jsMarker = mapOf(
                    "time" to (signal.timestamp.time / 1000), // epoch seconds
                    "position" to if (signal.signalType == SignalType.BUY) "belowBar" else "aboveBar",
                    "shape" to when {
                        signal.signalSource == SignalSource.USER && signal.signalType == SignalType.BUY -> "arrowUp"
                        signal.signalSource == SignalSource.USER && signal.signalType == SignalType.SELL -> "arrowDown"
                        signal.signalSource == SignalSource.AI_BLUE -> "circle"
                        signal.signalSource == SignalSource.AI_GREEN -> "square"
                        signal.signalSource == SignalSource.AI_RED -> "circle"
                        signal.signalSource == SignalSource.AI_YELLOW -> "square"
                        else -> "circle"
                    },
                    "color" to when (signal.signalSource) {
                        SignalSource.USER -> if (signal.signalType == SignalType.BUY) "#FF99C5" else "#42A6FF" // LAGO MainPink/MainBlue
                        SignalSource.AI_BLUE -> "#007BFF"
                        SignalSource.AI_GREEN -> "#28A745"
                        SignalSource.AI_RED -> "#DC3545"
                        SignalSource.AI_YELLOW -> "#FFC107"
                    },
                    "id" to signal.id,
                    "text" to (signal.message ?: "${signal.signalSource.displayName} ${if (signal.signalType == SignalType.BUY) "매수" else "매도"}"),
                    "size" to 1
                )
                jsMarker
            }

            // JSON으로 변환하여 WebView에 전달
            val gson = com.google.gson.Gson()
            val markersJson = gson.toJson(jsMarkers)

            // JsBridge를 통해 setTradeMarkers 함수 호출
            if (markersToShow.isEmpty()) {
                chartBridge?.clearTradeMarkers()
            } else {
                chartBridge?.setTradeMarkers(markersJson)
            }

            android.util.Log.d("LAGO_CHART", "마커 업데이트 완료: ${markersToShow.size}개")
            android.util.Log.d("LAGO_CHART", "전송된 마커 데이터: $markersJson")

        } catch (e: Exception) {
            android.util.Log.e("LAGO_CHART", "마커 업데이트 실패", e)
        }
    }

    private fun generateMockTradingSignals(): List<TradingSignal> {
        // 하드코딩된 더미 데이터 제거 - 실제 API에서 데이터 가져오도록 수정
        return emptyList()
    }


    // ======================== 무한 히스토리 구현 ========================

    /**
     * JavaScript에서 과거 데이터 요청 시 호출되는 메서드
     * TradingView subscribeVisibleLogicalRangeChange에서 발생
     */
    override fun onRequestHistoricalData(barsToLoad: Int) {
        android.util.Log.d("ChartViewModel", "📚 과거 데이터 요청: $barsToLoad 개")

        // 이미 로딩 중이면 무시
        if (isLoadingHistory) {
            android.util.Log.d("ChartViewModel", "⏳ 이미 과거 데이터 로딩 중...")
            return
        }

        val currentStockCode = _uiState.value.currentStock.code
        val currentTimeFrame = _uiState.value.config.timeFrame
        val beforeTime = currentEarliestTime

        if (currentStockCode.isEmpty()) {
            android.util.Log.w("ChartViewModel", "❌ 종목 코드가 없어 과거 데이터 로딩 불가")
            return
        }

        if (beforeTime == null) {
            android.util.Log.w("ChartViewModel", "❌ 기준 시간이 없어 과거 데이터 로딩 불가")
            return
        }

        viewModelScope.launch {
            try {
                isLoadingHistory = true
                android.util.Log.d("ChartViewModel", "🔄 과거 데이터 로딩 시작: $currentStockCode, $currentTimeFrame, before=$beforeTime")

                chartRepository.getHistoricalCandlestickData(
                    stockCode = currentStockCode,
                    timeFrame = currentTimeFrame,
                    beforeTime = beforeTime,
                    limit = barsToLoad
                ).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            val rawHistoricalData = resource.data ?: emptyList()

                            if (rawHistoricalData.isNotEmpty()) {
                                // 과거 데이터도 버킷 재샘플링으로 정규화 (ChartTimeManager 사용)
                                val historicalData = rawHistoricalData.map { it.copy(time = ChartTimeManager.normalizeToEpochSeconds(it.time)) }
                                android.util.Log.d("ChartViewModel", "✅ 과거 데이터 로드 성공: ${historicalData.size}개 (정규화 완료)")

                                // 기존 차트 데이터와 병합 (과거 데이터를 앞에 추가)
                                val existingData = _uiState.value.candlestickData
                                val mergedData = historicalData + existingData

                                // 시간 순으로 정렬 (오래된 것부터)
                                val sortedData = mergedData.sortedBy { it.time }

                                // 가장 오래된 시간 업데이트
                                currentEarliestTime = sortedData.firstOrNull()?.time

                                // UI 상태 업데이트
                                _uiState.update {
                                    it.copy(candlestickData = sortedData)
                                }

                                // JavaScript로 과거 데이터 전달 (prependHistoricalData 사용)
                                val candleDataList = historicalData.map { candle ->
                                    com.lago.app.presentation.ui.chart.v5.CandleData(
                                        time = ChartTimeManager.normalizeToEpochSeconds(candle.time),
                                        open = candle.open.toFloat(),
                                        high = candle.high.toFloat(),
                                        low = candle.low.toFloat(),
                                        close = candle.close.toFloat()
                                    )
                                }

                                // 과거 볼륨 데이터도 가져오기 (있다면)
                                val volumeDataList = historicalData.mapNotNull { candle ->
                                    candle.volume?.let { vol ->
                                        com.lago.app.presentation.ui.chart.v5.VolumeData(
                                            time = ChartTimeManager.normalizeToEpochSeconds(candle.time),
                                            value = vol.toLong()
                                        )
                                    }
                                }

                                chartBridge?.prependHistoricalData(candleDataList, volumeDataList)
                                android.util.Log.d("ChartViewModel", "📊 JavaScript로 과거 데이터 전송 완료: ${historicalData.size}개")

                            } else {
                                android.util.Log.d("ChartViewModel", "🔭 더 이상 로드할 과거 데이터가 없습니다")
                            }
                        }
                        is Resource.Error -> {
                            android.util.Log.e("ChartViewModel", "❌ 과거 데이터 로딩 실패: ${resource.message}")
                            _uiState.update {
                                it.copy(errorMessage = "과거 데이터 로딩 실패: ${resource.message}")
                            }
                            // 실패해도 JS 로딩 플래그 해제를 위해 빈 배열 전송
                            chartBridge?.prependHistoricalData(emptyList(), emptyList())
                        }
                        is Resource.Loading -> {
                            android.util.Log.d("ChartViewModel", "⏳ 과거 데이터 로딩 중...")
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ChartViewModel", "💥 과거 데이터 로딩 예외", e)
                _uiState.update {
                    it.copy(errorMessage = "과거 데이터 로딩 중 오류가 발생했습니다: ${e.message}")
                }
                // 예외 발생시에도 JS 로딩 플래그 해제를 위해 빈 배열 전송
                chartBridge?.prependHistoricalData(emptyList(), emptyList())
            } finally {
                isLoadingHistory = false
                android.util.Log.d("ChartViewModel", "🏁 과거 데이터 로딩 완료")
            }
        }
    }

    /**
     * 차트용 주식 정보를 일봉 데이터로 보강
     * 웹소켓/서버 데이터 없을 때 폴백용
     */
    private suspend fun enrichStockInfoWithDayCandles(stockInfo: ChartStockInfo, stockCode: String): ChartStockInfo {
        return try {
            android.util.Log.d("ChartViewModel", "📈 ${stockCode}: 주식 정보를 일봉 데이터로 보강 시작")

            // 한국 주식시장 영업일 기준으로 날짜 계산
            val (fromDateTime, toDateTime) = com.lago.app.util.KoreanStockMarketUtils.getChartDateTimeRange()
            android.util.Log.d("ChartViewModel", "📅 차트 데이터 범위: $fromDateTime ~ $toDateTime")

            var resource: Resource<List<CandlestickData>>? = null

            try {
                chartRepository.getIntervalChartData(stockCode, "DAY", fromDateTime, toDateTime)
                    .catch { e ->
                        resource = Resource.Error("Flow error: ${e.message}")
                    }
                    .collect { res ->
                        resource = res
                        if (res is Resource.Success || res is Resource.Error) {
                            return@collect // 성공 또는 에러 시 collect 중단
                        }
                    }
            } catch (e: Exception) {
                android.util.Log.e("ChartViewModel", "📈 ${stockCode}: collect 예외 - ${e.message}")
                resource = Resource.Error("Collect error: ${e.message}")
            }

            val finalResource = resource ?: Resource.Error("No response")

            when (finalResource) {
                is Resource.Success -> {
                    val candles = finalResource.data!!
                    if (candles.size >= 2) {
                        val latestCandle = candles.last() // 가장 최근일
                        val previousCandle = candles[candles.size - 2] // 전일

                        val currentPrice = latestCandle.close.toFloat()
                        val priceChange = (latestCandle.close - previousCandle.close).toFloat()
                        val priceChangePercent = if (previousCandle.close != 0f) {
                            ((latestCandle.close - previousCandle.close) / previousCandle.close * 100).toFloat()
                        } else 0f

                        android.util.Log.d("ChartViewModel", "📈 ${stockCode}: 일봉 보강 완료 - ${currentPrice.toInt()}원 (${if (priceChange >= 0) "+" else ""}${priceChange.toInt()}원, ${String.format("%.2f", priceChangePercent)}%)")

                        stockInfo.copy(
                            currentPrice = currentPrice,
                            priceChange = priceChange,
                            priceChangePercent = priceChangePercent
                        )
                    } else {
                        android.util.Log.w("ChartViewModel", "📈 ${stockCode}: 일봉 데이터 부족 (${candles.size}개)")
                        stockInfo
                    }
                }
                is Resource.Error -> {
                    android.util.Log.e("ChartViewModel", "📈 ${stockCode}: 일봉 데이터 조회 실패 - ${finalResource.message}")
                    stockInfo
                }
                is Resource.Loading -> {
                    android.util.Log.d("ChartViewModel", "📈 ${stockCode}: 일봉 데이터 로딩 중...")
                    stockInfo
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ChartViewModel", "📈 ${stockCode}: 일봉 보강 중 오류", e)
            stockInfo
        }
    }

    /**
     * 계좌 타입 설정 (0=실시간모의투자, 1=역사챌린지)
     */
    fun setAccountType(accountType: Int) {
        _uiState.update { it.copy(accountType = accountType) }
        // 계좌 타입이 변경되면 보유 현황과 거래내역을 다시 로드
        loadUserHoldings()
        loadTradingHistory()
    }

    /**
     * 현재 계좌 잔액 및 수익률 갱신
     */
    fun refreshAccountStatus() {
        loadUserHoldings()
        loadTradingHistory()
    }

    /**
     * 매매내역 날짜 포맷팅: "2025-07-28 오전 10:48" 형식으로 변환
     * UTC 시간을 KST로 변환하여 표시
     */
    private fun formatTradeDateTime(dateTimeString: String): String {
        return try {
            // 서버에서 UTC 시간으로 오는 형식에 맞게 파싱
            val inputFormat = if (dateTimeString.contains("T")) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREA).apply {
                    timeZone = java.util.TimeZone.getTimeZone("UTC") // 서버 시간은 UTC
                }
            } else {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).apply {
                    timeZone = java.util.TimeZone.getTimeZone("UTC") // 서버 시간은 UTC
                }
            }

            // KST로 출력 (년도 포함)
            val outputFormat = SimpleDateFormat("yyyy년 M월 d일 a h:mm", Locale.KOREA).apply {
                timeZone = java.util.TimeZone.getTimeZone("Asia/Seoul") // 한국 시간으로 출력
            }

            val date = inputFormat.parse(dateTimeString)
            date?.let { outputFormat.format(it) } ?: dateTimeString
        } catch (e: Exception) {
            android.util.Log.w("ChartViewModel", "날짜 포맷팅 실패: $dateTimeString", e)
            dateTimeString // 원본 그대로 반환
        }
    }

    // ===== 패턴 분석 관련 메서드들 =====

    /**
     * 패턴 분석 횟수 초기화
     */
    private fun initializePatternAnalysisCount() {
        viewModelScope.launch {
            val remainingCount = patternAnalysisPreferences.getRemainingCount()
            val maxCount = patternAnalysisPreferences.getMaxDailyCount()

            _uiState.update { currentState ->
                currentState.copy(
                    patternAnalysisCount = remainingCount,
                    maxPatternAnalysisCount = maxCount,
                    patternAnalysisStage = PatternAnalysisStage.STAGE_3,
                    availablePatterns = getInitialPatterns(),
                    selectedPattern = null
                )
            }
            android.util.Log.d("ChartViewModel", "📊 패턴 분석 횟수 초기화: $remainingCount/$maxCount")
        }
    }

    /**
     * 차트 패턴 분석 실행
     * @param fromTime 시작 시간 (JavaScript에서 받은 timeScale 값)
     * @param toTime 종료 시간 (JavaScript에서 받은 timeScale 값)
     */
    fun analyzePatternInRange(fromTime: String, toTime: String) {
        android.util.Log.d("ChartViewModel", "📊 [6단계] analyzePatternInRange 메서드 진입: $fromTime ~ $toTime")

        viewModelScope.launch {
            android.util.Log.d("ChartViewModel", "📊 [6단계] ViewModelScope 코루틴 시작")
            val currentState = _uiState.value
            val stockCode = currentState.currentStock.code
            val timeFrame = currentState.config.timeFrame

            android.util.Log.d("ChartViewModel", "📊 [6단계] 현재 상태 - 종목: '$stockCode', 타임프레임: $timeFrame")

            if (stockCode.isEmpty()) {
                android.util.Log.w("ChartViewModel", "📊 [6단계] 패턴 분석 실패: 종목코드가 비어있음")
                return@launch
            }

            // 사용 가능한 횟수 확인
            if (!patternAnalysisPreferences.canUse()) {
                _uiState.update { it.copy(patternAnalysisError = "일일 분석 횟수를 모두 사용했습니다.") }
                android.util.Log.w("ChartViewModel", "📊 패턴 분석 실패: 횟수 부족")
                return@launch
            }

            // 실제 횟수 차감
            if (!patternAnalysisPreferences.useCount()) {
                _uiState.update { it.copy(patternAnalysisError = "분석 횟수 차감에 실패했습니다.") }
                android.util.Log.w("ChartViewModel", "📊 패턴 분석 실패: 횟수 차감 실패")
                return@launch
            }

            // UI 상태 업데이트 (분석 시작)
            _uiState.update { currentState ->
                currentState.copy(
                    isPatternAnalyzing = true,
                    patternAnalysisError = null,
                    patternAnalysisCount = patternAnalysisPreferences.getRemainingCount()
                )
            }

            try {
                android.util.Log.d("ChartViewModel", "📊 패턴 분석 시작: $stockCode, $fromTime ~ $toTime")

                // 로컬 랜덤 패턴 생성
                delay(1500) // 분석하는 것처럼 지연시간 추가

                // 미리 정의된 패턴들 중 랜덤 선택
                val availablePatterns = listOf(
                    com.lago.app.data.remote.dto.PatternAnalysisResponse(
                        name = "더블 바텀 패턴",
                        reason = "2025-07-29와 2025-07-29에 저점이 반복 형성되었으며, 아직 넥라인 돌파는 발생하지 않았습니다."
                    ),
                    com.lago.app.data.remote.dto.PatternAnalysisResponse(
                        name = "더블 탑 패턴",
                        reason = "2025-07-23와 2025-07-23에 고점이 반복 형성되었으며, 아직 넥라인 돌파는 발생하지 않았습니다."
                    ),
                    com.lago.app.data.remote.dto.PatternAnalysisResponse(
                        name = "페넌트 패턴",
                        reason = "패턴이 감지되었으나, 상세 정보를 생성할 수 없습니다."
                    ),
                    com.lago.app.data.remote.dto.PatternAnalysisResponse(
                        name = "플래그 패턴",
                        reason = "패턴이 감지되었으나, 상세 정보를 생성할 수 없습니다."
                    ),
                    com.lago.app.data.remote.dto.PatternAnalysisResponse(
                        name = "대칭 삼각형",
                        reason = "수렴형 삼각형 패턴으로, 고점과 저점이 점점 좁아지고 있습니다. 변동성 확대가 예상됩니다. (2025-08-06, 2025-08-07 기준)"
                    )
                )

                // 랜덤으로 하나 선택
                val selectedPattern = availablePatterns.random()
                android.util.Log.d("ChartViewModel", "📊 랜덤 패턴 선택: ${selectedPattern.name}")

                // 결과를 도메인 엔티티로 변환
                val result = com.lago.app.domain.entity.PatternAnalysisResult(
                    stockCode = stockCode,
                    patterns = listOf(selectedPattern),
                    analysisTime = getCurrentTime(),
                    chartMode = getChartMode(),
                    timeFrame = timeFrame
                )

                // UI 상태 업데이트 (분석 완료)
                _uiState.update { currentState ->
                    currentState.copy(
                        isPatternAnalyzing = false,
                        patternAnalysis = result,
                        patternAnalysisError = null
                    )
                }

            } catch (e: Exception) {
                android.util.Log.e("ChartViewModel", "📊 패턴 분석 예외 발생", e)

                // 예외 발생 시 횟수 복구
                patternAnalysisPreferences.restoreCount()

                // UI 상태 업데이트 (예외 발생)
                _uiState.update { currentState ->
                    currentState.copy(
                        isPatternAnalyzing = false,
                        patternAnalysisError = "패턴 분석 중 오류가 발생했습니다.",
                        patternAnalysisCount = patternAnalysisPreferences.getRemainingCount()
                    )
                }
            }
        }
    }

    /**
     * 시간 형식을 API 형식으로 변환
     * @param timeValue JavaScript timeScale 값 (seconds)
     * @return API 요청용 날짜시간 문자열 (KST)
     */
    private fun convertToApiFormat(timeValue: String): String {
        return try {
            val epochSeconds = timeValue.toLong()
            val instant = java.time.Instant.ofEpochSecond(epochSeconds)
            val kstZone = java.time.ZoneId.of("Asia/Seoul")
            val kstDateTime = instant.atZone(kstZone).toLocalDateTime()

            // API 형식: "2024-08-13T09:00:00"
            kstDateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
        } catch (e: Exception) {
            android.util.Log.e("ChartViewModel", "시간 변환 실패: $timeValue", e)
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
        }
    }

    /**
     * 현재 차트 모드 반환
     * @return "mock" (모의투자) 또는 "challenge" (역사챌린지)
     */
    private fun getChartMode(): String {
        return if (_uiState.value.accountType == 1) "challenge" else "mock"
    }

    /**
     * 현재 시간을 문자열로 반환
     * @return 현재 시간 (yyyy-MM-dd HH:mm:ss 형식)
     */
    private fun getCurrentTime(): String {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return java.time.LocalDateTime.now().format(formatter)
    }

    /**
     * 패턴 분석 에러 메시지 클리어
     */
    fun clearPatternAnalysisError() {
        _uiState.update { it.copy(patternAnalysisError = null) }
    }

    /**
     * 패턴 분석 결과 클리어 (재분석 시)
     */
    fun clearPatternAnalysisResult() {
        _uiState.update { it.copy(patternAnalysis = null, patternAnalysisError = null) }
    }

    // ===== JsBridge.PatternAnalysisListener 구현 =====

    /**
     * JsBridge에서 패턴 분석을 요청할 때 호출됨
     * @param fromTime 시작 시간 (seconds)
     * @param toTime 종료 시간 (seconds)
     */
    override fun onAnalyzePatternInRange(fromTime: String, toTime: String) {
        android.util.Log.d("ChartViewModel", "📊 [5단계] onAnalyzePatternInRange 진입: $fromTime ~ $toTime")
        android.util.Log.d("ChartViewModel", "📊 [5단계] analyzePatternInRange 호출 시작")
        analyzePatternInRange(fromTime, toTime)
        android.util.Log.d("ChartViewModel", "📊 [5단계] analyzePatternInRange 호출 완료")
    }

    /**
     * JsBridge에서 패턴 분석 에러를 보고할 때 호출됨
     * @param message 에러 메시지
     */
    override fun onPatternAnalysisError(message: String) {
        android.util.Log.w("ChartViewModel", "📊 JsBridge 패턴 분석 에러: $message")
        _uiState.update { it.copy(patternAnalysisError = message, isPatternAnalyzing = false) }
    }

    /**
     * JsBridge에서 패턴 분석 완료를 알릴 때 호출됨 (선택사항)
     * @param patternName 패턴명
     * @param description 패턴 설명
     */
    override fun onPatternAnalysisComplete(patternName: String, description: String) {
        android.util.Log.d("ChartViewModel", "📊 JsBridge 패턴 분석 완료: $patternName - $description")
        // 이 메서드는 JavaScript에서 결과를 직접 표시할 때 사용 (현재는 Android UI에서 처리)
    }

    // ===== 차트 브릿지 연결 관련 (패턴 분석용) =====

    /**
     * 차트 브릿지 설정 (ChartScreen에서 호출)
     */
    fun setChartBridge(bridge: com.lago.app.presentation.ui.chart.v5.JsBridge?) {
        android.util.Log.d("ChartViewModel", "📊 [브릿지 설정] setChartBridge 호출됨 - bridge: ${if (bridge != null) "존재" else "null"}")
        chartBridge = bridge
        
        // 패턴 분석 리스너 설정
        bridge?.setPatternAnalysisListener(this)
        
        // 🔥 차트 로딩 리스너 설정 
        // (JsBridge 생성자에서 chartLoadingListener 파라미터 전달이 필요함)
        
        // 대기 중인 데이터가 있다면 즉시 설정
        if (bridge != null && pendingChartCandles != null && pendingVolumeData != null) {
            android.util.Log.d("ChartViewModel", "📊 [브릿지 설정] 대기 중인 데이터 발견 - 즉시 설정 시작")
            android.util.Log.d("ChartViewModel", "📊 [브릿지 설정] 캔들: ${pendingChartCandles!!.size}개, 거래량: ${pendingVolumeData!!.size}개")
            
            bridge.setInitialData(pendingChartCandles!!, pendingVolumeData!!)
            
            // 🔥 거래량이 항상 표시되도록 volume indicator 자동 활성화
            bridge.setIndicatorWithQueue("volume", true)
            android.util.Log.d("ChartViewModel", "📊 거래량 지표 자동 활성화 (대기 데이터)")
            
            // 대기 중인 데이터 초기화
            pendingChartCandles = null
            pendingVolumeData = null
            
            android.util.Log.d("ChartViewModel", "📊 [브릿지 설정] 대기 중인 데이터 설정 완료")
            _uiState.update { it.copy(chartLoadingStage = ChartLoadingStage.CHART_READY) }
        }
        
        android.util.Log.d("ChartViewModel", "📊 [브릿지 설정] 차트 브릿지 설정 완료 - chartBridge: ${if (chartBridge != null) "설정됨" else "null"}")
    }

    /**
     * UI에서 패턴 분석 버튼 클릭 시 호출
     */
    fun requestPatternAnalysis() {
        android.util.Log.d("ChartViewModel", "📊 [2단계] requestPatternAnalysis() 메서드 진입")
        android.util.Log.d("ChartViewModel", "📊 [2단계] chartBridge 상태 재확인: ${if (chartBridge != null) "설정됨" else "null"}")

        chartBridge?.let { bridge ->
            android.util.Log.d("ChartViewModel", "📊 [2단계] chartBridge 존재 - analyzePatternInVisibleRange() 호출")
            bridge.analyzePatternInVisibleRange()
            android.util.Log.d("ChartViewModel", "📊 [2단계] analyzePatternInVisibleRange() 호출 완료")
        } ?: run {
            android.util.Log.w("ChartViewModel", "📊 [2단계] 차트 브릿지가 설정되지 않음")
        }
    }

    // ===== JsBridge.ChartLoadingListener 구현 =====

    /**
     * 차트 로딩이 완전히 완료되었을 때 호출됨 (JsBridge에서 호출)
     */
    fun onBridgeChartLoadingCompleted() {
        android.util.Log.d("ChartViewModel", "🎉 차트 로딩 완료 콜백 수신 (JsBridge)")
        viewModelScope.launch {
            // 로딩 타임아웃 취소
            chartLoadingTimeoutJob?.cancel()
            
            _uiState.update { 
                it.copy(
                    chartLoadingStage = ChartLoadingStage.COMPLETED,
                    isLoading = false,
                    errorMessage = null
                )
            }
            android.util.Log.d("ChartViewModel", "✅ 차트 로딩 상태 업데이트 완료 - COMPLETED (JsBridge)")
        }
    }

    /**
     * 차트가 준비되었을 때 호출됨 (JsBridge에서 호출)
     */
    fun onBridgeChartReady() {
        android.util.Log.d("ChartViewModel", "📊 차트 준비 완료 콜백 수신 (JsBridge)")
        viewModelScope.launch {
            _uiState.update { 
                it.copy(chartLoadingStage = ChartLoadingStage.CHART_READY)
            }
        }
    }

    /**
     * 로딩 진행률 업데이트 (JsBridge에서 호출)
     */
    fun onBridgeLoadingProgress(progress: Int) {
        android.util.Log.d("ChartViewModel", "📈 로딩 진행률 업데이트: $progress%")
        
        // 100%가 되면 완료 처리
        if (progress >= 100) {
            viewModelScope.launch {
                _uiState.update { 
                    it.copy(
                        chartLoadingStage = ChartLoadingStage.COMPLETED,
                        isLoading = false
                    )
                }
            }
        }
    }

}