package com.lago.app.presentation.ui.chart.v5

import android.webkit.JavascriptInterface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.lago.app.presentation.ui.chart.WebChartScreen
import com.lago.app.domain.entity.TradingSignal
import com.lago.app.domain.entity.SignalType
import com.lago.app.domain.entity.SignalSource
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

/**
 * TradingView v5 Multi-Panel Chart for LAGO
 * Native multi-panel support with technical indicators
 */

@Serializable
data class JSMarker(
    val time: String,
    val position: String, // "belowBar" | "aboveBar"
    val shape: String, // "arrowUp" | "arrowDown" | "circle" | "square" 
    val color: String,
    val id: String,
    val text: String,
    val size: Int = 1
)

@Serializable
data class ChartData(
    val time: String,
    val value: Double
)

@Serializable
data class CandlestickData(
    val time: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double
)

@Serializable
data class MultiPanelData(
    val priceData: List<CandlestickData>,
    val indicators: List<IndicatorData> = emptyList(),
    val bollingerBands: BollingerBandsData? = null,
    val sma5Data: List<ChartData>? = null,
    val sma20Data: List<ChartData>? = null,
    val macdData: MACDChartData? = null
)

@Serializable
data class BollingerBandsData(
    val upperBand: List<ChartData>,
    val middleBand: List<ChartData>,
    val lowerBand: List<ChartData>
)

@Serializable
data class MACDChartData(
    val macdLine: List<ChartData>,
    val signalLine: List<ChartData>,
    val histogram: List<ChartData>
)

@Serializable
data class IndicatorData(
    val type: IndicatorType,
    val name: String,
    val data: List<ChartData>,
    val options: IndicatorOptions = IndicatorOptions()
)

@Serializable
data class IndicatorOptions(
    val color: String = "#2962FF",
    val lineWidth: Int = 2,
    val height: Int = 150,
    val visible: Boolean = true,
    val precision: Int = 2
)

@Serializable
data class ChartOptions(
    val width: Int = 600,
    val height: Int = 300,
    val layout: LayoutOptions = LayoutOptions(),
    val grid: GridOptions = GridOptions(),
    val priceScale: PriceScaleOptions = PriceScaleOptions(),
    val timeScale: TimeScaleOptions = TimeScaleOptions()
)

@Serializable
data class LayoutOptions(
    val backgroundColor: String = "#FFFFFF",
    val textColor: String = "#333333",
    val fontSize: Int = 12,
    val fontFamily: String = "Trebuchet MS, Roboto, Ubuntu, sans-serif"
)

@Serializable
data class GridOptions(
    val vertLines: LineOptions = LineOptions(),
    val horzLines: LineOptions = LineOptions()
)

@Serializable
data class LineOptions(
    val color: String = "#E6E6E6",
    val visible: Boolean = true
)

@Serializable
data class PriceScaleOptions(
    val position: String = "right",
    val mode: Int = 0,
    val autoScale: Boolean = true,
    val borderVisible: Boolean = true
)

@Serializable
data class TimeScaleOptions(
    val timeVisible: Boolean = true,
    val secondsVisible: Boolean = false,
    val borderVisible: Boolean = true,
    val rightOffset: Int = 0,
    val barSpacing: Int = 6,
    val tickMarkFormatter: String? = null // JavaScript function as string for custom formatting
)

enum class IndicatorType {
    RSI, MACD, VOLUME, SMA5, SMA20, BOLLINGER_BANDS
}

/**
 * Date formatting helper for TradingView chart compatibility
 */
private fun formatDateForChart(date: Date): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return formatter.format(date)
}

/**
 * LAGO v5 Multi-Panel Chart Component
 */
@Composable
fun MultiPanelChart(
    data: MultiPanelData,
    timeFrame: String = "D",
    chartOptions: ChartOptions = ChartOptions(),
    tradingSignals: List<TradingSignal> = emptyList(),
    modifier: Modifier = Modifier,
    onChartReady: (() -> Unit)? = null,
    onDataPointClick: ((String, Double, String) -> Unit)? = null,
    onCrosshairMove: ((String?, Double?, String?) -> Unit)? = null,
    onChartLoading: ((Boolean) -> Unit)? = null,
    onLoadingProgress: ((Int) -> Unit)? = null
) {
    
    // Create chart options with timeFrame-specific timeScale
    val finalChartOptions = remember(timeFrame, chartOptions) {
        val timeScaleOptions = DataConverter.createTimeScaleOptions(timeFrame)
        chartOptions.copy(timeScale = timeScaleOptions)
    }
    
    // Generate HTML content with embedded JavaScript
    val htmlContent = remember(data, finalChartOptions, tradingSignals) {
        generateMultiPanelHtml(data, finalChartOptions, tradingSignals)
    }
    
    // Use WebChartScreen with dark mode optimization
    WebChartScreen(
        htmlContent = htmlContent,
        modifier = modifier,
        onChartReady = onChartReady,
        onChartLoading = onChartLoading,
        onLoadingProgress = onLoadingProgress,
        additionalJavaScriptInterface = MultiPanelJavaScriptInterface(
            onDataPointClick = onDataPointClick,
            onCrosshairMove = onCrosshairMove
        ),
        interfaceName = "ChartInterface"
    )
}

/**
 * JavaScript interface for handling multi-panel chart events
 * Note: Basic chart ready/loading events are handled by WebChartScreen
 */
