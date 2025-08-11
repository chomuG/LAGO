package com.lago.app.data.scheduler

import android.util.Log
import com.lago.app.domain.entity.StockRealTimeData
import com.lago.app.domain.entity.ScreenType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartUpdateScheduler @Inject constructor() {
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // 화면별 업데이트 큐
    private val updateQueues = mapOf(
        ScreenType.CHART to UpdateQueue(16.6f, "차트"),        // 60fps
        ScreenType.STOCK_LIST to UpdateQueue(500f, "종목리스트"),  // 2fps  
        ScreenType.PORTFOLIO to UpdateQueue(1000f, "포트폴리오"),  // 1fps
        ScreenType.SUMMARY to UpdateQueue(5000f, "요약"),       // 0.2fps
        ScreenType.NEWS to UpdateQueue(10000f, "뉴스")         // 0.1fps
    )
    
    // 화면별 업데이트 스트림
    private val _chartUpdates = MutableSharedFlow<Map<String, StockRealTimeData>>()
    val chartUpdates: SharedFlow<Map<String, StockRealTimeData>> = _chartUpdates.asSharedFlow()
    
    private val _stockListUpdates = MutableSharedFlow<Map<String, StockRealTimeData>>()
    val stockListUpdates: SharedFlow<Map<String, StockRealTimeData>> = _stockListUpdates.asSharedFlow()
    
    private val _portfolioUpdates = MutableSharedFlow<Map<String, StockRealTimeData>>()
    val portfolioUpdates: SharedFlow<Map<String, StockRealTimeData>> = _portfolioUpdates.asSharedFlow()
    
    private val _summaryUpdates = MutableSharedFlow<Map<String, StockRealTimeData>>()
    val summaryUpdates: SharedFlow<Map<String, StockRealTimeData>> = _summaryUpdates.asSharedFlow()
    
    companion object {
        private const val TAG = "SmartUpdateScheduler"
    }
    
    init {
        startUpdateQueues()
    }
    
    fun scheduleUpdate(screenType: ScreenType, stockCode: String, data: StockRealTimeData) {
        scope.launch {
            updateQueues[screenType]?.add(stockCode, data)
        }
    }
    
    fun scheduleMultipleUpdates(screenType: ScreenType, updates: Map<String, StockRealTimeData>) {
        scope.launch {
            updateQueues[screenType]?.addMultiple(updates)
        }
    }
    
    // 특정 화면의 업데이트 주기 동적 변경
    fun adjustUpdateFrequency(screenType: ScreenType, newIntervalMs: Float) {
        updateQueues[screenType]?.updateInterval(newIntervalMs)
        Log.d(TAG, "$screenType 업데이트 주기 변경: ${newIntervalMs}ms")
    }
    
    // 화면이 포커스를 잃었을 때 업데이트 주기 줄이기
    fun pauseScreen(screenType: ScreenType) {
        updateQueues[screenType]?.pause()
        Log.d(TAG, "$screenType 업데이트 일시정지")
    }
    
    // 화면이 포커스를 얻었을 때 업데이트 재개
    fun resumeScreen(screenType: ScreenType) {
        updateQueues[screenType]?.resume()
        Log.d(TAG, "$screenType 업데이트 재개")
    }
    
    private fun startUpdateQueues() {
        updateQueues.forEach { (screenType, queue) ->
            scope.launch {
                queue.startProcessing { updates ->
                    when (screenType) {
                        ScreenType.CHART -> _chartUpdates.tryEmit(updates)
                        ScreenType.STOCK_LIST -> _stockListUpdates.tryEmit(updates)
                        ScreenType.PORTFOLIO -> _portfolioUpdates.tryEmit(updates)
                        ScreenType.SUMMARY -> _summaryUpdates.tryEmit(updates)
                        ScreenType.NEWS -> { /* 뉴스는 업데이트 스트림 불필요 */ }
                    }
                }
            }
        }
    }
    
    private inner class UpdateQueue(
        private var intervalMs: Float,
        private val name: String
    ) {
        private val pendingUpdates = mutableMapOf<String, StockRealTimeData>()
        private var lastUpdate = 0L
        private var isPaused = false
        private val lock = Mutex()
        
        suspend fun add(stockCode: String, data: StockRealTimeData) {
            if (isPaused) return
            
            lock.withLock {
                pendingUpdates[stockCode] = data
                
                val now = System.currentTimeMillis()
                if (now - lastUpdate >= intervalMs) {
                    flushUpdates()
                    lastUpdate = now
                }
            }
        }
        
        suspend fun addMultiple(updates: Map<String, StockRealTimeData>) {
            if (isPaused) return
            
            lock.withLock {
                pendingUpdates.putAll(updates)
                
                val now = System.currentTimeMillis()
                if (now - lastUpdate >= intervalMs) {
                    flushUpdates()
                    lastUpdate = now
                }
            }
        }
        
        suspend fun startProcessing(onFlush: suspend (Map<String, StockRealTimeData>) -> Unit) {
            while (true) {
                delay(intervalMs.toLong())
                
                if (isPaused) continue
                
                lock.withLock {
                    if (pendingUpdates.isNotEmpty()) {
                        val updates = pendingUpdates.toMap()
                        pendingUpdates.clear()
                        lastUpdate = System.currentTimeMillis()
                        
                        // UI 스레드에서 실행
                        onFlush(updates)
                        
                        Log.v(TAG, "$name: ${updates.size}개 종목 업데이트 처리")
                    }
                }
            }
        }
        
        private fun flushUpdates() {
            // startProcessing에서 자동으로 처리됨
        }
        
        fun updateInterval(newInterval: Float) {
            intervalMs = newInterval
        }
        
        fun pause() {
            isPaused = true
        }
        
        fun resume() {
            isPaused = false
        }
        
        fun getStats(): String {
            return "$name: ${pendingUpdates.size}개 대기, ${intervalMs}ms 주기, 일시정지: $isPaused"
        }
    }
    
    fun getSchedulerStats(): String {
        return updateQueues.entries.joinToString("\n") { (screenType, queue) ->
            queue.getStats()
        }
    }
    
    fun cleanup() {
        scope.cancel()
    }
}