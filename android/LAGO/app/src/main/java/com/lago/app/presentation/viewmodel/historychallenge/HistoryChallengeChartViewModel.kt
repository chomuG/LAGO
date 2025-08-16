package com.lago.app.presentation.viewmodel.historychallenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lago.app.domain.entity.*
import com.lago.app.domain.repository.ChartRepository
import com.lago.app.domain.usecase.AnalyzeChartPatternUseCase
import com.lago.app.data.local.prefs.UserPreferences
import com.lago.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import javax.inject.Inject
import com.lago.app.presentation.ui.chart.v5.HistoricalDataRequestListener
import com.lago.app.presentation.viewmodel.chart.ChartUiEvent
import com.lago.app.presentation.viewmodel.chart.ChartUiState
import com.lago.app.presentation.viewmodel.chart.ChartLoadingStage
import com.lago.app.presentation.viewmodel.chart.HoldingItem
import com.lago.app.presentation.viewmodel.chart.TradingItem
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.flow.sample
import kotlin.time.Duration.Companion.milliseconds

// MinuteBucket 클래스 제거됨 - 웹소켓에서 완전한 OHLCV 데이터를 받음

/**
 * 타임프레임별 버킷 시작 시각 계산 (TradingView 권장)
 * @param epochSec epoch seconds
 * @param timeFrame 타임프레임 ("1", "3", "5", "15", "30", "60", "D" 등)
 * @return 버킷 시작 시각 (epoch seconds)
 */
private fun bucketStartEpochSec(epochSec: Long, timeFrame: String): Long {
    val frameSec = when (timeFrame) {
        "1" -> 60L
        "3" -> 3 * 60L
        "5" -> 5 * 60L
        "10" -> 10 * 60L
        "15" -> 15 * 60L
        "30" -> 30 * 60L
        "60" -> 60 * 60L
        "D" -> 24 * 60 * 60L // 일봉은 24시간 단위로 스냅
        else -> 60L // 기본 1분
    }
    val bucketStart = (epochSec / frameSec) * frameSec
    
    // 디버그 로그
    val originalTime = Date(epochSec * 1000)
    val bucketTime = Date(bucketStart * 1000)
    android.util.Log.d("BucketSnap", "🕐 ${timeFrame}분봉 스냅: ${originalTime} → ${bucketTime}")
    
    return bucketStart
}

/**
 * 역사챌린지 전용 차트 ViewModel
 * 역사챌린지 데이터 구조에 맞춘 별도 구현
 */
