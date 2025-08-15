package com.lago.app.util

import android.util.Log
import com.lago.app.data.service.InitialPriceService
import com.lago.app.data.service.PriceInfo
import com.lago.app.domain.entity.StockRealTimeData
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 하이브리드 가격 계산기
 * 초기 일봉 데이터와 실시간 웹소켓 데이터를 결합하여 정확한 가격 계산
 */
@Singleton
class HybridPriceCalculator @Inject constructor(
    private val initialPriceService: InitialPriceService
) {
    
    companion object {
        private const val TAG = "HybridPriceCalculator"
    }
    
    /**
     * 하이브리드 가격 데이터 클래스
     */
    data class HybridPriceData(
        val stockCode: String,
        val currentPrice: Int,
        val basePrice: Int, // 일봉 기준 가격 (전일 종가)
        val changePrice: Int,
        val changeRate: Double,
        val isRealTime: Boolean, // 실시간 데이터 사용 여부
        val lastUpdated: Long = System.currentTimeMillis()
    )
    
    /**
     * 초기 가격 계산 결과
     */
    data class InitialPriceResult(
        val prices: Map<String, HybridPriceData>,
        val successCount: Int,
        val failedStocks: List<String>
    )
    
    /**
     * 종목 리스트에 대한 초기 가격 계산
     * @param stockCodes 종목 코드 리스트
     * @return 초기 가격 계산 결과
     */
    suspend fun calculateInitialPrices(stockCodes: List<String>): InitialPriceResult {
        if (stockCodes.isEmpty()) {
            Log.d(TAG, "종목 코드 리스트가 비어있음")
            return InitialPriceResult(emptyMap(), 0, emptyList())
        }
        
        Log.d(TAG, "초기 가격 계산 시작: ${stockCodes.size}개 종목")
        
        try {
            // 일주일치 일봉 데이터에서 최신 가격 정보 조회
            val priceInfoMap = initialPriceService.getLatestPriceInfo(stockCodes)
            
            val hybridPrices = mutableMapOf<String, HybridPriceData>()
            val failedStocks = mutableListOf<String>()
            
            stockCodes.forEach { stockCode ->
                val priceInfo = priceInfoMap[stockCode]
                if (priceInfo != null) {
                    val hybridData = HybridPriceData(
                        stockCode = stockCode,
                        currentPrice = priceInfo.closePrice,
                        basePrice = priceInfo.closePrice,
                        changePrice = priceInfo.changePrice,
                        changeRate = priceInfo.changeRate,
                        isRealTime = false
                    )
                    hybridPrices[stockCode] = hybridData
                    Log.v(TAG, "초기 가격 설정: $stockCode = ${priceInfo.closePrice}")
                } else {
                    failedStocks.add(stockCode)
                    Log.w(TAG, "초기 가격 조회 실패: $stockCode")
                }
            }
            
            val result = InitialPriceResult(
                prices = hybridPrices,
                successCount = hybridPrices.size,
                failedStocks = failedStocks
            )
            
            Log.d(TAG, "초기 가격 계산 완료: ${result.successCount}/${stockCodes.size}개 성공")
            return result
            
        } catch (e: Exception) {
            Log.e(TAG, "초기 가격 계산 중 오류", e)
            return InitialPriceResult(emptyMap(), 0, stockCodes)
        }
    }
    
    /**
     * 실시간 데이터로 가격 업데이트
     * @param basePrices 기본 가격 데이터 (일봉 기준)
     * @param realTimeData 실시간 웹소켓 데이터
     * @return 업데이트된 하이브리드 가격 데이터
     */
    fun updateWithRealTimeData(
        basePrices: Map<String, HybridPriceData>,
        realTimeData: Map<String, StockRealTimeData>
    ): Map<String, HybridPriceData> {
        if (basePrices.isEmpty()) {
            Log.w(TAG, "기본 가격 데이터가 비어있음")
            return emptyMap()
        }
        
        val updatedPrices = basePrices.toMutableMap()
        var realTimeUpdateCount = 0
        
        realTimeData.forEach { (stockCode, realtimeStock) ->
            val basePrice = basePrices[stockCode]
            if (basePrice != null) {
                val realTimePrice = realtimeStock.price.toInt()
                if (realTimePrice > 0) {
                    // 실시간 데이터로 업데이트
                    val changePrice = realTimePrice - basePrice.basePrice
                    val changeRate = if (basePrice.basePrice > 0) {
                        (changePrice.toDouble() / basePrice.basePrice) * 100
                    } else {
                        0.0
                    }
                    
                    updatedPrices[stockCode] = basePrice.copy(
                        currentPrice = realTimePrice,
                        changePrice = changePrice,
                        changeRate = changeRate,
                        isRealTime = true,
                        lastUpdated = System.currentTimeMillis()
                    )
                    
                    realTimeUpdateCount++
                    Log.v(TAG, "실시간 업데이트: $stockCode = $realTimePrice (기준: ${basePrice.basePrice})")
                }
            }
        }
        
        if (realTimeUpdateCount > 0) {
            Log.d(TAG, "실시간 데이터 업데이트 완료: ${realTimeUpdateCount}개 종목")
        }
        
        return updatedPrices
    }
    
    /**
     * 단일 종목의 하이브리드 가격 데이터 생성
     * @param stockCode 종목 코드
     * @param basePriceData 기본 가격 데이터
     * @param realTimeData 실시간 데이터 (옵셔널)
     * @return 하이브리드 가격 데이터
     */
    fun createHybridPriceData(
        stockCode: String,
        basePriceData: PriceInfo,
        realTimeData: StockRealTimeData? = null
    ): HybridPriceData {
        return if (realTimeData != null && realTimeData.price > 0) {
            // 실시간 데이터 사용
            val changePrice = realTimeData.price.toInt() - basePriceData.closePrice
            val changeRate = if (basePriceData.closePrice > 0) {
                (changePrice.toDouble() / basePriceData.closePrice) * 100
            } else {
                0.0
            }
            
            HybridPriceData(
                stockCode = stockCode,
                currentPrice = realTimeData.price.toInt(),
                basePrice = basePriceData.closePrice,
                changePrice = changePrice,
                changeRate = changeRate,
                isRealTime = true
            )
        } else {
            // 일봉 데이터 사용
            HybridPriceData(
                stockCode = stockCode,
                currentPrice = basePriceData.closePrice,
                basePrice = basePriceData.closePrice,
                changePrice = basePriceData.changePrice,
                changeRate = basePriceData.changeRate,
                isRealTime = false
            )
        }
    }
    
    /**
     * 가격 데이터를 StockRealTimeData 형태로 변환
     * @param hybridPrice 하이브리드 가격 데이터
     * @return StockRealTimeData 객체
     */
    fun toStockRealTimeData(hybridPrice: HybridPriceData): StockRealTimeData {
        return StockRealTimeData(
            stockCode = hybridPrice.stockCode,
            closePrice = hybridPrice.basePrice.toLong(),
            tradePrice = hybridPrice.currentPrice.toLong(),
            currentPrice = hybridPrice.currentPrice.toLong(),
            changePrice = hybridPrice.changePrice.toLong(),
            changeRate = hybridPrice.changeRate
        )
    }
    
    /**
     * 하이브리드 가격 데이터를 StockRealTimeData Map으로 변환
     * @param hybridPrices 하이브리드 가격 데이터 맵
     * @return StockRealTimeData 맵
     */
    fun toStockRealTimeDataMap(hybridPrices: Map<String, HybridPriceData>): Map<String, StockRealTimeData> {
        return hybridPrices.mapValues { (_, hybridPrice) ->
            toStockRealTimeData(hybridPrice)
        }
    }
    
    /**
     * 가격 데이터 유효성 검증
     * @param hybridPrice 하이브리드 가격 데이터
     * @return 유효 여부
     */
    fun isValidPrice(hybridPrice: HybridPriceData): Boolean {
        return hybridPrice.currentPrice > 0 && 
               hybridPrice.basePrice > 0 && 
               hybridPrice.stockCode.isNotBlank()
    }
    
    /**
     * 실시간 데이터 비율 계산
     * @param hybridPrices 하이브리드 가격 데이터 맵
     * @return 실시간 데이터 비율 (0.0 ~ 1.0)
     */
    fun calculateRealTimeRatio(hybridPrices: Map<String, HybridPriceData>): Double {
        if (hybridPrices.isEmpty()) return 0.0
        
        val realTimeCount = hybridPrices.values.count { it.isRealTime }
        return realTimeCount.toDouble() / hybridPrices.size
    }
    
    /**
     * 디버깅용 상태 정보
     * @param hybridPrices 하이브리드 가격 데이터 맵
     * @return 상태 정보 문자열
     */
    fun getStatusInfo(hybridPrices: Map<String, HybridPriceData>): String {
        val totalCount = hybridPrices.size
        val realTimeCount = hybridPrices.values.count { it.isRealTime }
        val baseDataCount = totalCount - realTimeCount
        val realTimeRatio = calculateRealTimeRatio(hybridPrices)
        
        return """
            HybridPriceCalculator 상태:
            - 총 종목 수: $totalCount
            - 실시간 데이터: $realTimeCount
            - 일봉 데이터: $baseDataCount  
            - 실시간 비율: ${String.format("%.1f", realTimeRatio * 100)}%
            - 마지막 업데이트: ${System.currentTimeMillis()}
        """.trimIndent()
    }
}