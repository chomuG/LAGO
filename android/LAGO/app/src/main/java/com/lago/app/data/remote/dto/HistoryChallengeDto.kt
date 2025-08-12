package com.lago.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class HistoryChallengeStockDto(
    @SerializedName("challenge_id") val challengeId: Int,
    @SerializedName("stock_code") val stockCode: String,
    @SerializedName("stock_name") val stockName: String,
    @SerializedName("current_price") val currentPrice: Float,
    @SerializedName("open_price") val openPrice: Float,
    @SerializedName("high_price") val highPrice: Float,
    @SerializedName("low_price") val lowPrice: Float,
    @SerializedName("close_price") val closePrice: Float,
    @SerializedName("fluctuation_rate") val fluctuationRate: Float,
    @SerializedName("trading_volume") val tradingVolume: Long,
    @SerializedName("market_cap") val marketCap: Long,
    @SerializedName("profit_rate") val profitRate: Float?
)