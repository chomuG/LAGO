package com.lago.app.data.remote.dto

import com.google.gson.annotations.SerializedName

// Base Response
data class BaseResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("code") val code: Int?,
    @SerializedName("data") val data: T?
)

// Simple Base Response without data
data class SimpleBaseResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("code") val code: Int?
)

// Note: StockInfoDto and StockInfoResponse are defined in StockDto.kt

// Candlestick Response
data class CandlestickResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<CandlestickDto>
)

data class CandlestickDto(
    @SerializedName("time") val time: Long,
    @SerializedName("open") val open: Float,
    @SerializedName("high") val high: Float,
    @SerializedName("low") val low: Float,
    @SerializedName("close") val close: Float,
    @SerializedName("volume") val volume: Long
)

// Volume Response
data class VolumeResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<VolumeDto>
)

data class VolumeDto(
    @SerializedName("time") val time: Long,
    @SerializedName("value") val value: Float,
    @SerializedName("color") val color: String?
)

// Stock Price Data Response (for initial data)
data class StockPriceDataDto(
    @SerializedName("stockInfoId") val stockInfoId: Int,
    @SerializedName("bucket") val bucket: String,
    @SerializedName("code") val code: String,
    @SerializedName("interval") val interval: String,
    @SerializedName("openPrice") val openPrice: Long,
    @SerializedName("highPrice") val highPrice: Long,
    @SerializedName("lowPrice") val lowPrice: Long,
    @SerializedName("closePrice") val closePrice: Long,
    @SerializedName("volume") val volume: Long
)

// Indicators Response
data class IndicatorsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: IndicatorsDto
)

data class IndicatorsDto(
    @SerializedName("sma5") val sma5: List<LineDataDto>?,
    @SerializedName("sma20") val sma20: List<LineDataDto>?,
    @SerializedName("sma60") val sma60: List<LineDataDto>?,
    @SerializedName("sma120") val sma120: List<LineDataDto>?,
    @SerializedName("rsi") val rsi: List<LineDataDto>?,
    @SerializedName("macd") val macd: MACDDto?,
    @SerializedName("bollinger_bands") val bollingerBands: BollingerBandsDto?
)

data class LineDataDto(
    @SerializedName("time") val time: Long,
    @SerializedName("value") val value: Float
)

data class MACDDto(
    @SerializedName("macd_line") val macdLine: List<LineDataDto>,
    @SerializedName("signal_line") val signalLine: List<LineDataDto>,
    @SerializedName("histogram") val histogram: List<VolumeDto>
)

data class BollingerBandsDto(
    @SerializedName("upper_band") val upperBand: List<LineDataDto>,
    @SerializedName("middle_band") val middleBand: List<LineDataDto>,
    @SerializedName("lower_band") val lowerBand: List<LineDataDto>
)

// Holdings Response
data class HoldingsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<HoldingDto>
)

data class HoldingDto(
    @SerializedName("stock_code") val stockCode: String,
    @SerializedName("stock_name") val stockName: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("avg_price") val averagePrice: Float,
    @SerializedName("current_price") val currentPrice: Float,
    @SerializedName("profit_loss") val profitLoss: Float,
    @SerializedName("profit_loss_percent") val profitLossPercent: Float,
    @SerializedName("total_value") val totalValue: Float
)

// Trading History Response
data class TradingHistoryResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: TradingHistoryPageDto
)

data class TradingHistoryPageDto(
    @SerializedName("content") val content: List<TradingDto>,
    @SerializedName("page") val page: Int,
    @SerializedName("size") val size: Int,
    @SerializedName("total_elements") val totalElements: Long,
    @SerializedName("total_pages") val totalPages: Int
)

data class TradingDto(
    @SerializedName("transaction_id") val transactionId: String,
    @SerializedName("stock_code") val stockCode: String,
    @SerializedName("stock_name") val stockName: String,
    @SerializedName("action_type") val actionType: String, // "BUY" or "SELL"
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("price") val price: Float,
    @SerializedName("total_amount") val totalAmount: Float,
    @SerializedName("created_at") val createdAt: String
)

