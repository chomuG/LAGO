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
import android.graphics.Color as AndroidColor

/**
 * 단순화된 차트 설정 - 단일 차트에 모든 데이터를 오버레이로 표시
 * 복잡한 멀티패널 로직 제거하고 성능 최적화
 */
fun setupSimplifiedChart(
    chartsView: ChartsView,
    candlestickData: List<CandlestickData>,
    volumeData: List<VolumeData>,
    sma5Data: List<LineData>,
    sma20Data: List<LineData>,
    config: ChartConfig,
    rsiData: List<LineData> = emptyList(),
    macdData: MACDResult? = null,
    bollingerBands: BollingerBandsResult? = null,
    chartHeight: Int = 400 // 차트 전체 높이 (dp)
) {
    chartsView.subscribeOnChartStateChange { state ->
        when (state) {
            is ChartsView.State.Ready -> {
                // 기본 차트 옵션 설정
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
                    // 차트 높이는 Compose에서 관리하므로 여기서 설정하지 않음
                }

                // 1. 메인 캔들스틱 차트
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

                // 2. SMA5 오버레이
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

                // 3. SMA20 오버레이
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

                // 4. 거래량 오버레이 (하단에 작게 표시)
                if (config.indicators.volume && volumeData.isNotEmpty()) {
                    val volumeChartData = volumeData.map { data ->
                        HistogramData(
                            time = ChartUtils.timestampToBusinessDay(data.time),
                            value = data.value / 1000f, // 스케일 조정으로 작게 표시
                            color = AndroidColor.parseColor(ChartColorConstants.VOLUME_COLOR).toIntColor()
                        )
                    }

                    chartsView.api.addHistogramSeries(
                        options = HistogramSeriesOptions(
                            priceScaleId = PriceScaleId("volume_overlay"),
                            color = AndroidColor.parseColor(ChartColorConstants.VOLUME_COLOR).toIntColor()
                        )
                    ) { api ->
                        api.setData(volumeChartData)
                        // 하단에 작게 표시
                        api.priceScale().applyOptions(
                            PriceScaleOptions(
                                scaleMargins = PriceScaleMargins(
                                    top = 0.8f,
                                    bottom = 0.02f
                                )
                            )
                        )
                    }
                }

                // 5. RSI 오버레이 (별도 스케일로 우측 하단에 표시)
                if (config.indicators.rsi && rsiData.isNotEmpty()) {
                    val rsiChartData = rsiData.map { data ->
                        TradingViewLineData(
                            time = ChartUtils.timestampToBusinessDay(data.time),
                            value = data.value
                        )
                    }

                    chartsView.api.addLineSeries(
                        options = LineSeriesOptions(
                            priceScaleId = PriceScaleId("rsi_overlay"),
                            color = AndroidColor.parseColor(ChartColorConstants.RSI_COLOR).toIntColor(),
                            lineWidth = LineWidth.TWO
                        )
                    ) { api ->
                        api.setData(rsiChartData)
                        // 우측 하단에 작게 표시
                        api.priceScale().applyOptions(
                            PriceScaleOptions(
                                scaleMargins = PriceScaleMargins(
                                    top = 0.75f,
                                    bottom = 0.02f
                                )
                            )
                        )
                    }
                }

                // 6. MACD 오버레이 (메인 차트 중앙 하단에 표시)
                if (config.indicators.macd && macdData != null) {
                    // MACD Line
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
                                lineWidth = LineWidth.ONE
                            )
                        ) { api ->
                            api.setData(macdLineData)
                            // 중앙 하단에 표시
                            api.priceScale().applyOptions(
                                PriceScaleOptions(
                                    scaleMargins = PriceScaleMargins(
                                        top = 0.6f,
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
                }

                // 7. Bollinger Bands 오버레이
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

/**
 * 차트 높이 조정은 Compose 레벨에서 처리하므로 별도 함수 불필요
 */