package com.lago.app.data.remote.websocket

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.lago.app.data.remote.dto.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import okhttp3.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketClient @Inject constructor() {
    
    private val gson = Gson()
    private var webSocket: WebSocket? = null
    private val eventChannel = Channel<WebSocketEvent>(Channel.UNLIMITED)
    
    // 웹소켓 클라이언트 설정
    private val client = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .pingInterval(20, TimeUnit.SECONDS) // Keep-alive ping
        .retryOnConnectionFailure(true)
        .build()
    
    val events: Flow<WebSocketEvent> = eventChannel.receiveAsFlow()
    
    private var currentConnectionState = WebSocketConnectionState.DISCONNECTED
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5
    private var baseReconnectDelay = 1000L // 1초
    
    // 현재 구독 중인 채널들 (재연결 시 복구용)
    private val subscriptions = mutableSetOf<Pair<String, String>>() // (symbol, timeframe)
    
    fun connect(wsUrl: String = "wss://i13d203.p.ssafy.io:8081/ws/chart") {
        if (currentConnectionState == WebSocketConnectionState.CONNECTED ||
            currentConnectionState == WebSocketConnectionState.CONNECTING) {
            Log.d(TAG, "Already connected or connecting")
            return
        }
        
        updateConnectionState(WebSocketConnectionState.CONNECTING)
        
        val request = Request.Builder()
            .url(wsUrl)
            .build()
        
        webSocket = client.newWebSocket(request, webSocketListener)
    }
    
    fun disconnect() {
        Log.d(TAG, "Disconnecting WebSocket")
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
        subscriptions.clear()
        updateConnectionState(WebSocketConnectionState.DISCONNECTED)
    }
    
    /**
     * 차트 데이터 구독
     * @param symbol 주식 코드 (e.g., "005930")
     * @param timeframe 시간프레임 (e.g., "D", "1", "5", "15")
     */
    fun subscribeCandlestick(symbol: String, timeframe: String) {
        val subscribeMsg = SubscribeMessage(
            action = "subscribe",
            channel = "candlestick",
            symbol = symbol,
            timeframe = timeframe
        )
        
        sendMessage(subscribeMsg)
        subscriptions.add(Pair(symbol, timeframe))
        Log.d(TAG, "Subscribed to candlestick: $symbol - $timeframe")
    }
    
    /**
     * 실시간 틱 데이터 구독
     */
    fun subscribeTick(symbol: String) {
        val subscribeMsg = SubscribeMessage(
            action = "subscribe",
            channel = "tick",
            symbol = symbol
        )
        
        sendMessage(subscribeMsg)
        Log.d(TAG, "Subscribed to tick: $symbol")
    }
    
    /**
     * 구독 해제
     */
    fun unsubscribe(symbol: String, timeframe: String) {
        val unsubscribeMsg = SubscribeMessage(
            action = "unsubscribe",
            channel = "candlestick",
            symbol = symbol,
            timeframe = timeframe
        )
        
        sendMessage(unsubscribeMsg)
        subscriptions.remove(Pair(symbol, timeframe))
        Log.d(TAG, "Unsubscribed from: $symbol - $timeframe")
    }
    
    private fun sendMessage(message: Any) {
        try {
            val json = gson.toJson(message)
            val success = webSocket?.send(json) ?: false
            if (!success) {
                Log.w(TAG, "Failed to send message: $json")
            } else {
                Log.d(TAG, "Sent message: $json")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message", e)
            eventChannel.trySend(WebSocketEvent.Error(e))
        }
    }
    
    private fun updateConnectionState(state: WebSocketConnectionState) {
        if (currentConnectionState != state) {
            currentConnectionState = state
            eventChannel.trySend(WebSocketEvent.ConnectionStateChanged(state))
            Log.d(TAG, "Connection state changed to: $state")
        }
    }
    
    private fun handleReconnect() {
        if (reconnectAttempts >= maxReconnectAttempts) {
            Log.w(TAG, "Max reconnect attempts reached")
            updateConnectionState(WebSocketConnectionState.ERROR)
            return
        }
        
        reconnectAttempts++
        val delay = baseReconnectDelay * (1L shl (reconnectAttempts - 1)) // Exponential backoff
        
        Log.d(TAG, "Reconnecting in ${delay}ms (attempt $reconnectAttempts)")
        updateConnectionState(WebSocketConnectionState.RECONNECTING)
        
        // Delayed reconnection (should be done with coroutines in real implementation)
        Thread.sleep(delay)
        connect()
    }
    
    private fun restoreSubscriptions() {
        Log.d(TAG, "Restoring ${subscriptions.size} subscriptions")
        subscriptions.forEach { (symbol, timeframe) ->
            subscribeCandlestick(symbol, timeframe)
        }
    }
    
    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "WebSocket opened")
            reconnectAttempts = 0
            updateConnectionState(WebSocketConnectionState.CONNECTED)
            
            // 재연결 시 기존 구독 복구
            if (subscriptions.isNotEmpty()) {
                restoreSubscriptions()
            }
        }
        
        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d(TAG, "Received message: $text")
            
            try {
                // 먼저 기본 WebSocket 응답으로 파싱 시도
                val response = gson.fromJson(text, WebSocketResponse::class.java)
                
                when (response.type) {
                    "candlestick" -> {
                        // 캔들스틱 데이터로 파싱
                        val candlestickData = gson.fromJson(
                            gson.toJson(response.data), 
                            RealtimeCandlestickDto::class.java
                        )
                        eventChannel.trySend(WebSocketEvent.CandlestickReceived(candlestickData))
                    }
                    "tick" -> {
                        // 틱 데이터로 파싱
                        val tickData = gson.fromJson(
                            gson.toJson(response.data), 
                            RealtimeTickDto::class.java
                        )
                        eventChannel.trySend(WebSocketEvent.TickReceived(tickData))
                    }
                    else -> {
                        // 일반 메시지
                        eventChannel.trySend(WebSocketEvent.MessageReceived(response))
                    }
                }
            } catch (e: JsonSyntaxException) {
                Log.e(TAG, "Failed to parse message: $text", e)
                eventChannel.trySend(WebSocketEvent.Error(e))
            }
        }
        
        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WebSocket closing: $code - $reason")
            updateConnectionState(WebSocketConnectionState.DISCONNECTED)
        }
        
        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WebSocket closed: $code - $reason")
            updateConnectionState(WebSocketConnectionState.DISCONNECTED)
        }
        
        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "WebSocket error", t)
            eventChannel.trySend(WebSocketEvent.Error(t))
            updateConnectionState(WebSocketConnectionState.ERROR)
            
            // 자동 재연결 시도
            handleReconnect()
        }
    }
    
    companion object {
        private const val TAG = "WebSocketClient"
    }
}