package com.lago.app.data.service

import android.util.Log
import com.lago.app.data.remote.api.ChartApiService
import com.lago.app.data.remote.dto.StockDayDto
import com.lago.app.util.MarketTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 초기 가격 계산을 위한 일봉 데이터 서비스
 * 일주일치 일봉 데이터에서 최신 거래일의 종가를 추출
 */
@Singleton
class InitialPriceService @Inject constructor(
    private val chartApiService: ChartApiService
) {
    
    companion object {
        private const val TAG = "InitialPriceService"
        private const val WEEK_DAYS = 7
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }
    
    /**
     * 일주일치 일봉 데이터에서 최신 거래일 종가 추출
     * @param stockCodes 종목 코드 리스트
     * @return Map<종목코드, 최신 거래일 종가>
     */
    suspend fun getLatestClosePrices(stockCodes: List<String>): Map<String, Int> = withContext(Dispatchers.IO) {
        if (stockCodes.isEmpty()) {
            Log.d(TAG, "종목 코드 리스트가 비어있음")
            return@withContext emptyMap()
        }
        
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(WEEK_DAYS.toLong())
        val startDateStr = startDate.format(DATE_FORMATTER)
        val endDateStr = endDate.format(DATE_FORMATTER)
        
        Log.d(TAG, "일주일치 일봉 조회 시작: ${stockCodes.size}개 종목 ($startDateStr ~ $endDateStr)")
        
        val results = mutableMapOf<String, Int>()
        
        // 병렬 처리로 성능 최적화
        val deferredResults = stockCodes.map { stockCode ->
            async {
                try {
                    val latestClosePrice = getLatestClosePriceForStock(stockCode, startDateStr, endDateStr)
                    if (latestClosePrice != null && latestClosePrice > 0) {
                        stockCode to latestClosePrice
                    } else {
                        Log.w(TAG, "최신 종가 조회 실패: $stockCode")
                        null
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "종목 처리 중 오류: $stockCode", e)
                    null
                }
            }
        }
        
        // 모든 비동기 작업 완료 대기
        deferredResults.awaitAll().filterNotNull().forEach { (stockCode, price) ->
            results[stockCode] = price
        }
        
        Log.d(TAG, "일주일치 일봉 조회 완료: ${results.size}/${stockCodes.size}개 성공")
        return@withContext results
    }
    
    /**
     * 단일 종목의 최신 거래일 종가 조회
     * @param stockCode 종목 코드
     * @param startDate 시작 날짜 (yyyy-MM-dd)
     * @param endDate 종료 날짜 (yyyy-MM-dd)
     * @return 최신 거래일 종가 (실패 시 null)
     */
    private suspend fun getLatestClosePriceForStock(
        stockCode: String, 
        startDate: String, 
        endDate: String
    ): Int? {
        return try {
            Log.v(TAG, "일봉 데이터 조회: $stockCode ($startDate ~ $endDate)")
            
            val dayDataList = chartApiService.getStockDayByCode(stockCode, startDate, endDate)
            
            if (dayDataList.isEmpty()) {
                Log.w(TAG, "일봉 데이터 없음: $stockCode")
                return null
            }
            
            // 날짜순 정렬 후 최신 데이터 선택
            val latestData = dayDataList
                .filter { it.closePrice > 0 } // 유효한 종가만 필터링
                .maxByOrNull { it.date } // 가장 최신 날짜
            
            if (latestData != null) {
                Log.d(TAG, "최신 종가 추출: $stockCode = ${latestData.closePrice} (${latestData.date})")
                latestData.closePrice
            } else {
                Log.w(TAG, "유효한 종가 없음: $stockCode")
                null
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "일봉 데이터 조회 실패: $stockCode", e)
            null
        }
    }
    
    /**
     * 특정 종목의 최신 거래일 종가 조회 (단일 종목용)
     * @param stockCode 종목 코드
     * @return 최신 거래일 종가 (실패 시 null)
     */
    suspend fun getLatestClosePrice(stockCode: String): Int? = withContext(Dispatchers.IO) {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(WEEK_DAYS.toLong())
        val startDateStr = startDate.format(DATE_FORMATTER)
        val endDateStr = endDate.format(DATE_FORMATTER)
        
        return@withContext getLatestClosePriceForStock(stockCode, startDateStr, endDateStr)
    }
    
    /**
     * 최신 거래일 종가와 변동률 정보 함께 조회
     * @param stockCodes 종목 코드 리스트
     * @return Map<종목코드, PriceInfo>
     */
    suspend fun getLatestPriceInfo(stockCodes: List<String>): Map<String, PriceInfo> = withContext(Dispatchers.IO) {
        if (stockCodes.isEmpty()) {
            return@withContext emptyMap()
        }
        
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(WEEK_DAYS.toLong())
        val startDateStr = startDate.format(DATE_FORMATTER)
        val endDateStr = endDate.format(DATE_FORMATTER)
        
        Log.d(TAG, "가격 정보 조회 시작: ${stockCodes.size}개 종목")
        
        val results = mutableMapOf<String, PriceInfo>()
        
        val deferredResults = stockCodes.map { stockCode ->
            async {
                try {
                    val priceInfo = getPriceInfoForStock(stockCode, startDateStr, endDateStr)
                    if (priceInfo != null) {
                        stockCode to priceInfo
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "가격 정보 조회 실패: $stockCode", e)
                    null
                }
            }
        }
        
        deferredResults.awaitAll().filterNotNull().forEach { (stockCode, priceInfo) ->
            results[stockCode] = priceInfo
        }
        
        Log.d(TAG, "가격 정보 조회 완료: ${results.size}/${stockCodes.size}개 성공")
        return@withContext results
    }
    
    /**
     * 단일 종목의 가격 정보 조회
     */
    private suspend fun getPriceInfoForStock(
        stockCode: String,
        startDate: String,
        endDate: String
    ): PriceInfo? {
        return try {
            val dayDataList = chartApiService.getStockDayByCode(stockCode, startDate, endDate)
            
            if (dayDataList.isEmpty()) {
                return null
            }
            
            // 최신 거래일 데이터
            val latestData = dayDataList
                .filter { it.closePrice > 0 }
                .maxByOrNull { it.date }
                ?: return null
            
            // 이전 거래일 데이터 (변동률 계산용)
            val previousData = dayDataList
                .filter { it.closePrice > 0 && it.date < latestData.date }
                .maxByOrNull { it.date }
            
            val changePrice = if (previousData != null) {
                latestData.closePrice - previousData.closePrice
            } else {
                0
            }
            
            val changeRate = if (previousData != null && previousData.closePrice > 0) {
                ((latestData.closePrice - previousData.closePrice).toDouble() / previousData.closePrice) * 100
            } else {
                0.0
            }
            
            PriceInfo(
                closePrice = latestData.closePrice,
                changePrice = changePrice,
                changeRate = changeRate,
                date = latestData.date
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "가격 정보 처리 실패: $stockCode", e)
            null
        }
    }
    
    /**
     * 서비스 상태 정보 (디버깅용)
     */
    fun getServiceStatus(): String {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(WEEK_DAYS.toLong())
        
        return """
            InitialPriceService 상태:
            - 현재 시각: ${MarketTimeUtils.getCurrentKoreaTime()}
            - 조회 기간: ${startDate.format(DATE_FORMATTER)} ~ ${endDate.format(DATE_FORMATTER)}
            - 시장 상태: ${MarketTimeUtils.getMarketStatusString()}
        """.trimIndent()
    }
}

/**
 * 가격 정보 데이터 클래스
 */
data class PriceInfo(
    val closePrice: Int,
    val changePrice: Int,
    val changeRate: Double,
    val date: String
)