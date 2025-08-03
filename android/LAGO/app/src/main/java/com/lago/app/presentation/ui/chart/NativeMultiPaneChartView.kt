package com.lago.app.presentation.ui.chart

import android.graphics.Color
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.lago.app.domain.entity.*
import com.tradingview.lightweightcharts.view.ChartsView

/**
 * v5 네이티브 멀티페인 API를 사용한 차트 뷰
 * addPane, removePane, panes 등의 v5 전용 기능 활용
 */
@Composable
fun NativeMultiPaneChartView(
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
    
    AndroidView(
        factory = { context ->
            ChartsView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                
                val chartView = this
                
                // v5 네이티브 멀티페인 초기화
                postDelayed({
                    // 먼저 차트가 제대로 생성되었는지 확인
                    try {
                        val webViewField = javaClass.getDeclaredField("webView")
                        webViewField.isAccessible = true
                        val webView = webViewField.get(chartView) as android.webkit.WebView
                        
                        webView.evaluateJavascript("""
                        (function() {
                            console.log("=== Chart Diagnosis ===");
                            console.log("window.LightweightCharts:", !!window.LightweightCharts);
                            console.log("window.chart:", !!window.chart);
                            if (window.chart) {
                                console.log("chart.addSeries:", typeof window.chart.addSeries);
                                console.log("chart methods:", Object.getOwnPropertyNames(Object.getPrototypeOf(window.chart)));
                            }
                            
                            // 차트가 없으면 직접 생성
                            if (!window.chart && window.LightweightCharts) {
                                console.log("Creating chart directly...");
                                window.chart = window.LightweightCharts.createChart(document.body, {
                                    width: window.innerWidth,
                                    height: window.innerHeight,
                                    layout: {
                                        background: { type: 'solid', color: '#FFFFFF' },
                                        textColor: '#333333'
                                    }
                                });
                                console.log("Chart created:", !!window.chart);
                            }
                            
                            return JSON.stringify({
                                hasLightweightCharts: !!window.LightweightCharts,
                                hasChart: !!window.chart,
                                hasAddSeries: window.chart ? typeof window.chart.addSeries === 'function' : false
                            });
                        })();
                        """.trimIndent()) { result ->
                            android.util.Log.d("ChartDiagnosis", "Initial check: $result")
                            
                            // 차트 설정 실행
                            setupNativeMultiPaneChart(
                                chartView, candlestickData, volumeData, sma5Data, sma20Data,
                                rsiData, macdData, bollingerBands, showRSI, showMACD,
                                showVolume, showBollingerBands
                            )
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("NativeMultiPane", "WebView access error: ${e.message}")
                    }
                }, 1500)
            }
        },
        update = { chartView ->
            // 설정 변경 시 차트 업데이트
            setupNativeMultiPaneChart(
                chartView, candlestickData, volumeData, sma5Data, sma20Data,
                rsiData, macdData, bollingerBands, showRSI, showMACD,
                showVolume, showBollingerBands
            )
        },
        modifier = modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.White)
    )
}