@HiltViewModel
class HistoryChallengeChartViewModel @Inject constructor(
    private val chartRepository: ChartRepository,
    private val analyzeChartPatternUseCase: AnalyzeChartPatternUseCase,
    private val userPreferences: UserPreferences,
    private val realTimeCache: com.lago.app.data.cache.RealTimeStockCache
) : ViewModel(), HistoricalDataRequestListener {
    
    private val _uiState = MutableStateFlow(ChartUiState())
    val uiState: StateFlow<ChartUiState> = _uiState.asStateFlow()
    
    private val _uiEvent = MutableSharedFlow<ChartUiEvent>()
    
    // 안전 타임아웃을 위한 Job
    private var chartLoadingTimeoutJob: Job? = null
    
    // 실시간 차트 업데이트를 위한 JsBridge
    private var chartBridge: com.lago.app.presentation.ui.chart.v5.JsBridge? = null
    var jsBridge: com.lago.app.presentation.ui.chart.v5.JsBridge? = null
    
    // 무한 히스토리 관련 상태 변수들
    private var currentEarliestTime: Long? = null
    private var isLoadingHistory = false
    private val gson = Gson()
    
    // 역사챌린지 웹소켓 연결 상태
    private var historyChallengeWebSocket: Job? = null
    
    // 현재 활성 역사챌린지 정보
    private var currentChallengeId: Int? = null
    
    init {
        loadHistoryChallengeList()
        
        // 실시간 캐시 데이터 모니터링 시작
        observeRealTimeData()
    }
    
    /**
     * 실시간 캐시 데이터 모니터링
     */
    private fun observeRealTimeData() {
        viewModelScope.launch {
            realTimeCache.quotes
                .sample(500) // 500ms마다 샘플링
                .collect { quotesMap ->
                    android.util.Log.d("HistoryChallengeChartViewModel", "🔥 실시간 데이터 수신: ${quotesMap.size}개 종목")
                    
                    // 현재 차트에 표시된 종목의 실시간 데이터 업데이트 (역사챌린지 전용 키 사용)
                    val currentStockCode = _uiState.value.currentStock.code
                    val historyChallengeKey = "HISTORY_CHALLENGE_$currentStockCode"
                    if (quotesMap.containsKey(historyChallengeKey)) {
                        val realTimeData = quotesMap[historyChallengeKey]!!
                        updateChartWithRealTimeData(realTimeData)
                        android.util.Log.d("HistoryChallengeChartViewModel", "🔥 ${currentStockCode} (키: $historyChallengeKey) 차트 실시간 업데이트: ${realTimeData.closePrice}원")
                    }
                }
        }
    }
    
    /**
     * 차트에 실시간 데이터 반영 (TradingView 권장 방식: series.update())
     */
    private fun updateChartWithRealTimeData(realTimeData: com.lago.app.domain.entity.StockRealTimeData) {
        _uiState.update { currentState ->
            val updatedStock = currentState.currentStock.copy(
                currentPrice = realTimeData.closePrice?.toFloat() ?: currentState.currentStock.currentPrice,
                priceChange = realTimeData.priceChange.toFloat(), // WebSocket의 실제 전일대비 가격차이
                priceChangePercent = realTimeData.priceChangePercent.toFloat() // calculated property 사용
            )
            
            currentState.copy(currentStock = updatedStock)
        }
        
        // 역사챌린지에서는 실시간 차트 업데이트를 웹소켓 데이터로만 처리
        // updateChartWithRealTimeData는 웹소켓에서 originDateTime을 받아야 함
        android.util.Log.d("HistoryChallengeChart", "역사챌린지: 실시간 업데이트는 웹소켓 originDateTime 기반으로만 처리")
    }
    
    /**
     * TradingView 권장 방식으로 차트 초기 데이터 설정 (series.setData)
     */
    private fun setInitialChartData(candlestickData: List<CandlestickData>, interval: String) {
        chartBridge?.let { bridge ->
            // 역사챌린지에서는 항상 epoch seconds로 시간 변환
            val chartCandles = candlestickData.map { candle ->
                // candle.time이 milliseconds면 seconds로 변환, 이미 seconds면 그대로 사용
                val epochSeconds = if (candle.time > 9999999999L) candle.time / 1000 else candle.time
                com.lago.app.presentation.ui.chart.v5.CandleData(
                    time = epochSeconds,
                    open = candle.open,
                    high = candle.high,
                    low = candle.low,
                    close = candle.close
                )
            }
            
            // 거래량 데이터 변환 (있는 경우)
            val volumeData = candlestickData.map { candle ->
                // candle.time이 milliseconds면 seconds로 변환, 이미 seconds면 그대로 사용
                val epochSeconds = if (candle.time > 9999999999L) candle.time / 1000 else candle.time
                com.lago.app.presentation.ui.chart.v5.VolumeData(
                    time = epochSeconds,
                    value = candle.volume,
                    color = if (candle.close >= candle.open) "#26a69a" else "#ef5350" // 상승/하락 색상
                )
            }
            
            // 차트에 초기 데이터 설정
            bridge.setInitialData(chartCandles, volumeData)
            android.util.Log.d("HistoryChallengeChart", "🔥 차트 초기 데이터 설정 완료: ${chartCandles.size}개 캔들")
        }
    }
    
    /**
     * 현재 선택된 타임프레임 반환
     */
    private fun getCurrentTimeFrame(): String {
        return _uiState.value.config.timeFrame
    }
    
    /**
     * TradingView 권장 방식으로 과거 데이터를 차트 앞쪽에 추가
     */
    private fun prependHistoricalDataToChart(historicalData: List<CandlestickData>) {
        chartBridge?.let { bridge ->
            // 역사챌린지에서는 항상 epoch seconds로 시간 변환
            val historicalCandles = historicalData.map { candle ->
                // candle.time이 milliseconds면 seconds로 변환, 이미 seconds면 그대로 사용
                val epochSeconds = if (candle.time > 9999999999L) candle.time / 1000 else candle.time
                com.lago.app.presentation.ui.chart.v5.CandleData(
                    time = epochSeconds,
                    open = candle.open,
                    high = candle.high,
                    low = candle.low,
                    close = candle.close
                )
            }
            
            // 과거 거래량 데이터 변환
            val historicalVolumes = historicalData.map { candle ->
                // candle.time이 milliseconds면 seconds로 변환, 이미 seconds면 그대로 사용
                val epochSeconds = if (candle.time > 9999999999L) candle.time / 1000 else candle.time
                com.lago.app.presentation.ui.chart.v5.VolumeData(
                    time = epochSeconds,
                    value = candle.volume,
                    color = if (candle.close >= candle.open) "#26a69a" else "#ef5350"
                )
            }
            
            // TradingView 권장 방식: 기존 데이터와 병합 후 setData 호출
            bridge.prependHistoricalData(historicalCandles, historicalVolumes)
            android.util.Log.d("HistoryChallengeChart", "🔥 무한 히스토리 데이터 차트에 추가 완료: ${historicalCandles.size}개 캔들")
        }
    }

    /**
     * 역사챌린지 데이터를 CandlestickData로 변환 (타임프레임별 버킷 시작 시각으로 스냅)
     */
    private fun convertHistoryChallengeData(data: Map<String, Any>, timeFrame: String = "1"): CandlestickData {
        val originDateTime = data["originDateTime"] as String
        val openPrice = (data["openPrice"] as Number).toFloat()
        val highPrice = (data["highPrice"] as Number).toFloat()
        val lowPrice = (data["lowPrice"] as Number).toFloat()
        val closePrice = (data["closePrice"] as Number).toFloat()
        val volume = (data["volume"] as Number).toLong()
        
        // originDateTime을 타임프레임별 버킷 시작 시각으로 변환 (TradingView 권장)
        val bucketStartTime = parseHistoryChallengeDateTime(originDateTime, timeFrame)
        
        return CandlestickData(
            time = bucketStartTime,
            open = openPrice,
            high = highPrice,
            low = lowPrice,
            close = closePrice,
            volume = volume
        )
    }
    
    /**
     * 역사챌린지 과거 데이터 로드
     */
    private fun loadHistoryChallengeData(challengeId: Int, interval: String, pastMinutes: Int? = null, pastDays: Int? = null) {
        viewModelScope.launch {
            try {
                android.util.Log.d("HistoryChallengeChart", "🔥 차트 데이터 로드 시작")
                android.util.Log.d("HistoryChallengeChart", "🔥 파라미터: challengeId=$challengeId, interval=$interval")
                android.util.Log.d("HistoryChallengeChart", "🔥 과거 기간: pastMinutes=$pastMinutes, pastDays=$pastDays")
                
                _uiState.update { it.copy(isLoading = true, chartLoadingStage = ChartLoadingStage.DATA_LOADING) }
                
                // 역사챌린지 전용 차트 API 호출 (현재 시간 기준)
                val response = chartRepository.getHistoryChallengeChart(challengeId, interval, pastMinutes, pastDays)
                
                response.collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            val data = resource.data ?: return@collect
                            
                            android.util.Log.d("HistoryChallengeChart", "🔥 차트 데이터 로드 성공: ${data.size}개 캔들")
                            if (data.isNotEmpty()) {
                                val firstCandle = data.first()
                                val lastCandle = data.last()
                                android.util.Log.d("HistoryChallengeChart", "🔥 첫 캔들: ${java.util.Date(firstCandle.time)} - ${firstCandle.close}원")
                                android.util.Log.d("HistoryChallengeChart", "🔥 마지막 캔들: ${java.util.Date(lastCandle.time)} - ${lastCandle.close}원")
                            }
                            
                            _uiState.update { state ->
                                state.copy(
                                    candlestickData = data,
                                    isLoading = false,
                                    chartLoadingStage = ChartLoadingStage.CHART_READY,
                                    errorMessage = null
                                )
                            }
                            
                            // TradingView 권장 방식으로 차트 초기 데이터 설정
                            setInitialChartData(data, interval)
                            
                            // 가장 오래된 시간 기록 (무한 히스토리용)
                            currentEarliestTime = data.minByOrNull { it.time }?.time
                            android.util.Log.d("HistoryChallengeChart", "🔥 가장 오래된 시간: ${currentEarliestTime?.let { java.util.Date(it) }}")
                        }
                        is Resource.Error -> {
                            android.util.Log.e("HistoryChallengeChart", "🚨 차트 데이터 로드 실패: ${resource.message}")
                            _uiState.update { 
                                it.copy(
                                    isLoading = false, 
                                    errorMessage = resource.message,
                                    chartLoadingStage = ChartLoadingStage.CHART_READY
                                ) 
                            }
                        }
                        is Resource.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = "데이터 로드 실패: ${e.message}",
                        chartLoadingStage = ChartLoadingStage.CHART_READY
                    ) 
                }
            }
        }
    }
    
    /**
     * 역사챌린지 실시간 웹소켓 연결
     */
    private fun connectHistoryChallengeWebSocket(challengeId: Int) {
        // 기존 연결 해제
        historyChallengeWebSocket?.cancel()
        
        historyChallengeWebSocket = viewModelScope.launch {
            try {
                android.util.Log.d("HistoryChallengeChart", "역사챌린지 웹소켓 연결 시작: challengeId=$challengeId")
                
                // STOMP WebSocket 연결 설정
                val stompClient = ua.naiksoftware.stomp.Stomp.over(
                    ua.naiksoftware.stomp.Stomp.ConnectionProvider.OKHTTP, 
                    com.lago.app.util.Constants.WS_STOCK_URL
                )
                
                // 역사챌린지 토픽 구독: /topic/history-challenge
                val subscription = stompClient.topic("/topic/history-challenge")
                    .subscribe({ stompMessage ->
                        try {
                            android.util.Log.d("HistoryChallengeChart", "웹소켓 메시지 수신: ${stompMessage.payload}")
                            
                            // JSON 파싱하여 HistoryChallengeWebSocketData로 변환
                            val webSocketData = gson.fromJson(
                                stompMessage.payload, 
                                com.lago.app.data.remote.dto.HistoryChallengeWebSocketData::class.java
                            )
                            
                            // 모든 데이터 처리 (rowId가 0이어도 유효한 데이터)
                            // 현재 타임프레임에 맞는 버킷 시작 시각으로 스냅
                            val currentTimeFrame = _uiState.value.config.timeFrame
                            val candleData = CandlestickData(
                                time = parseHistoryChallengeDateTime(webSocketData.originDateTime, currentTimeFrame),
                                open = webSocketData.openPrice.toFloat(),
                                high = webSocketData.highPrice.toFloat(),
                                low = webSocketData.lowPrice.toFloat(),
                                close = webSocketData.closePrice.toFloat(),
                                volume = webSocketData.volume.toLong()
                            )
                            
                            // UI 업데이트
                            updateRealTimeChart(candleData)
                        } catch (e: Exception) {
                            android.util.Log.e("HistoryChallengeChart", "웹소켓 데이터 파싱 실패", e)
                        }
                    }, { error ->
                        android.util.Log.e("HistoryChallengeChart", "웹소켓 구독 오류", error)
                    })
                
                // 연결 시작
                stompClient.connect()
                
                android.util.Log.d("HistoryChallengeChart", "역사챌린지 웹소켓 연결 완료")
                
            } catch (e: Exception) {
                android.util.Log.e("HistoryChallengeChart", "웹소켓 연결 실패", e)
            }
        }
    }
    
    /**
     * 역사챌린지 날짜시간 문자열을 타임프레임별 버킷 시작 시각으로 변환 (TradingView 권장)
     */
    private fun parseHistoryChallengeDateTime(dateTimeString: String, timeFrame: String = "1"): Long {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val parsedDate = format.parse(dateTimeString) ?: return 0L
            
            // 기본적으로 초, 밀리초 제거
            val calendar = Calendar.getInstance()
            calendar.time = parsedDate
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val rawEpochSec = calendar.timeInMillis / 1000
            
            // 타임프레임별 버킷 시작 시각으로 스냅
            bucketStartEpochSec(rawEpochSec, timeFrame)
        } catch (e: Exception) {
            try {
                val format2 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val parsedDate = format2.parse(dateTimeString) ?: return 0L
                
                val calendar = Calendar.getInstance()
                calendar.time = parsedDate
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val rawEpochSec = calendar.timeInMillis / 1000
                
                // 타임프레임별 버킷 시작 시각으로 스냅
                bucketStartEpochSec(rawEpochSec, timeFrame)
            } catch (e2: Exception) {
                0L
            }
        }
    }
    
    /**
     * 실시간 데이터 업데이트 (웹소켓 originDateTime 기반)
     * 웹소켓에서 받은 역사챌린지 데이터를 TradingView 차트에 반영
     */
    private fun updateRealTimeChart(candleData: CandlestickData) {
        // WebSocket에서 받은 데이터는 이미 버킷 시작 시각으로 스냅됨
        val bucketTime = candleData.time // 이미 타임프레임별 버킷 시작 시각
        val tickPrice = candleData.close
        val tickVolume = candleData.volume
        
        android.util.Log.d("HistoryChallengeChart", "📊 역사챌린지 데이터 수신: time=${Date(bucketTime * 1000)}, price=$tickPrice, volume=$tickVolume")
        
        // 현재 상태의 캔들스틱 데이터 업데이트
        _uiState.update { state ->
            val updatedCandles = state.candlestickData.toMutableList()
            
            // 같은 시간대면 마지막 캔들 업데이트, 다르면 새 캔들 추가
            if (updatedCandles.isNotEmpty()) {
                val lastCandle = updatedCandles.last()
                
                if (bucketTime == lastCandle.time) {
                    // 동일한 버킷: 기존 캔들 업데이트
                    updatedCandles[updatedCandles.size - 1] = candleData
                    android.util.Log.d("HistoryChallengeChart", "📊 기존 캔들 업데이트: ${Date(bucketTime * 1000)}")
                } else if (bucketTime > lastCandle.time) {
                    // 새로운 버킷: 새 캔들 추가
                    updatedCandles.add(candleData)
                    android.util.Log.d("HistoryChallengeChart", "📊 새 캔들 생성: ${Date(bucketTime * 1000)}")
                } else {
                    // 과거 시간 (정상적이지 않음)
                    android.util.Log.w("HistoryChallengeChart", "📊 과거 시간 데이터 무시: ${Date(bucketTime * 1000)} < ${Date(lastCandle.time * 1000)}")
                    return@update state
                }
            } else {
                updatedCandles.add(candleData)
                android.util.Log.d("HistoryChallengeChart", "📊 첫 캔들 생성: ${Date(bucketTime * 1000)}")
            }
            
            state.copy(
                candlestickData = updatedCandles,
                currentStock = state.currentStock.copy(
                    currentPrice = candleData.close,
                    priceChange = if (updatedCandles.size > 1) {
                        candleData.close - updatedCandles[updatedCandles.size - 2].close
                    } else 0f,
                    priceChangePercent = if (updatedCandles.size > 1) {
                        val prevClose = updatedCandles[updatedCandles.size - 2].close
                        ((candleData.close - prevClose) / prevClose) * 100
                    } else 0f
                )
            )
        }
        
        // TradingView 권장 방식으로 실시간 차트 업데이트
        chartBridge?.let { bridge ->
            val realTimeCandle = com.lago.app.presentation.ui.chart.v5.CandleData(
                time = bucketTime, // 이미 버킷 시작 시각
                open = candleData.open,
                high = candleData.high,
                low = candleData.low,
                close = candleData.close
            )
            
            // series.update() 방식: 동일 time = 덮어쓰기, 새 time = 새 바 추가
            bridge.updateRealTimeBar(realTimeCandle)
            android.util.Log.d("HistoryChallengeChart", "📊 차트 업데이트 완료: ${Date(bucketTime * 1000)}")
            
            // 거래량도 업데이트
            val realTimeVolume = com.lago.app.presentation.ui.chart.v5.VolumeData(
                time = bucketTime,
                value = candleData.volume,
                color = if (candleData.close >= candleData.open) "#26a69a" else "#ef5350"
            )
            bridge.updateRealTimeVolume(realTimeVolume)
        }
    }
    
    // 기존 aggregateTickToMinuteBar 함수 제거됨
    // 웹소켓에서 완전한 OHLCV 데이터를 받으므로 별도 집계 불필요
    
    /**
     * 역사챌린지 목록 로드
     */
    private fun loadHistoryChallengeList() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, chartLoadingStage = ChartLoadingStage.DATA_LOADING) }
                
                val response = chartRepository.getHistoryChallenge()
                response.collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            val challenge = resource.data ?: return@collect
                            // 단일 챌린지를 기본으로 설정
                            currentChallengeId = challenge.challengeId
                                
                            _uiState.update { state ->
                                state.copy(
                                    currentStock = ChartStockInfo(
                                        code = challenge.stockCode,
                                        name = challenge.stockName,
                                        currentPrice = challenge.currentPrice.toFloat(),
                                        priceChange = challenge.fluctuationPrice.toFloat(),
                                        priceChangePercent = challenge.fluctuationRate,
                                        previousDay = null
                                    ),
                                    config = ChartConfig(
                                        stockCode = challenge.stockCode,
                                        timeFrame = "1", // 역사챌린지는 1분봉
                                        indicators = ChartIndicators()
                                    ),
                                    isLoading = false,
                                    chartLoadingStage = ChartLoadingStage.CHART_READY
                                )
                            }
                                
                            // 역사챌린지 차트 데이터 로드 (현재 시간 기준 과거 100일)
                            loadHistoryChallengeData(
                                challengeId = challenge.challengeId,
                                interval = "DAY",
                                pastDays = 100
                            )
                                
                            // 웹소켓 연결
                            connectHistoryChallengeWebSocket(challenge.challengeId)
                        }
                        is Resource.Error -> {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false, 
                                    errorMessage = resource.message,
                                    chartLoadingStage = ChartLoadingStage.CHART_READY
                                ) 
                            }
                        }
                        is Resource.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = "역사챌린지 데이터 로드 실패: ${e.message}",
                        chartLoadingStage = ChartLoadingStage.CHART_READY
                    ) 
                }
            }
        }
    }
    
    private fun loadInitialData() {
        viewModelScope.launch {
            // 기본 주식 정보 설정 (삼성전자)
            _uiState.update { state ->
                state.copy(
                    currentStock = ChartStockInfo(
                        code = "005930",
                        name = "삼성전자",
                        currentPrice = 0f,
                        priceChange = 0f,
                        priceChangePercent = 0f,
                        previousDay = null
                    ),
                    config = ChartConfig(
                        stockCode = "005930",
                        timeFrame = "1", // 역사챌린지는 1분봉
                        indicators = ChartIndicators()
                    )
                )
            }
        }
    }
    
    /**
     * 역사챌린지 변경
     */
    fun changeHistoryChallenge(challengeId: Int) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, chartLoadingStage = ChartLoadingStage.DATA_LOADING) }
                
                // 챌린지 정보 로드 (단일 챌린지이므로 challengeId 검증 생략)
                val response = chartRepository.getHistoryChallenge()
                response.collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            val challenge = resource.data ?: return@collect
                            
                            // 요청된 challengeId와 실제 challengeId가 다르면 경고 로그
                            if (challenge.challengeId != challengeId) {
                                android.util.Log.w("HistoryChallengeChart", "요청된 challengeId($challengeId)와 실제 challengeId(${challenge.challengeId})가 다릅니다.")
                            }
                            
                            currentChallengeId = challenge.challengeId
                            
                            _uiState.update { state ->
                                state.copy(
                                    currentStock = ChartStockInfo(
                                        code = challenge.stockCode,
                                        name = challenge.stockName,
                                        currentPrice = challenge.currentPrice.toFloat(),
                                        priceChange = challenge.fluctuationPrice.toFloat(),
                                        priceChangePercent = challenge.fluctuationRate,
                                        previousDay = null
                                    ),
                                    config = state.config.copy(stockCode = challenge.stockCode)
                                )
                            }
                            
                            // 차트 데이터 로드 (현재 시간 기준 과거 기간)
                            val interval = convertTimeFrameToInterval(_uiState.value.config.timeFrame)
                            val (pastMinutes, pastDays) = getTimeFramePeriod(interval)
                            loadHistoryChallengeData(
                                challengeId = challenge.challengeId,
                                interval = interval,
                                pastMinutes = pastMinutes,
                                pastDays = pastDays
                            )
                            
                            // 웹소켓 재연결
                            connectHistoryChallengeWebSocket(challenge.challengeId)
                        }
                        is Resource.Error -> {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false, 
                                    errorMessage = resource.message,
                                    chartLoadingStage = ChartLoadingStage.CHART_READY
                                ) 
                            }
                        }
                        is Resource.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = "챌린지 변경 실패: ${e.message}",
                        chartLoadingStage = ChartLoadingStage.CHART_READY
                    ) 
                }
            }
        }
    }
    
    fun onEvent(event: ChartUiEvent) {
        when (event) {
            is ChartUiEvent.ChangeStock -> {
                changeStock(event.stockCode)
            }
            is ChartUiEvent.ChangeStockWithInfo -> {
                changeStockWithInfo(event.stockCode, event.stockInfo)
            }
            is ChartUiEvent.ChangeTimeFrame -> {
                changeTimeFrame(event.timeFrame)
            }
            is ChartUiEvent.ToggleIndicator -> {
                toggleIndicator(event.indicatorType, event.enabled)
            }
            is ChartUiEvent.RefreshData -> {
                refreshData()
            }
            is ChartUiEvent.ClearError -> {
                clearError()
            }
            is ChartUiEvent.ToggleFavorite -> {
                toggleFavorite()
            }
            is ChartUiEvent.ChangeBottomTab -> {
                changeBottomTab(event.tabIndex)
            }
            is ChartUiEvent.AnalyzePattern -> {
                analyzePattern()
            }
            is ChartUiEvent.BuyClicked -> {
                // 역사챌린지에서 구매 버튼 클릭
                android.util.Log.d("HistoryChallengeChart", "구매 버튼 클릭")
            }
            is ChartUiEvent.SellClicked -> {
                // 역사챌린지에서 판매 버튼 클릭
                android.util.Log.d("HistoryChallengeChart", "판매 버튼 클릭")
            }
            is ChartUiEvent.ShowIndicatorSettings -> {
                _uiState.update { it.copy(showIndicatorSettings = true) }
            }
            is ChartUiEvent.HideIndicatorSettings -> {
                _uiState.update { it.copy(showIndicatorSettings = false) }
            }
            is ChartUiEvent.ToggleIndicatorSettings -> {
                _uiState.update { it.copy(showIndicatorSettings = !it.showIndicatorSettings) }
            }
            is ChartUiEvent.LoadTradingSignals -> {
                loadTradingSignals()
            }
            is ChartUiEvent.ToggleUserTradingSignals -> {
                _uiState.update { it.copy(showUserTradingSignals = event.show) }
            }
            is ChartUiEvent.SelectAITradingSignals -> {
                _uiState.update { it.copy(selectedAI = event.aiSource) }
            }
            is ChartUiEvent.BackPressed -> {
                // 뒤로 가기 처리
            }
        }
    }
    
    private fun changeStock(stockCode: String) {
        viewModelScope.launch {
            // 기존 웹소켓 연결 해제
            historyChallengeWebSocket?.cancel()
            
            try {
                _uiState.update { it.copy(isLoading = true, chartLoadingStage = ChartLoadingStage.DATA_LOADING) }
                
                val stockInfoResult = chartRepository.getStockInfo(stockCode)
                stockInfoResult.collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            val stockInfo = resource.data ?: return@collect
                            
                            _uiState.update { state ->
                                state.copy(
                                    currentStock = stockInfo,
                                    config = state.config.copy(stockCode = stockCode),
                                    chartLoadingStage = ChartLoadingStage.DATA_LOADING
                                )
                            }
                            
                            // 차트 데이터 로드 (역사챌린지용)
                            currentChallengeId?.let { challengeId ->
                                val currentConfig = _uiState.value.config
                                val interval = convertTimeFrameToInterval(currentConfig.timeFrame)
                                val (pastMinutes, pastDays) = getTimeFramePeriod(interval)
                                loadHistoryChallengeData(
                                    challengeId = challengeId,
                                    interval = interval,
                                    pastMinutes = pastMinutes,
                                    pastDays = pastDays
                                )
                                
                                // 실시간 웹소켓 연결
                                connectHistoryChallengeWebSocket(challengeId)
                            }
                            
                            // 매매내역과 보유현황 로드
                            loadTradingHistory(stockCode)
                            loadHoldings()
                        }
                        is Resource.Error -> {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false, 
                                    errorMessage = resource.message,
                                    chartLoadingStage = ChartLoadingStage.CHART_READY
                                ) 
                            }
                        }
                        is Resource.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = "주식 정보 로드 실패: ${e.message}",
                        chartLoadingStage = ChartLoadingStage.CHART_READY
                    ) 
                }
            }
        }
    }
    
    private fun changeStockWithInfo(stockCode: String, stockInfo: ChartStockInfo) {
        _uiState.update { state ->
            state.copy(
                currentStock = stockInfo,
                config = state.config.copy(stockCode = stockCode)
            )
        }
        // 역사챌린지용 데이터 로드
        currentChallengeId?.let { challengeId ->
            val interval = convertTimeFrameToInterval(_uiState.value.config.timeFrame)
            val (pastMinutes, pastDays) = getTimeFramePeriod(interval)
            loadHistoryChallengeData(
                challengeId = challengeId,
                interval = interval,
                pastMinutes = pastMinutes,
                pastDays = pastDays
            )
            connectHistoryChallengeWebSocket(challengeId)
        }
        loadTradingHistory(stockCode)
        loadHoldings()
    }
    
    private fun changeTimeFrame(timeFrame: String) {
        _uiState.update { state ->
            state.copy(config = state.config.copy(timeFrame = timeFrame))
        }
        // 역사챌린지용 타임프레임 변경
        currentChallengeId?.let { challengeId ->
            val interval = convertTimeFrameToInterval(timeFrame)
            val (pastMinutes, pastDays) = getTimeFramePeriod(interval)
            loadHistoryChallengeData(
                challengeId = challengeId,
                interval = interval,
                pastMinutes = pastMinutes,
                pastDays = pastDays
            )
        }
    }
    
    /**
     * UI 타임프레임을 API interval로 변환
     */
    private fun convertTimeFrameToInterval(timeFrame: String): String {
        return when (timeFrame) {
            "1" -> "MINUTE"
            "3" -> "MINUTE3"
            "5" -> "MINUTE5"
            "10" -> "MINUTE10"
            "15" -> "MINUTE15"
            "30" -> "MINUTE30"
            "60" -> "MINUTE60"
            "D" -> "DAY"
            "W" -> "WEEK"
            "M" -> "MONTH"
            "Y" -> "YEAR"
            else -> "DAY"
        }
    }
    
    /**
     * interval에 따른 적절한 과거 기간 반환
     */
    private fun getTimeFramePeriod(interval: String): Pair<Int?, Int?> {
        return when (interval) {
            "MINUTE", "MINUTE3", "MINUTE5" -> Pair(1440, null) // 24시간 (1440분)
            "MINUTE10", "MINUTE15" -> Pair(4320, null) // 3일 (4320분)
            "MINUTE30", "MINUTE60" -> Pair(10080, null) // 7일 (10080분)
            "DAY" -> Pair(null, 100) // 100일
            "WEEK" -> Pair(null, 365) // 52주 (365일)
            "MONTH" -> Pair(null, 730) // 24개월 (730일)
            "YEAR" -> Pair(null, 1825) // 5년 (1825일)
            else -> Pair(null, 100) // 기본 100일
        }
    }
    
    /**
     * 현재 시간을 API 형식으로 반환
     * 현재 시간으로 요청하면 서버에서 해당하는 과거 데이터를 반환
     */
    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }
    
    /**
     * 차트 표시를 위한 적절한 기간 계산
     * interval에 따라 fromDateTime을 과거로 설정하여 충분한 데이터 확보
     */
    private fun getChartDateRange(interval: String): Pair<String, String> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val toDateTime = dateFormat.format(calendar.time) // 현재 시간
        
        // interval별로 적절한 과거 기간 설정
        when (interval) {
            "MINUTE", "MINUTE3", "MINUTE5" -> {
                calendar.add(Calendar.HOUR_OF_DAY, -24) // 24시간 전
            }
            "MINUTE10", "MINUTE15" -> {
                calendar.add(Calendar.DAY_OF_MONTH, -3) // 3일 전
            }
            "MINUTE30", "MINUTE60" -> {
                calendar.add(Calendar.DAY_OF_MONTH, -7) // 1주일 전
            }
            "DAY" -> {
                calendar.add(Calendar.DAY_OF_MONTH, -100) // 100일 전
            }
            "WEEK" -> {
                calendar.add(Calendar.WEEK_OF_YEAR, -52) // 52주 전
            }
            "MONTH" -> {
                calendar.add(Calendar.MONTH, -24) // 24개월 전
            }
            "YEAR" -> {
                calendar.add(Calendar.YEAR, -5) // 5년 전
            }
            else -> {
                calendar.add(Calendar.DAY_OF_MONTH, -100) // 기본 100일 전
            }
        }
        
        val fromDateTime = dateFormat.format(calendar.time)
        return Pair(fromDateTime, toDateTime)
    }
    
    private fun toggleIndicator(indicatorType: String, enabled: Boolean) {
        _uiState.update { state ->
            val indicators = when (indicatorType) {
                "sma5" -> state.config.indicators.copy(sma5 = enabled)
                "sma20" -> state.config.indicators.copy(sma20 = enabled)
                "sma60" -> state.config.indicators.copy(sma60 = enabled)
                "sma120" -> state.config.indicators.copy(sma120 = enabled)
                "rsi" -> state.config.indicators.copy(rsi = enabled)
                "macd" -> state.config.indicators.copy(macd = enabled)
                "bollingerBands" -> state.config.indicators.copy(bollingerBands = enabled)
                "volume" -> state.config.indicators.copy(volume = enabled)
                else -> state.config.indicators
            }
            state.copy(config = state.config.copy(indicators = indicators))
        }
        
        // 지표 변경시 데이터 다시 로드
        refreshData()
    }
    
    private fun refreshData() {
        val currentState = _uiState.value
        // 역사챌린지용 데이터 새로고침
        currentChallengeId?.let { challengeId ->
            val interval = convertTimeFrameToInterval(currentState.config.timeFrame)
            val (pastMinutes, pastDays) = getTimeFramePeriod(interval)
            loadHistoryChallengeData(
                challengeId = challengeId,
                interval = interval,
                pastMinutes = pastMinutes,
                pastDays = pastDays
            )
        }
    }
    
    private fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    private fun toggleFavorite() {
        _uiState.update { it.copy(isFavorite = !it.isFavorite) }
    }
    
    private fun changeBottomTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedBottomTab = tabIndex) }
    }
    
    private fun analyzePattern() {
        val currentState = _uiState.value
        
        // 분석 횟수 체크
        if (currentState.patternAnalysisCount >= currentState.maxPatternAnalysisCount) {
            _uiState.update { 
                it.copy(patternAnalysisError = "일일 패턴 분석 횟수를 초과했습니다.")
            }
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.update { 
                    it.copy(
                        isPatternAnalyzing = true,
                        patternAnalysisError = null
                    ) 
                }
                
                val result = analyzeChartPatternUseCase(
                    stockCode = currentState.currentStock.code,
                    timeFrame = currentState.config.timeFrame
                )
                
                result.collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _uiState.update { state ->
                                state.copy(
                                    isPatternAnalyzing = false,
                                    patternAnalysis = resource.data,
                                    patternAnalysisCount = state.patternAnalysisCount + 1,
                                    patternAnalysisError = null
                                )
                            }
                        }
                        is Resource.Error -> {
                            _uiState.update { 
                                it.copy(
                                    isPatternAnalyzing = false,
                                    patternAnalysisError = resource.message ?: "패턴 분석 중 오류가 발생했습니다."
                                ) 
                            }
                        }
                        is Resource.Loading -> {
                            _uiState.update { it.copy(isPatternAnalyzing = true) }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isPatternAnalyzing = false,
                        patternAnalysisError = "패턴 분석 실패: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    private fun loadTradingSignals() {
        viewModelScope.launch {
            try {
                // 역사챌린지에서는 매매신호가 제공되지 않음
                // AI 매매봇은 일반 모의투자에서만 사용
                _uiState.update { state ->
                    state.copy(tradingSignals = emptyList())
                }
                android.util.Log.d("HistoryChallengeChart", "역사챌린지: 매매신호 기능 비활성화")
            } catch (e: Exception) {
                android.util.Log.e("HistoryChallengeChart", "매매신호 로드 실패", e)
            }
        }
    }
    
    private fun loadTradingHistory(stockCode: String) {
        viewModelScope.launch {
            try {
                // 역사챌린지는 개별 계좌가 아닌 글로벌 챌린지이므로
                // 개인 매매내역은 제공되지 않음
                _uiState.update { state ->
                    state.copy(tradingHistory = emptyList())
                }
                android.util.Log.d("HistoryChallengeChart", "역사챌린지: 개인 매매내역 없음")
            } catch (e: Exception) {
                android.util.Log.e("HistoryChallengeChart", "매매내역 로드 실패", e)
            }
        }
    }
    
    private fun loadHoldings() {
        viewModelScope.launch {
            try {
                // 역사챌린지는 실제 보유주식이 아닌 시뮬레이션이므로
                // 보유현황은 제공되지 않음
                _uiState.update { state ->
                    state.copy(holdingItems = emptyList())
                }
                android.util.Log.d("HistoryChallengeChart", "역사챌린지: 보유현황 없음")
            } catch (e: Exception) {
                android.util.Log.e("HistoryChallengeChart", "보유현황 로드 실패", e)
            }
        }
    }
    
    // 차트 준비 완료 콜백
    fun onChartReady() {
        android.util.Log.d("HistoryChallengeChart", "차트 준비 완료")
        _uiState.update { it.copy(isLoading = false, chartLoadingStage = ChartLoadingStage.CHART_READY) }
    }
    
    // 차트 로딩 상태 변경 콜백
    fun onChartLoadingChanged(isLoading: Boolean) {
        if (isLoading) {
            _uiState.update { it.copy(chartLoadingStage = ChartLoadingStage.WEBVIEW_LOADING) }
        }
    }
    
    // 차트 브릿지 설정
    fun setChartBridge(bridge: com.lago.app.presentation.ui.chart.v5.JsBridge) {
        this.chartBridge = bridge
        this.jsBridge = bridge // jsBridge도 함께 설정
        android.util.Log.d("HistoryChallengeChart", "차트 브릿지 설정 완료")
        
        // 역사챌린지는 1분봉으로 설정
        bridge.updateTimeFrame("1")
        android.util.Log.d("HistoryChallengeChart", "TimeFrame을 1분봉으로 설정")
    }
    
    // HistoricalDataRequestListener 구현
    override fun onRequestHistoricalData(barsToLoad: Int) {
        if (isLoadingHistory) return
        
        isLoadingHistory = true
        android.util.Log.d("HistoryChallengeChart", "과거 데이터 요청: $barsToLoad bars")
        
        viewModelScope.launch {
            try {
                val earliestTime = currentEarliestTime ?: run {
                    android.util.Log.w("HistoryChallengeChart", "earliestTime이 설정되지 않음")
                    isLoadingHistory = false
                    return@launch
                }
                
                currentChallengeId?.let { challengeId ->
                    // 역사챌린지 과거 데이터 추가 로드
                    val currentConfig = _uiState.value.config
                    val interval = convertTimeFrameToInterval(currentConfig.timeFrame)
                    
                    // earliestTime을 기준으로 이전 데이터 요청
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val beforeDateTime = dateFormat.format(Date(earliestTime))
                    
                    android.util.Log.d("HistoryChallengeChart", "🔥 무한 히스토리 로드: $beforeDateTime 이전 ${barsToLoad}개 캔들")
                    
                    // 무한 히스토리 API 호출
                    val response = chartRepository.getHistoryChallengeHistoricalData(
                        challengeId = challengeId,
                        interval = interval,
                        beforeDateTime = beforeDateTime,
                        limit = barsToLoad
                    )
                    
                    response.collect { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                val historicalData = resource.data ?: emptyList()
                                
                                if (historicalData.isNotEmpty()) {
                                    android.util.Log.d("HistoryChallengeChart", "🔥 무한 히스토리 성공: ${historicalData.size}개 캔들 로드")
                                    
                                    // 기존 데이터 앞에 과거 데이터 추가 (TradingView 권장 방식)
                                    _uiState.update { state ->
                                        val combinedData = (historicalData + state.candlestickData).sortedBy { it.time }
                                        state.copy(candlestickData = combinedData)
                                    }
                                    
                                    // 가장 오래된 시간 업데이트
                                    currentEarliestTime = historicalData.minByOrNull { it.time }?.time
                                    android.util.Log.d("HistoryChallengeChart", "🔥 새로운 earliestTime: ${currentEarliestTime?.let { Date(it) }}")
                                    
                                    // TradingView 권장 방식으로 과거 데이터 추가
                                    prependHistoricalDataToChart(historicalData)
                                } else {
                                    android.util.Log.w("HistoryChallengeChart", "무한 히스토리: 더 이상 과거 데이터가 없음")
                                }
                            }
                            is Resource.Error -> {
                                android.util.Log.e("HistoryChallengeChart", "🚨 무한 히스토리 로드 실패: ${resource.message}")
                            }
                            is Resource.Loading -> {
                                android.util.Log.d("HistoryChallengeChart", "무한 히스토리 로딩 중...")
                            }
                        }
                    }
                } ?: android.util.Log.w("HistoryChallengeChart", "currentChallengeId가 설정되지 않음")
                
            } catch (e: Exception) {
                android.util.Log.e("HistoryChallengeChart", "과거 데이터 로드 실패", e)
            } finally {
                isLoadingHistory = false
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        historyChallengeWebSocket?.cancel()
        chartLoadingTimeoutJob?.cancel()
    }
}