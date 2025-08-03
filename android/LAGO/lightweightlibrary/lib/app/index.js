import 'lightweight';
import FunctionManager from './function-manager.js';
import ChartRegistrationFunctionsController from './chart-registration-functions-controller.js';
import PluginManager from './plugin-manager.js';
import { logger } from './logger.js';
import { initLocator } from './service-locator/locator-component.js';

// 플러그인 import
import '../plugins/trend-line.js';
import '../plugins/panel-resizer.js';

// v4 LightweightCharts API 사용
const OriginalLightweightCharts = window.LightweightCharts

console.log("=== v4.1.0 + Plugins Loaded ===");
console.log("LightweightCharts v4:", OriginalLightweightCharts);
console.log("LightweightCharts keys:", OriginalLightweightCharts ? Object.keys(OriginalLightweightCharts) : "undefined");
console.log("createChart function:", typeof OriginalLightweightCharts?.createChart);
console.log("TrendLine plugin:", typeof window.TrendLine);

logger.setLevel("warning")

onmessage = function (message) {
    const connectionMessage = JSON.parse(message.data)

    if (connectionMessage.messageType !== "Message::Connection") {
        logger.e("Connection message is not valid")
        return
    }

    const logLevel = connectionMessage.data.logLevel
    logger.setLevel(logLevel)
    logger.d("Received connection message", message)

    const port = message.ports[0]
    const functionManager = new FunctionManager(port)

    const pluginManager = new PluginManager()
    window.pluginManager = pluginManager

    // 차트가 준비될 때까지 대기 - v4에서는 addCandlestickSeries 메서드 사용
    function initializeWrapperSystem() {
        if (!window.chart || typeof window.chart.addCandlestickSeries !== 'function') {
            console.log("Chart not ready yet, waiting... v4 uses addCandlestickSeries");
            setTimeout(initializeWrapperSystem, 100);
            return;
        }
        
        console.log("Initializing wrapper system with chart:", !!window.chart);
        console.log("Chart addCandlestickSeries (v4):", typeof window.chart.addCandlestickSeries);
        
        initLocator(functionManager, pluginManager, window.chart);

        const functionsController = new ChartRegistrationFunctionsController(
            window.chart,
            functionManager,
            pluginManager
        )
        functionsController.registerFunctions()
        window.functionsController = functionsController
        
        // 래퍼 시스템 초기화 완료를 알림
        window.wrapperSystemReady = true;
        console.log("✅ Wrapper system initialized successfully");
        console.log("✅ chartApplyOptions function registered");
    }
    
    // 래퍼 시스템 초기화 시작
    initializeWrapperSystem();

    logger.d("Connection has been established")
    port.onmessage = function (event) {
        const nativeMessage = JSON.parse(event.data)

        if (nativeMessage.data.fn) {
            logger.d("function", nativeMessage.data.fn)
        }
        logger.d("data", JSON.stringify(nativeMessage.data))

        switch (nativeMessage.messageType) {
            case 'Message::Function':
                functionManager.call(nativeMessage.data)
                break;
            case 'Message::Subscription':
                functionManager.subscribe(nativeMessage.data)
                break;
            case 'Message::SubscriptionCancellation':
                functionManager.unsubscribe(nativeMessage.data)
                break;
        }
    }
}

window.onresize = () => {
    window.chart.resize(window.innerWidth, window.innerHeight)
}

onload = () => {
    try {
        console.log("=== Chart Creation Process ===");
        
        // 래퍼 시스템이 요구하는 차트 생성 방식 사용
        // 래퍼가 기대하는 것은 window.chart가 아니라 내부적으로 관리되는 차트
        console.log("LightweightCharts available:", !!window.LightweightCharts);
        console.log("OriginalLightweightCharts available:", !!OriginalLightweightCharts);
        console.log("createChart function:", typeof OriginalLightweightCharts?.createChart);
        
        // 네이티브 차트를 생성하되, 래퍼가 접근할 수 있도록 설정
        if (OriginalLightweightCharts && typeof OriginalLightweightCharts.createChart === 'function') {
            const nativeChart = OriginalLightweightCharts.createChart(document.body, {
                width: window.innerWidth, 
                height: window.innerHeight,
                layout: {
                    background: { type: 'solid', color: '#FFFFFF' },
                    textColor: '#333333'
                },
                grid: {
                    vertLines: { color: '#F0F0F0' },
                    horzLines: { color: '#F0F0F0' }
                }
            });
            
            console.log("Native chart created:", !!nativeChart);
            console.log("Native chart keys:", Object.keys(nativeChart));
            console.log("Native chart methods:", Object.getOwnPropertyNames(Object.getPrototypeOf(nativeChart)));
            
            // v4 메서드 확인
            console.log("addCandlestickSeries (v4):", typeof nativeChart?.addCandlestickSeries);
            console.log("addLineSeries (v4):", typeof nativeChart?.addLineSeries);
            console.log("addHistogramSeries (v4):", typeof nativeChart?.addHistogramSeries);
            console.log("Chart API version: v4");
            
            // 래퍼 시스템을 위한 차트 설정
            window.chart = nativeChart;
            
            console.log("Chart assigned to window.chart");
            console.log("window.chart exists:", !!window.chart);
            console.log("window.chart methods available:", Object.getOwnPropertyNames(Object.getPrototypeOf(window.chart)));
            
            // 플러그인 지원을 위한 전역 노출
            window.createTrendLine = function(series, p1, p2, options) {
                if (window.TrendLine) {
                    return new window.TrendLine(nativeChart, series, p1, p2, options);
                }
                console.error("TrendLine plugin not available");
                return null;
            };
            
            // 패널 리사이저 초기화
            window.panelResizer = new window.PanelResizer(nativeChart);
            
            // 패널 크기 업데이트 함수
            window.updatePanelSizes = function(sizes) {
                console.log("Panel sizes updated:", sizes);
                // 네이티브로 패널 크기 전달
                if (window.functionManager) {
                    window.functionManager.call({
                        fn: 'updatePanelSizes',
                        args: [sizes]
                    });
                }
            };
            
            // 차트 크기 변경 시 separator 위치 업데이트
            window.addEventListener('resize', () => {
                if (window.panelResizer) {
                    window.panelResizer.updateSeparatorPositions();
                }
            });
            
            console.log("=== v4 + Multi-Panel Ready ===");
            console.log("Chart version: v4.1.0");
            console.log("TrendLine plugin: ✅");  
            console.log("PanelResizer plugin: ✅");
            console.log("Multi-panel: PriceScaleMargins with resizable separators");
            console.log("Chart methods:", Object.getOwnPropertyNames(nativeChart.__proto__));
            console.log("chartApplyOptions ready:", typeof window.chartApplyOptions);
            
        } else {
            throw new Error("OriginalLightweightCharts.createChart not available");
        }
        
        console.log("=== Chart Creation Complete ===");
        
    } catch (error) {
        console.error("Chart creation failed:", error);
        console.error("Error stack:", error.stack);
    }
}
