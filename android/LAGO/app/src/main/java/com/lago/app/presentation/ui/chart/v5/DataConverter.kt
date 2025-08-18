package com.lago.app.presentation.ui.chart.v5

import com.lago.app.domain.entity.CandlestickData as DomainCandlestickData
import com.lago.app.domain.entity.LineData as DomainLineData
import com.lago.app.domain.entity.VolumeData as DomainVolumeData
import com.lago.app.domain.entity.MACDResult as DomainMACDResult
import com.lago.app.domain.entity.BollingerBandsResult as DomainBollingerBandsResult
import com.lago.app.presentation.ui.chart.v5.MACDChartData
import java.text.SimpleDateFormat
import java.util.*

/**
 * Data converter between LAGO domain entities and v5 chart data structures
 */
object DataConverter {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    /**
     * Create TimeScaleOptions based on timeframe
     */
    fun createTimeScaleOptions(timeFrame: String): TimeScaleOptions {
        return when (timeFrame) {
            "1", "3", "5", "10", "15", "30" -> {
                // 분봉: 시:분 형식으로 표시 (KST 적용)
                TimeScaleOptions(
                    timeVisible = true,
                    secondsVisible = false,
                    borderVisible = true,
                    rightOffset = 0,
                    barSpacing = 6,
                    tickMarkFormatter = """
                        function(time) {
                            var date = new Date(time * 1000);
                            return ('0' + date.getHours()).slice(-2) + ':' + ('0' + date.getMinutes()).slice(-2);
                        }
                    """.trimIndent()
                )
            }
            "60" -> {
                // 1시간봉: 월/일 시:분 형식 (KST 적용)
                TimeScaleOptions(
                    timeVisible = true,
                    secondsVisible = false,
                    borderVisible = true,
                    rightOffset = 0,
                    barSpacing = 6,
                    tickMarkFormatter = """
                        function(time) {
                            var date = new Date(time * 1000);
                            return (date.getMonth() + 1) + '/' + date.getDate() + ' ' + 
                                   ('0' + date.getHours()).slice(-2) + ':' + ('0' + date.getMinutes()).slice(-2);
                        }
                    """.trimIndent()
                )
            }
            "D" -> {
                // 일봉: 월/일 형식 (KST 적용)
                TimeScaleOptions(
                    timeVisible = true,
                    secondsVisible = false,
                    borderVisible = true,
                    rightOffset = 0,
                    barSpacing = 6,
                    tickMarkFormatter = """
                        function(time) {
                            var date = new Date(time * 1000);
                            return (date.getMonth() + 1) + '/' + date.getDate();
                        }
                    """.trimIndent()
                )
            }
            "W" -> {
                // 주봉: 년/월/일 형식 (KST 적용)
                TimeScaleOptions(
                    timeVisible = true,
                    secondsVisible = false,
                    borderVisible = true,
                    rightOffset = 0,
                    barSpacing = 6,
                    tickMarkFormatter = """
                        function(time) {
                            var date = new Date(time * 1000);
                            return date.getFullYear() + '/' + (date.getMonth() + 1) + '/' + date.getDate();
                        }
                    """.trimIndent()
                )
            }
            "M", "Y" -> {
                // 월봉, 년봉: 년/월 형식 (KST 적용)
                TimeScaleOptions(
                    timeVisible = true,
                    secondsVisible = false,
                    borderVisible = true,
                    rightOffset = 0,
                    barSpacing = 6,
                    tickMarkFormatter = """
                        function(time) {
                            var date = new Date(time * 1000);
                            return date.getFullYear() + '/' + (date.getMonth() + 1);
                        }
                    """.trimIndent()
                )
            }
            else -> TimeScaleOptions() // 기본값
        }
    }
    
    /**
     * Convert timestamp to epoch seconds using ChartTimeManager
     */
    private fun convertTimestamp(timestamp: Long): Long {
        return ChartTimeManager.normalizeToEpochSeconds(timestamp)
    }
    
    /**
     * Convert LAGO domain CandlestickData to v5 CandlestickData
     */
    fun convertCandlestickData(domainData: List<DomainCandlestickData>): List<CandlestickData> {
        return domainData.map { candle ->
            CandlestickData(
                time = convertTimestamp(candle.time), // Use epoch seconds
                open = candle.open.toDouble(),
                high = candle.high.toDouble(),
                low = candle.low.toDouble(),
                close = candle.close.toDouble()
            )
        }
    }
    
    /**
     * Convert LAGO domain LineData to v5 ChartData
     */
    fun convertLineData(domainData: List<DomainLineData>): List<ChartData> {
        return domainData.map { line ->
            ChartData(
                time = convertTimestamp(line.time), // Use epoch seconds
                value = line.value.toDouble()
            )
        }
    }
    
