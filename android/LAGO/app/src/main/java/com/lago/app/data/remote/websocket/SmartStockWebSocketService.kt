package com.lago.app.data.remote.websocket

import android.util.Log
import com.lago.app.data.cache.RealTimeStockCache
import com.lago.app.data.local.prefs.UserPreferences
import com.lago.app.data.remote.dto.WebSocketConnectionState
import com.lago.app.domain.entity.StockRealTimeData
import com.lago.app.domain.entity.StockPriority
import com.lago.app.domain.entity.ScreenType
import com.lago.app.util.Constants
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartStockWebSocketService @Inject constructor(
    private val userPreferences: UserPreferences,
    private val realTimeCache: RealTimeStockCache,
    private val smartUpdateScheduler: com.lago.app.data.scheduler.SmartUpdateScheduler,
    private val gson: Gson
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var stompClient: StompClient? = null
    private val compositeDisposable = CompositeDisposable()
    private var isConnected = false
    
    // 현재 활성 구독들
    private val activeSubscriptions = mutableMapOf<String, Disposable>()
    
    // 화면별 필요 종목 추적
    private val stockListVisibleStocks = mutableSetOf<String>()
    private val portfolioStocks = mutableSetOf<String>()
    private val watchListStocks = mutableSetOf<String>()
    private var currentChartStock: String? = null
    
    // 연결 상태
    private val _connectionState = MutableStateFlow(WebSocketConnectionState.DISCONNECTED)
    val connectionState: StateFlow<WebSocketConnectionState> = _connectionState.asStateFlow()
    
    companion object {
        private const val TAG = "SmartStockWebSocket"
        private val WS_URL = Constants.WS_STOCK_URL
        private const val RECONNECT_DELAY = 3000L
        private const val MAX_RECONNECT_ATTEMPTS = 5
        // 서버 지원에 따라 변경 가능한 토픽들
        private const val ALL_STOCKS_TOPIC = "/topic/stocks/all" // 전체 종목 구독 토픽
        // 대안 1: private const val ALL_STOCKS_TOPIC = "/topic/stocks/*"  // 와일드카드
        // 대안 2: private const val ALL_STOCKS_TOPIC = "/topic/broadcast"  // 브로드캐스트
        // 대안 3: private const val ALL_STOCKS_TOPIC = "/topic/realtime"  // 실시간 전체
    }
    
    fun connect() {
        if (isConnected) {
            Log.w(TAG, "🔌 이미 연결되어 있음")
            return
        }
        
        scope.launch {
            try {
                _connectionState.value = WebSocketConnectionState.CONNECTING
                Log.w(TAG, "🔌 WebSocket 연결 시작: $WS_URL")
                
                stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, WS_URL).apply {
                    val lifecycleDisposable = lifecycle()
                        .subscribe { lifecycleEvent ->
                            when (lifecycleEvent.type) {
                                ua.naiksoftware.stomp.dto.LifecycleEvent.Type.OPENED -> {
                                    Log.w(TAG, "✅ WebSocket 연결 성공!")
                                    this@SmartStockWebSocketService.isConnected = true
                                    _connectionState.value = WebSocketConnectionState.CONNECTED
                                    
                                    // 기본 구독 시작
                                    scope.launch {
                                        delay(1000) // 연결 안정화
                                        Log.w(TAG, "📡 1초 후 구독 시작...")
                                        initializeSubscriptions()
                                    }
                                }
                                ua.naiksoftware.stomp.dto.LifecycleEvent.Type.CLOSED -> {
                                    Log.d(TAG, "Smart WebSocket connection closed")
                                    this@SmartStockWebSocketService.isConnected = false
                                    _connectionState.value = WebSocketConnectionState.DISCONNECTED
                                }
                                ua.naiksoftware.stomp.dto.LifecycleEvent.Type.ERROR -> {
                                    Log.e(TAG, "Smart WebSocket connection error", lifecycleEvent.exception)
                                    _connectionState.value = WebSocketConnectionState.ERROR
                                    scheduleReconnection()
                                }
                                ua.naiksoftware.stomp.dto.LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> {
                                    Log.w(TAG, "Failed server heartbeat")
                                    _connectionState.value = WebSocketConnectionState.ERROR
                                }
                            }
                        }
                    
                    compositeDisposable.add(lifecycleDisposable)
                    connect()
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "WebSocket connection failed", e)
                _connectionState.value = WebSocketConnectionState.ERROR
                scheduleReconnection()
            }
        }
    }
    
    private suspend fun initializeSubscriptions() {
        try {
            Log.d(TAG, "초기 구독 시작")
            
            // 기본적으로 모든 종목 구독 (구독 해제 안함)
            val defaultStocks = listOf(
                "005930", "000660", "035420", "035720", "207940", "373220",
                "051910", "006400", "068270", "003550", "105560", "055550",
                "034730", "000270", "066570", "028260", "012330", "096770",
                "017670", "316140", "018260", "005380", "011200", "259960",
                "032830", "005490", "028050", "000100", "000720", "005850"
            )
            
            Log.d(TAG, "기본 ${defaultStocks.size}개 종목 구독 시작")
            subscribeToStocks(defaultStocks)
            
        } catch (e: Exception) {
            Log.e(TAG, "초기 구독 실패", e)
        }
    }
    
    private fun subscribeToStocks(stockCodes: List<String>) {
        val client = stompClient ?: return
        
        stockCodes.forEach { stockCode ->
            if (!activeSubscriptions.containsKey(stockCode)) {
                try {
                    val subscription = subscribeToStock(client, stockCode)
                    activeSubscriptions[stockCode] = subscription
                    Log.d(TAG, "구독 성공: $stockCode")
                } catch (e: Exception) {
                    Log.e(TAG, "구독 실패: $stockCode", e)
                }
            }
        }
        
        Log.d(TAG, "총 활성 구독: ${activeSubscriptions.size}개")
    }
    
    private fun subscribeToStock(client: StompClient, stockCode: String): Disposable {
        return client.topic("/topic/stocks/$stockCode")
            .subscribe({ stompMessage ->
                try {
                    Log.d(TAG, "메시지 수신 - $stockCode: ${stompMessage.payload}")
                    val stockData = gson.fromJson(stompMessage.payload, StockRealTimeData::class.java)
                    
                    // stockCode가 비어있으면 토픽에서 추출
                    val finalStockData = if (stockData.stockCode.isBlank()) {
                        stockData.copy(stockCode = stockCode)
                    } else {
                        stockData
                    }
                    
                    processStockUpdate(finalStockData)
                } catch (e: Exception) {
                    Log.e(TAG, "데이터 파싱 오류 - $stockCode", e)
                }
            }, { error ->
                Log.e(TAG, "구독 오류 - $stockCode", error)
            })
    }
    
    // === 화면별 구독 관리 ===
    
    fun updateChartStock(stockCode: String?) {
        val oldStock = currentChartStock
        currentChartStock = stockCode
        
        // 우선순위 변경 (전체 구독이므로 개별 구독 불필요)
        oldStock?.let { realTimeCache.setStockPriority(it, StockPriority.WARM) }
        stockCode?.let { realTimeCache.setStockPriority(it, StockPriority.HOT) }
        
        Log.d(TAG, "차트 종목 변경: $stockCode")
        
        // 차트 종목이 구독되어 있지 않으면 추가 구독
        if (stockCode != null && !activeSubscriptions.containsKey(stockCode)) {
            stompClient?.let { client ->
                try {
                    val subscription = subscribeToStock(client, stockCode)
                    activeSubscriptions[stockCode] = subscription
                    Log.d(TAG, "차트 종목 추가 구독: $stockCode")
                } catch (e: Exception) {
                    Log.e(TAG, "차트 종목 구독 실패: $stockCode", e)
                }
            }
        }
    }
    
    fun updateVisibleStocks(visibleStocks: List<String>) {
        stockListVisibleStocks.clear()
        stockListVisibleStocks.addAll(visibleStocks)
        
        // 보이는 종목들을 WARM 우선순위로 설정
        val priorities = visibleStocks.associateWith { StockPriority.WARM }
        realTimeCache.setMultipleStockPriorities(priorities)
        
        Log.d(TAG, "가시 영역 업데이트: ${visibleStocks.size}개 종목")
        
        // 보이는 종목들 중 구독되지 않은 것들 추가 구독
        val unsubscribedStocks = visibleStocks.filter { !activeSubscriptions.containsKey(it) }
        if (unsubscribedStocks.isNotEmpty()) {
            Log.d(TAG, "추가 구독 필요: ${unsubscribedStocks.size}개")
            subscribeToStocks(unsubscribedStocks)
        }
    }
    
    fun updatePortfolioStocks(portfolioStocks: List<String>) {
        this.portfolioStocks.clear()
        this.portfolioStocks.addAll(portfolioStocks)
        
        // 포트폴리오 종목들을 WARM 우선순위로 설정
        val priorities = portfolioStocks.associateWith { StockPriority.WARM }
        realTimeCache.setMultipleStockPriorities(priorities)
        
        Log.d(TAG, "포트폴리오 업데이트: ${portfolioStocks.size}개 종목 (전체 구독 모드)")
    }
    
    fun updateWatchListStocks(watchListStocks: List<String>) {
        this.watchListStocks.clear()
        this.watchListStocks.addAll(watchListStocks)
        
        // 관심종목들을 WARM 우선순위로 설정
        val priorities = watchListStocks.associateWith { StockPriority.WARM }
        realTimeCache.setMultipleStockPriorities(priorities)
        
        Log.d(TAG, "관심종목 업데이트: ${watchListStocks.size}개 종목 (전체 구독 모드)")
    }
    
    // === 스마트 구독 (구독만 하고 해제 안함) ===
    
    private fun refreshSubscriptions() {
        // 구독 해제 없이 필요한 종목만 추가 구독
        Log.d(TAG, "구독 유지 모드 - 해제 없이 추가만")
    }
    
    private fun processStockUpdate(stockData: StockRealTimeData) {
        Log.w(TAG, "🔥 processStockUpdate 시작 - ${stockData.stockCode}")
        Log.w(TAG, "🔥 가격 정보: price=${stockData.price}, closePrice=${stockData.closePrice}, currentPrice=${stockData.currentPrice}, tradePrice=${stockData.tradePrice}")
        Log.w(TAG, "🔥 변동 정보: priceChange=${stockData.priceChange}, priceChangePercent=${stockData.priceChangePercent}")
        Log.w(TAG, "🔥 웹소켓 등락률: fluctuationRate=${stockData.fluctuationRate}, changeRate=${stockData.changeRate}, rate=${stockData.rate}")
        
        // 캐시에 저장 (우선순위별 자동 분류)
        realTimeCache.updateStock(stockData.stockCode, stockData)
        Log.d(TAG, "캐시 저장 완료 - ${stockData.stockCode}, 저장된 가격: ${stockData.price}")
        
        // SmartUpdateScheduler로 업데이트 전달
        Log.d(TAG, "STOCK_LIST 업데이트 스케줄링 - ${stockData.stockCode}")
        smartUpdateScheduler.scheduleUpdate(
            com.lago.app.domain.entity.ScreenType.STOCK_LIST, 
            stockData.stockCode, 
            stockData
        )
        
        // 차트 화면용 업데이트도 전달
        if (stockData.stockCode == currentChartStock) {
            Log.d(TAG, "CHART 업데이트 스케줄링 - ${stockData.stockCode} (currentChartStock)")
            smartUpdateScheduler.scheduleUpdate(
                com.lago.app.domain.entity.ScreenType.CHART, 
                stockData.stockCode, 
                stockData
            )
        } else {
            Log.d(TAG, "차트 업데이트 스킵 - ${stockData.stockCode} (currentChartStock: $currentChartStock)")
        }
        
        Log.v(TAG, "실시간 데이터 수신: ${stockData.stockCode} = ${stockData.price}")
    }
    
    // === 사용자 데이터 가져오기 ===
    
    private suspend fun getUserPortfolioStocks(): List<String> {
        // TODO: 실제 포트폴리오 API에서 가져오기
        return emptyList()
    }
    
    private suspend fun getUserWatchListStocks(): List<String> {
        // TODO: 실제 관심종목 API에서 가져오기
        return emptyList()
    }
    
    // === 연결 관리 ===
    
    private fun scheduleReconnection() {
        scope.launch {
            delay(RECONNECT_DELAY)
            if (!isConnected) {
                Log.d(TAG, "재연결 시도...")
                connect()
            }
        }
    }
    
    fun disconnect() {
        scope.launch {
            try {
                activeSubscriptions.values.forEach { it.dispose() }
                activeSubscriptions.clear()
                compositeDisposable.clear()
                stompClient?.disconnect()
                stompClient = null
                isConnected = false
                _connectionState.value = WebSocketConnectionState.DISCONNECTED
                Log.d(TAG, "WebSocket 연결 해제")
            } catch (e: Exception) {
                Log.e(TAG, "연결 해제 중 오류", e)
            }
        }
    }
    
    // === 디버깅 ===
    
    fun getSubscriptionStats(): String {
        return """
            활성 구독: ${activeSubscriptions.size}개
            연결 상태: ${if (isConnected) "연결됨" else "연결 안됨"}
            차트 종목: $currentChartStock
            포트폴리오: ${portfolioStocks.size}개
            관심종목: ${watchListStocks.size}개  
            보이는 종목: ${stockListVisibleStocks.size}개
            ${realTimeCache.getCacheStats()}
        """.trimIndent()
    }
    
    fun cleanup() {
        disconnect()
        scope.cancel()
    }
}