package com.lago.app.presentation.ui.chart

import android.graphics.Color
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.lago.app.domain.entity.*
import com.tradingview.lightweightcharts.api.chart.models.color.surface.SolidColor
import com.tradingview.lightweightcharts.api.chart.models.color.toIntColor
import com.tradingview.lightweightcharts.api.interfaces.SeriesApi
import com.tradingview.lightweightcharts.api.options.models.*
import com.tradingview.lightweightcharts.api.series.enums.LineStyle
import com.tradingview.lightweightcharts.api.series.enums.LineWidth
import com.tradingview.lightweightcharts.api.series.models.PriceScaleId
import com.tradingview.lightweightcharts.view.ChartsView

/**
 * 분할 가능한 차트 뷰 - 메인 차트와 지표 차트를 분리하고 드래그로 비율 조절
 */
@Composable
fun SplittableChartView(
    candlestickData: List<CandlestickData>,
    volumeData: List<VolumeData>,
    sma5Data: List<LineData>,
    sma20Data: List<LineData>,
    config: ChartConfig,
    // 추가 지표 데이터
    rsiData: List<LineData> = emptyList(),
    macdData: MACDResult? = null,
    bollingerBands: BollingerBandsResult? = null,
    // 지표 표시 설정
    showRSI: Boolean = false,
    showMACD: Boolean = false,
    showVolume: Boolean = true,
    showBollingerBands: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    
    // 메인 차트와 지표 차트의 비율 상태 (0.6 = 60% 메인, 40% 지표)
    var mainChartRatio by remember { mutableStateOf(0.65f) }
    
    // 차트 참조들
    val mainChartRef = remember { mutableStateOf<ChartsView?>(null) }
    val indicatorChartRef = remember { mutableStateOf<ChartsView?>(null) }
    
    // 시간축 동기화를 위한 상태
    var isUpdatingTimeRange by remember { mutableStateOf(false) }
    
    // 시리즈 참조들 - 메인 차트
    val candlestickSeries = remember { mutableStateOf<SeriesApi?>(null) }
    val sma5Series = remember { mutableStateOf<SeriesApi?>(null) }
    val sma20Series = remember { mutableStateOf<SeriesApi?>(null) }
    val bollingerUpperSeries = remember { mutableStateOf<SeriesApi?>(null) }
    val bollingerMiddleSeries = remember { mutableStateOf<SeriesApi?>(null) }
    val bollingerLowerSeries = remember { mutableStateOf<SeriesApi?>(null) }
    
    // 시리즈 참조들 - 지표 차트
    val volumeSeries = remember { mutableStateOf<SeriesApi?>(null) }
    val rsiSeries = remember { mutableStateOf<SeriesApi?>(null) }
    val macdSeries = remember { mutableStateOf<SeriesApi?>(null) }
    val macdSignalSeries = remember { mutableStateOf<SeriesApi?>(null) }
    val macdHistogramSeries = remember { mutableStateOf<SeriesApi?>(null) }
    
    // 지표가 표시되는지 확인
    val hasIndicators = showVolume || showRSI || showMACD
    
    // 시간축 동기화 설정
    LaunchedEffect(mainChartRef.value, indicatorChartRef.value) {
        val mainChart = mainChartRef.value
        val indicatorChart = indicatorChartRef.value
        
        if (mainChart != null && indicatorChart != null && hasIndicators) {
            setupTimeRangeSync(mainChart, indicatorChart) { isUpdatingTimeRange = it }
        }
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        // 메인 차트 (캔들스틱, 이평선, 볼린저 밴드)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (hasIndicators) mainChartRatio else 1f)
        ) {
            AndroidView(
                factory = { context ->
                    ChartsView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        mainChartRef.value = this
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .background(ComposeColor.White),
                update = { chartView ->
                    setupMainChart(
                        chartView,
                        candlestickData,
                        sma5Data,
                        sma20Data,
                        bollingerBands,
                        showBollingerBands,
                        candlestickSeries,
                        sma5Series,
                        sma20Series,
                        bollingerUpperSeries,
                        bollingerMiddleSeries,
                        bollingerLowerSeries
                    )
                }
            )
        }
        
        // 분할선 (지표가 있을 때만 표시)
        if (hasIndicators) {
            ChartDivider(
                onDrag = { dragAmount ->
                    val containerHeight = 400.dp // 전체 차트 높이 근사값
                    val deltaRatio = with(density) { dragAmount / containerHeight.toPx() }
                    mainChartRatio = (mainChartRatio - deltaRatio).coerceIn(0.3f, 0.8f)
                }
            )
        }
        
        // 지표 차트 (거래량, RSI, MACD)
        if (hasIndicators) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f - mainChartRatio)
            ) {
                AndroidView(
                    factory = { context ->
                        ChartsView(context).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            indicatorChartRef.value = this
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ComposeColor.White),
                    update = { chartView ->
                        setupIndicatorChart(
                            chartView,
                            volumeData,
                            rsiData,
                            macdData,
                            candlestickData,
                            showVolume,
                            showRSI,
                            showMACD,
                            volumeSeries,
                            rsiSeries,
                            macdSeries,
                            macdSignalSeries,
                            macdHistogramSeries
                        )
                    }
                )
            }
        }
    }
    
    // 메모리 정리
    DisposableEffect(Unit) {
        onDispose {
            // 모든 시리즈 정리
            listOf(
                candlestickSeries, sma5Series, sma20Series,
                bollingerUpperSeries, bollingerMiddleSeries, bollingerLowerSeries,
                volumeSeries, rsiSeries, macdSeries, macdSignalSeries, macdHistogramSeries
            ).forEach { seriesRef ->
                seriesRef.value = null
            }
            
            // 차트 뷰 정리
            mainChartRef.value?.api?.remove()
            indicatorChartRef.value?.api?.remove()
            mainChartRef.value = null
            indicatorChartRef.value = null
        }
    }
}

