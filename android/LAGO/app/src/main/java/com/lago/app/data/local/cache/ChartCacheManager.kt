package com.lago.app.data.local.cache

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lago.app.data.local.dao.ChartCacheDao
import com.lago.app.data.local.entity.CachedChartDataEntity
import com.lago.app.data.local.entity.CachedStockInfoEntity
import com.lago.app.domain.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChartCacheManager @Inject constructor(
    private val chartCacheDao: ChartCacheDao,
    private val gson: Gson
) {
    
    companion object {
        private const val TAG = "ChartCacheManager"
        
        // 캐시 만료 시간 설정 (분 단위)
        private const val STOCK_INFO_CACHE_DURATION = 5L // 5분
        private const val CANDLESTICK_CACHE_DURATION = 15L // 15분
        private const val VOLUME_CACHE_DURATION = 15L // 15분
        private const val INDICATORS_CACHE_DURATION = 30L // 30분
        
        // 최대 캐시 크기
        private const val MAX_CACHE_SIZE = 1000
        
        // 데이터 타입
        private const val DATA_TYPE_CANDLESTICK = "candlestick"
        private const val DATA_TYPE_VOLUME = "volume"
        private const val DATA_TYPE_INDICATORS = "indicators"
    }
    
    // ===== STOCK INFO CACHE =====
    
    suspend fun getCachedStockInfo(stockCode: String): StockInfo? = withContext(Dispatchers.IO) {
        return@withContext try {
            val currentTime = System.currentTimeMillis()
            val cached = chartCacheDao.getCachedStockInfo(stockCode, currentTime)
            
            cached?.let {
                Log.d(TAG, "Cache hit for stock info: $stockCode")
                StockInfo(
                    code = it.stockCode,
                    name = it.stockName,
                    currentPrice = it.currentPrice,
                    priceChange = it.priceChange,
                    priceChangePercent = it.priceChangePercent
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cached stock info", e)
            null
        }
    }
    
    suspend fun cacheStockInfo(stockInfo: StockInfo) = withContext(Dispatchers.IO) {
        try {
            val currentTime = System.currentTimeMillis()
            val validUntil = currentTime + (STOCK_INFO_CACHE_DURATION * 60 * 1000)
            
            val entity = CachedStockInfoEntity(
                stockCode = stockInfo.code,
                stockName = stockInfo.name,
                currentPrice = stockInfo.currentPrice,
                priceChange = stockInfo.priceChange,
                priceChangePercent = stockInfo.priceChangePercent,
                lastUpdated = currentTime,
                validUntil = validUntil
            )
            
            chartCacheDao.insertStockInfo(entity)
            Log.d(TAG, "Cached stock info: ${stockInfo.code}")
        } catch (e: Exception) {
            Log.e(TAG, "Error caching stock info", e)
        }
    }
    
    // ===== CANDLESTICK DATA CACHE =====
    
    suspend fun getCachedCandlestickData(
        stockCode: String, 
        timeFrame: String, 
        period: Int
    ): List<CandlestickData>? = withContext(Dispatchers.IO) {
        return@withContext try {
            val cacheKey = "${stockCode}_${timeFrame}_${DATA_TYPE_CANDLESTICK}_${period}"
            val currentTime = System.currentTimeMillis()
            val cached = chartCacheDao.getCachedChartData(cacheKey, currentTime)
            
            cached?.let {
                Log.d(TAG, "Cache hit for candlestick: $cacheKey")
                val type = object : TypeToken<List<CandlestickData>>() {}.type
                gson.fromJson(it.jsonData, type)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cached candlestick data", e)
            null
        }
    }
    
    suspend fun cacheCandlestickData(
        stockCode: String, 
        timeFrame: String, 
        period: Int,
        data: List<CandlestickData>
    ) = withContext(Dispatchers.IO) {
        try {
            val currentTime = System.currentTimeMillis()
            val validUntil = currentTime + (CANDLESTICK_CACHE_DURATION * 60 * 1000)
            val cacheKey = "${stockCode}_${timeFrame}_${DATA_TYPE_CANDLESTICK}_${period}"
            
            val entity = CachedChartDataEntity(
                cacheKey = cacheKey,
                stockCode = stockCode,
                timeFrame = timeFrame,
                dataType = DATA_TYPE_CANDLESTICK,
                jsonData = gson.toJson(data),
                lastUpdated = currentTime,
                validUntil = validUntil,
                period = period
            )
            
            chartCacheDao.insertChartData(entity)
            Log.d(TAG, "Cached candlestick data: $cacheKey")
            
            // 캐시 크기 제한
            manageCacheSize()
        } catch (e: Exception) {
            Log.e(TAG, "Error caching candlestick data", e)
        }
    }
    
    // ===== VOLUME DATA CACHE =====
    
    suspend fun getCachedVolumeData(
        stockCode: String, 
        timeFrame: String, 
        period: Int
    ): List<VolumeData>? = withContext(Dispatchers.IO) {
        return@withContext try {
            val cacheKey = "${stockCode}_${timeFrame}_${DATA_TYPE_VOLUME}_${period}"
            val currentTime = System.currentTimeMillis()
            val cached = chartCacheDao.getCachedChartData(cacheKey, currentTime)
            
            cached?.let {
                Log.d(TAG, "Cache hit for volume: $cacheKey")
                val type = object : TypeToken<List<VolumeData>>() {}.type
                gson.fromJson(it.jsonData, type)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cached volume data", e)
            null
        }
    }
    
    suspend fun cacheVolumeData(
        stockCode: String, 
        timeFrame: String, 
        period: Int,
        data: List<VolumeData>
    ) = withContext(Dispatchers.IO) {
        try {
            val currentTime = System.currentTimeMillis()
            val validUntil = currentTime + (VOLUME_CACHE_DURATION * 60 * 1000)
            val cacheKey = "${stockCode}_${timeFrame}_${DATA_TYPE_VOLUME}_${period}"
            
            val entity = CachedChartDataEntity(
                cacheKey = cacheKey,
                stockCode = stockCode,
                timeFrame = timeFrame,
                dataType = DATA_TYPE_VOLUME,
                jsonData = gson.toJson(data),
                lastUpdated = currentTime,
                validUntil = validUntil,
                period = period
            )
            
            chartCacheDao.insertChartData(entity)
            Log.d(TAG, "Cached volume data: $cacheKey")
        } catch (e: Exception) {
            Log.e(TAG, "Error caching volume data", e)
        }
    }
    
    // ===== INDICATORS DATA CACHE =====
    
    suspend fun getCachedIndicators(
        stockCode: String, 
        timeFrame: String, 
        indicators: List<String>,
        period: Int
    ): ChartIndicatorData? = withContext(Dispatchers.IO) {
        return@withContext try {
            val indicatorsKey = indicators.sorted().joinToString(",")
            val cacheKey = "${stockCode}_${timeFrame}_${DATA_TYPE_INDICATORS}_${indicatorsKey}_${period}"
            val currentTime = System.currentTimeMillis()
            val cached = chartCacheDao.getCachedChartData(cacheKey, currentTime)
            
            cached?.let {
                Log.d(TAG, "Cache hit for indicators: $cacheKey")
                gson.fromJson(it.jsonData, ChartIndicatorData::class.java)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cached indicators", e)
            null
        }
    }
    
    suspend fun cacheIndicators(
        stockCode: String, 
        timeFrame: String, 
        indicators: List<String>,
        period: Int,
        data: ChartIndicatorData
    ) = withContext(Dispatchers.IO) {
        try {
            val currentTime = System.currentTimeMillis()
            val validUntil = currentTime + (INDICATORS_CACHE_DURATION * 60 * 1000)
            val indicatorsKey = indicators.sorted().joinToString(",")
            val cacheKey = "${stockCode}_${timeFrame}_${DATA_TYPE_INDICATORS}_${indicatorsKey}_${period}"
            
            val entity = CachedChartDataEntity(
                cacheKey = cacheKey,
                stockCode = stockCode,
                timeFrame = timeFrame,
                dataType = DATA_TYPE_INDICATORS,
                jsonData = gson.toJson(data),
                lastUpdated = currentTime,
                validUntil = validUntil,
                period = period
            )
            
            chartCacheDao.insertChartData(entity)
            Log.d(TAG, "Cached indicators data: $cacheKey")
        } catch (e: Exception) {
            Log.e(TAG, "Error caching indicators data", e)
        }
    }
    
    // ===== CACHE MANAGEMENT =====
    
    /**
     * 만료된 캐시 데이터 정리
     */
    suspend fun clearExpiredCache() = withContext(Dispatchers.IO) {
        try {
            val currentTime = System.currentTimeMillis()
            chartCacheDao.clearExpiredChartData(currentTime)
            chartCacheDao.clearExpiredStockInfo(currentTime)
            Log.d(TAG, "Cleared expired cache data")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing expired cache", e)
        }
    }
    
    /**
     * 캐시 크기 관리 (LRU 방식)
     */
    private suspend fun manageCacheSize() {
        try {
            val cacheCount = chartCacheDao.getCacheCount()
            if (cacheCount > MAX_CACHE_SIZE) {
                chartCacheDao.limitCacheSize(MAX_CACHE_SIZE * 80 / 100) // 80%로 줄임
                Log.d(TAG, "Reduced cache size from $cacheCount to ${MAX_CACHE_SIZE * 80 / 100}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error managing cache size", e)
        }
    }
    
    /**
     * 특정 주식의 캐시 무효화 (실시간 업데이트 시 사용)
     */
    suspend fun invalidateStockCache(stockCode: String, timeFrame: String? = null) = withContext(Dispatchers.IO) {
        try {
            if (timeFrame != null) {
                chartCacheDao.clearChartDataForStock(stockCode, timeFrame)
                Log.d(TAG, "Invalidated cache for $stockCode - $timeFrame")
            } else {
                // 모든 타임프레임 무효화는 추후 필요시 구현
                Log.d(TAG, "Partial cache invalidation for $stockCode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error invalidating stock cache", e)
        }
    }
    
    /**
     * 캐시 통계 조회
     */
    suspend fun getCacheStatistics(): String = withContext(Dispatchers.IO) {
        return@withContext try {
            val currentTime = System.currentTimeMillis()
            val stats = chartCacheDao.getCacheStats(currentTime)
            
            """
            Cache Statistics:
            - Total entries: ${stats?.totalCount ?: 0}
            - Valid entries: ${stats?.validCount ?: 0}
            - Expired entries: ${stats?.expiredCount ?: 0}
            - Latest update: ${stats?.latestUpdate?.let { java.util.Date(it) } ?: "Never"}
            """.trimIndent()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cache statistics", e)
            "Cache statistics unavailable: ${e.message}"
        }
    }
    
    /**
     * 전체 캐시 초기화 (디버그/설정용)
     */
    suspend fun clearAllCache() = withContext(Dispatchers.IO) {
        try {
            chartCacheDao.clearAllChartCache()
            chartCacheDao.clearAllStockInfoCache()
            Log.d(TAG, "Cleared all cache data")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing all cache", e)
        }
    }
}