class MultiPanelJavaScriptInterface(
    private val onDataPointClick: ((String, Double, String) -> Unit)?,
    private val onCrosshairMove: ((String?, Double?, String?) -> Unit)?
) {
    @JavascriptInterface
    fun onDataPointClicked(time: String, value: Double, panelId: String) {
        onDataPointClick?.invoke(time, value, panelId)
    }
    
    @JavascriptInterface
    fun onCrosshairMoved(time: String?, value: Double?, panelId: String?) {
        onCrosshairMove?.invoke(time, value, panelId)
    }
}

/**
 * Generates HTML content with TradingView Lightweight Charts v5 using native addPane API
 */
private fun generateMultiPanelHtml(
    data: MultiPanelData,
    options: ChartOptions,
    tradingSignals: List<TradingSignal> = emptyList()
): String {
    val json = Json { ignoreUnknownKeys = true }
    
    val priceDataJson = json.encodeToString(data.priceData)
    val indicatorsJson = json.encodeToString(data.indicators)
    val optionsJson = json.encodeToString(options)
    val bollingerBandsJson = json.encodeToString(data.bollingerBands)
    val sma5DataJson = json.encodeToString(data.sma5Data)
    val sma20DataJson = json.encodeToString(data.sma20Data)
    
    // Base64로 인코딩하여 안전하게 전달
    val macdDataJson = json.encodeToString(data.macdData)
    
    // TradingSignal을 JavaScript 마커 형식으로 변환
    val jsMarkers = tradingSignals.map { signal ->
        JSMarker(
            time = formatDateForChart(signal.timestamp),
            position = if (signal.signalType == SignalType.BUY) "belowBar" else "aboveBar",
            shape = when {
                signal.signalSource == SignalSource.USER && signal.signalType == SignalType.BUY -> "arrowUp"
                signal.signalSource == SignalSource.USER && signal.signalType == SignalType.SELL -> "arrowDown"
                signal.signalSource == SignalSource.AI_BLUE -> "circle"
                signal.signalSource == SignalSource.AI_GREEN -> "square"
                signal.signalSource == SignalSource.AI_RED -> "circle"
                signal.signalSource == SignalSource.AI_YELLOW -> "square"
                else -> "circle"
            },
            color = when (signal.signalSource) {
                SignalSource.USER -> if (signal.signalType == SignalType.BUY) "#FF99C5" else "#42A6FF" // LAGO 색상
                SignalSource.AI_BLUE -> "#007BFF"
                SignalSource.AI_GREEN -> "#28A745"
                SignalSource.AI_RED -> "#DC3545"
                SignalSource.AI_YELLOW -> "#FFC107"
            },
            id = signal.id,
            text = signal.message ?: "${signal.signalSource.displayName} ${if (signal.signalType == SignalType.BUY) "매수" else "매도"}",
            size = 1
        )
    }
    val tradingSignalsJson = json.encodeToString(jsMarkers)
    
    val priceDataBase64 = android.util.Base64.encodeToString(priceDataJson.toByteArray(), android.util.Base64.NO_WRAP)
    val indicatorsBase64 = android.util.Base64.encodeToString(indicatorsJson.toByteArray(), android.util.Base64.NO_WRAP)
    val optionsBase64 = android.util.Base64.encodeToString(optionsJson.toByteArray(), android.util.Base64.NO_WRAP)
    val bollingerBandsBase64 = android.util.Base64.encodeToString(bollingerBandsJson.toByteArray(), android.util.Base64.NO_WRAP)
    val sma5DataBase64 = android.util.Base64.encodeToString(sma5DataJson.toByteArray(), android.util.Base64.NO_WRAP)
    val sma20DataBase64 = android.util.Base64.encodeToString(sma20DataJson.toByteArray(), android.util.Base64.NO_WRAP)
    val macdDataBase64 = android.util.Base64.encodeToString(macdDataJson.toByteArray(), android.util.Base64.NO_WRAP)
    val tradingSignalsBase64 = android.util.Base64.encodeToString(tradingSignalsJson.toByteArray(), android.util.Base64.NO_WRAP)
    
    return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        body {
            margin: 0;
            padding: 0;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
            background-color: #FFFFFF;
        }
        /* Hide TradingView logo */
        a#tv-attr-logo {
            display: none !important;
            visibility: hidden !important;
            opacity: 0 !important;
        }
        .legend {
            position: absolute;
            top: 4px;
            left: 4px;
            background: rgba(255, 255, 255, 0.85);
            padding: 3px 6px;
            border-radius: 3px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.1);
            font-size: 9px;
            font-weight: 400;
            color: #555;
            z-index: 1000;
            pointer-events: none;
            font-family: 'Segoe UI', -apple-system, sans-serif;
            line-height: 1.2;
        }
        .legend-item {
            display: inline-block;
            margin-right: 6px;
            margin-bottom: 1px;
        }
        .legend-color {
            display: inline-block;
            width: 8px;
            height: 1.5px;
            margin-right: 3px;
            vertical-align: middle;
        }
        #chart-container {
            width: 100%;
            height: 100vh;
            background-color: #FFFFFF;
        }
        #loading {
            display: none;
        }
    </style>
