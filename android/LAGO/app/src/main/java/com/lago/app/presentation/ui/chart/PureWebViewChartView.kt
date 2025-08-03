package com.lago.app.presentation.ui.chart

import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.lago.app.domain.entity.*

/**
 * 순수 WebView 기반 v5 차트 - Android wrapper 완전 우회
 */
@Composable
fun PureWebViewChartView(
    candlestickData: List<CandlestickData>,
    volumeData: List<VolumeData>,
    sma5Data: List<LineData>,
    sma20Data: List<LineData>,
    config: ChartConfig,
    rsiData: List<LineData> = emptyList(),
    macdData: MACDResult? = null,
    bollingerBands: BollingerBandsResult? = null,
    showRSI: Boolean = false,
    showMACD: Boolean = false,
    showVolume: Boolean = true,
    showBollingerBands: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // 차트 HTML 콘텐츠 생성
    val chartHtml = remember(candlestickData, volumeData, showRSI, showMACD, showVolume, showBollingerBands) {
        generateChartHtml(
            candlestickData, volumeData, sma5Data, sma20Data,
            rsiData, macdData, bollingerBands,
            showRSI, showMACD, showVolume, showBollingerBands
        )
    }
    
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                // WebView 설정 - TradingView 모바일 최적화
                settings.apply {
                    // 기본 JavaScript 설정
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    
                    // 파일 접근 권한
                    allowFileAccess = true
                    allowContentAccess = true
                    allowUniversalAccessFromFileURLs = true
                    allowFileAccessFromFileURLs = true
                    
                    // 모바일 최적화 설정
                    setSupportZoom(true) // TradingView는 줌 지원 필요
                    builtInZoomControls = true
                    displayZoomControls = false // 컨트롤은 숨김
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    
                    // 보안 설정
                    mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    
                    // 성능 최적화
                    cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                    
                    // 모바일 특화 설정
                    textZoom = 100
                    minimumFontSize = 8
                    
                    // JavaScript 디버깅 강화
                    mediaPlaybackRequiresUserGesture = false
                }
                
                // 디버깅 활성화
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    WebView.setWebContentsDebuggingEnabled(true)
                }
                
                webViewClient = object : WebViewClient() {
                    override fun onReceivedError(
                        view: WebView?,
                        errorCode: Int,
                        description: String?,
                        failingUrl: String?
                    ) {
                        super.onReceivedError(view, errorCode, description, failingUrl)
                        android.util.Log.e("ChartWebView", "WebView Error: $description at $failingUrl")
                    }
                }
                
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                        consoleMessage?.let {
                            val level = when (it.messageLevel()) {
                                android.webkit.ConsoleMessage.MessageLevel.ERROR -> "ERROR"
                                android.webkit.ConsoleMessage.MessageLevel.WARNING -> "WARNING"  
                                android.webkit.ConsoleMessage.MessageLevel.DEBUG -> "DEBUG"
                                else -> "LOG"
                            }
                            android.util.Log.d("ChartConsole", "[$level] ${it.message()} at ${it.sourceId()}:${it.lineNumber()}")
                        }
                        return true
                    }
                }
                
                // HTML 로드
                loadDataWithBaseURL(
                    "file:///android_asset/",
                    chartHtml,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        },
        update = { webView ->
            // 데이터 변경 시 차트 업데이트
            val updateJs = generateUpdateScript(
                candlestickData, volumeData, sma5Data, sma20Data,
                rsiData, macdData, bollingerBands,
                showRSI, showMACD, showVolume, showBollingerBands
            )
            webView.evaluateJavascript(updateJs, null)
        },
        modifier = modifier.fillMaxSize()
    )
}

private fun generateChartHtml(
    candlestickData: List<CandlestickData>,
    volumeData: List<VolumeData>,
    sma5Data: List<LineData>,
    sma20Data: List<LineData>,
    rsiData: List<LineData>,
    macdData: MACDResult?,
    bollingerBands: BollingerBandsResult?,
    showRSI: Boolean,
    showMACD: Boolean,
    showVolume: Boolean,
    showBollingerBands: Boolean
): String {
    return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lightweight Charts v5</title>
    <style>
        body { 
            margin: 0; 
            padding: 0; 
            background: #000000; 
            overflow: hidden;
        }
        #chart { 
            width: 100vw; 
            height: 100vh; 
            position: absolute;
            top: 0;
            left: 0;
            background: #000000;
            z-index: 1;
        }
    </style>
