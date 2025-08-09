package com.lago.app.data.remote.dto

import com.google.gson.annotations.SerializedName

// Base Response
data class BaseResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("code") val code: Int?
)

// Stock Info Response
data class StockInfoResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: StockInfoDto
)

data class StockInfoDto(
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
    @SerializedName("market") val market: String,
    @SerializedName("current_price") val currentPrice: Float,
    @SerializedName("price_change") val priceChange: Float,
    @SerializedName("price_change_percent") val priceChangePercent: Float,
    @SerializedName("high_price") val highPrice: Float,
    @SerializedName("low_price") val lowPrice: Float,
    @SerializedName("open_price") val openPrice: Float,
    @SerializedName("volume") val volume: Long,
    @SerializedName("market_cap") val marketCap: Long?,
    @SerializedName("updated_at") val updatedAt: String
)

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

// Stock List Response
data class StockListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: StockListPageDto
)

data class StockListPageDto(
    @SerializedName("content") val content: List<StockItemDto>,
    @SerializedName("page") val page: Int,
    @SerializedName("size") val size: Int,
    @SerializedName("total_elements") val totalElements: Long,
    @SerializedName("total_pages") val totalPages: Int
)

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