package com.lago.app.domain.entity

data class ChartStockInfo(
    val code: String,
    val name: String,
    val currentPrice: Float,
    val priceChange: Float,
    val priceChangePercent: Float
)

data class CandlestickData(
    val time: Long,
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
    val volume: Long
)

data class VolumeData(
    val time: Long,
    val value: Float,
    val color: String? = null
)

data class LineData(
    val time: Long,
    val value: Float
)

data class MACDResult(
    val macdLine: List<LineData>,
    val signalLine: List<LineData>,
    val histogram: List<VolumeData>
)

data class BollingerBandsResult(
    val upperBand: List<LineData>,
    val middleBand: List<LineData>,
    val lowerBand: List<LineData>
)

data class ChartConfig(
    val stockCode: String,
    val timeFrame: String,
    val indicators: ChartIndicators
)

data class ChartIndicators(
    val sma5: Boolean = true,
    val sma20: Boolean = true,
    val sma60: Boolean = false,
    val sma120: Boolean = false,
    val rsi: Boolean = false,
    val macd: Boolean = true,
    val bollingerBands: Boolean = false,
    val volume: Boolean = true
)