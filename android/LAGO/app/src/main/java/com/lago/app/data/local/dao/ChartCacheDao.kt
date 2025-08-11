package com.lago.app.data.local.dao

import androidx.room.*
import com.lago.app.data.local.entity.CachedChartDataEntity
import com.lago.app.data.local.entity.CachedStockInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChartCacheDao {
    
    // ===== CHART DATA CACHE =====
    
    @Query("SELECT * FROM cached_chart_data WHERE cacheKey = :cacheKey AND validUntil > :currentTime LIMIT 1")
    suspend fun getCachedChartData(cacheKey: String, currentTime: Long): CachedChartDataEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChartData(data: CachedChartDataEntity)
    
    @Query("DELETE FROM cached_chart_data WHERE validUntil < :currentTime")
    suspend fun clearExpiredChartData(currentTime: Long)
    
    @Query("DELETE FROM cached_chart_data WHERE stockCode = :stockCode AND timeFrame = :timeFrame")
    suspend fun clearChartDataForStock(stockCode: String, timeFrame: String)
    
    @Query("SELECT COUNT(*) FROM cached_chart_data")
    suspend fun getCacheCount(): Int
    
    @Query("DELETE FROM cached_chart_data WHERE cacheKey NOT IN (SELECT cacheKey FROM cached_chart_data ORDER BY lastUpdated DESC LIMIT :maxSize)")
    suspend fun limitCacheSize(maxSize: Int)
    
    // ===== STOCK INFO CACHE =====
    
    @Query("SELECT * FROM cached_stock_info WHERE stockCode = :stockCode AND validUntil > :currentTime LIMIT 1")
    suspend fun getCachedStockInfo(stockCode: String, currentTime: Long): CachedStockInfoEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockInfo(stockInfo: CachedStockInfoEntity)
    
    @Query("DELETE FROM cached_stock_info WHERE validUntil < :currentTime")
    suspend fun clearExpiredStockInfo(currentTime: Long)
    
    // ===== UTILITY METHODS =====
    
    /**
     * 특정 주식의 모든 캐시 데이터 조회 (디버그용)
     */
    @Query("SELECT * FROM cached_chart_data WHERE stockCode = :stockCode")
    fun getAllCachedDataForStock(stockCode: String): Flow<List<CachedChartDataEntity>>
    
    /**
     * 캐시 통계 정보
     */
    @Query("""
        SELECT 
            COUNT(*) as totalCount,
            COUNT(CASE WHEN validUntil > :currentTime THEN 1 END) as validCount,
            COUNT(CASE WHEN validUntil <= :currentTime THEN 1 END) as expiredCount,
            MAX(lastUpdated) as latestUpdate
        FROM cached_chart_data
    """)
    suspend fun getCacheStats(currentTime: Long): CacheStats?
    
    /**
     * 전체 캐시 초기화
     */
    @Query("DELETE FROM cached_chart_data")
    suspend fun clearAllChartCache()
    
    @Query("DELETE FROM cached_stock_info")
    suspend fun clearAllStockInfoCache()
}

/**
 * 캐시 통계 정보
 */
data class CacheStats(
    val totalCount: Int,
    val validCount: Int,
    val expiredCount: Int,
    val latestUpdate: Long?
)