/**
 * 드래그 가능한 차트 분할선
 */
@Composable
private fun ChartDivider(
    onDrag: (Float) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .background(ComposeColor.White)
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    onDrag(dragAmount.y)
                }
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 드래그 핸들 표시
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .background(
                    color = ComposeColor.Gray.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(2.dp)
                )
        )
    }
}

/**
 * 메인 차트 설정 (캔들스틱, 이평선, 볼린저 밴드)
 */
private fun setupMainChart(
    chartView: ChartsView,
    candlestickData: List<CandlestickData>,
    sma5Data: List<LineData>,
    sma20Data: List<LineData>,
    bollingerBands: BollingerBandsResult?,
    showBollingerBands: Boolean,
    candlestickSeries: MutableState<SeriesApi?>,
    sma5Series: MutableState<SeriesApi?>,
    sma20Series: MutableState<SeriesApi?>,
    bollingerUpperSeries: MutableState<SeriesApi?>,
    bollingerMiddleSeries: MutableState<SeriesApi?>,
    bollingerLowerSeries: MutableState<SeriesApi?>
) {
    try {
        // 차트 기본 옵션 설정
        chartView.api.applyOptions {
            layout = layoutOptions {
                background = SolidColor(Color.parseColor("#FFFFFF"))
                textColor = Color.parseColor("#333333").toIntColor()
            }
            
            grid = gridOptions {
                vertLines = gridLineOptions {
                    color = Color.parseColor("#E6E6E6").toIntColor()
                }
                horzLines = gridLineOptions {
                    color = Color.parseColor("#E6E6E6").toIntColor()
                }
            }
            
            rightPriceScale = priceScaleOptions {
                scaleMargins = priceScaleMargins {
                    top = 0.1f
                    bottom = 0.1f
                }
                borderVisible = false
                visible = true
            }
            
            timeScale = timeScaleOptions {
                borderVisible = false
                timeVisible = false // 메인 차트에서는 시간 숨김
                secondsVisible = false
            }
            
            crosshair = crosshairOptions {
                // Default crosshair settings
            }
        }
        
        // 1. 캔들스틱 시리즈
        if (candlestickData.isNotEmpty()) {
            chartView.api.addCandlestickSeries(
                options = CandlestickSeriesOptions(
                    upColor = Color.parseColor("#ef5350").toIntColor(),
                    downColor = Color.parseColor("#26a69a").toIntColor(),
                    borderVisible = false,
                    wickUpColor = Color.parseColor("#ef5350").toIntColor(),
                    wickDownColor = Color.parseColor("#26a69a").toIntColor()
                ),
                onSeriesCreated = { api ->
                    candlestickSeries.value = api
                    api.setData(candlestickData.map { data ->
                        com.tradingview.lightweightcharts.api.series.models.CandlestickData(
                            time = ChartUtils.timestampToBusinessDay(data.time),
                            open = data.open,
                            high = data.high,
                            low = data.low,
                            close = data.close
                        )
                    })
                }
            )
        }
        
        // 2. SMA5 시리즈
        if (sma5Data.isNotEmpty()) {
            chartView.api.addLineSeries(
                options = LineSeriesOptions(
                    color = Color.parseColor("#FF6B35").toIntColor(),
                    lineWidth = LineWidth.TWO
                ),
                onSeriesCreated = { api ->
                    sma5Series.value = api
                    api.setData(sma5Data.map { data ->
                        com.tradingview.lightweightcharts.api.series.models.LineData(
                            time = ChartUtils.timestampToBusinessDay(data.time),
                            value = data.value
                        )
                    })
                }
            )
        }
        
        // 3. SMA20 시리즈
        if (sma20Data.isNotEmpty()) {
            chartView.api.addLineSeries(
                options = LineSeriesOptions(
                    color = Color.parseColor("#4ECDC4").toIntColor(),
                    lineWidth = LineWidth.TWO
                ),
                onSeriesCreated = { api ->
                    sma20Series.value = api
                    api.setData(sma20Data.map { data ->
                        com.tradingview.lightweightcharts.api.series.models.LineData(
                            time = ChartUtils.timestampToBusinessDay(data.time),
                            value = data.value
                        )
                    })
                }
            )
        }
        
        // 4. 볼린저 밴드 시리즈 (선택적)
        if (showBollingerBands && bollingerBands != null) {
            // 상단 밴드
            chartView.api.addLineSeries(
                options = LineSeriesOptions(
                    color = Color.parseColor("#FFCC02").toIntColor(),
                    lineWidth = LineWidth.ONE,
                    lineStyle = LineStyle.DASHED
                ),
                onSeriesCreated = { api ->
                    bollingerUpperSeries.value = api
                    api.setData(bollingerBands.upperBand.map { data ->
                        com.tradingview.lightweightcharts.api.series.models.LineData(
                            time = ChartUtils.timestampToBusinessDay(data.time),
                            value = data.value
                        )
                    })
                }
            )
            
            // 중간 밴드
            chartView.api.addLineSeries(
                options = LineSeriesOptions(
                    color = Color.parseColor("#FFCC02").toIntColor(),
                    lineWidth = LineWidth.ONE
                ),
                onSeriesCreated = { api ->
                    bollingerMiddleSeries.value = api
                    api.setData(bollingerBands.middleBand.map { data ->
                        com.tradingview.lightweightcharts.api.series.models.LineData(
                            time = ChartUtils.timestampToBusinessDay(data.time),
                            value = data.value
                        )
                    })
                }
            )
            
            // 하단 밴드
            chartView.api.addLineSeries(
                options = LineSeriesOptions(
                    color = Color.parseColor("#FFCC02").toIntColor(),
                    lineWidth = LineWidth.ONE,
                    lineStyle = LineStyle.DASHED
                ),
                onSeriesCreated = { api ->
                    bollingerLowerSeries.value = api
                    api.setData(bollingerBands.lowerBand.map { data ->
                        com.tradingview.lightweightcharts.api.series.models.LineData(
                            time = ChartUtils.timestampToBusinessDay(data.time),
                            value = data.value
                        )
                    })
                }
            )
        }
        
    } catch (e: Exception) {
        // 오류 무시 - 차트는 계속 표시
    }
}

