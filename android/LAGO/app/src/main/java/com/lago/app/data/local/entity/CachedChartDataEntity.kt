package com.lago.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_chart_data")
data class CachedChartDataEntity(
    @PrimaryKey
    val cacheKey: String, // "{stockCode}_{timeFrame}_{dataType}" 형식
    val stockCode: String,
    val timeFrame: String, // "D", "1", "5", "15", "30", "60"
    val dataType: String, // "candlestick", "volume", "indicators"
    val jsonData: String, // JSON 형태로 직렬화된 데이터
    val lastUpdated: Long, // 마지막 업데이트 시간 (timestamp)
    val validUntil: Long, // 캐시 만료 시간 (timestamp)
    val period: Int = 100 // 조회한 기간 (기본 100)
)

@Entity(tableName = "cached_stock_info")
data class CachedStockInfoEntity(
    @PrimaryKey
    val stockCode: String,
    val stockName: String,
    val currentPrice: Float,
    val priceChange: Float,
    val priceChangePercent: Float,
    val lastUpdated: Long,
    val validUntil: Long
)