private fun setupNativeMultiPaneChart(
    chartView: ChartsView,
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
) {
    try {
        // v5 네이티브 멀티페인 API 테스트
        val nativeMultiPaneJs = """
            (function() {
                console.log("=== Native Multi-Pane Chart Setup ===");
                
                if (!window.chart) {
                    console.log("Chart not ready yet");
                    return JSON.stringify({success: false, error: "Chart not ready"});
                }
                
                try {
                    // 현재 페인 상태 확인
                    console.log("Current chart methods:", Object.getOwnPropertyNames(Object.getPrototypeOf(window.chart)));
                    console.log("addPane available:", typeof window.chart.addPane);
                    console.log("panes available:", typeof window.chart.panes);
                    
                    // 네이티브 멀티페인 API 테스트
                    if (typeof window.chart.testNativeMultiPane === 'function') {
                        console.log("Testing native multi-pane...");
                        const result = window.chart.testNativeMultiPane();
                        console.log("Test result:", result);
                    }
                    
                    // v5 네이티브 멀티페인 실제 구현
                    console.log("Setting up native multi-pane chart with v5 API...");
                    
                    // 기존 시리즈와 패널 정리
                    console.log("Clearing existing series and panes...");
                    if (window.chart && typeof window.chart.panes === 'function') {
                        const currentPanes = window.chart.panes();
                        console.log("Current panes count before clear:", currentPanes.length);
                        
                        // 기존 시리즈 정리
                        if (window.mainCandlestickSeries) window.chart.removeSeries(window.mainCandlestickSeries);
                        if (window.sma5Series) window.chart.removeSeries(window.sma5Series);
                        if (window.sma20Series) window.chart.removeSeries(window.sma20Series);
                        if (window.volumeSeries) window.chart.removeSeries(window.volumeSeries);
                        if (window.rsiSeries) window.chart.removeSeries(window.rsiSeries);
                        if (window.macdSeries) window.chart.removeSeries(window.macdSeries);
                        if (window.signalSeries) window.chart.removeSeries(window.signalSeries);
                        if (window.histogramSeries) window.chart.removeSeries(window.histogramSeries);
                        if (window.bollingerUpperSeries) window.chart.removeSeries(window.bollingerUpperSeries);
                        if (window.bollingerMiddleSeries) window.chart.removeSeries(window.bollingerMiddleSeries);
                        if (window.bollingerLowerSeries) window.chart.removeSeries(window.bollingerLowerSeries);
                        
                        // 추가된 패널들 제거 (메인 패널은 제외)
                        while (currentPanes.length > 1) {
                            try {
                                window.chart.removePane(currentPanes[currentPanes.length - 1]);
                                const newPanes = window.chart.panes();
                                if (newPanes.length === currentPanes.length) break; // 무한루프 방지
                            } catch (e) {
                                console.log("Error removing pane:", e);
                                break;
                            }
                        }
                        
                        console.log("Panes after cleanup:", window.chart.panes().length);
                        
                        // 시리즈 변수들 초기화
                        window.mainCandlestickSeries = null;
                        window.sma5Series = null;
                        window.sma20Series = null;
                        window.volumeSeries = null;
                        window.rsiSeries = null;
                        window.macdSeries = null;
                        window.signalSeries = null;
                        window.histogramSeries = null;
                        window.bollingerUpperSeries = null;
                        window.bollingerMiddleSeries = null;
                        window.bollingerLowerSeries = null;
                    }
                    
                    // 차트 기본 설정 (라이트 모드)
                    window.chart.applyOptions({
                        layout: {
                            background: { type: 'solid', color: '#FFFFFF' },
                            textColor: '#333333'
                        },
                        grid: {
                            vertLines: { color: '#F0F0F0' },
                            horzLines: { color: '#F0F0F0' }
                        },
                        rightPriceScale: {
                            borderVisible: false,
                            visible: true,
                            scaleMargins: {
                                top: 0.05,
                                bottom: 0.05
                            }
                        },
                        timeScale: {
                            borderVisible: false,
                            timeVisible: true,
                            secondsVisible: false
                        },
                        crosshair: {
                            mode: 1, // Normal mode
                            vertLine: {
                                color: '#C8C8C8',
                                width: 1,
                                style: 2, // Dashed
                                visible: true,
                                labelVisible: true
                            },
                            horzLine: {
                                color: '#C8C8C8',
                                width: 1,
                                style: 2, // Dashed
                                visible: true,
                                labelVisible: true
                            }
                        }
                    });
                    
                    // 1. 메인 페인: 캔들스틱 + 이동평균선
                    console.log("=== Creating Main Pane ===");
                    const mainPane = window.chart.panes()[0]; // 기본 페인
                    console.log("Main pane:", !!mainPane);
                    
                    // 캔들스틱 시리즈 추가 (v5 문자열 방식)
                    if (window.LightweightCharts) {
                        console.log("Adding candlestick series...");
                        const candlestickSeries = window.chart.addSeries('Candlestick', {
                            upColor: '#ef5350',
                            downColor: '#26a69a',
                            borderVisible: false,
                            wickUpColor: '#ef5350',
                            wickDownColor: '#26a69a'
                        });
                        
                        // 실제 캔들스틱 데이터 사용
                        const candleData = ${candlestickData.map { candle ->
                            """{ time: '${ChartUtils.timestampToDateString(candle.time)}', open: ${candle.open}, high: ${candle.high}, low: ${candle.low}, close: ${candle.close} }"""
                        }.joinToString(", ", "[", "]")};
                        
                        console.log("Setting candlestick data, count:", candleData.length);
                        candlestickSeries.setData(candleData);
                        window.mainCandlestickSeries = candlestickSeries;
                        
                        // SMA5 추가 (메인 페인)
                        const sma5Series = window.chart.addSeries('Line', {
                            color: '#FF6B35',
                            lineWidth: 2
                        });
                        
                        const sma5Data = ${sma5Data.map { data ->
                            """{ time: '${ChartUtils.timestampToDateString(data.time)}', value: ${data.value} }"""
                        }.joinToString(", ", "[", "]")};
                        
                        sma5Series.setData(sma5Data);
                        window.sma5Series = sma5Series;
                        
                        // SMA20 추가 (메인 페인)
                        const sma20Series = window.chart.addSeries('Line', {
                            color: '#4ECDC4',
                            lineWidth: 2
                        });
                        
                        const sma20Data = ${sma20Data.map { data ->
                            """{ time: '${ChartUtils.timestampToDateString(data.time)}', value: ${data.value} }"""
                        }.joinToString(", ", "[", "]")};
                        
                        sma20Series.setData(sma20Data);
                        window.sma20Series = sma20Series;
                        
                        // 볼린저 밴드 추가 (메인 페인) - showBollingerBands가 true일 때만
                        if (${showBollingerBands}) {
                            const bollingerUpperSeries = window.chart.addSeries('Line', {
                                color: '#FFCC02',
                                lineWidth: 1
                            });
                            
                            const bollingerMiddleSeries = window.chart.addSeries('Line', {
                                color: '#FFCC02',
                                lineWidth: 1
                            });
                            
                            const bollingerLowerSeries = window.chart.addSeries('Line', {
                                color: '#FFCC02',
                                lineWidth: 1
                            });
                            
                            const bollingerUpperData = ${bollingerBands?.upperBand?.map { data ->
                                """{ time: '${ChartUtils.timestampToDateString(data.time)}', value: ${data.value} }"""
                            }?.joinToString(", ", "[", "]") ?: "[]"};
                            
                            const bollingerMiddleData = ${bollingerBands?.middleBand?.map { data ->
                                """{ time: '${ChartUtils.timestampToDateString(data.time)}', value: ${data.value} }"""
                            }?.joinToString(", ", "[", "]") ?: "[]"};
                            
                            const bollingerLowerData = ${bollingerBands?.lowerBand?.map { data ->
                                """{ time: '${ChartUtils.timestampToDateString(data.time)}', value: ${data.value} }"""
                            }?.joinToString(", ", "[", "]")} ?: "[]"};
                            
                            bollingerUpperSeries.setData(bollingerUpperData);
                            bollingerMiddleSeries.setData(bollingerMiddleData);
                            bollingerLowerSeries.setData(bollingerLowerData);
                            
                            window.bollingerUpperSeries = bollingerUpperSeries;
                            window.bollingerMiddleSeries = bollingerMiddleSeries;
                            window.bollingerLowerSeries = bollingerLowerSeries;
                        }
                    }
                    
                    // 2. 거래량 페인 생성 (기본 표시)
                    console.log("=== Creating Volume Pane ===");
                    const volumePane = window.chart.addPane();
                    console.log("Volume pane created:", !!volumePane);
                    
                    if (volumePane) {
                        const volumeSeries = volumePane.addSeries('Histogram', {
                            color: '#26a69a'
                        });
                        
                        const volumeSeriesData = ${volumeData.map { vol ->
                            """{ time: '${ChartUtils.timestampToDateString(vol.time)}', value: ${vol.value}, color: '${if (vol.color == "red") "#ef5350" else "#26a69a"}' }"""
                        }.joinToString(", ", "[", "]")};
                        
                        volumeSeries.setData(volumeSeriesData);
                        window.volumeSeries = volumeSeries;
                    }
                    
                    // 3. RSI 페인 생성 (showRSI가 true일 때만)
                    const showRSI = ${showRSI};
                    if (showRSI) {
                        console.log("=== Creating RSI Pane ===");
                        const rsiPane = window.chart.addPane();
                        console.log("RSI pane created:", !!rsiPane);
                        
                        if (rsiPane) {
                            const rsiSeries = rsiPane.addSeries('Line', {
                                color: '#9013FE',
                                lineWidth: 2
                            });
                            
                            const rsiSeriesData = ${rsiData.map { data ->
                                """{ time: '${ChartUtils.timestampToDateString(data.time)}', value: ${data.value} }"""
                            }.joinToString(", ", "[", "]")};
                            
                            rsiSeries.setData(rsiSeriesData);
                            window.rsiSeries = rsiSeries;
                        }
                    }
                    
                    // 4. MACD 페인 생성 (showMACD가 true일 때만)
                    const showMACD = ${showMACD};
                    if (showMACD) {
                        console.log("=== Creating MACD Pane ===");
                        const macdPane = window.chart.addPane();
                        console.log("MACD pane created:", !!macdPane);
                        
                        if (macdPane) {
                            // MACD Line
                            const macdSeries = macdPane.addSeries('Line', {
                                color: '#4CAF50',
                                lineWidth: 2
                            });
                            
                            // Signal Line  
                            const signalSeries = macdPane.addSeries('Line', {
                                color: '#F44336',
                                lineWidth: 2
                            });
                            
                            // Histogram
                            const histogramSeries = macdPane.addSeries('Histogram', {
                                color: '#42A5F5'
                            });
                            
                            const macdLineData = ${macdData?.macdLine?.map { data ->
                                """{ time: '${ChartUtils.timestampToDateString(data.time)}', value: ${data.value} }"""
                            }?.joinToString(", ", "[", "]") ?: "[]"};
                            
                            const signalLineData = ${macdData?.signalLine?.map { data ->
                                """{ time: '${ChartUtils.timestampToDateString(data.time)}', value: ${data.value} }"""
                            }?.joinToString(", ", "[", "]") ?: "[]"};
                            
                            const histogramData = ${macdData?.histogram?.map { data ->
                                """{ time: '${ChartUtils.timestampToDateString(data.time)}', value: ${data.value}, color: ${if (data.value >= 0) "'#26a69a'" else "'#ef5350'"} }"""
                            }?.joinToString(", ", "[", "]") ?: "[]"};
                            
                            macdSeries.setData(macdLineData);
                            signalSeries.setData(signalLineData);
                            histogramSeries.setData(histogramData);
                            
                            window.macdSeries = macdSeries;
                            window.signalSeries = signalSeries;
                            window.histogramSeries = histogramSeries;
                        }
                    }
                    
                    // 최종 페인 상태 확인
                    const finalPanes = window.chart.panes();
                    console.log("=== Final Multi-Pane Status ===");
                    console.log("Total panes created:", finalPanes.length);
                    console.log("Main pane (candlestick + SMA):", !!window.mainCandlestickSeries && !!window.sma5Series);
                    console.log("Volume pane:", !!window.volumeSeries);
                    console.log("RSI pane:", showRSI ? !!window.rsiSeries : "disabled");
                    console.log("MACD pane:", showMACD ? !!window.macdSeries : "disabled");
                    
                    return JSON.stringify({
                        success: true, 
                        addPaneAvailable: typeof window.chart.addPane === 'function',
                        panesAvailable: typeof window.chart.panes === 'function',
                        currentPanesCount: finalPanes.length,
                        mainPane: !!window.mainCandlestickSeries,
                        volumePane: !!window.volumeSeries,
                        rsiPane: showRSI ? !!window.rsiSeries : false,
                        macdPane: showMACD ? !!window.macdSeries : false,
                        nativeMultiPaneImplemented: true
                    });
                    
                } catch (error) {
                    console.error("Native multi-pane setup error:", error);
                    return JSON.stringify({success: false, error: error.message});
                }
            })();
        """.trimIndent()
        
        // WebView에 접근하여 JS 실행
        try {
            val webViewField = chartView.javaClass.getDeclaredField("webView")
            webViewField.isAccessible = true
            val webView = webViewField.get(chartView) as android.webkit.WebView
            
            webView.evaluateJavascript(nativeMultiPaneJs) { result ->
                android.util.Log.d("NativeMultiPane", "Result: $result")
            }
        } catch (reflectionError: Exception) {
            android.util.Log.e("NativeMultiPane", "Reflection error: ${reflectionError.message}")
        }
        
    } catch (e: Exception) {
        android.util.Log.e("NativeMultiPane", "Setup error: ${e.message}")
    }
}