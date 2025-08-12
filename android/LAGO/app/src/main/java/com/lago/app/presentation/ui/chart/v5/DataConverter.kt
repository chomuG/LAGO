package com.lago.app.presentation.ui.chart.v5

import com.lago.app.domain.entity.CandlestickData as DomainCandlestickData
import com.lago.app.domain.entity.LineData as DomainLineData
import com.lago.app.domain.entity.VolumeData as DomainVolumeData
import com.lago.app.domain.entity.MACDResult as DomainMACDResult
import com.lago.app.domain.entity.BollingerBandsResult as DomainBollingerBandsResult
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
                // 분봉: 시:분 형식으로 표시
                TimeScaleOptions(
                    timeVisible = true,
                    secondsVisible = false,
                    borderVisible = true,
                    rightOffset = 0,
                    barSpacing = 6,
                    tickMarkFormatter = """
                        function(time) {
                            var date = new Date(time);
                            return ('0' + date.getHours()).slice(-2) + ':' + ('0' + date.getMinutes()).slice(-2);
                        }
                    """.trimIndent()
                )
            }
            "60" -> {
                // 1시간봉: 월/일 시:분 형식
                TimeScaleOptions(
                    timeVisible = true,
                    secondsVisible = false,
                    borderVisible = true,
                    rightOffset = 0,
                    barSpacing = 6,
                    tickMarkFormatter = """
                        function(time) {
                            var date = new Date(time);
                            return (date.getMonth() + 1) + '/' + date.getDate() + ' ' + 
                                   ('0' + date.getHours()).slice(-2) + ':' + ('0' + date.getMinutes()).slice(-2);
                        }
                    """.trimIndent()
                )
            }
            "D" -> {
                // 일봉: 월/일 형식
                TimeScaleOptions(
                    timeVisible = true,
                    secondsVisible = false,
                    borderVisible = true,
                    rightOffset = 0,
                    barSpacing = 6,
                    tickMarkFormatter = """
                        function(time) {
                            var date = new Date(time);
                            return (date.getMonth() + 1) + '/' + date.getDate();
                        }
                    """.trimIndent()
                )
            }
            "W" -> {
                // 주봉: 년/월/일 형식
                TimeScaleOptions(
                    timeVisible = true,
                    secondsVisible = false,
                    borderVisible = true,
                    rightOffset = 0,
                    barSpacing = 6,
                    tickMarkFormatter = """
                        function(time) {
                            var date = new Date(time);
                            return date.getFullYear() + '/' + (date.getMonth() + 1) + '/' + date.getDate();
                        }
                    """.trimIndent()
                )
            }
            "M", "Y" -> {
                // 월봉, 년봉: 년/월 형식
                TimeScaleOptions(
                    timeVisible = true,
                    secondsVisible = false,
                    borderVisible = true,
                    rightOffset = 0,
                    barSpacing = 6,
                    tickMarkFormatter = """
                        function(time) {
                            var date = new Date(time);
                            return date.getFullYear() + '/' + (date.getMonth() + 1);
                        }
                    """.trimIndent()
                )
            }
            else -> TimeScaleOptions() // 기본값
        }
    }
    
    /**
     * Convert milliseconds timestamp to yyyy-MM-dd format
     */
    private fun convertTimestamp(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }
    
    /**
     * Convert LAGO domain CandlestickData to v5 CandlestickData
     */
    fun convertCandlestickData(domainData: List<DomainCandlestickData>): List<CandlestickData> {
        return domainData.map { candle ->
            CandlestickData(
                time = convertTimestamp(candle.time), // Use yyyy-MM-dd format
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
                time = convertTimestamp(line.time), // Use yyyy-MM-dd format
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
                time = convertTimestamp(volume.time), // Use yyyy-MM-dd format
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
        
        // Volume 지표 추가
        if (enabledIndicators.volume && volumeData.isNotEmpty()) {
            indicators.add(
                IndicatorData(
                    type = IndicatorType.VOLUME,
                    name = "거래량",
                    data = convertVolumeData(volumeData),
                    options = IndicatorOptions(
                        color = "#FF9800",
                        height = 80
                    )
                )
            )
        }
        
        // RSI 지표 추가
        if (enabledIndicators.rsi && rsiData.isNotEmpty()) {
            indicators.add(
                IndicatorData(
                    type = IndicatorType.RSI,
                    name = "RSI (14)",
                    data = convertLineData(rsiData),
                    options = IndicatorOptions(
                        color = "#9C27B0",
                        height = 100
                    )
                )
            )
        }
        
        // MACD 지표 추가
        if (enabledIndicators.macd && macdData != null && macdData.macdLine.isNotEmpty()) {
            indicators.add(
                IndicatorData(
                    type = IndicatorType.MACD,
                    name = "MACD (12,26,9)",
                    data = convertLineData(macdData.macdLine),
                    options = IndicatorOptions(
                        color = "#2196F3",
                        height = 100
                    )
                )
            )
        }
        
        // 볼린저 밴드 지표 추가
        if (enabledIndicators.bollingerBands && bollingerBands != null && bollingerBands.upperBand.isNotEmpty()) {
            // Upper band 추가
            indicators.add(
                IndicatorData(
                    type = IndicatorType.BOLLINGER_BANDS,
                    name = "볼린저 밴드 (상단)",
                    data = convertLineData(bollingerBands.upperBand),
                    options = IndicatorOptions(
                        color = "#607D8B",
                        height = 120
                    )
                )
            )
            
            // Middle band (SMA) 추가
            indicators.add(
                IndicatorData(
                    type = IndicatorType.BOLLINGER_BANDS,
                    name = "볼린저 밴드 (중간)",
                    data = convertLineData(bollingerBands.middleBand),
                    options = IndicatorOptions(
                        color = "#FF9800",
                        height = 120
                    )
                )
            )
            
            // Lower band 추가
            indicators.add(
                IndicatorData(
                    type = IndicatorType.BOLLINGER_BANDS,
                    name = "볼린저 밴드 (하단)",
                    data = convertLineData(bollingerBands.lowerBand),
                    options = IndicatorOptions(
                        color = "#607D8B",
                        height = 120
                    )
                )
            )
        }
        
        // SMA5 지표 - 메인 패널에 오버레이로 표시하기 위해 별도 처리 필요
        // 현재는 MultiPanelChart가 메인 패널 오버레이를 지원하지 않으므로 생략
        
        // SMA20 지표 - 메인 패널에 오버레이로 표시하기 위해 별도 처리 필요
        // 현재는 MultiPanelChart가 메인 패널 오버레이를 지원하지 않으므로 생략
        
        return MultiPanelData(
            priceData = convertCandlestickData(candlestickData),
            indicators = indicators
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