</head>
<body>
    <div id="loading">Loading LAGO Chart v5...</div>
    <div id="chart-container" style="display: none;"></div>
    
    <script>
        // Base64 디코딩 및 JSON 파싱
        function decodeBase64(base64) {
            return atob(base64);
        }
        
        // 스크립트 동적 로딩
        function loadScript(src, callback) {
            const script = document.createElement('script');
            script.src = src;
            script.onload = callback;
            script.onerror = function() {
                console.error('Failed to load script:', src);
                if (typeof Android !== 'undefined' && Android.onChartError) {
                    Android.onChartError('Failed to load chart library');
                }
            };
            document.head.appendChild(script);
        }
        
        // 전역 변수
        let chart;
        let panes = [];
        let series = [];
        
        // TradingView Lightweight Charts v5 라이브러리 로드 (CDN + CSS로 로고 숨김)
        loadScript('https://unpkg.com/lightweight-charts/dist/lightweight-charts.standalone.production.js', function() {
            initLAGOMultiPanelChart();
        });
        
        function initLAGOMultiPanelChart() {
            try {
                // 로딩 화면 숨기고 차트 컨테이너 표시
                document.getElementById('loading').style.display = 'none';
                document.getElementById('chart-container').style.display = 'block';
                
                // Base64에서 디코딩 후 JSON 파싱
                const priceDataJson = decodeBase64('$priceDataBase64');
                const indicatorsJson = decodeBase64('$indicatorsBase64');
                const optionsJson = decodeBase64('$optionsBase64');
                
                const priceData = JSON.parse(priceDataJson);
                const indicators = JSON.parse(indicatorsJson);
                const chartOptions = JSON.parse(optionsJson);
                
                console.log('LAGO Price data:', priceData);
                console.log('LAGO Indicators:', indicators);
                
                // LAGO 테마 색상 적용
                chart = LightweightCharts.createChart(
                    document.getElementById('chart-container'),
                    {
                        width: window.innerWidth,
                        height: window.innerHeight,
                        layout: {
                            backgroundColor: '#FFFFFF',
                            textColor: '#333333',
                            fontSize: 12,
                        },
                        grid: {
                            vertLines: {
                                color: '#e1e1e1',
                            },
                            horzLines: {
                                color: '#e1e1e1',
                            },
                        },
                        priceScale: {
                            position: 'right',
                            borderVisible: false,
                            // 한국 주식 가격 포맷 (소숫점 없음)
                            mode: LightweightCharts.PriceScaleMode.Normal,
                            autoScale: true,
                        },
                        timeScale: {
                            borderVisible: chartOptions.timeScale?.borderVisible ?? false,
                            timeVisible: chartOptions.timeScale?.timeVisible ?? true,
                            secondsVisible: chartOptions.timeScale?.secondsVisible ?? false,
                            rightOffset: chartOptions.timeScale?.rightOffset ?? 0,
                            barSpacing: chartOptions.timeScale?.barSpacing ?? 6,
                            tickMarkFormatter: chartOptions.timeScale?.tickMarkFormatter ? 
                                eval('(' + chartOptions.timeScale.tickMarkFormatter + ')') : undefined,
                        },
                    }
                );
                
                // 메인 캔들스틱 시리즈 추가 (LAGO 색상 테마 + 한국 주식 정수 포맷)
                const mainSeries = chart.addSeries(LightweightCharts.CandlestickSeries, {
                    upColor: '#FF99C5',      // LAGO MainPink
                    downColor: '#42A6FF',    // LAGO MainBlue  
                    borderVisible: false,
                    wickUpColor: '#FF99C5',
                    wickDownColor: '#42A6FF',
                    priceFormat: {
                        type: 'price',
                        precision: 0,        // 소숫점 0자리 (정수만)
                        minMove: 1,          // 최소 이동 단위 1원
                    },
                });
                
                mainSeries.setData(priceData);
                series.push({ series: mainSeries, name: 'OHLC', paneIndex: 0 });
                
                // 매수/매도 신호 마커 시스템 초기화
                let markersApi = null;
                
                // Android에서 호출할 수 있는 매수/매도 신호 설정 함수
                window.setTradeMarkers = function(markersJson) {
                    try {
                        const markers = JSON.parse(markersJson);
                        console.log('LAGO: Setting', markers.length, 'trade markers');
                        
                        if (!markersApi && markers.length > 0) {
                            // 첫 번째 마커 생성
                            markersApi = LightweightCharts.createSeriesMarkers(mainSeries, markers);
                        } else if (markersApi) {
                            // 기존 마커 업데이트
                            markersApi.setMarkers(markers);
                        }
                        
                        console.log('✅ Trade markers updated successfully');
                    } catch (error) {
                        console.error('❌ Failed to set trade markers:', error);
                    }
                };
                
                // 마커 제거 함수
                window.clearTradeMarkers = function() {
                    if (markersApi) {
                        markersApi.setMarkers([]);
                        console.log('Trade markers cleared');
                    }
                };
                
                // 초기 매수/매도 신호 적용
                try {
                    const tradingSignalsData = JSON.parse(decodeBase64('$tradingSignalsBase64'));
                    console.log('LAGO: Initial trading signals loaded:', tradingSignalsData.length);
                    
                    if (tradingSignalsData && tradingSignalsData.length > 0) {
                        // createSeriesMarkers API로 초기 마커 생성
                        markersApi = LightweightCharts.createSeriesMarkers(mainSeries, tradingSignalsData);
                        console.log('✅ Initial trade markers created successfully');
                        
                        // 마커 요약 정보 로깅
                        const buyCount = tradingSignalsData.filter(m => m.position === 'belowBar').length;
                        const sellCount = tradingSignalsData.filter(m => m.position === 'aboveBar').length;
                        console.log('  📊 Buy signals: ' + buyCount + ', Sell signals: ' + sellCount);
                    }
                } catch (error) {
                    console.error('❌ Failed to load initial trade markers:', error);
                }
                
                // 메인 패널 레전드 항목들 (OHLC는 제외)
                const mainLegendItems = [];
                
                // 메인 패널 레전드 항목들은 위에서 이미 선언됨
                
                // 볼린저밴드를 메인 차트에 오버레이로 추가
                const bollingerBandsData = JSON.parse(decodeBase64('$bollingerBandsBase64'));
                if (bollingerBandsData && bollingerBandsData.upperBand && bollingerBandsData.upperBand.length > 0) {
                    // 상단 밴드 (정수 포맷)
                    const upperBandSeries = chart.addSeries(LightweightCharts.LineSeries, {
                        color: '#9E9E9E',
                        lineWidth: 1,
                        lineStyle: LightweightCharts.LineStyle.Dashed,
                        priceFormat: {
                            type: 'price',
                            precision: 0,
                            minMove: 1,
                        },
                    });
                    upperBandSeries.setData(bollingerBandsData.upperBand);
                    
                    // 중간 밴드 (SMA) (정수 포맷)
                    const middleBandSeries = chart.addSeries(LightweightCharts.LineSeries, {
                        color: '#FF9800',
                        lineWidth: 2,
                        priceFormat: {
                            type: 'price',
                            precision: 0,
                            minMove: 1,
                        },
                    });
                    middleBandSeries.setData(bollingerBandsData.middleBand);
                    
                    // 하단 밴드 (정수 포맷)
                    const lowerBandSeries = chart.addSeries(LightweightCharts.LineSeries, {
                        color: '#9E9E9E',
                        lineWidth: 1,
                        lineStyle: LightweightCharts.LineStyle.Dashed,
                        priceFormat: {
                            type: 'price',
                            precision: 0,
                            minMove: 1,
                        },
                    });
                    lowerBandSeries.setData(bollingerBandsData.lowerBand);
                    
                    series.push({ series: upperBandSeries, name: 'BB상한', paneIndex: 0, color: '#9E9E9E' });
                    series.push({ series: middleBandSeries, name: 'BB중심', paneIndex: 0, color: '#FF9800' });
                    series.push({ series: lowerBandSeries, name: 'BB하한', paneIndex: 0, color: '#9E9E9E' });
                    mainLegendItems.push('볼린저밴드');
                }
                
                // SMA5를 메인 차트에 오버레이로 추가 (정수 포맷)
                const sma5DataDecoded = JSON.parse(decodeBase64('$sma5DataBase64'));
                if (sma5DataDecoded && sma5DataDecoded.length > 0) {
                    const sma5Series = chart.addSeries(LightweightCharts.LineSeries, {
                        color: '#FF5722',
                        lineWidth: 2,
                        priceFormat: {
                            type: 'price',
                            precision: 0,
                            minMove: 1,
                        },
                    });
                    sma5Series.setData(sma5DataDecoded);
                    series.push({ series: sma5Series, name: 'SMA5', paneIndex: 0, color: '#FF5722' });
                    mainLegendItems.push('5일선');
                }
                
                // SMA20을 메인 차트에 오버레이로 추가 (정수 포맷)
                const sma20DataDecoded = JSON.parse(decodeBase64('$sma20DataBase64'));
                if (sma20DataDecoded && sma20DataDecoded.length > 0) {
                    const sma20Series = chart.addSeries(LightweightCharts.LineSeries, {
                        color: '#4CAF50',
                        lineWidth: 2,
                        priceFormat: {
                            type: 'price',
                            precision: 0,
                            minMove: 1,
                        },
                    });
                    sma20Series.setData(sma20DataDecoded);
                    series.push({ series: sma20Series, name: 'SMA20', paneIndex: 0, color: '#4CAF50' });
                    mainLegendItems.push('20일선');
                }
                
                // 메인 패널 레전드 생성 (지표가 있을 때만)
                if (mainLegendItems.length > 0) {
                    createPaneLegend(0, mainLegendItems);
                }
                
                // 보조지표용 패널들 추가
                console.log('LAGO: Creating', indicators.length, 'indicator panels');
                indicators.forEach((indicator, index) => {
                    console.log('LAGO: Processing indicator:', indicator.type, indicator.name);
                    createLAGOIndicatorPane(indicator, index + 1, priceData);
                });
                
                // 패널 높이 조정
                adjustPaneHeights();
                
                // 이벤트 핸들러 추가
                setupEventHandlers();
                
                // 자동 리사이즈
                setupAutoResize();
                
                // 전역 접근을 위한 노출
                window.lightweightChart = chart;
                window.chartPanes = panes;
                window.chartSeries = series;
                
                // 전체 레전드 시스템 초기화 (렌더링 완료 후)
                setTimeout(() => {
                    console.log('\\n=== Initializing All Legends ===');
                    console.log('Chart container display:', document.getElementById('chart-container').style.display);
                    console.log('Total panes:', chart.panes().length);
                    
                    // 모든 Canvas 요소 확인
                    const allCanvases = document.querySelectorAll('canvas');
                    console.log('Total canvas elements found:', allCanvases.length);
                    allCanvases.forEach((canvas, idx) => {
                        const rect = canvas.getBoundingClientRect();
                        console.log('Canvas ' + idx + ':', 'size=' + rect.width + 'x' + rect.height, 'pos=(' + rect.left + ',' + rect.top + ')');
                    });
                    
                    // 메인 패널 레전드 강제 재생성
                    if (mainLegendItems.length > 0) {
                        console.log('Creating main panel legend with items:', mainLegendItems);
                        createSimpleLegend(0, mainLegendItems);
                    }
                    
                    // 보조지표 레전드 강제 재생성
                    indicators.forEach((indicator, index) => {
                        const paneIndex = index + 1;
                        let legendItems = [];
                        
                        switch (indicator.type.toLowerCase()) {
                            case 'macd':
                                legendItems = ['MACD (12,26,9)'];
                                break;
                            case 'rsi':
                                legendItems = ['RSI'];
                                break;
                            case 'volume':
                                legendItems = ['거래량'];
                                break;
                        }
                        
                        if (legendItems.length > 0) {
                            console.log('Creating legend for pane ' + paneIndex + ' with items:', legendItems);
                            createSimpleLegend(paneIndex, legendItems);
                        }
                    });
                    
                }, 2500);
                
                console.log('LAGO Multi-Panel Chart v5 initialized successfully');
                
                // ✅ 빠른 차트 로딩: Android 신호를 빠르게 전송
                setTimeout(() => {
                    if (typeof Android !== 'undefined' && Android.onChartReady) {
                        console.log('LAGO: Chart ready - sending Android.onChartReady signal');
                        Android.onChartReady();
                    }
                }, 100); // 로딩 시간 대폭 단축
                
            } catch (error) {
                console.error('LAGO Multi-panel chart initialization error:', error);
                document.getElementById('loading').innerHTML = 'Chart initialization failed: ' + error.message;
                document.getElementById('loading').style.display = 'block';
                document.getElementById('chart-container').style.display = 'none';
                
                // 에러 발생 시에도 Android에 신호
                if (typeof Android !== 'undefined' && Android.onChartError) {
                    Android.onChartError(error.message);
                }
            }
        }
        
        function createLAGOIndicatorPane(indicator, paneIndex, candleData) {
            // v5 네이티브 API로 새 패널 추가
            const pane = chart.addPane(true);
            panes.push(pane);
            
            let indicatorSeries;
            
            // LAGO 보조지표 색상 테마
            const lagoColors = {
                rsi: '#9C27B0',
                macd: '#2196F3',
                volume: '#FF9800',
                sma5: '#FF5722',
                sma20: '#4CAF50',
                bollinger_bands: '#607D8B'
            };
            
            // 보조지표 타입에 따른 시리즈 생성
            switch (indicator.type.toLowerCase()) {
                case 'rsi':
                    indicatorSeries = chart.addSeries(LightweightCharts.LineSeries, {
                        color: lagoColors.rsi,
                        lineWidth: 2,
                    }, pane.paneIndex());
                    
                    // RSI 기준선 추가 (70, 30)
                    const rsiRef70 = chart.addSeries(LightweightCharts.LineSeries, {
                        color: '#666666',
                        lineWidth: 1,
                        lineStyle: LightweightCharts.LineStyle.Dashed,
                    }, pane.paneIndex());
                    
                    const rsiRef30 = chart.addSeries(LightweightCharts.LineSeries, {
                        color: '#666666',
                        lineWidth: 1,
                        lineStyle: LightweightCharts.LineStyle.Dashed,
                    }, pane.paneIndex());
                    
                    if (indicator.data.length > 0) {
                        const startTime = indicator.data[0].time;
                        const endTime = indicator.data[indicator.data.length - 1].time;
                        rsiRef70.setData([
                            { time: startTime, value: 70 },
                            { time: endTime, value: 70 }
                        ]);
                        rsiRef30.setData([
                            { time: startTime, value: 30 },
                            { time: endTime, value: 30 }
                        ]);
                    }
                    
                    // RSI 패널 레전드 생성
                    createPaneLegend(pane.paneIndex(), ['RSI']);
                    break;
                    
                case 'macd':
                    console.log('LAGO: Creating MACD panel with full data');
                    
                    // MACD 전체 데이터 가져오기
                    const macdFullData = JSON.parse(decodeBase64('$macdDataBase64'));
                    
                    if (macdFullData) {
                        // MACD Line (파란색)
                        const macdLineSeries = chart.addSeries(LightweightCharts.LineSeries, {
                            color: '#2196F3',
                            lineWidth: 1,
                            priceScaleId: 'macd',
                        }, pane.paneIndex());
                        macdLineSeries.setData(macdFullData.macdLine || []);
                        
                        // Signal Line (빨간색)
                        const signalLineSeries = chart.addSeries(LightweightCharts.LineSeries, {
                            color: '#FF5722',
                            lineWidth: 1,
                            priceScaleId: 'macd',
                        }, pane.paneIndex());
                        signalLineSeries.setData(macdFullData.signalLine || []);
                        
                        // Histogram (색상별로 표시)
                        const histogramDataWithColors = (macdFullData.histogram || []).map(point => ({
                            time: point.time,
                            value: point.value,
                            color: point.value >= 0 ? '#FF99C5' : '#42A6FF' // MainPink : MainBlue
                        }));
                        
                        const histogramSeries = chart.addSeries(LightweightCharts.HistogramSeries, {
                            priceFormat: {
                                type: 'price',
                                precision: 4,
                                minMove: 0.0001,
                            },
                            priceScaleId: 'macd',
                        }, pane.paneIndex());
                        histogramSeries.setData(histogramDataWithColors);
                        
                        // 제로 라인
                        const zeroLine = chart.addSeries(LightweightCharts.LineSeries, {
                            color: '#666666',
                            lineWidth: 1,
                            lineStyle: LightweightCharts.LineStyle.Dashed,
                            priceScaleId: 'macd',
                        }, pane.paneIndex());
                        
                        if (macdFullData.macdLine && macdFullData.macdLine.length > 0) {
                            const startTime = macdFullData.macdLine[0].time;
                            const endTime = macdFullData.macdLine[macdFullData.macdLine.length - 1].time;
                            zeroLine.setData([
                                { time: startTime, value: 0 },
                                { time: endTime, value: 0 }
                            ]);
                        }
                        
                        // MACD 패널 레전드 생성
                        createPaneLegend(pane.paneIndex(), ['MACD (12,26,9)']);
                        
                        // 메인 시리즈는 히스토그램으로 설정
                        indicatorSeries = histogramSeries;
                    }
                    break;
                    
                case 'volume':
                    // 거래량 데이터에 캔들 색상 매핑
                    const volumeDataWithColors = indicator.data.map((vol, index) => {
                        const priceCandle = candleData[index];
                        let color = '#FF99C5'; // 기본값 MainPink
                        
                        if (priceCandle && priceCandle.close !== undefined && priceCandle.open !== undefined) {
                            color = priceCandle.close >= priceCandle.open ? '#FF99C5' : '#42A6FF'; // MainPink : MainBlue
                        }
                        
                        return {
                            time: vol.time,
                            value: vol.value,
                            color: color
                        };
                    });
                    
                    indicatorSeries = chart.addSeries(LightweightCharts.HistogramSeries, {
                        priceFormat: {
                            type: 'volume',
                        },
                    }, pane.paneIndex());
                    
                    indicatorSeries.setData(volumeDataWithColors);
                    
                    // 거래량 패널 레전드 생성
                    createPaneLegend(pane.paneIndex(), ['거래량']);
                    break;
                    
                case 'sma5':
                    indicatorSeries = chart.addSeries(LightweightCharts.LineSeries, {
                        color: lagoColors.sma5,
                        lineWidth: 2,
                    }, pane.paneIndex());
                    break;
                    
                case 'sma20':
                    indicatorSeries = chart.addSeries(LightweightCharts.LineSeries, {
                        color: lagoColors.sma20,
                        lineWidth: 2,
                    }, pane.paneIndex());
                    break;
                    
                case 'bollinger_bands':
                    indicatorSeries = chart.addSeries(LightweightCharts.LineSeries, {
                        color: lagoColors.bollinger_bands,
                        lineWidth: 2,
                    }, pane.paneIndex());
                    break;
                    
                default:
                    indicatorSeries = chart.addSeries(LightweightCharts.LineSeries, {
                        color: indicator.options.color || '#2962FF',
                        lineWidth: indicator.options.lineWidth || 2,
                    }, pane.paneIndex());
            }
            
            // volume과 macd의 경우 이미 setData가 호출되었으므로 건너뜀
            if (indicator.type.toLowerCase() !== 'volume' && indicator.type.toLowerCase() !== 'macd') {
                indicatorSeries.setData(indicator.data);
            }
            
            series.push({
                series: indicatorSeries,
                name: indicator.name,
                paneIndex: pane.paneIndex(),
                type: indicator.type,
                color: getDefaultColor(indicator.name)
            });
        }
        
        function adjustPaneHeights() {
            const allPanes = chart.panes();
            const totalPanes = allPanes.length;
            
            if (totalPanes === 1) {
                return;
            }
            
            // LAGO 최적화: 메인 패널 70%, 보조지표 30% 균등 분할
            const mainPaneStretch = 350; // 메인 차트 비중 증가
            const indicatorPaneStretch = totalPanes > 1 ? 80 : 50;
            
            allPanes.forEach((pane, index) => {
                if (index === 0) {
                    // 메인 패널
                    pane.setStretchFactor(mainPaneStretch);
                } else {
                    // 보조지표 패널
                    pane.setStretchFactor(indicatorPaneStretch);
                }
            });
        }
        
        function setupEventHandlers() {
            chart.subscribeCrosshairMove(param => {
                try {
                    if (typeof ChartInterface !== 'undefined' && ChartInterface.onCrosshairMoved) {
                        const time = param.time ? param.time.toString() : null;
                        const mainSeriesData = series.find(s => s.paneIndex === 0);
                        const value = param.seriesData && mainSeriesData && param.seriesData.get(mainSeriesData.series) 
                            ? param.seriesData.get(mainSeriesData.series) 
                            : null;
                        ChartInterface.onCrosshairMoved(time, value, 'lago-multi-panel');
                    }
                } catch (e) {
                    console.error('LAGO Crosshair event error:', e);
                }
            });
            
            chart.subscribeClick(param => {
                try {
                    if (typeof ChartInterface !== 'undefined' && ChartInterface.onDataPointClicked && param.time && param.seriesData) {
                        const mainSeriesData = series.find(s => s.paneIndex === 0);
                        if (mainSeriesData) {
                            const value = param.seriesData.get(mainSeriesData.series);
                            if (value !== undefined) {
                                ChartInterface.onDataPointClicked(param.time.toString(), value, 'lago-multi-panel');
                            }
                        }
                    }
                } catch (e) {
                    console.error('LAGO Click event error:', e);
                }
            });
        }
        
        function setupAutoResize() {
            function resizeChart() {
                chart.applyOptions({
                    width: window.innerWidth,
                    height: window.innerHeight
                });
            }
            
            window.addEventListener('resize', resizeChart);
            resizeChart();
        }
        
        // 완전히 새로운 접근: TradingView 레전드 시스템 사용
        function createPaneLegend(paneIndex, items) {
            console.log('\\n=== Creating Legend for Panel ' + paneIndex + ' ===');
            console.log('Items:', items);
            
            // 기존 레전드 시스템 제거 후 새 방식 사용
            const existingLegends = document.querySelectorAll('[id^="legend-pane-"]');
            existingLegends.forEach(legend => legend.remove());
            
            // 충분한 시간 후 레전드 생성 (DOM과 Canvas가 완전히 로드된 후)
            setTimeout(() => {
                createSimpleLegend(paneIndex, items);
            }, 2000);
        }
        
        // 단순하고 효과적인 레전드 생성 시스템
        function createSimpleLegend(paneIndex, items) {
            console.log('Creating simple legend for pane', paneIndex, 'items:', items);
            
            // 컨테이너 찾기 (chart-container 또는 container)
            let container = document.getElementById('chart-container');
            if (!container) {
                container = document.getElementById('container');
            }
            if (!container) {
                container = document.body; // 최종 대안
                console.log('Using document.body as container');
            } else {
                console.log('Using container:', container.id);
            }
            
            // Canvas 요소들을 찾아서 위치 기반으로 정렬
            const allCanvases = Array.from(container.querySelectorAll('canvas'));
            console.log('Found canvases:', allCanvases.length);
            
            if (allCanvases.length === 0) {
                console.log('No canvas elements found - using fallback strategy');
                // 폴백 전략: 컨테이너에 직접 레전드 추가
                createFallbackLegend(container, paneIndex, items);
                return;
            }
            
            // Canvas들을 Y 좌표로 그룹화하여 패널별로 정리
            const canvasGroups = {};
            allCanvases.forEach((canvas, idx) => {
                const rect = canvas.getBoundingClientRect();
                const y = Math.round(rect.top);
                
                if (!canvasGroups[y]) {
                    canvasGroups[y] = [];
                }
                canvasGroups[y].push({
                    canvas,
                    originalIndex: idx,
                    y: rect.top,
                    x: rect.left,
                    width: rect.width,
                    height: rect.height,
                    parent: canvas.parentElement
                });
            });
            
            // Y 좌표순으로 패널 그룹 정렬
            const sortedYPositions = Object.keys(canvasGroups).map(Number).sort((a, b) => a - b);
            console.log('Panel Y positions found:', sortedYPositions);
            console.log('Canvas groups per Y position:', sortedYPositions.map(y => canvasGroups[y].length));
            
            if (paneIndex >= sortedYPositions.length) {
                console.log('Panel index ' + paneIndex + ' is out of range (max: ' + (sortedYPositions.length - 1) + ')');
                return;
            }
            
            // 해당 패널의 첫 번째 Canvas를 대표로 사용
            const targetY = sortedYPositions[paneIndex];
            const targetCanvasGroup = canvasGroups[targetY];
            const targetCanvas = targetCanvasGroup[0]; // 첫 번째 Canvas 사용
            const targetParent = targetCanvas.parent;
            
            console.log('Selected panel ' + paneIndex + ' at Y=' + targetY + ' with ' + targetCanvasGroup.length + ' canvases');
            
            if (!targetParent) {
                console.log('Target parent not found');
                return;
            }
            
            // 레전드 생성
            const legendId = 'simple-legend-' + paneIndex;
            let legend = document.getElementById(legendId);
            
            if (!legend) {
                legend = document.createElement('div');
                legend.id = legendId;
                legend.className = 'simple-legend';
            }
            
            // 레전드 내용 생성
            if (items && items.length > 0) {
                let legendHTML = '';
                items.forEach(item => {
                    const color = getDefaultColor(item);
                    legendHTML += '<span style="color: ' + color + '; font-size: 10px; margin-right: 8px; display: inline-block;">■ ' + item + '</span>';
                });
                legend.innerHTML = legendHTML;
                console.log('Legend HTML for panel ' + paneIndex + ':', legendHTML);
            } else {
                // 빈 항목일 경우 기본 표시
                legend.innerHTML = '<span style="font-size: 10px; color: #666;">Panel ' + paneIndex + '</span>';
                console.log('Using default legend for panel ' + paneIndex);
            }
            
            // 스타일 설정 (명시적으로)
            legend.style.position = 'absolute';
            legend.style.top = '6px';
            legend.style.left = '6px';
            legend.style.zIndex = '1000';
            legend.style.fontSize = '10px';
            legend.style.fontFamily = 'Arial, sans-serif';
            legend.style.color = '#666';
            legend.style.backgroundColor = 'rgba(255, 255, 255, 0.95)';
            legend.style.padding = '4px 6px';
            legend.style.borderRadius = '4px';
            legend.style.border = '1px solid rgba(200, 200, 200, 0.5)';
            legend.style.pointerEvents = 'none';
            legend.style.whiteSpace = 'nowrap';
            legend.style.boxShadow = '0 1px 2px rgba(0,0,0,0.1)';
            legend.style.minWidth = '50px';
            legend.style.minHeight = '20px';
            legend.style.display = 'block';
            
            // 부모 요소에 relative position 설정
            if (!targetParent.style.position || targetParent.style.position === 'static') {
                targetParent.style.position = 'relative';
            }
            
            // 기존 레전드 제거 후 새 레전드 추가
            const existingLegend = targetParent.querySelector('.simple-legend');
            if (existingLegend && existingLegend !== legend) {
                existingLegend.remove();
            }
            
            targetParent.appendChild(legend);
            
            console.log('Simple legend created for panel ' + paneIndex + ' in parent:', targetParent.tagName);
            
            // 위치 검증
            setTimeout(() => {
                const legendRect = legend.getBoundingClientRect();
                const parentRect = targetParent.getBoundingClientRect();
                
                const isWithinBounds = legendRect.left >= parentRect.left && 
                                     legendRect.top >= parentRect.top && 
                                     legendRect.right <= parentRect.right && 
                                     legendRect.bottom <= parentRect.bottom;
                
                console.log('Legend position check - Panel ' + paneIndex + ': ' + (isWithinBounds ? 'OK' : 'Outside bounds'));
                console.log('  Legend: (' + Math.round(legendRect.left) + ', ' + Math.round(legendRect.top) + ') ' + Math.round(legendRect.width) + 'x' + Math.round(legendRect.height));
                console.log('  Parent: (' + Math.round(parentRect.left) + ', ' + Math.round(parentRect.top) + ') ' + Math.round(parentRect.width) + 'x' + Math.round(parentRect.height));
            }, 100);
        }
        
        // 폴백 레전드 생성 (Canvas를 찾을 수 없을 때)
        function createFallbackLegend(container, paneIndex, items) {
            console.log('Creating fallback legend for pane', paneIndex);
            
            const legendId = 'fallback-legend-' + paneIndex;
            let legend = document.getElementById(legendId);
            
            if (!legend) {
                legend = document.createElement('div');
                legend.id = legendId;
                legend.className = 'fallback-legend';
            }
            
            // 레전드 내용 생성
            if (items && items.length > 0) {
                let legendHTML = '<strong>Panel ' + paneIndex + ':</strong> ';
                items.forEach((item, idx) => {
                    const color = getDefaultColor(item);
                    if (idx > 0) legendHTML += ', ';
                    legendHTML += '<span style="color: ' + color + ';">■ ' + item + '</span>';
                });
                legend.innerHTML = legendHTML;
            }
            
            // 스타일 설정
            Object.assign(legend.style, {
                position: 'absolute',
                top: (10 + paneIndex * 25) + 'px',
                left: '10px',
                zIndex: '2000',
                fontSize: '12px',
                fontFamily: 'Arial, sans-serif',
                color: '#333',
                backgroundColor: 'rgba(255, 255, 255, 0.9)',
                padding: '4px 8px',
                borderRadius: '4px',
                border: '1px solid rgba(200, 200, 200, 0.7)',
                pointerEvents: 'none',
                whiteSpace: 'nowrap',
                boxShadow: '0 2px 4px rgba(0,0,0,0.2)'
            });
            
            // 컨테이너 위치 설정
            if (!container.style.position || container.style.position === 'static') {
                container.style.position = 'relative';
            }
            
            // 기존 폴백 레전드 제거 후 새 레전드 추가
            const existingLegend = container.querySelector('#' + legendId);
            if (existingLegend && existingLegend !== legend) {
                existingLegend.remove();
            }
            
            container.appendChild(legend);
            
            console.log('Fallback legend created for pane ' + paneIndex);
        }
        
        // 메인 패널 레전드 생성 (이동평균선, 볼린저밴드 등)
        function createMainPanelLegend() {
            const mainLegendItems = [];
            
            // 활성화된 이동평균선 및 메인 패널 지표 수집
            if (multiPanelData.sma5Data && multiPanelData.sma5Data.length > 0) {
                mainLegendItems.push('5일선');
            }
            if (multiPanelData.sma20Data && multiPanelData.sma20Data.length > 0) {
                mainLegendItems.push('20일선');
            }
            if (multiPanelData.bollingerBands) {
                mainLegendItems.push('볼린저밴드');
            }
            
            if (mainLegendItems.length > 0) {
                console.log('Creating main panel legend with items:', mainLegendItems);
                createSimpleLegend(0, mainLegendItems);
            }
        }
        
        // 기본 색상 반환 함수
        function getDefaultColor(item) {
            const colorMap = {
                '5일선': '#FF5722',
                '20일선': '#4CAF50',
                '볼린저밴드': '#FF9800',
                '거래량': '#FF9800',
                'RSI': '#9C27B0',
                'MACD (12,26,9)': '#2196F3'
            };
            return colorMap[item] || '#333333';
        }
        
    </script>
</body>
</html>
    """.trimIndent()
}