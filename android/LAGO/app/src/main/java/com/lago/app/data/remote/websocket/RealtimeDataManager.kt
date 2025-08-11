package com.lago.app.data.remote.websocket

import android.util.Log
import com.lago.app.data.remote.dto.*
import com.lago.app.data.local.cache.ChartCacheManager
import com.lago.app.domain.entity.CandlestickData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealtimeDataManager @Inject constructor(
    private val webSocketClient: WebSocketClient,
    private val cacheManager: ChartCacheManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // 연결 상태
    private val _connectionState = MutableStateFlow(WebSocketConnectionState.DISCONNECTED)
    val connectionState: StateFlow<WebSocketConnectionState> = _connectionState.asStateFlow()
    
    // 실시간 캔들스틱 데이터
    private val _realtimeCandlestick = MutableSharedFlow<RealtimeCandlestickDto>()
    val realtimeCandlestick: SharedFlow<RealtimeCandlestickDto> = _realtimeCandlestick.asSharedFlow()
    
    // 실시간 틱 데이터
    private val _realtimeTick = MutableSharedFlow<RealtimeTickDto>()
    val realtimeTick: SharedFlow<RealtimeTickDto> = _realtimeTick.asSharedFlow()
    
    // 에러 상태
    private val _errors = MutableSharedFlow<Throwable>()
    val errors: SharedFlow<Throwable> = _errors.asSharedFlow()
    
    // 현재 구독 중인 심볼과 타임프레임
    private var currentSubscription: Pair<String, String>? = null
    
    init {
        // 웹소켓 이벤트 처리
        scope.launch {
            webSocketClient.events.collect { event ->
                handleWebSocketEvent(event)
            }
        }
    }
    
    /**
     * 웹소켓 연결 시작
     */
    fun connect() {
        Log.d(TAG, "Starting WebSocket connection")
        webSocketClient.connect()
    }
    
    /**
     * 웹소켓 연결 종료
     */
    fun disconnect() {
        Log.d(TAG, "Stopping WebSocket connection")
        currentSubscription = null
        webSocketClient.disconnect()
    }
    
    /**
     * 특정 주식의 실시간 차트 데이터 구독
     * 이전 구독은 자동으로 해제됨
     */
    fun subscribeToChart(symbol: String, timeframe: String) {
        scope.launch {
            // 이전 구독 해제
            currentSubscription?.let { (prevSymbol, prevTimeframe) ->
                if (prevSymbol != symbol || prevTimeframe != timeframe) {
                    webSocketClient.unsubscribe(prevSymbol, prevTimeframe)
                }
            }
            
            // 새 구독 시작
            currentSubscription = Pair(symbol, timeframe)
            webSocketClient.subscribeCandlestick(symbol, timeframe)
            webSocketClient.subscribeTick(symbol) // 실시간 가격도 함께 구독
            
            Log.d(TAG, "Subscribed to chart data: $symbol - $timeframe")
        }
    }
    
    /**
     * 현재 구독 해제
     */
    fun unsubscribeFromChart() {
        currentSubscription?.let { (symbol, timeframe) ->
            webSocketClient.unsubscribe(symbol, timeframe)
            currentSubscription = null
            Log.d(TAG, "Unsubscribed from chart data")
        }
    }
    
    /**
     * 특정 심볼의 실시간 데이터만 필터링해서 반환
     */
    fun getRealtimeDataForSymbol(symbol: String): Flow<RealtimeCandlestickDto> {
        return realtimeCandlestick.filter { it.symbol == symbol }
    }
    
    /**
     * 실시간 캔들스틱 데이터를 도메인 엔티티로 변환
     */
    fun getRealtimeCandlestickAsEntity(symbol: String): Flow<CandlestickData> {
        return getRealtimeDataForSymbol(symbol).map { dto ->
            CandlestickData(
                time = dto.timestamp,
                open = dto.open,
                high = dto.high,
                low = dto.low,
                close = dto.close,
                volume = dto.volume
            )
        }
    }
    
    /**
     * 연결 상태를 체크하고 필요시 재연결
     */
    fun ensureConnection() {
        if (_connectionState.value == WebSocketConnectionState.DISCONNECTED) {
            connect()
        }
    }
    
    private suspend fun handleWebSocketEvent(event: WebSocketEvent) {
        when (event) {
            is WebSocketEvent.ConnectionStateChanged -> {
                _connectionState.emit(event.state)
                Log.d(TAG, "Connection state changed: ${event.state}")
            }
            
            is WebSocketEvent.CandlestickReceived -> {
                _realtimeCandlestick.emit(event.data)
                Log.d(TAG, "Candlestick received: ${event.data.symbol} - ${event.data.close}")
                
                // 실시간 데이터 수신 시 관련 캐시 무효화
                scope.launch {
                    cacheManager.invalidateStockCache(event.data.symbol, event.data.timeframe)
                }
            }
            
            is WebSocketEvent.TickReceived -> {
                _realtimeTick.emit(event.data)
                Log.d(TAG, "Tick received: ${event.data.symbol} - ${event.data.price}")
            }
            
            is WebSocketEvent.MessageReceived -> {
                Log.d(TAG, "Message received: ${event.message.type}")
            }
            
            is WebSocketEvent.Error -> {
                _errors.emit(event.throwable)
                Log.e(TAG, "WebSocket error", event.throwable)
            }
        }
    }
    
    /**
     * 리소스 정리
     */
    fun cleanup() {
        scope.cancel()
        disconnect()
    }
    
    companion object {
        private const val TAG = "RealtimeDataManager"
    }
}