/**
 * 지표 차트 설정 (거래량, RSI, MACD)
 */
private fun setupIndicatorChart(
    chartView: ChartsView,
    volumeData: List<VolumeData>,
    rsiData: List<LineData>,
    macdData: MACDResult?,
    candlestickData: List<CandlestickData>,
    showVolume: Boolean,
    showRSI: Boolean,
    showMACD: Boolean,
    volumeSeries: MutableState<SeriesApi?>,
    rsiSeries: MutableState<SeriesApi?>,
    macdSeries: MutableState<SeriesApi?>,
    macdSignalSeries: MutableState<SeriesApi?>,
    macdHistogramSeries: MutableState<SeriesApi?>
) {
    try {
        // 차트 기본 옵션 설정
        chartView.api.applyOptions {
            layout = layoutOptions {
                background = SolidColor(Color.parseColor("#FFFFFF"))
                textColor = Color.parseColor("#333333").toIntColor()
            }
            
            grid = gridOptions {
                vertLines = gridLineOptions {
                    color = Color.parseColor("#E6E6E6").toIntColor()
                }
                horzLines = gridLineOptions {
                    color = Color.parseColor("#E6E6E6").toIntColor()
                }
            }
            
            rightPriceScale = priceScaleOptions {
                scaleMargins = priceScaleMargins {
                    top = 0.1f
                    bottom = calculateIndicatorBottomMargin(showVolume, showRSI, showMACD)
                }
                borderVisible = false
                visible = true
            }
            
            timeScale = timeScaleOptions {
                borderVisible = false
                timeVisible = true // 지표 차트에서는 시간 표시
                secondsVisible = false
            }
            
            crosshair = crosshairOptions {
                // Default crosshair settings
            }
        }
        
        // 1. 거래량 시리즈 (최상단)
        if (showVolume && volumeData.isNotEmpty()) {
            val volumeWithColors = volumeData.mapIndexed { index, volume ->
                val candle = candlestickData.getOrNull(index)
                val isRising = candle?.let { it.close >= it.open } ?: true
                
                com.tradingview.lightweightcharts.api.series.models.HistogramData(
                    time = ChartUtils.timestampToBusinessDay(volume.time),
                    value = volume.value,
                    color = if (isRising) {
                        Color.parseColor("#ef5350").toIntColor()
                    } else {
                        Color.parseColor("#26a69a").toIntColor()
                    }
                )
            }
            
            chartView.api.addHistogramSeries(
                options = HistogramSeriesOptions(
                    color = Color.parseColor("#64B5F6").toIntColor(),
                    priceScaleId = PriceScaleId("volume")
                ),
                onSeriesCreated = { api ->
                    volumeSeries.value = api
                    api.setData(volumeWithColors)
                    api.priceScale().applyOptions(
                        PriceScaleOptions(
                            scaleMargins = PriceScaleMargins(
                                top = 0.0f,
                                bottom = if (showRSI || showMACD) 0.7f else 0.0f
                            )
                        )
                    )
                }
            )
        }
        
        // 2. RSI 시리즈 (중간)
        if (showRSI && rsiData.isNotEmpty()) {
            chartView.api.addLineSeries(
                options = LineSeriesOptions(
                    color = Color.parseColor("#9013FE").toIntColor(),
                    lineWidth = LineWidth.TWO,
                    priceScaleId = PriceScaleId("rsi")
                ),
                onSeriesCreated = { api ->
                    rsiSeries.value = api
                    api.setData(rsiData.map { data ->
                        com.tradingview.lightweightcharts.api.series.models.LineData(
                            time = ChartUtils.timestampToBusinessDay(data.time),
                            value = data.value
                        )
                    })
                    api.priceScale().applyOptions(
                        PriceScaleOptions(
                            scaleMargins = PriceScaleMargins(
                                top = if (showVolume) 0.35f else 0.0f,
                                bottom = if (showMACD) 0.35f else 0.0f
                            )
                        )
                    )
                }
            )
        }
        
        // 3. MACD 시리즈 (하단)
        if (showMACD && macdData != null) {
            // MACD 라인
            chartView.api.addLineSeries(
                options = LineSeriesOptions(
                    color = Color.parseColor("#4CAF50").toIntColor(),
                    lineWidth = LineWidth.TWO,
                    priceScaleId = PriceScaleId("macd")
                ),
                onSeriesCreated = { api ->
                    macdSeries.value = api
                    api.setData(macdData.macdLine.map { data ->
                        com.tradingview.lightweightcharts.api.series.models.LineData(
                            time = ChartUtils.timestampToBusinessDay(data.time),
                            value = data.value
                        )
                    })
                    api.priceScale().applyOptions(
                        PriceScaleOptions(
                            scaleMargins = PriceScaleMargins(
                                top = when {
                                    showVolume && showRSI -> 0.7f
                                    showVolume || showRSI -> 0.35f
                                    else -> 0.0f
                                },
                                bottom = 0.0f
                            )
                        )
                    )
                }
            )
            
            // Signal 라인
            chartView.api.addLineSeries(
                options = LineSeriesOptions(
                    color = Color.parseColor("#F44336").toIntColor(),
                    lineWidth = LineWidth.TWO,
                    priceScaleId = PriceScaleId("macd")
                ),
                onSeriesCreated = { api ->
                    macdSignalSeries.value = api
                    api.setData(macdData.signalLine.map { data ->
                        com.tradingview.lightweightcharts.api.series.models.LineData(
                            time = ChartUtils.timestampToBusinessDay(data.time),
                            value = data.value
                        )
                    })
                }
            )
            
            // 히스토그램
            if (macdData.histogram.isNotEmpty()) {
                chartView.api.addHistogramSeries(
                    options = HistogramSeriesOptions(
                        color = Color.parseColor("#42A5F5").toIntColor(),
                        priceScaleId = PriceScaleId("macd")
                    ),
                    onSeriesCreated = { api ->
                        macdHistogramSeries.value = api
                        api.setData(macdData.histogram.map { data ->
                            com.tradingview.lightweightcharts.api.series.models.HistogramData(
                                time = ChartUtils.timestampToBusinessDay(data.time),
                                value = data.value
                            )
                        })
                    }
                )
            }
        }
        
    } catch (e: Exception) {
        // 오류 무시 - 차트는 계속 표시
    }
}

