package com.lago.app.presentation.ui.chart.v5

import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.JavascriptInterface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.*

/**
 * TradingView v5 Multi-Panel Chart for LAGO
 * Native multi-panel support with technical indicators
 */

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
    val indicators: List<IndicatorData> = emptyList()
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
 * LAGO v5 Multi-Panel Chart Component
 */
@Composable
fun MultiPanelChart(
    data: MultiPanelData,
    timeFrame: String = "D",
    chartOptions: ChartOptions = ChartOptions(),
    modifier: Modifier = Modifier,
    onChartReady: (() -> Unit)? = null,
    onDataPointClick: ((String, Double, String) -> Unit)? = null,
    onCrosshairMove: ((String?, Double?, String?) -> Unit)? = null
) {
    val context = LocalContext.current
    var webView by remember { mutableStateOf<WebView?>(null) }
    
    // Create chart options with timeFrame-specific timeScale
    val finalChartOptions = remember(timeFrame, chartOptions) {
        val timeScaleOptions = DataConverter.createTimeScaleOptions(timeFrame)
        chartOptions.copy(timeScale = timeScaleOptions)
    }
    
    // Generate HTML content with embedded JavaScript
    val htmlContent = remember(data, finalChartOptions) {
        generateMultiPanelHtml(data, finalChartOptions)
    }
    
    AndroidView(
        factory = { context ->
            val webView = WebView(context)
            webView.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    allowFileAccessFromFileURLs = true
                    allowUniversalAccessFromFileURLs = true
                }
                
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }
                
                cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
            }
            
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    onChartReady?.invoke()
                }
            }
            
            // Add JavaScript interface for callbacks using reflection
            try {
                val method = webView.javaClass.getMethod(
                    "addJavascriptInterface", 
                    Any::class.java, 
                    String::class.java
                )
                method.invoke(
                    webView,
                    MultiPanelJavaScriptInterface(
                        onDataPointClick = onDataPointClick,
                        onCrosshairMove = onCrosshairMove
                    ),
                    "Android"
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            webView
        },
        update = { view ->
            view.loadDataWithBaseURL(
                "https://unpkg.com/",
                htmlContent,
                "text/html",
                "UTF-8",
                null
            )
        },
        modifier = modifier.fillMaxSize()
    )
}

/**
 * JavaScript interface for handling multi-panel chart events
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
    options: ChartOptions
): String {
    val json = Json { ignoreUnknownKeys = true }
    
    val priceDataJson = json.encodeToString(data.priceData)
    val indicatorsJson = json.encodeToString(data.indicators)
    val optionsJson = json.encodeToString(options)
    
    // Base64로 인코딩하여 안전하게 전달
    val priceDataBase64 = android.util.Base64.encodeToString(priceDataJson.toByteArray(), android.util.Base64.NO_WRAP)
    val indicatorsBase64 = android.util.Base64.encodeToString(indicatorsJson.toByteArray(), android.util.Base64.NO_WRAP)
    val optionsBase64 = android.util.Base64.encodeToString(optionsJson.toByteArray(), android.util.Base64.NO_WRAP)
    
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
        }
        #chart-container {
            width: 100%;
            height: 100vh;
        }
        #loading {
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            font-size: 18px;
            color: #666;
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
                document.getElementById('loading').innerHTML = 'Failed to load chart library';
            };
            document.head.appendChild(script);
        }
        
        // 전역 변수
        let chart;
        let panes = [];
        let series = [];
        
        // TradingView Lightweight Charts v5 라이브러리 로드 (CDN)
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
                
                // 메인 캔들스틱 시리즈 추가 (LAGO 색상 테마)
                const mainSeries = chart.addSeries(LightweightCharts.CandlestickSeries, {
                    upColor: '#FF69B4',      // LAGO MainPink
                    downColor: '#4285F4',    // LAGO MainBlue  
                    borderVisible: false,
                    wickUpColor: '#FF69B4',
                    wickDownColor: '#4285F4',
                });
                
                mainSeries.setData(priceData);
                series.push({ series: mainSeries, name: 'Price', paneIndex: 0 });
                
                // 보조지표용 패널들 추가
                indicators.forEach((indicator, index) => {
                    createLAGOIndicatorPane(indicator, index + 1);
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
                
                console.log('LAGO Multi-Panel Chart v5 initialized successfully');
                
            } catch (error) {
                console.error('LAGO Multi-panel chart initialization error:', error);
                document.getElementById('loading').innerHTML = 'Chart initialization failed: ' + error.message;
                document.getElementById('loading').style.display = 'block';
                document.getElementById('chart-container').style.display = 'none';
            }
        }
        
        function createLAGOIndicatorPane(indicator, paneIndex) {
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
                    break;
                    
                case 'macd':
                    // MACD 라인
                    indicatorSeries = chart.addSeries(LightweightCharts.LineSeries, {
                        color: lagoColors.macd,
                        lineWidth: 2,
                    }, pane.paneIndex());
                    
                    // 제로 라인
                    const zeroLine = chart.addSeries(LightweightCharts.LineSeries, {
                        color: '#666666',
                        lineWidth: 1,
                        lineStyle: LightweightCharts.LineStyle.Dashed,
                    }, pane.paneIndex());
                    
                    if (indicator.data.length > 0) {
                        const startTime = indicator.data[0].time;
                        const endTime = indicator.data[indicator.data.length - 1].time;
                        zeroLine.setData([
                            { time: startTime, value: 0 },
                            { time: endTime, value: 0 }
                        ]);
                    }
                    break;
                    
                case 'volume':
                    indicatorSeries = chart.addSeries(LightweightCharts.HistogramSeries, {
                        color: lagoColors.volume,
                    }, pane.paneIndex());
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
            
            indicatorSeries.setData(indicator.data);
            series.push({
                series: indicatorSeries,
                name: indicator.name,
                paneIndex: pane.paneIndex(),
                type: indicator.type
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
                    if (typeof Android !== 'undefined' && Android.onCrosshairMoved) {
                        const time = param.time ? param.time.toString() : null;
                        const mainSeriesData = series.find(s => s.paneIndex === 0);
                        const value = param.seriesData && mainSeriesData && param.seriesData.get(mainSeriesData.series) 
                            ? param.seriesData.get(mainSeriesData.series) 
                            : null;
                        Android.onCrosshairMoved(time, value, 'lago-multi-panel');
                    }
                } catch (e) {
                    console.error('LAGO Crosshair event error:', e);
                }
            });
            
            chart.subscribeClick(param => {
                try {
                    if (typeof Android !== 'undefined' && Android.onDataPointClicked && param.time && param.seriesData) {
                        const mainSeriesData = series.find(s => s.paneIndex === 0);
                        if (mainSeriesData) {
                            const value = param.seriesData.get(mainSeriesData.series);
                            if (value !== undefined) {
                                Android.onDataPointClicked(param.time.toString(), value, 'lago-multi-panel');
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
        
    </script>
</body>
</html>
    """.trimIndent()
}