    /**
     * Convert LAGO domain VolumeData to v5 ChartData for histogram
     */
    fun convertVolumeData(domainData: List<DomainVolumeData>): List<ChartData> {
        return domainData.map { volume ->
            ChartData(
                time = convertTimestamp(volume.time), // Use epoch seconds
                value = volume.value.toDouble()
            )
        }
    }
    
    /**
     * Create MultiPanelData from LAGO domain entities
     */
    fun createMultiPanelData(
        candlestickData: List<DomainCandlestickData>,
        volumeData: List<DomainVolumeData> = emptyList(),
        sma5Data: List<DomainLineData> = emptyList(),
        sma20Data: List<DomainLineData> = emptyList(),
        rsiData: List<DomainLineData> = emptyList(),
        macdData: DomainMACDResult? = null,
        bollingerBands: DomainBollingerBandsResult? = null,
        enabledIndicators: EnabledIndicators,
        timeFrame: String = "D"
    ): MultiPanelData {
        val indicators = mutableListOf<IndicatorData>()
        
        // Volume 지표 추가 (데이터가 없어도 패널 생성)
        if (enabledIndicators.volume) {
            indicators.add(
                IndicatorData(
                    type = IndicatorType.VOLUME,
                    name = "거래량",
                    data = if (volumeData.isNotEmpty()) convertVolumeData(volumeData) else emptyList(),
                    options = IndicatorOptions(
                        color = "#FF9800",
                        height = 80
                    )
                )
            )
        }
        
        // RSI 지표 추가 (데이터가 없어도 패널 생성)
        if (enabledIndicators.rsi) {
            indicators.add(
                IndicatorData(
                    type = IndicatorType.RSI,
                    name = "RSI (14)",
                    data = if (rsiData.isNotEmpty()) convertLineData(rsiData) else emptyList(),
                    options = IndicatorOptions(
                        color = "#9C27B0",
                        height = 100
                    )
                )
            )
        }
        
        // MACD 지표 추가 (데이터가 없어도 패널 생성)
        if (enabledIndicators.macd) {
            android.util.Log.d("LAGO_CHART", "Adding MACD indicator - enabled:${enabledIndicators.macd}, data:${macdData != null}, dataSize:${macdData?.macdLine?.size ?: 0}")
            indicators.add(
                IndicatorData(
                    type = IndicatorType.MACD,
                    name = "MACD (12,26,9)",
                    data = if (macdData != null && macdData.macdLine.isNotEmpty()) convertLineData(macdData.macdLine) else emptyList(),
                    options = IndicatorOptions(
                        color = "#2196F3",
                        height = 100
                    )
                )
            )
        } else {
            android.util.Log.d("LAGO_CHART", "MACD not added - enabled:${enabledIndicators.macd}")
        }
        
        // 볼린저 밴드는 메인 차트에 오버레이로 표시하기 위해 별도 처리 필요
        // MultiPanelChart에서 메인 패널에 직접 추가됨
        
        return MultiPanelData(
            priceData = convertCandlestickData(candlestickData),
            indicators = indicators,
            bollingerBands = if (enabledIndicators.bollingerBands && bollingerBands != null) {
                BollingerBandsData(
                    upperBand = convertLineData(bollingerBands.upperBand),
                    middleBand = convertLineData(bollingerBands.middleBand),
                    lowerBand = convertLineData(bollingerBands.lowerBand)
                )
            } else null,
            sma5Data = if (enabledIndicators.sma5 && sma5Data.isNotEmpty()) {
                convertLineData(sma5Data)
            } else null,
            sma20Data = if (enabledIndicators.sma20 && sma20Data.isNotEmpty()) {
                convertLineData(sma20Data)
            } else null,
            macdData = if (enabledIndicators.macd && macdData != null) {
                MACDChartData(
                    macdLine = convertLineData(macdData.macdLine),
                    signalLine = convertLineData(macdData.signalLine),
                    histogram = convertVolumeData(macdData.histogram)
                )
            } else null
        )
    }
}

/**
 * Indicator configuration from LAGO ChartConfig
 */
data class EnabledIndicators(
    val volume: Boolean = false,
    val rsi: Boolean = false,
    val macd: Boolean = false,
    val sma5: Boolean = false,
    val sma20: Boolean = false,
    val bollingerBands: Boolean = false
)

/**
 * Convert LAGO ChartConfig.indicators to EnabledIndicators
 */
fun com.lago.app.domain.entity.ChartIndicators.toEnabledIndicators(): EnabledIndicators {
    return EnabledIndicators(
        volume = this.volume,
        rsi = this.rsi,
        macd = this.macd,
        sma5 = this.sma5,
        sma20 = this.sma20,
        bollingerBands = this.bollingerBands
    )
}