// 지표 개수에 따른 하단 마진 계산
private fun calculateIndicatorBottomMargin(showVolume: Boolean, showRSI: Boolean, showMACD: Boolean): Float {
    var margin = 0.1f // 기본 마진
    
    if (showVolume) margin += 0.2f
    if (showRSI) margin += 0.2f  
    if (showMACD) margin += 0.2f
    
    return margin.coerceAtMost(0.8f) // 최대 80%까지만
}

/**
 * 두 차트 간 시간축 동기화 설정
 */
private fun setupTimeRangeSync(
    mainChart: ChartsView,
    indicatorChart: ChartsView,
    setUpdating: (Boolean) -> Unit
) {
    try {
        var isMainUpdating = false
        var isIndicatorUpdating = false
        
        // 메인 차트의 시간 범위 변경을 지표 차트에 동기화
        mainChart.api.timeScale.subscribeVisibleTimeRangeChange { timeRange ->
            if (timeRange != null && !isIndicatorUpdating) {
                isMainUpdating = true
                try {
                    indicatorChart.api.timeScale.setVisibleRange(timeRange)
                } catch (e: Exception) {
                    // 동기화 실패 시 무시
                } finally {
                    isMainUpdating = false
                }
            }
        }
        
        // 지표 차트의 시간 범위 변경을 메인 차트에 동기화
        indicatorChart.api.timeScale.subscribeVisibleTimeRangeChange { timeRange ->
            if (timeRange != null && !isMainUpdating) {
                isIndicatorUpdating = true
                try {
                    mainChart.api.timeScale.setVisibleRange(timeRange)
                } catch (e: Exception) {
                    // 동기화 실패 시 무시
                } finally {
                    isIndicatorUpdating = false
                }
            }
        }
        
    } catch (e: Exception) {
        // 동기화 설정 실패 시 무시
    }
}