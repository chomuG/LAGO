package com.lago.app.presentation.ui.chart

import com.lago.app.domain.entity.CandlestickData
import com.lago.app.domain.entity.ChartConfig
import com.lago.app.domain.entity.LineData
import com.lago.app.domain.entity.VolumeData
import com.lago.app.domain.entity.MACDResult
import com.lago.app.domain.entity.BollingerBandsResult
import com.tradingview.lightweightcharts.view.ChartsView
import com.tradingview.lightweightcharts.api.options.models.*
import com.tradingview.lightweightcharts.api.series.enums.SeriesType
import com.tradingview.lightweightcharts.api.series.models.CandlestickData as TradingViewCandlestickData
import com.tradingview.lightweightcharts.api.series.models.Time
import com.tradingview.lightweightcharts.api.series.models.LineData as TradingViewLineData
import com.tradingview.lightweightcharts.api.series.enums.LineWidth
import com.tradingview.lightweightcharts.api.chart.models.color.surface.SolidColor
import com.tradingview.lightweightcharts.api.chart.models.color.toIntColor
import com.tradingview.lightweightcharts.api.series.enums.LineStyle
import com.tradingview.lightweightcharts.api.series.models.HistogramData
import com.tradingview.lightweightcharts.api.series.models.PriceScaleId
import com.tradingview.lightweightcharts.api.series.models.PriceFormat
import android.graphics.Color as AndroidColor

// Chart constants
internal object ChartConstants {
    // 패널 간격 설정 (0.01f = 1%, 0.05f = 5%)
    const val PANEL_SPACING = 0.02f  // 기본 2% 간격
    const val MIN_PANEL_SPACING = 0.01f  // 최소 1%
    const val MAX_PANEL_SPACING = 0.05f  // 최대 5%
}

// Chart color constants
internal object ChartColorConstants {
    const val CHART_UP_COLOR = "#FF99C5"
    const val CHART_DOWN_COLOR = "#42A6FF"
    const val SMA5_COLOR = "#F5A623"
    const val SMA20_COLOR = "#50E3C2"
    const val SMA60_COLOR = "#9013FE"
    const val SMA120_COLOR = "#E91E63"
    const val VOLUME_COLOR = "#666666"
    const val RSI_COLOR = "#9C27B0"
    const val MACD_LINE_COLOR = "#4CAF50"
    const val MACD_SIGNAL_COLOR = "#F44336"
    const val MACD_HISTOGRAM_COLOR = "#2196F3"
    const val BOLLINGER_COLOR = "#FF9800"
    const val GRID_COLOR = "#E6E6E6"
    const val TEXT_COLOR = "#616161"
}

// 전역 패널 크기 상태
var globalPanelSizes = PanelSizes()

