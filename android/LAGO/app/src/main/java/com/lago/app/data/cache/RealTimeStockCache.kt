package com.lago.app.data.cache

import android.util.LruCache
import com.lago.app.domain.entity.StockRealTimeData
import com.lago.app.domain.entity.StockPriority
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealTimeStockCache @Inject constructor() {
    
    // L1: 즉시 접근 (현재 화면) - 50개
    private val hotCache = LruCache<String, StockRealTimeData>(50)
    
    // L2: 자주 사용 (포트폴리오, 관심종목) - 200개
    private val warmCache = LruCache<String, StockRealTimeData>(200)
    
    // L3: 가끔 사용 (전체 목록) - 1000개
    private val coldCache = LruCache<String, StockRealTimeData>(1000)
    
    // 종목별 우선순위 추적
    private val stockPriorities = mutableMapOf<String, StockPriority>()
    
    // 실시간 업데이트 스트림
    private val _stockUpdates = MutableSharedFlow<StockRealTimeData>()
    val stockUpdates: SharedFlow<StockRealTimeData> = _stockUpdates.asSharedFlow()
    
    // 포트폴리오용 특별 스트림
    private val _portfolioUpdates = MutableSharedFlow<StockRealTimeData>()
    val portfolioUpdates: SharedFlow<StockRealTimeData> = _portfolioUpdates.asSharedFlow()
    
    // 차트용 고빈도 스트림
    private val _chartUpdates = MutableSharedFlow<StockRealTimeData>()
    val chartUpdates: SharedFlow<StockRealTimeData> = _chartUpdates.asSharedFlow()
    
    fun updateStock(stockCode: String, data: StockRealTimeData) {
        val priority = stockPriorities[stockCode] ?: StockPriority.COLD
        
        // 우선순위에 따라 적절한 캐시에 저장
        when (priority) {
            StockPriority.HOT -> {
                hotCache.put(stockCode, data)
                _chartUpdates.tryEmit(data)
            }
            StockPriority.WARM -> {
                warmCache.put(stockCode, data)
                _portfolioUpdates.tryEmit(data)
            }
            StockPriority.COLD -> {
                coldCache.put(stockCode, data)
            }
        }
        
        // 모든 구독자에게 알림
        _stockUpdates.tryEmit(data)
    }
    
    fun getStockData(stockCode: String): StockRealTimeData? {
        return hotCache.get(stockCode)
            ?: warmCache.get(stockCode)
            ?: coldCache.get(stockCode)
    }
    
    fun setStockPriority(stockCode: String, priority: StockPriority) {
        stockPriorities[stockCode] = priority
        
        // 우선순위 변경 시 캐시 이동
        moveStockToAppropriateCache(stockCode, priority)
    }
    
    fun setMultipleStockPriorities(stocks: Map<String, StockPriority>) {
        stockPriorities.putAll(stocks)
        stocks.forEach { (stockCode, priority) ->
            moveStockToAppropriateCache(stockCode, priority)
        }
    }
    
    private fun moveStockToAppropriateCache(stockCode: String, priority: StockPriority) {
        // 기존 캐시에서 데이터 찾기
        val data = getStockData(stockCode) ?: return
        
        // 모든 캐시에서 제거
        hotCache.remove(stockCode)
        warmCache.remove(stockCode)
        coldCache.remove(stockCode)
        
        // 새로운 우선순위 캐시에 저장
        when (priority) {
            StockPriority.HOT -> hotCache.put(stockCode, data)
            StockPriority.WARM -> warmCache.put(stockCode, data)
            StockPriority.COLD -> coldCache.put(stockCode, data)
        }
    }
    
    fun clearStockPriority(stockCode: String) {
        stockPriorities.remove(stockCode)
        
        // HOT/WARM에서 COLD로 이동
        val data = getStockData(stockCode)
        if (data != null) {
            hotCache.remove(stockCode)
            warmCache.remove(stockCode)
            coldCache.put(stockCode, data)
        }
    }
    
    fun getCacheStats(): String {
        return """
            Hot Cache: ${hotCache.size()}/50
            Warm Cache: ${warmCache.size()}/200  
            Cold Cache: ${coldCache.size()}/1000
            Priorities: ${stockPriorities.size}
        """.trimIndent()
    }
}