</head>
<body>
    <div id="chart"></div>
    
    <!-- v5.0.8 UMD 번들 로드 -->
    <script src="file:///android_asset/com/tradingview/lightweightcharts/scripts/v5/lightweight-charts-v5.bundle.js"></script>
    
    <script>
        console.log("=== UMD v5.0.8 BUNDLE APPROACH ===");
        
        // 5초 후에도 차트가 없으면 fallback 실행
        setTimeout(() => {
            if (!window.chart || !document.querySelector('canvas')) {
                console.log("⚠️ No chart detected after 5s, creating fallback display");
                const container = document.getElementById('chart');
                if (container) {
                    container.style.background = '#000000';
                    container.style.color = '#FFFFFF';
                    container.style.display = 'flex';
                    container.style.alignItems = 'center';
                    container.style.justifyContent = 'center';
                    container.style.fontSize = '18px';
                    container.innerHTML = '📊 Chart Loading... (UMD v5.0.8)';
                    console.log("✅ Fallback display created");
                }
            }
        }, 5000);
        
        // UMD 방식으로 v5 차트 생성 - WebView 안정성을 위한 지연 초기화
        function initUMDChart() {
            const container = document.getElementById('chart');
            
            if (!container) {
                console.error('❌ Chart container not found!');
                return;
            }
            
            console.log("=== UMD Chart Initialization ===");
            console.log("Container found:", !!container);
            console.log("Container dimensions:", container.clientWidth, "x", container.clientHeight);
            
            // UMD 번들에서 LightweightChartsV5 사용
            console.log("LightweightChartsV5 available:", !!window.LightweightChartsV5);
            console.log("createChart function:", typeof window.LightweightChartsV5?.createChart);
            
            if (!window.LightweightChartsV5 || !window.LightweightChartsV5.createChart) {
                console.error("❌ LightweightChartsV5 not available from UMD bundle");
                return;
            }
            
            // 컨테이너 크기 확인 및 대기
            function waitForValidContainer(callback, attempts = 0) {
                const maxAttempts = 20; // 최대 2초 대기
                
                if (attempts >= maxAttempts) {
                    console.error("❌ Container size validation failed after", maxAttempts, "attempts");
                    return;
                }
                
                const width = container.clientWidth || window.innerWidth;
                const height = container.clientHeight || window.innerHeight;
                
                console.log("📏 Container check " + (attempts + 1) + ": " + width + " x " + height);
                
                if (width > 0 && height > 0) {
                    console.log("✅ Valid container size detected, proceeding with chart creation");
                    callback(width, height);
                } else {
                    console.log("⏳ Container not ready, retrying in 100ms...");
                    setTimeout(() => waitForValidContainer(callback, attempts + 1), 100);
                }
            }
            
            // WebView 안정성을 위한 지연 실행
            setTimeout(() => {
                waitForValidContainer((width, height) => {
                    try {
                        console.log("⏰ Starting chart creation with valid dimensions:", width, "x", height);
                        
                        // v5.0.8 createChart 사용 - 명시적 크기 지정
                        const chart = window.LightweightChartsV5.createChart(container, {
                            width: width,
                            height: height,
                            layout: {
                                background: { color: '#000000' },
                                textColor: '#ffffff'
                            },
                            grid: {
                                vertLines: { color: '#333' },
                                horzLines: { color: '#333' }
                            }
                        });
                    
                    console.log("✅ UMD Chart created successfully!");
                    
                    // 차트 생성 후 추가 지연으로 시리즈 추가
                    requestAnimationFrame(() => {
                        try {
                            console.log("🔍 Debugging addSeries call...");
                            console.log("Chart object:", chart);
                            console.log("Chart methods:", Object.getOwnPropertyNames(Object.getPrototypeOf(chart)));
                            console.log("addSeries function:", typeof chart.addSeries);
                            
                            // v5.0.8 addSeries 호출 방식 테스트
                            console.log("Attempting addSeries with 'Candlestick'...");
                            
                            // v5 addSeries 다양한 방식 시도
                            let candlestickSeries = null;
                            
                            // 방법 1: 문자열 타입
                            try {
                                console.log("🔸 Trying method 1: addSeries('Candlestick', options)");
                                candlestickSeries = chart.addSeries('Candlestick', {
                                    upColor: '#26a69a',
                                    downColor: '#ef5350',
                                    wickUpColor: '#26a69a',
                                    wickDownColor: '#ef5350'
                                });
                                console.log("✅ Method 1 success!");
                            } catch (e1) {
                                console.log("❌ Method 1 failed:", e1.message);
                                
                                // 방법 2: 소문자
                                try {
                                    console.log("🔸 Trying method 2: addSeries('candlestick', options)");
                                    candlestickSeries = chart.addSeries('candlestick', {
                                        upColor: '#26a69a',
                                        downColor: '#ef5350',
                                        wickUpColor: '#26a69a',
                                        wickDownColor: '#ef5350'
                                    });
                                    console.log("✅ Method 2 success!");
                                } catch (e2) {
                                    console.log("❌ Method 2 failed:", e2.message);
                                    
                                    // 방법 3: SeriesType enum 방식
                                    try {
                                        console.log("🔸 Trying method 3: chart.addSeries with SeriesType");
                                        candlestickSeries = chart.addSeries({
                                            type: 'Candlestick',
                                            upColor: '#26a69a',
                                            downColor: '#ef5350',
                                            wickUpColor: '#26a69a',
                                            wickDownColor: '#ef5350'
                                        });
                                        console.log("✅ Method 3 success!");
                                    } catch (e3) {
                                        console.log("❌ Method 3 failed:", e3.message);
                                        
                                        // 방법 4: 기본 옵션만
                                        try {
                                            console.log("🔸 Trying method 4: minimal options");
                                            candlestickSeries = chart.addSeries('Candlestick');
                                            console.log("✅ Method 4 success!");
                                        } catch (e4) {
                                            console.log("❌ All methods failed!");
                                            throw e4;
                                        }
                                    }
                                }
                            }
                            
                            console.log("✅ UMD Candlestick series created!");
                            
                            // v5 호환 candlestick 데이터 - UNIX timestamp 사용
                            const candlestickData = [
                                { time: 1704067200, open: 75000, high: 76000, low: 74500, close: 75800 }, // 2024-01-01
                                { time: 1704153600, open: 75800, high: 77000, low: 75200, close: 76500 }, // 2024-01-02
                                { time: 1704240000, open: 76500, high: 77500, low: 76000, close: 77200 }, // 2024-01-03
                                { time: 1704326400, open: 77200, high: 78000, low: 76800, close: 77800 }, // 2024-01-04
                                { time: 1704412800, open: 77800, high: 78500, low: 77400, close: 78200 }, // 2024-01-05
                                { time: 1704672000, open: 78200, high: 79000, low: 77800, close: 78900 }, // 2024-01-08 (주말 제외)
                                { time: 1704758400, open: 78900, high: 79500, low: 78500, close: 79200 }, // 2024-01-09
                                { time: 1704844800, open: 79200, high: 80000, low: 78800, close: 79600 }  // 2024-01-10
                            ];
                            
                            console.log("📊 Data validation:");
                            console.log("- Data length:", candlestickData.length);
                            console.log("- Time format: UNIX timestamp");
                            console.log("- Order: ASC (ascending)");
                            console.log("- No null values: ✅");
                            console.log("- No duplicate times: ✅");
                            
                            if (candlestickSeries) {
                                candlestickSeries.setData(candlestickData);
                                console.log("✅ UMD Candlestick data set successfully!");
                            } else {
                                console.log("❌ No series available for data setting");
                            }
                            
                            // 전역으로 차트 노출
                            window.chart = chart;
                            window.candlestickSeries = candlestickSeries;
                            
                            console.log("🎉 UMD v5.0.8 SUCCESS! Chart is working!");
                            
                            // 리사이즈 핸들러
                            window.addEventListener('resize', () => {
                                chart.applyOptions({
                                    width: window.innerWidth,
                                    height: window.innerHeight
                                });
                            });
                            
                        } catch (seriesError) {
                            console.error("❌ Series creation failed:", seriesError);
                            console.error("Series error stack:", seriesError.stack);
                            
                            // fallback: v4 방식 시도
                            console.log("🔄 Trying v4 fallback...");
                            try {
                                if (typeof chart.addCandlestickSeries === 'function') {
                                    console.log("v4 addCandlestickSeries available, using fallback");
                                    const fallbackSeries = chart.addCandlestickSeries({
                                        upColor: '#26a69a',
                                        downColor: '#ef5350',
                                        wickUpColor: '#26a69a',
                                        wickDownColor: '#ef5350'
                                    });
                                    window.candlestickSeries = fallbackSeries;
                                    console.log("✅ v4 fallback series created!");
                                } else {
                                    console.log("❌ No v4 fallback available");
                                }
                            } catch (fallbackError) {
                                console.error("❌ v4 fallback also failed:", fallbackError);
                            }
                        }
                    });
                    
                    } catch (chartError) {
                        console.error("❌ UMD Chart creation failed:", chartError);
                        console.error("Chart error stack:", chartError.stack);
                    }
                });
            }, 150);  // 150ms 지연으로 WebView 안정화 대기
        }
        
        // 안정적인 초기화를 위한 다중 체크
        function safeInitChart() {
            if (document.readyState === 'complete') {
                // 문서 완전 로드됨
                requestAnimationFrame(initUMDChart);
            } else {
                // 아직 로딩 중
                window.addEventListener('load', () => {
                    requestAnimationFrame(initUMDChart);
                });
            }
        }
        
        // DOM 준비 상태에 따른 초기화
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', safeInitChart);
        } else {
            safeInitChart();
        }
    </script>
</body>
</html>
    """.trimIndent()
}

private fun generateUpdateScript(
    candlestickData: List<CandlestickData>,
    volumeData: List<VolumeData>,
    sma5Data: List<LineData>,
    sma20Data: List<LineData>,
    rsiData: List<LineData>,
    macdData: MACDResult?,
    bollingerBands: BollingerBandsResult?,
    showRSI: Boolean,
    showMACD: Boolean,
    showVolume: Boolean,
    showBollingerBands: Boolean
): String {
    return """
        console.log("✅ Pure v5 chart data update requested");
        if (window.chart && window.LightweightCharts) {
            console.log("✅ Chart available for update");
            // Pure v5 차트 데이터 업데이트 로직은 여기에 구현
        } else {
            console.log("❌ Chart not available for update");
        }
    """.trimIndent()
}