fun setupChart(
    chartsView: ChartsView,
    candlestickData: List<CandlestickData>,
    volumeData: List<VolumeData>,
    sma5Data: List<LineData>,
    sma20Data: List<LineData>,
    config: ChartConfig,
    rsiData: List<LineData> = emptyList(),
    macdData: MACDResult? = null,
    bollingerBands: BollingerBandsResult? = null,
    panelSizes: PanelSizes = globalPanelSizes
) {
    chartsView.subscribeOnChartStateChange { state ->
        when (state) {
            is ChartsView.State.Ready -> {
                // 차트 초기화 (기존 시리즈 누적 방지)
                // TradingView v4에서는 차트를 새로 생성하는 방식으로 처리
                
                chartsView.api.applyOptions {
                    layout = layoutOptions {
                        background = SolidColor(AndroidColor.WHITE)
                        textColor = AndroidColor.parseColor(ChartColorConstants.TEXT_COLOR).toIntColor()
                    }
                    grid = gridOptions {
                        vertLines = gridLineOptions {
                            color = AndroidColor.parseColor(ChartColorConstants.GRID_COLOR).toIntColor()
                            style = LineStyle.DASHED
                        }
                        horzLines = gridLineOptions {
                            color = AndroidColor.parseColor(ChartColorConstants.GRID_COLOR).toIntColor()
                            style = LineStyle.DASHED
                        }
                    }
                }

                if (candlestickData.isNotEmpty()) {
                    val chartData = candlestickData.map { data ->
                        TradingViewCandlestickData(
                            time = ChartUtils.timestampToBusinessDay(data.time),
                            open = data.open,
                            high = data.high,
                            low = data.low,
                            close = data.close
                        )
                    }

                    chartsView.api.addCandlestickSeries(
                        options = CandlestickSeriesOptions(
                            upColor = AndroidColor.parseColor(ChartColorConstants.CHART_UP_COLOR).toIntColor(),
                            downColor = AndroidColor.parseColor(ChartColorConstants.CHART_DOWN_COLOR).toIntColor(),
                            borderVisible = false,
                            wickUpColor = AndroidColor.parseColor(ChartColorConstants.CHART_UP_COLOR).toIntColor(),
                            wickDownColor = AndroidColor.parseColor(ChartColorConstants.CHART_DOWN_COLOR).toIntColor()
                        )
                    ) { api ->
                        api.setData(chartData)
                    }
                }

                if (config.indicators.sma5 && sma5Data.isNotEmpty()) {
                    val sma5ChartData = sma5Data.map { data ->
                        TradingViewLineData(
                            time = ChartUtils.timestampToBusinessDay(data.time),
                            value = data.value
                        )
                    }

                    chartsView.api.addLineSeries(
                        options = LineSeriesOptions(
                            color = AndroidColor.parseColor(ChartColorConstants.SMA5_COLOR).toIntColor(),
                            lineWidth = LineWidth.TWO
                        )
                    ) { api ->
                        api.setData(sma5ChartData)
                    }
                }

                if (config.indicators.sma20 && sma20Data.isNotEmpty()) {
                    val sma20ChartData = sma20Data.map { data ->
                        TradingViewLineData(
                            time = ChartUtils.timestampToBusinessDay(data.time),
                            value = data.value
                        )
                    }

                    chartsView.api.addLineSeries(
                        options = LineSeriesOptions(
                            color = AndroidColor.parseColor(ChartColorConstants.SMA20_COLOR).toIntColor(),
                            lineWidth = LineWidth.TWO
                        )
                    ) { api ->
                        api.setData(sma20ChartData)
                    }
                }

                // 멀티패널 구현: 리사이즈 가능한 패널 레이아웃
                val panelSpacing = ChartConstants.PANEL_SPACING
                
                // 패널 크기에서 누적 위치 계산
                var accumulatedHeight = 0f
                val mainChartEnd = panelSizes.mainChartHeight
                val volumeEnd = mainChartEnd + panelSizes.volumeHeight
                val rsiEnd = volumeEnd + panelSizes.rsiHeight
                val macdEnd = rsiEnd + panelSizes.macdHeight
                
                // 메인 차트 영역 설정
                chartsView.api.applyOptions {
                    rightPriceScale = priceScaleOptions {
                        scaleMargins = priceScaleMargins {
                            top = 0.02f
                            bottom = 1.0f - mainChartEnd + panelSpacing
                        }
                    }
                }

                // 1. Volume 패널
                if (config.indicators.volume && volumeData.isNotEmpty()) {
                    val topMargin = mainChartEnd + panelSpacing
                    val bottomMargin = volumeEnd
                    val volumeChartData = volumeData.map { data ->
                        HistogramData(
                            time = ChartUtils.timestampToBusinessDay(data.time),
                            value = data.value,
                            color = if (data.value > 0)
                                AndroidColor.parseColor(ChartColorConstants.CHART_UP_COLOR).toIntColor()
                            else
                                AndroidColor.parseColor(ChartColorConstants.CHART_DOWN_COLOR).toIntColor()
                        )
                    }

                    chartsView.api.addHistogramSeries(
                        options = HistogramSeriesOptions(
                            priceScaleId = PriceScaleId("volume"),
                            color = AndroidColor.parseColor(ChartColorConstants.VOLUME_COLOR).toIntColor(),
                            priceFormat = PriceFormat.priceFormatBuiltIn(
                                type = PriceFormat.Type.VOLUME,
                                precision = 0,
                                minMove = 1.0f
                            )
                        )
                    ) { api ->
                        api.setData(volumeChartData)
                        api.priceScale().applyOptions(
                            PriceScaleOptions(
                                scaleMargins = PriceScaleMargins(
                                    top = topMargin,
                                    bottom = 1.0f - bottomMargin
                                )
                            )
                        )
                    }
                }

                // 2. RSI 패널
                if (config.indicators.rsi && rsiData.isNotEmpty()) {
                    val topMargin = volumeEnd + panelSpacing
                    val bottomMargin = rsiEnd
                    val rsiChartData = rsiData.map { data ->
                        TradingViewLineData(
                            time = ChartUtils.timestampToBusinessDay(data.time),
                            value = data.value
                        )
                    }

                    chartsView.api.addLineSeries(
                        options = LineSeriesOptions(
                            priceScaleId = PriceScaleId("rsi"),
                            color = AndroidColor.parseColor(ChartColorConstants.RSI_COLOR).toIntColor(),
                            lineWidth = LineWidth.TWO
                            // PriceFormat 제거 - RSI는 0-100 범위의 기본 포맷 사용
                        )
                    ) { api ->
                        api.setData(rsiChartData)
                        api.priceScale().applyOptions(
                            PriceScaleOptions(
                                scaleMargins = PriceScaleMargins(
                                    top = topMargin,
                                    bottom = 1.0f - bottomMargin
                                )
                            )
                        )
                    }
                }

                // 3. MACD 오버레이 (메인 차트에 표시)
                if (config.indicators.macd && macdData != null) {
                    // MACD Line (메인 라인)
                    if (macdData.macdLine.isNotEmpty()) {
                        val macdLineData = macdData.macdLine.map { data ->
                            TradingViewLineData(
                                time = ChartUtils.timestampToBusinessDay(data.time),
                                value = data.value
                            )
                        }

                        chartsView.api.addLineSeries(
                            options = LineSeriesOptions(
                                priceScaleId = PriceScaleId("macd_overlay"),
                                color = AndroidColor.parseColor(ChartColorConstants.MACD_LINE_COLOR).toIntColor(),
                                lineWidth = LineWidth.TWO
                            )
                        ) { api ->
                            api.setData(macdLineData)
                            // 메인 차트 하단에 작게 표시하기 위한 스케일 설정
                            api.priceScale().applyOptions(
                                PriceScaleOptions(
                                    scaleMargins = PriceScaleMargins(
                                        top = 0.85f, // 메인 차트 하단 15% 영역 사용
                                        bottom = 0.02f
                                    )
                                )
                            )
                        }
                    }

                    // Signal Line
                    if (macdData.signalLine.isNotEmpty()) {
                        val signalLineData = macdData.signalLine.map { data ->
                            TradingViewLineData(
                                time = ChartUtils.timestampToBusinessDay(data.time),
                                value = data.value
                            )
                        }

                        chartsView.api.addLineSeries(
                            options = LineSeriesOptions(
                                priceScaleId = PriceScaleId("macd_overlay"),
                                color = AndroidColor.parseColor(ChartColorConstants.MACD_SIGNAL_COLOR).toIntColor(),
                                lineWidth = LineWidth.ONE,
                                lineStyle = LineStyle.DASHED
                            )
                        ) { api ->
                            api.setData(signalLineData)
                        }
                    }

                    // MACD Histogram (히스토그램)
                    if (macdData.histogram.isNotEmpty()) {
                        val macdHistogramData = macdData.histogram.map { data ->
                            HistogramData(
                                time = ChartUtils.timestampToBusinessDay(data.time),
                                value = data.value,
                                color = if (data.value >= 0)
                                    AndroidColor.parseColor(ChartColorConstants.MACD_HISTOGRAM_COLOR).toIntColor()
                                else
                                    AndroidColor.parseColor(ChartColorConstants.MACD_SIGNAL_COLOR).toIntColor()
                            )
                        }

                        chartsView.api.addHistogramSeries(
                            options = HistogramSeriesOptions(
                                priceScaleId = PriceScaleId("macd_overlay"),
                                color = AndroidColor.parseColor(ChartColorConstants.MACD_HISTOGRAM_COLOR).toIntColor()
                            )
                        ) { api ->
                            api.setData(macdHistogramData)
                        }
                    }
                }

                // 4. Bollinger Bands (메인 차트에 오버레이)
                if (config.indicators.bollingerBands && bollingerBands != null) {
                    // Upper Band
                    val upperBandData = bollingerBands.upperBand.map { data ->
                        TradingViewLineData(
                            time = ChartUtils.timestampToBusinessDay(data.time),
                            value = data.value
                        )
                    }

                    chartsView.api.addLineSeries(
                        options = LineSeriesOptions(
                            color = AndroidColor.parseColor(ChartColorConstants.BOLLINGER_COLOR).toIntColor(),
                            lineWidth = LineWidth.ONE,
                            lineStyle = LineStyle.DASHED
                        )
                    ) { api ->
                        api.setData(upperBandData)
                    }

                    // Middle Band
                    val middleBandData = bollingerBands.middleBand.map { data ->
                        TradingViewLineData(
                            time = ChartUtils.timestampToBusinessDay(data.time),
                            value = data.value
                        )
                    }

                    chartsView.api.addLineSeries(
                        options = LineSeriesOptions(
                            color = AndroidColor.parseColor(ChartColorConstants.BOLLINGER_COLOR).toIntColor(),
                            lineWidth = LineWidth.ONE
                        )
                    ) { api ->
                        api.setData(middleBandData)
                    }

                    // Lower Band
                    val lowerBandData = bollingerBands.lowerBand.map { data ->
                        TradingViewLineData(
                            time = ChartUtils.timestampToBusinessDay(data.time),
                            value = data.value
                        )
                    }

                    chartsView.api.addLineSeries(
                        options = LineSeriesOptions(
                            color = AndroidColor.parseColor(ChartColorConstants.BOLLINGER_COLOR).toIntColor(),
                            lineWidth = LineWidth.ONE,
                            lineStyle = LineStyle.DASHED
                        )
                    ) { api ->
                        api.setData(lowerBandData)
                    }
                }
            }
            else -> {}
        }
    }
}