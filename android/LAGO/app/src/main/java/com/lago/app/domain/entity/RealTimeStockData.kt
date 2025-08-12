package com.lago.app.domain.entity

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import com.google.gson.annotations.SerializedName

@Serializable
data class StockRealTimeData(
    @SerialName("code")
    @SerializedName("code") 
    val stockCode: String,
    
    @SerialName("closePrice")
    @SerializedName("closePrice") 
    val closePrice: Long? = null,
    
    @SerialName("openPrice")
    @SerializedName("openPrice") 
    val openPrice: Long? = null,
    
    @SerialName("highPrice")
    @SerializedName("highPrice") 
    val highPrice: Long? = null,
    
    @SerialName("lowPrice")
    @SerializedName("lowPrice") 
    val lowPrice: Long? = null,
    
    @SerialName("volume")
    @SerializedName("volume") 
    val volume: Long? = null,
    
    @SerialName("minuteKey")
    @SerializedName("minuteKey") 
    val minuteKey: String? = null,
    
    @SerialName("parsedDateTime")
    @SerializedName("parsedDateTime") 
    val parsedDateTime: String? = null,
    
    // 호환용 (다른 소스에서 올 수도 있음)
    @SerialName("tradePrice")
    @SerializedName("tradePrice") 
    val tradePrice: Long? = null,
    
    @SerialName("currentPrice")
    @SerializedName("currentPrice") 
    val currentPrice: Long? = null,
    
    // 가격 변동 정보
    @SerialName("changePrice")
    @SerializedName("changePrice")
    val changePrice: Long? = null,
    
    @SerialName("changeRate")
    @SerializedName("changeRate")
    val changeRate: Double? = null,
    
    @SerialName("change")
    @SerializedName("change")
    val change: Long? = null,
    
    @SerialName("rate")
    @SerializedName("rate")
    val rate: Double? = null,
    
    val timestamp: Long = System.currentTimeMillis()
) {
    // 실제 가격 계산 (우선순위: tradePrice > currentPrice > closePrice)
    val price: Double
        get() = (tradePrice ?: currentPrice ?: closePrice ?: 0L).toDouble()
    
    // 호환성을 위한 기존 필드들
    val priceChange: Double
        get() = (changePrice ?: change ?: 0L).toDouble()
    
    val priceChangePercent: Double
        get() = changeRate ?: rate ?: 0.0
}

data class PortfolioReturn(
    val stockCode: String,
    val stockName: String,
    val quantity: Int,
    val buyPrice: Double,
    val currentPrice: Double,
    val profit: Double,
    val returnRate: Double,
    val totalValue: Double
)

data class TotalPortfolioSummary(
    val totalInvestment: Double,
    val totalCurrentValue: Double,
    val totalProfit: Double,
    val totalReturnRate: Double,
    val stockReturns: List<PortfolioReturn>
)

enum class StockPriority {
    HOT,    // 현재 보고있는 종목 (차트 화면)
    WARM,   // 포트폴리오 + 관심종목
    COLD    // 전체 목록에서 가끔 보는 종목
}

enum class ScreenType {
    CHART,
    STOCK_LIST,
    PORTFOLIO,
    SUMMARY,
    NEWS
}