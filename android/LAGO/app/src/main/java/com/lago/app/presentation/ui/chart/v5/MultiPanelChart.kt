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
    val time: Long, // Epoch seconds timestamp (like 1529899200) - most compatible format
    val position: String, // "belowBar" | "aboveBar"
    val shape: String, // "arrowUp" | "arrowDown" | "circle" | "square" 
    val color: String,
    val id: String,
    val text: String,
    val size: Int = 1
)

@Serializable
data class ChartData(
    val time: Long, // epoch seconds로 통일
    val value: Double
)

@Serializable
data class CandlestickData(
    val time: Long, // epoch seconds로 통일
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
 * TradingView v5 supports multiple time formats:
 * - timestamp: epoch seconds (like 1529899200) - MOST COMPATIBLE for all timeframes
 * - businessDay: { year: 2019, month: 6, day: 1 } - day resolution only
 * - businessDayString: '2021-02-03' - day resolution only
 * 
 * Using timestamp format for maximum compatibility with minutes/hours/days/weeks/months/years
 */
private fun formatDateForChart(date: Date): Long {
    // ChartTimeManager 정규화와 동일한 로직 적용
    val timestamp = date.time
    return if (timestamp > 9999999999L) timestamp / 1000 else timestamp
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
    onWebViewReady: ((android.webkit.WebView) -> Unit)? = null,
    onDataPointClick: ((String, Double, String) -> Unit)? = null,
    onCrosshairMove: ((String?, Double?, String?) -> Unit)? = null,
    onRequestHistory: ((Int) -> Unit)? = null,
    onChartLoading: ((Boolean) -> Unit)? = null,
    onLoadingProgress: ((Int) -> Unit)? = null
) {
    
    // Create chart options with timeFrame-specific timeScale (timeFrame 변경 시 갱신)
    val finalChartOptions = remember(timeFrame, chartOptions) {
        val timeScaleOptions = DataConverter.createTimeScaleOptions(timeFrame)
        chartOptions.copy(timeScale = timeScaleOptions)
    }
    
    // Generate HTML content - 실제 데이터로 생성하여 패널/레전드 초기화
    val htmlContent = remember(data, timeFrame, chartOptions, tradingSignals) {
        generateMultiPanelHtml(data, finalChartOptions, tradingSignals, timeFrame)
    }
    
    // Use WebChartScreen with dark mode optimization
    WebChartScreen(
        htmlContent = htmlContent,
        modifier = modifier,
        onChartReady = onChartReady,
        onWebViewReady = onWebViewReady,
        onChartLoading = onChartLoading,
        onLoadingProgress = onLoadingProgress,
        additionalJavaScriptInterface = MultiPanelJavaScriptInterface(
            onDataPointClick = onDataPointClick,
            onCrosshairMove = onCrosshairMove,
            onRequestHistory = onRequestHistory
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
    private val onCrosshairMove: ((String?, Double?, String?) -> Unit)?,
    private val onRequestHistory: ((Int) -> Unit)?
) {
    @JavascriptInterface
    fun onDataPointClicked(time: String, value: Double, panelId: String) {
        onDataPointClick?.invoke(time, value, panelId)
    }
    
    @JavascriptInterface
    fun onCrosshairMoved(time: String?, value: Double?, panelId: String?) {
        onCrosshairMove?.invoke(time, value, panelId)
    }
    
    @JavascriptInterface
    fun requestHistoricalData(bars: Int) {
        onRequestHistory?.invoke(bars)
    }
}

/**
 * Generates HTML content with TradingView Lightweight Charts v5 using native addPane API
 */
private fun generateMultiPanelHtml(
    data: MultiPanelData,
    options: ChartOptions,
    tradingSignals: List<TradingSignal> = emptyList(),
    timeFrame: String = "D"
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
        .pane-legend {
            position: absolute;
            left: 8px;
            top: 0;
            padding: 2px 6px;
            border-radius: 6px;
            background: rgba(255,255,255,.85);
            font: 11px/1.2 'Segoe UI', -apple-system, sans-serif;
            color: #555;
            pointer-events: none;
            white-space: nowrap;
            visibility: visible;
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
        
        // ▼ 전역 상태 선포 (undefined 가드)
        let legends = [];
        let paneSeriesMap = [];
        const seriesInfoMap = new Map();
        
        // 전역 변수
        let chart;
        let panes = [];
        let series = [];
        
        // 패널별 레전드 시스템 변수
        let currentSymbol = 'STOCK';
        
        // 시간프레임별 업데이트를 위한 변수
        let currentTimeFrame = '${timeFrame}'; // Kotlin에서 전달받는 timeFrame
        
        // 무한 히스토리 관련 변수
        let isLoadingHistory = false;
        let currentDataLength = 0;
        let mainSeries = null;
        
        // TradingView Lightweight Charts v5.0.8 라이브러리 로드 (addPane API 지원)
        loadScript('https://unpkg.com/lightweight-charts@5.0.8/dist/lightweight-charts.standalone.production.js', function() {
            initLAGOMultiPanelChart();
        });
        
        function initLAGOMultiPanelChart() {
            // 전역 네임스페이스들 미리 준비 (초기에 꼭!)
            window.seriesMap       = window.seriesMap || {};
            window.__mainData      = window.__mainData || [];
            window.__volData       = window.__volData  || [];
            window.setInitialData  = window.setInitialData || function(){};
            window.updateBar       = window.updateBar || function(){};
            window.updateVolume    = window.updateVolume || function(){};
            window.updateSymbolName= window.updateSymbolName || function(){};
            
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
                
                // 🔥 거래량 자동 활성화 보장
                if (!indicators.some(ind => ind.type === 'volume')) {
                    console.log('LAGO: 거래량 지표가 없어 자동 추가');
                    indicators.push({ type: 'volume', enabled: true, options: {} });
                    console.log('LAGO: 거래량 지표 자동 추가 완료');
                }
                
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
                mainSeries = chart.addSeries(LightweightCharts.CandlestickSeries, {
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
                
                // F) 캐시 보장 후 데이터 설정
                window.__mainData = Array.isArray(priceData) ? priceData : [];
                mainSeries.setData(window.__mainData);
                series.push({ series: mainSeries, name: 'OHLC', paneIndex: 0 });
                
                // seriesMap에 메인 시리즈 추가 (기존 volume 등 보존)
                window.seriesMap.main = mainSeries;
                
                // 메인 시리즈 정보 저장
                seriesInfoMap.set(mainSeries, { name: currentSymbol, color: '#333' });
                
                // 레전드 초기화는 모든 패널/시리즈 생성 후에 실행
                
                // 무한 히스토리 이벤트 리스너 추가
                chart.timeScale().subscribeVisibleLogicalRangeChange(function(logicalRange) {
                    if (!logicalRange || isLoadingHistory) return;
                    
                    // 왼쪽 버퍼가 10개 미만이면 더 많은 과거 데이터 로드
                    if (logicalRange.from < 10) {
                        const barsToLoad = Math.max(20, Math.ceil(50 - logicalRange.from)); // ✅ 정수화
                        loadMoreHistoricalData(barsToLoad);
                    }
                });
                
                // 초기 데이터 길이 저장
                currentDataLength = priceData.length;
                
                // Android에서 호출할 수 있는 매수/매도 신호 설정 함수
                window.setTradeMarkers = function(markersJson) {
                    try {
                        const markers = JSON.parse(markersJson);
                        console.log('LAGO: Setting', markers.length, 'trade markers');
                        
                        // E) 마커 안전 설정
                        if (Array.isArray(markers) && markers.length > 0) {
                            mainSeries.setMarkers(markers);
                        } else {
                            mainSeries.setMarkers([]); // 지우는 경우
                        }
                        
                        console.log('✅ Trade markers updated successfully');
                    } catch (error) {
                        console.error('❌ Failed to set trade markers:', error);
                    }
                };
                
                // 마커 제거 함수
                window.clearTradeMarkers = function() {
                    mainSeries.setMarkers([]);
                    console.log('Trade markers cleared');
                };
                
                // 초기 매수/매도 신호 적용
                try {
                    const tradingSignalsData = JSON.parse(decodeBase64('$tradingSignalsBase64'));
                    console.log('LAGO: Initial trading signals loaded:', tradingSignalsData.length);
                    
                    if (tradingSignalsData && tradingSignalsData.length > 0) {
                        // v5 API 호환: setMarkers만 사용
                        mainSeries.setMarkers(tradingSignalsData);
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
                    
                    // 볼린저밴드 시리즈 정보 저장
                    seriesInfoMap.set(middleBandSeries, { name: '볼린저밴드', color: '#FF9800' });
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
                    
                    // SMA5 시리즈 정보 저장
                    seriesInfoMap.set(sma5Series, { name: '5일선', color: '#FF5722' });
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
                    
                    // SMA20 시리즈 정보 저장
                    seriesInfoMap.set(sma20Series, { name: '20일선', color: '#4CAF50' });
                }
                
                // 메인 패널 레전드는 TradingView 방식으로 이미 생성됨
                
                // 보조지표용 패널들 추가
                console.log('🔍 LAGO: Creating', indicators.length, 'indicator panels');
                console.log('🔍 Indicators data:', indicators);
                
                // 실제 데이터 크기 로깅
                console.log('sizes:',
                    'price', priceData.length,
                    'ind', indicators.map(i => (i?.type||'')+':'+(i?.data?.length||0)).join(','),
                    'sma5', (JSON.parse(decodeBase64('$sma5DataBase64'))||[]).length,
                    'sma20', (JSON.parse(decodeBase64('$sma20DataBase64'))||[]).length,
                    'bb', !!JSON.parse(decodeBase64('$bollingerBandsBase64')),
                    'macd', !!JSON.parse(decodeBase64('$macdDataBase64'))
                );
                
                // 🔥 빈 패널 방지: 데이터가 있고 활성화된 지표만 패널 생성
                let validPaneIndex = 1; // 메인 패널(0) 다음부터
                indicators.forEach((indicator, index) => {
                    try {
                        const type = (indicator?.type ?? '').toString().toLowerCase();
                        const hasData = indicator?.data?.length > 0;
                        const isEnabled = indicator?.enabled !== false; // 명시적으로 false가 아니면 활성화
                        
                        console.log('🔍 indicator:', type, 'hasData:', hasData, 'enabled:', isEnabled, 'points:', indicator?.data?.length ?? 0);
                        
                        // 🔥 데이터가 있고 활성화된 지표만 패널 생성
                        if (hasData && isEnabled && type) {
                            console.log('✅ Creating pane for valid indicator:', type);
                            createLAGOIndicatorPane({ ...indicator, type }, validPaneIndex, priceData);
                            validPaneIndex++; // 다음 유효한 패널 인덱스
                        } else {
                            console.log('🚫 Skipping empty/disabled indicator:', type, 'hasData:', hasData, 'enabled:', isEnabled);
                        }
                    } catch (e) {
                        console.error('❌ Indicator pane failed:', indicator?.type, e);
                    }
                });
                
                // 패널 높이 조정
                adjustPaneHeights();
                
                // 모든 패널/시리즈 생성 완료 후 레전드 초기화
                const container = document.getElementById('chart-container');
                reInitLegends(container);
                
                // ✅ 레전드 DOM 완성 후 크로스헤어 구독
                chart.subscribeCrosshairMove(updatePaneLegends);
                
                // 이벤트 핸들러 추가
                setupEventHandlers();
                
                // 자동 리사이즈
                setupAutoResize();
                
                // 전역 접근을 위한 노출
                window.lightweightChart = chart;
                window.chartPanes = panes;
                window.chartSeries = series;
                
                // 패널별 레전드 시스템 완료 처리
                setTimeout(() => {
                    refreshPaneLegends();
                    layoutLegends();
                    updateStaticLegends(); // 초기 정적 레전드 표시
                    console.log('\\n=== Panel Legend System Initialized ===');
                }, 500);
                
                console.log('LAGO Multi-Panel Chart v5 initialized successfully');
                
                // ✅ 차트 준비 완료 신호 - 통일된 함수로 관리
                window.notifyChartReady = function() {
                    if (window._chartReadyCalled) return; // 중복 호출 방지
                    window._chartReadyCalled = true;
                    
                    console.log('LAGO: Chart ready - notifying interface');
                    if (window.ChartInterface && ChartInterface.onChartReady) {
                        ChartInterface.onChartReady();
                    } else if (window.Android && Android.onChartReady) {
                        Android.onChartReady();
                    }
                };
                
                // 초기화 완료 후 콜백 호출
                setTimeout(window.notifyChartReady, 100);
                
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
            console.log('🚀 Creating pane for:', indicator.type, 'paneIndex:', paneIndex);
            
            // v5 네이티브 API로 새 패널 추가
            const pane = chart.addPane(true);
            console.log('✅ Pane created:', pane, 'pane.paneIndex():', pane.paneIndex());
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
                    indicatorSeries = pane.addSeries(LightweightCharts.LineSeries, {
                        color: lagoColors.rsi,
                        lineWidth: 2,
                    });
                    
                    // RSI 기준선 추가 (70, 30)
                    const rsiRef70 = pane.addSeries(LightweightCharts.LineSeries, {
                        color: '#666666',
                        lineWidth: 1,
                        lineStyle: LightweightCharts.LineStyle.Dashed,
                    });
                    
                    const rsiRef30 = pane.addSeries(LightweightCharts.LineSeries, {
                        color: '#666666',
                        lineWidth: 1,
                        lineStyle: LightweightCharts.LineStyle.Dashed,
                    });
                    
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
                    
                    // RSI 시리즈 정보 저장
                    seriesInfoMap.set(indicatorSeries, { name: 'RSI', color: '#9C27B0' });
                    console.log('✅ RSI panel completed, indicatorSeries:', indicatorSeries);
                    break;
                    
                case 'macd':
                    console.log('LAGO: Creating MACD panel with full data');
                    
                    // MACD 전체 데이터 가져오기
                    const macdFullData = JSON.parse(decodeBase64('$macdDataBase64'));
                    
                    if (macdFullData) {
                        // MACD Line (파란색)
                        const macdLineSeries = pane.addSeries(LightweightCharts.LineSeries, {
                            color: '#2196F3',
                            lineWidth: 1,
                            priceScaleId: 'macd',
                        });
                        macdLineSeries.setData(macdFullData.macdLine || []);
                        
                        // Signal Line (빨간색)
                        const signalLineSeries = pane.addSeries(LightweightCharts.LineSeries, {
                            color: '#FF5722',
                            lineWidth: 1,
                            priceScaleId: 'macd',
                        });
                        signalLineSeries.setData(macdFullData.signalLine || []);
                        
                        // Histogram (색상별로 표시)
                        const histogramDataWithColors = (macdFullData.histogram || []).map(point => ({
                            time: point.time,
                            value: point.value,
                            color: point.value >= 0 ? '#FF99C5' : '#42A6FF' // MainPink : MainBlue
                        }));
                        
                        const histogramSeries = pane.addSeries(LightweightCharts.HistogramSeries, {
                            priceFormat: {
                                type: 'price',
                                precision: 4,
                                minMove: 0.0001,
                            },
                            priceScaleId: 'macd',
                        });
                        histogramSeries.setData(histogramDataWithColors);
                        
                        // 제로 라인
                        const zeroLine = pane.addSeries(LightweightCharts.LineSeries, {
                            color: '#666666',
                            lineWidth: 1,
                            lineStyle: LightweightCharts.LineStyle.Dashed,
                            priceScaleId: 'macd',
                        });
                        
                        if (macdFullData.macdLine && macdFullData.macdLine.length > 0) {
                            const startTime = macdFullData.macdLine[0].time;
                            const endTime = macdFullData.macdLine[macdFullData.macdLine.length - 1].time;
                            zeroLine.setData([
                                { time: startTime, value: 0 },
                                { time: endTime, value: 0 }
                            ]);
                        }
                        
                        // MACD 시리즈 정보 저장
                        seriesInfoMap.set(histogramSeries, { name: 'MACD(12,26,9)', color: '#2196F3' });
                        
                        // 메인 시리즈는 히스토그램으로 설정
                        indicatorSeries = histogramSeries;
                        console.log('✅ MACD panel completed, indicatorSeries:', indicatorSeries);
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
                    
                    indicatorSeries = pane.addSeries(LightweightCharts.HistogramSeries, {
                        priceFormat: {
                            type: 'volume',
                        },
                    });
                    
                    indicatorSeries.setData(volumeDataWithColors);
                    
                    // seriesMap에 볼륨 시리즈 추가 (전역)
                    window.seriesMap.volume = indicatorSeries;
                    
                    // 거래량 시리즈 정보 저장
                    seriesInfoMap.set(indicatorSeries, { name: '거래량', color: '#FF9800' });
                    console.log('✅ Volume panel completed, indicatorSeries:', indicatorSeries);
                    break;
                    
                case 'sma5':
                    indicatorSeries = pane.addSeries(LightweightCharts.LineSeries, {
                        color: lagoColors.sma5,
                        lineWidth: 2,
                    });
                    break;
                    
                case 'sma20':
                    indicatorSeries = pane.addSeries(LightweightCharts.LineSeries, {
                        color: lagoColors.sma20,
                        lineWidth: 2,
                    });
                    break;
                    
                case 'bollinger_bands':
                    indicatorSeries = pane.addSeries(LightweightCharts.LineSeries, {
                        color: lagoColors.bollinger_bands,
                        lineWidth: 2,
                    });
                    break;
                    
                default:
                    indicatorSeries = pane.addSeries(LightweightCharts.LineSeries, {
                        color: indicator.options.color || '#2962FF',
                        lineWidth: indicator.options.lineWidth || 2,
                    });
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
            
            // 레전드 초기화는 모든 패널 생성 후 한 번만 실행 (initLAGOMultiPanelChart에서 처리)
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
                // 패널별 레전드 위치 재조정
                if (legends.length > 0) {
                    layoutLegends();
                }
            }
            
            // 윈도우 리사이즈 이벤트 리스너 추가
            window.addEventListener('resize', layoutLegends);
            
            window.addEventListener('resize', resizeChart);
            resizeChart();
            
            // === 시간축 관리 함수들 ===
            
            // 프레임 확인 함수
            function isIntraday(tf) { 
                return ['1','3','5','10','15','30','60'].includes(tf); 
            }

            // LightweightCharts Time → epochSec 변환
            function toEpochSecFromLWTime(t) {
                if (typeof t === 'number') return t;
                if (t && typeof t === 'object' && 'year' in t) {
                    const d = new Date(Date.UTC(t.year, (t.month||1)-1, (t.day||1), 0,0,0));
                    // 시간 정규화 - ChartTimeManager와 동일한 로직
                    const timestamp = d.getTime();
                    return timestamp > 9999999999 ? Math.floor(timestamp/1000) : timestamp;
                }
                return null;
            }

            // 시간축 라벨 포맷 함수 (KST 기준)
            function formatTickLabel(tf, epochSec) {
                if (epochSec == null) return '';
                // KST로 변환 (+9시간)
                const d = new Date((epochSec + (9 * 60 * 60)) * 1000);
                const pad = n => ('0'+n).slice(-2);
                const Y = d.getUTCFullYear(), M = pad(d.getUTCMonth()+1), D = pad(d.getUTCDate());
                const h = pad(d.getUTCHours()), m = pad(d.getUTCMinutes());
                return isIntraday(tf) ? (M + '/' + D + ' ' + h + ':' + m) : (Y + '-' + M + '-' + D);
            }

            // 프레임별 시간축 동적 설정
            function applyTimeScaleForFrame(tf) {
                chart.applyOptions({
                    timeScale: {
                        timeVisible: isIntraday(tf),   // 일봉 이상은 false
                        secondsVisible: false,
                        borderVisible: true,
                        tickMarkFormatter: (t) => {
                            const epoch = toEpochSecFromLWTime(t);
                            return formatTickLabel(tf, epoch);
                        },
                    }
                });
                console.log('LAGO: TimeScale applied for frame', tf, 'intraday:', isIntraday(tf));
            }
            
            // 에포크 초 검증 함수
            function ensureEpochSeconds(arr) {
                if (!Array.isArray(arr) || !arr.length) return arr;
                const t = arr[0].time;
                if (typeof t === 'number' && t > 1000000000) return arr; // 에포크 초 확인
                console.warn('LAGO: time is not UTCTimestamp(seconds). Intraday scale might not work:', t);
                return arr;
            }
            
            
            // 차트 초기화 직후 기존 timeScale 적용 (재생성 방식이므로 불필요한 동적 함수 제거)
            if (currentTimeFrame) {
                applyTimeScaleForFrame(currentTimeFrame);
            }
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
                    const color = item.color || getDefaultColor(item.name);
                    legendHTML += '<span class="legend-item"><span class="legend-color" style="background-color: ' + color + ';"></span>' + item.name + '</span>';
                });
                legend.innerHTML = legendHTML;
                console.log('Legend HTML for panel ' + paneIndex + ':', legendHTML);
            } else {
                // 빈 항목일 경우 기본 표시
                legend.innerHTML = '<span style="font-size: 10px; color: #666;">Panel ' + paneIndex + '</span>';
                console.log('Using default legend for panel ' + paneIndex);
            }
            
            // 기존 커스텀 스타일 적용
            legend.style.position = 'absolute';
            legend.style.top = '4px';
            legend.style.left = '4px';
            legend.style.zIndex = '1000';
            legend.style.fontSize = '11px';
            legend.style.fontFamily = "'Segoe UI', -apple-system, sans-serif";
            legend.style.fontWeight = '400';
            legend.style.color = '#555';
            legend.style.pointerEvents = 'none';
            legend.style.lineHeight = '1.2';
            legend.style.whiteSpace = 'nowrap';
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
        
        // ========== 패널별 레전드 시스템 ========== 
        
        // 패널별 레전드 초기화
        function initializePaneLegends(container) {
            const panes = chart.panes();
            legends = panes.map((_, i) => {
                const el = document.createElement('div');
                el.className = 'pane-legend';
                el.id = 'legend-pane-' + i;
                el.textContent = '';
                container.appendChild(el);
                return el;
            });
        }
        
        // 재초기화를 위한 레전드 헬퍼 (기존 DOM 제거 후 재생성)
        function reInitLegends(container) {
            // 기존 레전드 DOM 모두 제거
            const existingLegends = container.querySelectorAll('.pane-legend');
            existingLegends.forEach(el => el.remove());
            
            // ✅ panes 안전 가드
            const panes = (chart.panes && typeof chart.panes === 'function') ? chart.panes() : [];
            const paneCount = Array.isArray(panes) ? panes.length : 1;

            legends = Array.from({ length: paneCount }, (_, i) => {
                const el = document.createElement('div');
                el.className = 'pane-legend';
                el.id = 'legend-pane-' + i;
                container.appendChild(el);
                return el;
            });
            
            // 순서대로 1회 호출
            refreshPaneLegends();
            layoutLegends();
            updateStaticLegends();
        }
        
        // 패널별 시리즈 매핑 갱신
        function refreshPaneLegends() {
            paneSeriesMap = chart.panes().map(pane => new Set(pane.getSeries()));
        }
        
        // 레전드 위치 자동 배치
        function layoutLegends() {
            let top = 0;
            const paneList = chart.panes();
            paneList.forEach((pane, i) => {
                if (legends[i]) {
                    const size = chart.paneSize(i); // v5.0.8: 인덱스 숫자 전달
                    if (!size) return; // 아직 레이아웃 전이면 건너뜀
                    const { width, height } = size;
                    legends[i].style.top = (top + 4) + 'px';
                    legends[i].style.width = Math.max(0, width - 16) + 'px';
                    top += height;
                }
            });
        }
        
        // 값 포맷팅 함수
        function formatValue(d) {
            if (!d) return '';
            const v = d.value ?? d.close ?? d.lastPrice ?? d.high ?? d.low ?? d.open;
            return v != null ? Number(v).toFixed(2) : '';
        }
        
        // 패널별 레전드 업데이트 (모든 패널 항상 표시)
        function updatePaneLegends(param) {
            if (!Array.isArray(legends) || legends.length === 0) return;
            
            // 크로스헤어가 있을 때만 실시간 값 업데이트
            if (!param.time || param.point === undefined) {
                // 크로스헤어가 없을 때는 기본 레전드만 표시
                updateStaticLegends();
                return;
            }
            
            // 모든 패널의 레전드 업데이트
            paneSeriesMap.forEach((seriesSet, paneIndex) => {
                if (!legends[paneIndex]) return;
                
                const parts = [];
                for (const s of seriesSet) {
                    const dataAt = param.seriesData.get(s);
                    if (!dataAt) continue;
                    
                    const seriesInfo = seriesInfoMap.get(s);
                    const name = seriesInfo?.name || 'series';
                    const color = seriesInfo?.color || '#333';
                    
                    // 메인 패널인 경우 종목명 + OHLC 표시
                    if (paneIndex === 0 && s === window.seriesMap.main) {
                        const o = dataAt.open !== undefined ? dataAt.open.toFixed(0) : '';
                        const h = dataAt.high !== undefined ? dataAt.high.toFixed(0) : '';
                        const l = dataAt.low !== undefined ? dataAt.low.toFixed(0) : '';
                        const c = dataAt.close !== undefined ? dataAt.close.toFixed(0) : '';
                        parts.push('<span style="color: #333;"><strong>' + currentSymbol + '</strong> O:' + o + ' H:' + h + ' L:' + l + ' C:' + c + '</span>');
                    } else {
                        // 보조지표는 이름: 값 형태로 표시
                        const value = formatValue(dataAt);
                        if (value) {
                            parts.push('<span style="color: ' + color + ';">' + name + ': <strong>' + value + '</strong></span>');
                        }
                    }
                }
                legends[paneIndex].innerHTML = parts.join(' · ') || '&nbsp;';
            });
        }
        
        // 정적 레전드 업데이트 (크로스헤어 없을 때)
        function updateStaticLegends() {
            paneSeriesMap.forEach((seriesSet, paneIndex) => {
                if (!legends[paneIndex]) return;
                
                const parts = [];
                for (const s of seriesSet) {
                    const seriesInfo = seriesInfoMap.get(s);
                    const name = seriesInfo?.name || 'series';
                    const color = seriesInfo?.color || '#333';
                    
                    // 메인 패널인 경우 종목명만 표시
                    if (paneIndex === 0 && s === window.seriesMap.main) {
                        parts.push('<span style="color: #333;"><strong>' + currentSymbol + '</strong></span>');
                    } else {
                        // 보조지표는 이름만 표시
                        parts.push('<span style="color: ' + color + ';">' + name + '</span>');
                    }
                }
                legends[paneIndex].innerHTML = parts.join(' · ') || '&nbsp;';
            });
        }
        
        // 종목명 업데이트 함수
        function updateSymbolName(symbolName) {
            currentSymbol = symbolName || 'STOCK';
            // 정적 레전드 업데이트로 변경 (legendRows 변수 대신)
            updateStaticLegends();
        }
        
        // ===== 시간 재계산 로직 전부 제거됨 =====
        // Kotlin이 정확한 버킷 시작 시각을 계산해서 내려주므로 JS는 그대로 사용
        
        

        // ========== 실시간 업데이트용 전역 함수들 (최소 변경) ==========
        
        // 1) 혹시라도 너무 빨리 호출될 때 ReferenceError 방지용 "빈 함수"를 먼저 깔아둠
        window.seriesMap     = window.seriesMap     || {};
        window.setInitialData = window.setInitialData || function(){ console.warn('setInitialData called before init'); };
        window.updateBar      = window.updateBar      || function(){ console.warn('updateBar called before init'); };
        window.updateVolume   = window.updateVolume   || function(){ console.warn('updateVolume called before init'); };
        window.updateSymbolName = window.updateSymbolName || function(){ console.warn('updateSymbolName called before init'); };
        
        // (mainSeries와 chart가 생성된 "이후"에 실제 구현으로 덮어쓰기)
        
        // 🔥 차트 준비 완료 - onChartReady 콜백 호출
        console.log('LAGO: Chart initialization completed');
        try {
            if (window.ChartInterface && ChartInterface.onChartReady) {
                console.log('LAGO: 📞 ChartInterface.onChartReady() 호출');
                ChartInterface.onChartReady();
            }
            if (window.Android && Android.onChartReady) {
                console.log('LAGO: 📞 Android.onChartReady() 호출');
                Android.onChartReady();
            }
        } catch(e) {
            console.error('LAGO: ❌ onChartReady 콜백 호출 중 오류:', e);
        }
        
        // 2) 초기 데이터 세팅 - 기존 JsBridge 호출과 호환
        window.setInitialData = function(candlesJsonOrSeriesId, volumesJsonOrArray) {
            try {
                // 🔥 기존 방식: setInitialData(candlesJson, volumesJson) 
                if (typeof candlesJsonOrSeriesId === 'string' && candlesJsonOrSeriesId.charAt(0) === '[') {
                    console.log('LAGO: setInitialData (legacy format) called');
                    
                    // 캔들 데이터 설정
                    const candles = JSON.parse(candlesJsonOrSeriesId || '[]');
                    if (candles.length > 0 && window.seriesMap.main) {
                        window.seriesMap.main.setData(candles);
                        console.log('LAGO: Main candles loaded -', candles.length, 'items');
                    }
                    
                    // 거래량 데이터 설정
                    const volumes = JSON.parse(volumesJsonOrArray || '[]');
                    if (volumes.length > 0 && window.seriesMap.volume) {
                        window.seriesMap.volume.setData(volumes);
                        console.log('LAGO: Volume data loaded -', volumes.length, 'items');
                    }
                    
                    chart.timeScale().fitContent();
                    console.log('LAGO: setInitialData completed (legacy format)');
                    
                    // 🔥 로딩 완료 콜백 호출
                    if (window.ChartInterface && ChartInterface.onChartLoadingCompleted) {
                        console.log('LAGO: 📞 ChartInterface.onChartLoadingCompleted() 호출');
                        ChartInterface.onChartLoadingCompleted();
                    }
                    if (window.Android && Android.onChartLoadingCompleted) {
                        console.log('LAGO: 📞 Android.onChartLoadingCompleted() 호출');
                        Android.onChartLoadingCompleted();
                    }
                    
                } else {
                    // 🔥 새로운 방식: setInitialData(seriesId, jsonArray)
                    const seriesId = candlesJsonOrSeriesId;
                    const jsonArray = volumesJsonOrArray;
                    
                    console.log('LAGO: setInitialData (new format) for seriesId:', seriesId);
                    
                    const arr = JSON.parse(jsonArray);
                    const s = window.seriesMap[seriesId];
                    if (s) {
                        s.setData(arr);
                        chart.timeScale().fitContent();
                        console.log('LAGO: setInitialData for', seriesId, arr.length);
                    } else {
                        console.warn('LAGO: unknown seriesId in setInitialData', seriesId);
                        console.log('LAGO: Available seriesIds:', Object.keys(window.seriesMap || {}));
                    }
                }
            } catch (e) { 
                console.error('LAGO setInitialData error', e);
                console.log('LAGO: Available seriesIds:', Object.keys(window.seriesMap || {}));
            }
        };
        
        // 3) 실시간 캔들 업데이트 - 단순화 (Kotlin이 정답 time을 내려줌)
        window.updateBar = function(seriesId, jsonBar) {
            try {
                const s = window.seriesMap[seriesId];
                if (!s) { 
                    console.warn('unknown seriesId', seriesId); 
                    return; 
                }
                const bar = JSON.parse(jsonBar); // { time: epochSec(초), ohlc... } ← 반드시 버킷 시작 시각
                s.update(bar);

                // 실시간 따라가기: 실시간에 붙어 있을 때만 스크롤
                const sp = chart.timeScale().scrollPosition();
                if (sp <= 0.1) chart.timeScale().scrollToRealTime();

                // 디버그: 15분봉이면 분이 0/15/30/45인지 확인
                // console.log('[DBG] new bar', new Date(bar.time*1000).toISOString());
            } catch(e) { 
                console.error('updateBar error', e); 
            }
        };
        
        // 4) 실시간 거래량 업데이트 (HistogramSeries) - 캔들 색상과 동기화
        window.updateVolume = function(jsonBar) {
            try {
                const v = JSON.parse(jsonBar); // {time, value}
                if (window.seriesMap.volume) {
                    // 캐시된 캔들 데이터 확인 (브라우저 호환성을 위해 .data() 대신 캐시 사용)
                    const lastCandle = (window.__mainData || [])[(window.__mainData || []).length - 1];
                    
                    if (lastCandle && v.time === lastCandle.time) {
                        // 캔들 색상에 따라 볼륨 색상 결정
                        const isRising = lastCandle.close >= lastCandle.open;
                        const volumeColor = isRising ? '#FF99C5' : '#42A6FF'; // MainPink : MainBlue
                        
                        // 색상 정보가 포함된 볼륨 데이터 업데이트
                        const coloredVolumeData = {
                            time: v.time,
                            value: v.value,
                            color: volumeColor
                        };
                        
                        window.seriesMap.volume.update(coloredVolumeData);
                        console.log('LAGO: updateVolume with color sync', coloredVolumeData);
                    } else {
                        // 시간이 다르면 기본 상승 색상 사용
                        const coloredVolumeData = {
                            time: v.time,
                            value: v.value,
                            color: '#FF99C5' // MainPink
                        };
                        window.seriesMap.volume.update(coloredVolumeData);
                    }
                }
            } catch (e) { console.error('LAGO updateVolume error', e); }
        };
        
        // 🔥 JsBridge 호환성을 위한 실시간 업데이트 함수들
        window.updateRealTimeBar = function(jsonBar) {
            try {
                console.log('LAGO: updateRealTimeBar called:', jsonBar);
                
                if (!window.seriesMap.main) {
                    console.warn('LAGO: 메인 시리즈가 없어 실시간 캔들 업데이트 불가');
                    return;
                }
                
                const bar = JSON.parse(jsonBar);
                console.log('LAGO: 파싱된 실시간 캔들:', bar);
                
                window.seriesMap.main.update(bar);
                
                // 실시간 따라가기
                const sp = chart.timeScale().scrollPosition();
                if (sp <= 0.1) chart.timeScale().scrollToRealTime();
                
                console.log('LAGO: ✅ 실시간 캔들 업데이트 완료');
            } catch (e) {
                console.error('LAGO: updateRealTimeBar 오류:', e);
            }
        };
        
        window.updateRealTimeVolume = function(jsonVol) {
            try {
                console.log('LAGO: updateRealTimeVolume called:', jsonVol);
                
                if (!window.seriesMap.volume) {
                    console.warn('LAGO: 거래량 시리즈가 없어 실시간 거래량 업데이트 불가');
                    return;
                }
                
                const vol = JSON.parse(jsonVol);
                console.log('LAGO: 파싱된 실시간 거래량:', vol);
                
                // 색상 추가 (필요시)
                if (!vol.color) {
                    vol.color = '#FF99C5'; // 기본 상승 색상
                }
                
                window.seriesMap.volume.update(vol);
                console.log('LAGO: ✅ 실시간 거래량 업데이트 완료');
            } catch (e) {
                console.error('LAGO: updateRealTimeVolume 오류:', e);
            }
        };
        
        // 5) 종목명 업데이트 (TradingView 레전드 연동)
        window.updateSymbolName = function(symbolName) {
            try {
                updateSymbolName(symbolName);
                console.log('LAGO: Symbol name updated to', symbolName);
            } catch (e) { console.error('LAGO updateSymbolName error', e); }
        };
        
        // 6) 시간프레임 업데이트는 이미 위에서 applyTimeScaleForFrame 버전으로 정의됨
        
        // 7) 무한 히스토리 관련 함수들
        function loadMoreHistoricalData(barsToLoad) {
            if (isLoadingHistory || barsToLoad <= 0) return;
            
            isLoadingHistory = true;
            console.log('LAGO: Requesting', barsToLoad, 'historical bars');
            
            // ✅ ChartInterface 이름으로 브릿지 호출 (현재 사용 중인 이름)
            if (window.ChartInterface && ChartInterface.requestHistoricalData) {
                ChartInterface.requestHistoricalData(barsToLoad);
            } else if (window.Android && Android.requestHistoricalData) {
                // 구버전 호환
                Android.requestHistoricalData(barsToLoad);
            } else {
                console.warn('LAGO: requestHistoricalData bridge not available');
                isLoadingHistory = false;
            }
        }
        
        // ========== Android 호환성을 위한 wrapper 함수들 ==========
        
        // 전역 캐시 (공식 API에 data()가 없으니 우리가 보관)
        window.__mainData = [];
        window.__volData  = [];
        
        // 초기 데이터 세팅 (2개 파라미터 지원)
        window.setSeriesData = function(candlesJson, volumesJson) {
            try {
                const normalizeTime = (t) => (t > 9999999999 ? Math.floor(t/1000) : t);

                if (candlesJson && window.seriesMap.main) {
                    const arr = JSON.parse(candlesJson).map(p => ({ ...p, time: normalizeTime(p.time) }));
                    window.__mainData = arr;
                    window.seriesMap.main.setData(arr);
                    console.log('LAGO: setSeriesData - main series updated with', arr.length, 'candles');
                }

                // 볼륨 시리즈가 있을 때만 세팅 (indicator: 'volume' 켠 상태)
                if (volumesJson && window.seriesMap.volume) {
                    const vol = JSON.parse(volumesJson).map(v => ({ ...v, time: normalizeTime(v.time) }));
                    window.__volData = vol;
                    window.seriesMap.volume.setData(vol);
                    console.log('LAGO: setSeriesData - volume series updated with', vol.length, 'bars');
                }

                chart.timeScale().fitContent();
                
                // 데이터 설정 완료 후 차트 준비 신호
                setTimeout(() => {
                    if (window.notifyChartReady) {
                        window.notifyChartReady();
                    }
                }, 50);
                
            } catch (e) {
                console.error('setSeriesData error', e);
            }
        };
        
        window.updateRealTimeBar = function (barJson) {
            try {
                const bar = JSON.parse(barJson);
                const normalizeTime = (t) => (t > 9999999999 ? Math.floor(t/1000) : t);
                const normalizedBar = { ...bar, time: normalizeTime(bar.time) };
                
                if (window.seriesMap.main) {
                    window.seriesMap.main.update(normalizedBar);
                    // 캐시도 업데이트 (같은 시간이면 덮어쓰기, 새 시간이면 추가)
                    const existingIndex = window.__mainData.findIndex(d => d.time === normalizedBar.time);
                    if (existingIndex >= 0) {
                        window.__mainData[existingIndex] = normalizedBar;
                    } else {
                        window.__mainData.push(normalizedBar);
                    }
                }
            } catch (e) {
                console.error('updateRealTimeBar error', e);
            }
        };
        
        window.updateRealTimeVolume = function (vbarJson) {
            try {
                const vbar = JSON.parse(vbarJson);
                const normalizeTime = (t) => (t > 9999999999 ? Math.floor(t/1000) : t);
                const normalizedVol = { ...vbar, time: normalizeTime(vbar.time) };
                
                if (window.seriesMap.volume) {
                    window.seriesMap.volume.update(normalizedVol);
                    // 캐시도 업데이트
                    const existingIndex = window.__volData.findIndex(d => d.time === normalizedVol.time);
                    if (existingIndex >= 0) {
                        window.__volData[existingIndex] = normalizedVol;
                    } else {
                        window.__volData.push(normalizedVol);
                    }
                }
            } catch (e) {
                console.error('updateRealTimeVolume error', e);
            }
        };

        // LAGO 네임스페이스 래퍼 (사용자 요청 함수명들)
        window.LAGO = {
            setInitialData: function(data) {
                return window.setInitialData ? window.setInitialData('main', data) : undefined;
            },
            updateBar: function(bar) {
                return window.updateBar ? window.updateBar('main', bar) : undefined;
            },
            updateVolume: function(vbar) {
                return window.updateVolume ? window.updateVolume(vbar) : undefined;
            }
        };
        
        // 과거 데이터 앞쪽 추가 (브릿지와 시그니처 맞춤)
        window.prependHistoricalData = function(candlesJson, volumesJson) {
            try {
                const normalizeTime = (t) => (t > 9999999999 ? Math.floor(t/1000) : t);

                if (candlesJson && window.seriesMap.main) {
                    const older = JSON.parse(candlesJson).map(p => ({ ...p, time: normalizeTime(p.time) }));
                    window.__mainData = [...older, ...(window.__mainData || [])];
                    window.seriesMap.main.setData(window.__mainData);
                    console.log('LAGO: prependHistoricalData - prepended', older.length, 'candles');
                }

                if (volumesJson && window.seriesMap.volume) {
                    const olderVol = JSON.parse(volumesJson).map(v => ({ ...v, time: normalizeTime(v.time) }));
                    window.__volData = [...olderVol, ...(window.__volData || [])];
                    window.seriesMap.volume.setData(window.__volData);
                    console.log('LAGO: prependHistoricalData - prepended', olderVol.length, 'volume bars');
                }
            } catch (e) {
                console.error('prependHistoricalData error', e);
            } finally {
                isLoadingHistory = false;
            }
        };
        
        // Android에서 호출할 함수 - 과거 데이터 추가
        window.addHistoricalData = function(newDataJson) {
            try {
                const newData = JSON.parse(newDataJson);
                if (!newData || newData.length === 0) {
                    console.log('LAGO: No historical data to add');
                    isLoadingHistory = false;
                    return;
                }
                
                // v5 API 호환: 캐시된 데이터 사용
                const currentData = window.__mainData || [];
                
                // 현재 뷰 범위 저장 (뷰 보정을 위해)
                const prevRange = chart.timeScale().getVisibleLogicalRange();
                const prevLength = currentData.length;
                
                // 새 데이터를 앞에 붙여서 전체 데이터 갱신
                const mergedData = [...newData, ...currentData];
                window.__mainData = mergedData;
                mainSeries.setData(window.__mainData);
                
                // 뷰 범위 보정 (앞에 추가된 만큼 인덱스 조정)
                const addedCount = newData.length;
                if (prevRange && addedCount > 0) {
                    chart.timeScale().setVisibleLogicalRange({
                        from: prevRange.from + addedCount,
                        to: prevRange.to + addedCount,
                    });
                }
                
                currentDataLength = mergedData.length;
                console.log('LAGO: Historical data loaded:', addedCount, 'bars added, total:', currentDataLength);
                
            } catch (e) {
                console.error('LAGO: Error adding historical data:', e);
            } finally {
                isLoadingHistory = false;
            }
        };
        
        // ===== 패턴 분석 관련 JavaScript 브릿지 =====
        
        // ChartBridge 객체 생성 (JsBridge와 연결)
        window.ChartBridge = {
            // 🔥 차트 로딩 관련 콜백들
            onChartLoadingCompleted: function() {
                console.log('[ChartBridge] 📞 onChartLoadingCompleted 호출됨');
                if (window.ChartInterface && window.ChartInterface.onChartLoadingCompleted) {
                    window.ChartInterface.onChartLoadingCompleted();
                } else if (window.AndroidInterface && window.AndroidInterface.onChartLoadingCompleted) {
                    window.AndroidInterface.onChartLoadingCompleted();
                }
            },
            
            onChartReady: function() {
                console.log('[ChartBridge] 📞 onChartReady 호출됨');
                if (window.ChartInterface && window.ChartInterface.onChartReady) {
                    window.ChartInterface.onChartReady();
                } else if (window.AndroidInterface && window.AndroidInterface.onChartReady) {
                    window.AndroidInterface.onChartReady();
                }
            },
            
            onLoadingProgress: function(progress) {
                console.log('[ChartBridge] 📞 onLoadingProgress 호출됨:', progress);
                if (window.ChartInterface && window.ChartInterface.onLoadingProgress) {
                    window.ChartInterface.onLoadingProgress(progress);
                } else if (window.AndroidInterface && window.AndroidInterface.onLoadingProgress) {
                    window.AndroidInterface.onLoadingProgress(progress);
                }
            },
            
            // 기존 패턴 분석 관련 콜백들
            onVisibleRangeAnalysis: function(fromTime, toTime) {
                // JsBridge의 analyzePatternInRange 메서드 호출
                if (window.ChartInterface && window.ChartInterface.analyzePatternInRange) {
                    window.ChartInterface.analyzePatternInRange(fromTime, toTime);
                } else if (window.AndroidInterface && window.AndroidInterface.analyzePatternInRange) {
                    window.AndroidInterface.analyzePatternInRange(fromTime, toTime);
                }
            },
            
            onPatternAnalysisError: function(message) {
                // JsBridge의 onPatternAnalysisError 메서드 호출
                if (window.ChartInterface && window.ChartInterface.onPatternAnalysisError) {
                    window.ChartInterface.onPatternAnalysisError(message);
                } else if (window.AndroidInterface && window.AndroidInterface.onPatternAnalysisError) {
                    window.AndroidInterface.onPatternAnalysisError(message);
                }
            }
        };
        
        // 패턴 분석 요청 함수
        window.requestPatternAnalysis = function() {
            try {
                const visibleRange = chart.timeScale().getVisibleTimeRange();
                if (visibleRange && window.ChartBridge) {
                    window.ChartBridge.onVisibleRangeAnalysis(
                        Math.floor(visibleRange.from).toString(),
                        Math.floor(visibleRange.to).toString()
                    );
                    console.log('LAGO: Pattern analysis requested for range:', Math.floor(visibleRange.from), 'to', Math.floor(visibleRange.to));
                } else {
                    console.error('차트가 준비되지 않았거나 ChartBridge가 없습니다.');
                    if (window.ChartBridge) {
                        window.ChartBridge.onPatternAnalysisError('차트 영역을 가져올 수 없습니다.');
                    }
                }
            } catch (error) {
                console.error('패턴 분석 요청 실패:', error);
                if (window.ChartBridge) {
                    window.ChartBridge.onPatternAnalysisError('차트 영역을 가져올 수 없습니다.');
                }
            }
        };
        
        // 차트의 보이는 영역에서 패턴 분석을 실행 (JsBridge에서 호출)
        window.analyzePatternInVisibleRange = function() {
            try {
                const visibleRange = chart.timeScale().getVisibleTimeRange();
                if (visibleRange && window.ChartBridge) {
                    window.ChartBridge.onVisibleRangeAnalysis(
                        Math.floor(visibleRange.from).toString(),
                        Math.floor(visibleRange.to).toString()
                    );
                    console.log('LAGO: analyzePatternInVisibleRange called successfully');
                } else {
                    console.error('차트가 준비되지 않았거나 ChartBridge가 없습니다.');
                    if (window.ChartBridge) {
                        window.ChartBridge.onPatternAnalysisError('차트 영역을 가져올 수 없습니다.');
                    }
                }
            } catch (error) {
                console.error('패턴 분석 요청 실패:', error);
                if (window.ChartBridge) {
                    window.ChartBridge.onPatternAnalysisError('차트 영역을 가져올 수 없습니다.');
                }
            }
        };
        
        // 패턴 분석 결과 표시 함수
        window.displayPatternResult = function(resultJson) {
            try {
                const result = JSON.parse(resultJson);
                console.log('LAGO: 패턴 분석 결과:', result);
                
                // 차트에 패턴 결과 표시 (향후 확장 가능)
                // 예: 패턴 영역 하이라이트, 마커 추가 등
                
            } catch (error) {
                console.error('패턴 결과 표시 실패:', error);
            }
        };
        
        // D) 시간 타입 안전 변환 헬퍼
        function toEpochSec(t) {
            if (typeof t === 'number') return Math.floor(t);
            if (t && typeof t === 'object' && 'year' in t) {
                // BusinessDay { year, month, day }
                return Math.floor(Date.UTC(t.year, (t.month || 1) - 1, t.day || 1) / 1000);
            }
            return null;
        }

        function getVisibleTimeRangeSafe() {
            const ts = chart.timeScale();
            const r = ts.getVisibleTimeRange ? ts.getVisibleTimeRange()
                    : (ts.getVisibleRange ? ts.getVisibleRange() : null);
            if (!r) return null;
            const from = toEpochSec(r.from);
            const to = toEpochSec(r.to);
            if (from == null || to == null) return null;
            return { from, to };
        }
        
        // 보이는 영역 정보 반환 함수 (디버깅용)
        window.getVisibleRange = function() {
            try {
                const visible = getVisibleTimeRangeSafe();
                return visible ? { from: visible.from, to: visible.to } : null;
            } catch (error) {
                console.error('getVisibleRange 실패:', error);
                return null;
            }
        };
        
    
        // ===== LAGO strict v5.0.8 bridge (injected by atomic loader) =====
        (function(){ /* minimal shell; extended below */ })();
        // ===== end LAGO strict v5.0.8 bridge =====
    
    
        // ===== LAGO atomic loader & indicator data support =====
        (function(){
            try {
                // No-ops for legacy
                window.layoutLegends = function(){};
                window.reInitLegends = function(){};

                var chart = window.chart;
                var mainSeries = window.__mainSeries;
                var volumeSeries = null;
                var indicatorSeriesMap = window.__indicatorSeriesMap || {};
                var indicatorPaneIndexMap = window.__indicatorPaneIndexMap || {};
                var followRT = true;
                var activeLoadId = null;
                var pendingOps = []; // queued ops until endLoad
                var pendingCandles = null;
                var pendingVolumes = null;

                function ensureChart(){
                    if (chart && mainSeries) return true;
                    var el = document.getElementById('chart-container') || document.querySelector('#chart-container');
                    if (!el || !LightweightCharts?.createChart) return false;
                    chart = LightweightCharts.createChart(el, {
                        timeScale: { rightOffset: 2, timeVisible: true, secondsVisible: true },
                        layout: { textColor: '#333', background: { type: 'solid', color: '#fff' } },
                    });
                    window.chart = chart;
                    mainSeries = chart.addSeries(LightweightCharts.CandlestickSeries, {}, 0);
                    window.__mainSeries = mainSeries;

                    var ts = chart.timeScale();
                    ts.subscribeVisibleLogicalRangeChange(function(){
                        var range = ts.getVisibleLogicalRange();
                        if (!range || !mainSeries?.barsInLogicalRange) { followRT = true; return; }
                        var info = mainSeries.barsInLogicalRange(range) || {};
                        followRT = (info.barsAfter ?? 0) < 1;
                    });

                    // Chart ready 콜백은 window.notifyChartReady()로 통일 관리
                    return true;
                }

                function toCandles(arr){
                    if (!arr) return [];
                    if (typeof arr === 'string') { try { arr = JSON.parse(arr); } catch(e){ return []; } }
                    if (!Array.isArray(arr)) return [];
                    var out = [];
                    for (var i=0;i<arr.length;i++){
                        var c = arr[i]||{};
                        var t = Number(c.time), o=Number(c.open), h=Number(c.high), l=Number(c.low), cl=Number(c.close);
                        if (isFinite(t)&&t>0&&isFinite(o)&&isFinite(h)&&isFinite(l)&&isFinite(cl)) out.push({time:t,open:o,high:h,low:l,close:cl});
                    }
                    return out;
                }
                function toVolumes(arr){
                    if (!arr) return [];
                    if (typeof arr === 'string') { try { arr = JSON.parse(arr); } catch(e){ return []; } }
                    if (!Array.isArray(arr)) return [];
                    var out = [];
                    for (var i=0;i<arr.length;i++){
                        var v = arr[i]||{}; var t=Number(v.time), val=Number(v.value);
                        if (isFinite(t)&&t>0&&isFinite(val)) out.push({time:t,value:val,color:v.color});
                    }
                    return out;
                }
                function toLinePoints(arr){
                    if (!arr) return [];
                    if (typeof arr === 'string') { try { arr = JSON.parse(arr); } catch(e){ return []; } }
                    if (!Array.isArray(arr)) return [];
                    var out = [];
                    for (var i=0;i<arr.length;i++){
                        var p = arr[i]||{}; var t=Number(p.time), val=Number(p.value);
                        if (isFinite(t)&&t>0&&isFinite(val)) out.push({time:t, value:val});
                    }
                    return out;
                }

                function paneCount(){ try { return chart?.panes?.().length ?? 1; } catch(e){ return 1; } }
                function getOrCreatePaneIndexForKey(key){
                    if (Object.prototype.hasOwnProperty.call(indicatorPaneIndexMap, key)) return indicatorPaneIndexMap[key];
                    var idx = paneCount(); // 0=main
                    indicatorPaneIndexMap[key] = idx;
                    window.__indicatorPaneIndexMap = indicatorPaneIndexMap;
                    return idx;
                }
                function maybeRemovePane(idx, key){
                    try {
                        var panes = chart?.panes?.() || [];
                        if (!panes[idx]) return;
                        var series = panes[idx].getSeries ? panes[idx].getSeries() : [];
                        if (!series || series.length === 0) {
                            chart.removePane(idx);
                            if (key) delete indicatorPaneIndexMap[key];
                        }
                    } catch(e){}
                }

                function createIndicator(key, payload){
                    if (!ensureChart()) return;
                    var k = String(key||'').toLowerCase();
                    var idx = getOrCreatePaneIndexForKey(k);
                    var s = null;
                    // parse payload
                    var cfg = payload;
                    if (typeof cfg === 'string') { try { cfg = JSON.parse(cfg); } catch(e){} }

                    if (k === 'volume') {
                        s = chart.addSeries(LightweightCharts.HistogramSeries, { priceFormat: { type: 'volume' } }, idx);
                        volumeSeries = s;
                        if (cfg?.points) s.setData(toVolumes(cfg.points));
                    } else if (k === 'rsi') {
                        s = chart.addSeries(LightweightCharts.LineSeries, { color: '#7f8cff' }, idx);
                        if (cfg?.points) s.setData(toLinePoints(cfg.points));
                    } else if (k === 'macd') {
                        s = chart.addSeries(LightweightCharts.LineSeries, { color: '#ff7f7f' }, idx);
                        if (cfg?.points) s.setData(toLinePoints(cfg.points));
                    } else if (k.startsWith('sma')) {
                        s = chart.addSeries(LightweightCharts.LineSeries, { color: '#999999' }, 0);
                        if (cfg?.points) s.setData(toLinePoints(cfg.points));
                    } else {
                        s = chart.addSeries(LightweightCharts.LineSeries, {}, idx);
                        if (cfg?.points) s.setData(toLinePoints(cfg.points));
                    }
                    indicatorSeriesMap[k] = s;
                    window.__indicatorSeriesMap = indicatorSeriesMap;
                }

                // ---- Atomic load protocol ----
                window.beginLoad = function(loadId){
                    activeLoadId = String(loadId||Date.now());
                    pendingOps = [];
                    pendingCandles = null;
                    pendingVolumes = null;
                };
                window.endLoad = function(loadId){
                    var id = String(loadId||"");
                    if (activeLoadId === null || (id && id !== activeLoadId)) return;
                    if (!ensureChart()) return;
                    // Apply base data first
                    if (pendingCandles) {
                        var c = toCandles(pendingCandles);
                        mainSeries.setData(c);
                        window.__mainData = c;
                    }
                    if (pendingVolumes && volumeSeries) {
                        var v = toVolumes(pendingVolumes);
                        volumeSeries.setData(v);
                    }
                    // Then apply queued ops (indicators etc.)
                    for (var i=0;i<pendingOps.length;i++){
                        try { pendingOps[i](); } catch(e){ console.warn('pending op failed', e); }
                    }
                    pendingOps = [];
                    if (followRT) try { chart.timeScale().scrollToRealTime(); } catch(e){}
                    activeLoadId = null;
                };

                window.setSeriesData = function(candles, volumes){
                    if (!ensureChart()) return;
                    if (activeLoadId) {
                        pendingCandles = candles;
                        pendingVolumes = volumes;
                        return;
                    }
                    var c = toCandles(candles);
                    mainSeries.setData(c);
                    window.__mainData = c;
                    if (volumeSeries && volumes) {
                        var v = toVolumes(volumes);
                        volumeSeries.setData(v);
                    }
                    if (followRT) try { chart.timeScale().scrollToRealTime(); } catch(e){}
                };

                window.setIndicatorEnabled = function(type, enabled, payload){
                    var k = String(type||'').toLowerCase();
                    var op = function(){
                        if (enabled) {
                            if (!indicatorSeriesMap[k]) createIndicator(k, payload);
                            else if (payload) {
                                // update existing data
                                var cfg = payload; if (typeof cfg==='string') { try { cfg = JSON.parse(cfg); } catch(e){} }
                                var s = indicatorSeriesMap[k];
                                if (s && cfg?.points) {
                                    var pts = (k==='volume') ? toVolumes(cfg.points) : toLinePoints(cfg.points);
                                    try { s.setData(pts); } catch(e){}
                                }
                            }
                        } else {
                            var s = indicatorSeriesMap[k];
                            if (s?.remove) try { s.remove(); } catch(e){}
                            delete indicatorSeriesMap[k];
                            var idx = indicatorPaneIndexMap[k];
                            if (typeof idx==='number') maybeRemovePane(idx, k);
                            if (k==='volume') volumeSeries = null;
                        }
                    };
                    if (activeLoadId) pendingOps.push(op); else op();
                };

                window.addIndicatorPane = function(type){ window.setIndicatorEnabled(type, true, null); };
                window.removeIndicatorPane = function(type){ window.setIndicatorEnabled(type, false, null); };

                window.setTimeFrame = function(tf){ window.currentTimeFrame = String(tf||"D"); };

                window.updateRealTimeBar = function(bar){
                    if (!ensureChart()) return;
                    var arr = toCandles([bar]);
                    if (arr.length===0) return;
                    mainSeries.update(arr[0]);
                    if (followRT) try { chart.timeScale().scrollToRealTime(); } catch(e){}
                };
                window.updateRealTimeVolume = function(bar){
                    if (!ensureChart() || !volumeSeries) return;
                    var arr = toVolumes([bar]);
                    if (arr.length===0) return;
                    volumeSeries.update(arr[0]);
                };
            } catch(e){
                console.error('LAGO atomic loader init failed', e);
            }
        })();
        // ===== end atomic loader =====

    </script>
</body>
</html>
    """.trimIndent()
}