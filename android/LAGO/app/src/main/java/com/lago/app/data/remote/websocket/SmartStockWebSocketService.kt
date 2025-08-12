package com.lago.app.data.remote.websocket

import android.util.Log
import com.lago.app.data.cache.RealTimeStockCache
import com.lago.app.data.local.prefs.UserPreferences
import com.lago.app.data.remote.dto.WebSocketConnectionState
import com.lago.app.domain.entity.StockRealTimeData
import com.lago.app.domain.entity.StockPriority
import com.lago.app.domain.entity.ScreenType
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
        private const val WS_URL = "ws://i13d203.p.ssafy.io:8081/ws/chart"
        private const val RECONNECT_DELAY = 3000L
        private const val MAX_RECONNECT_ATTEMPTS = 5
    }
    
    fun connect() {
        scope.launch {
            try {
                _connectionState.value = WebSocketConnectionState.CONNECTING
                
                stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, WS_URL).apply {
                    val lifecycleDisposable = lifecycle()
                        .subscribe { lifecycleEvent ->
                            when (lifecycleEvent.type) {
                                ua.naiksoftware.stomp.dto.LifecycleEvent.Type.OPENED -> {
                                    Log.d(TAG, "Smart WebSocket connected successfully")
                                    this@SmartStockWebSocketService.isConnected = true
                                    _connectionState.value = WebSocketConnectionState.CONNECTED
                                    
                                    // 기본 구독 시작
                                    scope.launch {
                                        initializeBasicSubscriptions()
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
    
    private suspend fun initializeBasicSubscriptions() {
        // 포트폴리오 종목 자동 구독
        val userPortfolio = getUserPortfolioStocks()
        val userWatchList = getUserWatchListStocks()
        
        updatePortfolioStocks(userPortfolio)
        updateWatchListStocks(userWatchList)
        
        refreshSubscriptions()
    }
    
    // === 화면별 구독 관리 ===
    
    fun updateChartStock(stockCode: String?) {
        val oldStock = currentChartStock
        currentChartStock = stockCode
        
        // 우선순위 변경
        oldStock?.let { realTimeCache.setStockPriority(it, StockPriority.WARM) }
        stockCode?.let { realTimeCache.setStockPriority(it, StockPriority.HOT) }
        
        refreshSubscriptions()
    }
    
    fun updateVisibleStocks(visibleStocks: List<String>) {
        stockListVisibleStocks.clear()
        stockListVisibleStocks.addAll(visibleStocks)
        
        // 보이는 종목들을 WARM 우선순위로 설정
        val priorities = visibleStocks.associateWith { StockPriority.WARM }
        realTimeCache.setMultipleStockPriorities(priorities)
        
        refreshSubscriptions()
    }
    
    fun updatePortfolioStocks(portfolioStocks: List<String>) {
        this.portfolioStocks.clear()
        this.portfolioStocks.addAll(portfolioStocks)
        
        // 포트폴리오 종목들을 WARM 우선순위로 설정
        val priorities = portfolioStocks.associateWith { StockPriority.WARM }
        realTimeCache.setMultipleStockPriorities(priorities)
        
        refreshSubscriptions()
    }
    
    fun updateWatchListStocks(watchListStocks: List<String>) {
        this.watchListStocks.clear()
        this.watchListStocks.addAll(watchListStocks)
        
        // 관심종목들을 WARM 우선순위로 설정
        val priorities = watchListStocks.associateWith { StockPriority.WARM }
        realTimeCache.setMultipleStockPriorities(priorities)
        
        refreshSubscriptions()
    }
    
    // === 스마트 구독 로직 ===
    
    private fun refreshSubscriptions() {
        if (!isConnected) return
        
        scope.launch {
            val neededStocks = calculateNeededStocks()
            updateSubscriptions(neededStocks)
        }
    }
    
    private fun calculateNeededStocks(): Set<String> {
        return buildSet {
            // 1순위: 현재 차트 종목 (HOT)
            currentChartStock?.let { add(it) }
            
            // 2순위: 포트폴리오 (WARM)
            addAll(portfolioStocks)
            
            // 3순위: 관심종목 (WARM)
            addAll(watchListStocks)
            
            // 4순위: 현재 보이는 종목들 (WARM)
            addAll(stockListVisibleStocks)
        }
    }
    
    private fun updateSubscriptions(neededStocks: Set<String>) {
        val client = stompClient ?: return
        
        // 새로 구독할 종목들
        val newSubscriptions = neededStocks - activeSubscriptions.keys
        // 구독 해제할 종목들  
        val unneededSubscriptions = activeSubscriptions.keys - neededStocks
        
        Log.d(TAG, "구독 업데이트 - 신규: ${newSubscriptions.size}, 해제: ${unneededSubscriptions.size}")
        
        // 불필요한 구독 해제
        unneededSubscriptions.forEach { stockCode ->
            try {
                activeSubscriptions[stockCode]?.dispose()
                activeSubscriptions.remove(stockCode)
                realTimeCache.clearStockPriority(stockCode)
                Log.d(TAG, "구독 해제: $stockCode")
            } catch (e: Exception) {
                Log.w(TAG, "구독 해제 실패: $stockCode", e)
            }
        }
        
        // 새로운 구독 추가
        newSubscriptions.forEach { stockCode ->
            try {
                val subscription = subscribeToStock(client, stockCode)
                activeSubscriptions[stockCode] = subscription
                Log.d(TAG, "구독 추가: $stockCode")
            } catch (e: Exception) {
                Log.e(TAG, "구독 추가 실패: $stockCode", e)
            }
        }
        
        Log.d(TAG, "총 활성 구독: ${activeSubscriptions.size}개")
        Log.d(TAG, "캐시 현황: ${realTimeCache.getCacheStats()}")
    }
    
    private fun subscribeToStock(client: StompClient, stockCode: String): Disposable {
        return client.topic("/topic/stock/$stockCode")
            .subscribe({ stompMessage ->
                try {
                    val stockData = gson.fromJson(stompMessage.payload, StockRealTimeData::class.java)
                    processStockUpdate(stockData)
                } catch (e: Exception) {
                    Log.e(TAG, "데이터 파싱 오류 - $stockCode", e)
                }
            }, { error ->
                Log.e(TAG, "구독 오류 - $stockCode", error)
            })
    }
    
    private fun processStockUpdate(stockData: StockRealTimeData) {
        // 캐시에 저장 (우선순위별 자동 분류)
        realTimeCache.updateStock(stockData.stockCode, stockData)
        
        Log.v(TAG, "실시간 데이터 수신: ${stockData.stockCode} = ${stockData.currentPrice}")
    }
    
    // === 사용자 데이터 가져오기 ===
    
    private suspend fun getUserPortfolioStocks(): List<String> {
        // TODO: 실제 포트폴리오 API에서 가져오기
        return listOf("005930", "000660", "035420") // 임시 데이터
    }
    
    private suspend fun getUserWatchListStocks(): List<String> {
        // TODO: 실제 관심종목 API에서 가져오기
        return listOf("035720", "373220", "207940") // 임시 데이터
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
    
    fun getActiveSubscriptions(): Set<String> = activeSubscriptions.keys.toSet()
    
    fun getSubscriptionStats(): String {
        return """
            활성 구독: ${activeSubscriptions.size}개
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