// Favorites Response
data class FavoritesResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<String> // Stock codes
)

// Note: StockListResponse and StockListPageDto are defined in StockDto.kt

data class StockItemDto(
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
    @SerializedName("market") val market: String, // "KOSPI", "KOSDAQ"
    @SerializedName("current_price") val currentPrice: Int,
    @SerializedName("price_change") val priceChange: Int,
    @SerializedName("price_change_percent") val priceChangePercent: Double,
    @SerializedName("volume") val volume: Long,
    @SerializedName("market_cap") val marketCap: Long?,
    @SerializedName("sector") val sector: String?,
    @SerializedName("is_favorite") val isFavorite: Boolean = false,
    @SerializedName("updated_at") val updatedAt: String
)

// Pattern Analysis Request/Response
data class PatternAnalysisRequest(
    @SerializedName("time_frame") val timeFrame: String, // "1", "5", "15", "D", etc.
    @SerializedName("start_time") val startTime: String?, // ISO 8601 format or null for current visible range
    @SerializedName("end_time") val endTime: String?, // ISO 8601 format or null for current visible range
    @SerializedName("analysis_type") val analysisType: String = "comprehensive" // "comprehensive", "trend", "reversal"
)

data class PatternAnalysisResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: PatternAnalysisDataDto
)

data class PatternAnalysisDataDto(
    @SerializedName("patterns") val patterns: List<PatternResultDto>,
    @SerializedName("analysis_time") val analysisTime: String,
    @SerializedName("confidence_score") val confidenceScore: Float
)

data class PatternResultDto(
    @SerializedName("pattern_name") val patternName: String,
    @SerializedName("description") val description: String,
    @SerializedName("confidence") val confidence: Float,
    @SerializedName("recommendation") val recommendation: String?
)

// Simple Stock Response (actual API format)
data class SimpleStockDto(
    @SerializedName("stockInfoId") val stockInfoId: Int,
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
    @SerializedName("market") val market: String
)

// New API DTOs based on Swagger spec
data class StockDayDto(
    @SerializedName("date") val date: String,
    @SerializedName("openPrice") val openPrice: Int,
    @SerializedName("highPrice") val highPrice: Int,
    @SerializedName("lowPrice") val lowPrice: Int,
    @SerializedName("closePrice") val closePrice: Int,
    @SerializedName("volume") val volume: Int,
    @SerializedName("fluctuationRate") val fluctuationRate: Float
)

data class StockMinuteDto(
    @SerializedName("stockInfoId") val stockInfoId: Int,
    @SerializedName("date") val date: String,
    @SerializedName("openPrice") val openPrice: Int,
    @SerializedName("highPrice") val highPrice: Int,
    @SerializedName("lowPrice") val lowPrice: Int,
    @SerializedName("closePrice") val closePrice: Int,
    @SerializedName("volume") val volume: Int
)

data class StockMonthDto(
    @SerializedName("date") val date: Int,
    @SerializedName("openPrice") val openPrice: Int,
    @SerializedName("highPrice") val highPrice: Int,
    @SerializedName("lowPrice") val lowPrice: Int,
    @SerializedName("closePrice") val closePrice: Int,
    @SerializedName("volume") val volume: Int,
    @SerializedName("fluctuationRate") val fluctuationRate: Float
)

data class StockYearDto(
    @SerializedName("date") val date: Int,
    @SerializedName("openPrice") val openPrice: Int,
    @SerializedName("highPrice") val highPrice: Int,
    @SerializedName("lowPrice") val lowPrice: Int,
    @SerializedName("closePrice") val closePrice: Int,
    @SerializedName("volume") val volume: Int,
    @SerializedName("fluctuationRate") val fluctuationRate: Float
)