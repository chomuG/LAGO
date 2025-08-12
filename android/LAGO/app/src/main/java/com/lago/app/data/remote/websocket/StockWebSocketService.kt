package com.lago.app.data.remote.websocket

import android.util.Log
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.StompMessage
import com.google.gson.Gson
import com.lago.app.data.remote.dto.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockWebSocketService @Inject constructor(
    private val gson: Gson
) {
    
    companion object {
        private const val TAG = "StockWebSocketService"
        private const val WS_URL = "ws://192.168.100.162:8081/ws-stock/websocket"
        
        // STOMP 구독 경로
        private const val TOPIC_STOCK = "/topic/stocks/%s"          // /topic/stocks/{stockCode}
        private const val TOPIC_ALL = "/topic/stocks/all"           // /topic/stocks/all (전체 방송)
        private const val TOPIC_CHART = "/topic/chart/%s/%s"        // /topic/chart/{stockCode}/{timeFrame}
        
        // 재연결 설정
        private const val RECONNECT_INTERVAL = 5000L // 5초
        private const val MAX_RECONNECT_ATTEMPTS = 10
    }
    
    private var stompClient: StompClient? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val compositeDisposable = CompositeDisposable()
    
    // 연결 상태 관리
    private val _connectionState = MutableStateFlow(WebSocketConnectionState.DISCONNECTED)
    val connectionState: StateFlow<WebSocketConnectionState> = _connectionState.asStateFlow()
    
    // 실시간 데이터 스트림
    private val _realtimeCandlestick = MutableSharedFlow<RealtimeCandlestickDto>()
    val realtimeCandlestick: SharedFlow<RealtimeCandlestickDto> = _realtimeCandlestick.asSharedFlow()
    
    private val _realtimeTick = MutableSharedFlow<RealtimeTickDto>()
    val realtimeTick: SharedFlow<RealtimeTickDto> = _realtimeTick.asSharedFlow()
    
    private val _errors = MutableSharedFlow<Throwable>()
    val errors: SharedFlow<Throwable> = _errors.asSharedFlow()
    
    // 현재 구독 정보
    private val currentSubscriptions = mutableSetOf<String>()
    private var reconnectAttempts = 0
    
    // Debounce를 위한 버퍼
    private val dataBuffer = mutableListOf<RealtimeCandlestickDto>()
    private var debounceJob: Job? = null
    
    /**
     * WebSocket 연결 시작
     */
    fun connect() {
        if (_connectionState.value == WebSocketConnectionState.CONNECTED ||
            _connectionState.value == WebSocketConnectionState.CONNECTING) {
            Log.d(TAG, "Already connected or connecting")
            return
        }
        
        Log.d(TAG, "Connecting to WebSocket: $WS_URL")
        Log.d(TAG, "Available STOMP topics: ${TOPIC_STOCK}, ${TOPIC_ALL}, ${TOPIC_CHART}")
        _connectionState.value = WebSocketConnectionState.CONNECTING
        
        try {
            // SockJS를 사용한 STOMP 클라이언트 생성
            stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, WS_URL).apply {
                // 연결 이벤트 처리
                val lifecycleDisposable = lifecycle()
                    .subscribe { lifecycleEvent ->
                        when (lifecycleEvent.type) {
                            ua.naiksoftware.stomp.dto.LifecycleEvent.Type.OPENED -> {
                                Log.d(TAG, "SockJS WebSocket connection opened successfully")
                                Log.d(TAG, "Server endpoint: $WS_URL")
                                _connectionState.value = WebSocketConnectionState.CONNECTED
                                reconnectAttempts = 0
                                
                                // 연결 테스트를 위한 전체 구독 시도
                                Log.d(TAG, "Testing connection with /topic/stocks/all subscription")
                                subscribeToAllStocks()
                                
                                // 기존 구독 복원
                                restoreSubscriptions()
                            }
                            ua.naiksoftware.stomp.dto.LifecycleEvent.Type.CLOSED -> {
                                Log.d(TAG, "WebSocket connection closed")
                                _connectionState.value = WebSocketConnectionState.DISCONNECTED
                                
                                // 자동 재연결 시도
                                if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                                    scheduleReconnect()
                                }
                            }
                            ua.naiksoftware.stomp.dto.LifecycleEvent.Type.ERROR -> {
                                Log.e(TAG, "WebSocket connection error: ${lifecycleEvent.exception}")
                                _connectionState.value = WebSocketConnectionState.ERROR
                                scope.launch {
                                    _errors.emit(lifecycleEvent.exception)
                                }
                                
                                if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                                    scheduleReconnect()
                                }
                            }
                            ua.naiksoftware.stomp.dto.LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> {
                                Log.w(TAG, "Failed server heartbeat")
                                _connectionState.value = WebSocketConnectionState.ERROR
                                
                                if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                                    scheduleReconnect()
                                }
                            }
                        }
                    }
                
                compositeDisposable.add(lifecycleDisposable)
                
                // 연결 시작
                connect()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating STOMP client", e)
            _connectionState.value = WebSocketConnectionState.ERROR
            scope.launch {
                _errors.emit(e)
            }
        }
    }
    
    /**
     * WebSocket 연결 해제
     */
    fun disconnect() {
        Log.d(TAG, "Disconnecting WebSocket")
        currentSubscriptions.clear()
        stompClient?.disconnect()
        stompClient = null
        _connectionState.value = WebSocketConnectionState.DISCONNECTED
    }
    
    /**
     * 특정 종목의 실시간 차트 데이터 구독
     */
    fun subscribeToCandlestickData(stockCode: String, timeFrame: String) {
        val client = stompClient ?: return
        val topic = TOPIC_CHART.format(stockCode, timeFrame)
        
        if (currentSubscriptions.contains(topic)) {
            Log.d(TAG, "Already subscribed to $topic")
            return
        }
        
        try {
            val subscription = client.topic(topic)
                .subscribe({ stompMessage ->
                    handleCandlestickMessage(stompMessage)
                }, { error ->
                    Log.e(TAG, "Error subscribing to candlestick data: $topic", error)
                    scope.launch {
                        _errors.emit(error)
                    }
                })
            
            compositeDisposable.add(subscription)
            currentSubscriptions.add(topic)
            Log.d(TAG, "Subscribed to candlestick data: $topic")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error subscribing to candlestick data", e)
            scope.launch {
                _errors.emit(e)
            }
        }
    }
    
    /**
     * 특정 종목의 실시간 틱 데이터 구독
     */
    fun subscribeToTickData(stockCode: String) {
        val client = stompClient ?: return
        val topic = TOPIC_STOCK.format(stockCode)
        
        if (currentSubscriptions.contains(topic)) {
            Log.d(TAG, "Already subscribed to $topic")
            return
        }
        
        try {
            val subscription = client.topic(topic)
                .subscribe({ stompMessage ->
                    handleTickMessage(stompMessage)
                }, { error ->
                    Log.e(TAG, "Error subscribing to tick data: $topic", error)
                    scope.launch {
                        _errors.emit(error)
                    }
                })
            
            compositeDisposable.add(subscription)
            currentSubscriptions.add(topic)
            Log.d(TAG, "Subscribed to tick data: $topic")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error subscribing to tick data", e)
            scope.launch {
                _errors.emit(e)
            }
        }
    }
    
    /**
     * 전체 종목 방송 구독
     */
    fun subscribeToAllStocks() {
        val client = stompClient ?: return
        val topic = TOPIC_ALL
        
        if (currentSubscriptions.contains(topic)) {
            Log.d(TAG, "Already subscribed to $topic")
            return
        }
        
        try {
            val subscription = client.topic(topic)
                .subscribe({ stompMessage ->
                    handleTickMessage(stompMessage)
                }, { error ->
                    Log.e(TAG, "Error subscribing to all stocks: $topic", error)
                    scope.launch {
                        _errors.emit(error)
                    }
                })
            
            compositeDisposable.add(subscription)
            currentSubscriptions.add(topic)
            Log.d(TAG, "Subscribed to all stocks: $topic")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error subscribing to all stocks", e)
            scope.launch {
                _errors.emit(e)
            }
        }
    }
    
    /**
     * 구독 해제
     */
    fun unsubscribe(topic: String) {
        currentSubscriptions.remove(topic)
        Log.d(TAG, "Unsubscribed from $topic")
    }
    
    /**
     * 모든 구독 해제
     */
    fun unsubscribeAll() {
        currentSubscriptions.clear()
        Log.d(TAG, "Unsubscribed from all topics")
    }
    
    // ===== PRIVATE METHODS =====
    
    private fun handleCandlestickMessage(stompMessage: StompMessage) {
        try {
            Log.d(TAG, "Received candlestick message: ${stompMessage.payload}")
            val candlestickData = gson.fromJson(stompMessage.payload, RealtimeCandlestickDto::class.java)
            
            // Debounce 처리 - 100ms 간격으로 배치 처리
            synchronized(dataBuffer) {
                dataBuffer.add(candlestickData)
            }
            
            debounceJob?.cancel()
            debounceJob = scope.launch {
                delay(100) // 100ms 대기
                
                val bufferedData = synchronized(dataBuffer) {
                    dataBuffer.toList().also { dataBuffer.clear() }
                }
                
                // 가장 최신 데이터만 전송 (같은 종목+타임프레임)
                val latestData = bufferedData
                    .groupBy { "${it.symbol}_${it.timeframe}" }
                    .mapValues { it.value.maxByOrNull { data -> data.timestamp } }
                    .values
                    .filterNotNull()
                
                latestData.forEach { data ->
                    _realtimeCandlestick.emit(data)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing candlestick message", e)
            scope.launch {
                _errors.emit(e)
            }
        }
    }
    
    private fun handleTickMessage(stompMessage: StompMessage) {
        try {
            Log.d(TAG, "Received tick message: ${stompMessage.payload}")
            val tickData = gson.fromJson(stompMessage.payload, RealtimeTickDto::class.java)
            
            scope.launch {
                _realtimeTick.emit(tickData)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing tick message", e)
            scope.launch {
                _errors.emit(e)
            }
        }
    }
    
    private fun scheduleReconnect() {
        reconnectAttempts++
        val delay = RECONNECT_INTERVAL * reconnectAttempts // 점진적 지연
        
        Log.d(TAG, "Scheduling reconnect in ${delay}ms (attempt $reconnectAttempts)")
        _connectionState.value = WebSocketConnectionState.RECONNECTING
        
        scope.launch {
            delay(delay)
            if (_connectionState.value == WebSocketConnectionState.RECONNECTING) {
                connect()
            }
        }
    }
    
    private fun restoreSubscriptions() {
        Log.d(TAG, "Restoring ${currentSubscriptions.size} subscriptions")
        val subscriptionsToRestore = currentSubscriptions.toList()
        currentSubscriptions.clear()
        
        subscriptionsToRestore.forEach { topic ->
            when {
                topic.contains("/topic/chart/") -> {
                    // 차트 데이터 구독 복원
                    val parts = topic.split("/")
                    if (parts.size >= 5) {
                        val stockCode = parts[3]
                        val timeFrame = parts[4]
                        subscribeToCandlestickData(stockCode, timeFrame)
                    }
                }
                topic == TOPIC_ALL -> {
                    // 전체 방송 구독 복원
                    subscribeToAllStocks()
                }
                topic.contains("/topic/stocks/") -> {
                    // 개별 종목 틱 데이터 구독 복원
                    val parts = topic.split("/")
                    if (parts.size >= 4) {
                        val stockCode = parts[3]
                        subscribeToTickData(stockCode)
                    }
                }
            }
        }
    }
    
    /**
     * 리소스 정리
     */
    fun cleanup() {
        debounceJob?.cancel()
        compositeDisposable.clear()
        scope.cancel()
        disconnect()
    }
}