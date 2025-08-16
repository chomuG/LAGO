package com.lago.app.presentation.ui.chart.v5

import android.webkit.WebView
import android.webkit.JavascriptInterface
import org.json.JSONObject
import com.google.gson.Gson
import java.util.ArrayDeque
import java.text.SimpleDateFormat
import java.util.*

// 기존 호환성을 위한 원래 데이터 구조 유지
data class Candle(val time: Long, val open: Int, var high: Int, var low: Int, var close: Int)
data class VolumeBar(val time: Long, val value: Long)

// TradingView 차트 전용 데이터 구조 (새로 추가)
data class CandleData(val time: Any, val open: Float, val high: Float, val low: Float, val close: Float)
data class VolumeData(val time: Any, val value: Long, val color: String? = null)

// 무한 히스토리 데이터 요청 리스너
interface HistoricalDataRequestListener {
    fun onRequestHistoricalData(barsToLoad: Int)
}

class JsBridge(
    private val webView: WebView, 
    private val gson: Gson = Gson(),
    private val historicalDataListener: HistoricalDataRequestListener? = null
) {
    private val queue = ArrayDeque<String>()
    private var ready = false

    fun markReady() {
        ready = true
        while (queue.isNotEmpty()) {
            eval(queue.removeFirst())
        }
    }

    /**
     * 차트 초기 데이터 설정 (TradingView 권장: series.setData())
     */
    fun setInitialData(candles: List<CandleData>, volumes: List<VolumeData> = emptyList()) {
        val candlesJson = gson.toJson(candles)
        val volumesJson = gson.toJson(volumes)
        enqueue("""window.setSeriesData(${candlesJson.quote()}, ${volumesJson.quote()})""")
    }

    /**
     * 실시간 데이터 업데이트 (TradingView 권장: series.update())
     * 동일 time = 현재 바 덮어쓰기, 새로운 time = 새 바 추가
     */
    fun updateRealTimeBar(bar: CandleData) {
        val barJson = gson.toJson(bar)
        enqueue("""window.updateRealTimeBar(${barJson.quote()})""")
    }

    fun updateRealTimeVolume(vol: VolumeData) {
        val volJson = gson.toJson(vol)
        enqueue("""window.updateRealTimeVolume(${volJson.quote()})""")
    }

    // 기존 호환성을 위한 래퍼 메서드들
    fun setLegacyInitialData(candles: List<Candle>, volumes: List<VolumeBar> = emptyList()) {
        val convertedCandles = candles.map { CandleData(it.time, it.open.toFloat(), it.high.toFloat(), it.low.toFloat(), it.close.toFloat()) }
        val convertedVolumes = volumes.map { VolumeData(it.time, it.value) }
        setInitialData(convertedCandles, convertedVolumes)
    }

    fun updateBar(bar: Candle) {
        updateRealTimeBar(CandleData(bar.time, bar.open.toFloat(), bar.high.toFloat(), bar.low.toFloat(), bar.close.toFloat()))
    }

    fun updateVolume(vol: VolumeBar) {
        updateRealTimeVolume(VolumeData(vol.time, vol.value))
    }

    fun updateSymbolName(symbolName: String) {
        enqueue("""window.updateSymbolName('${symbolName.replace("'", "\\'")}')""")
    }

    fun updateTimeFrame(timeFrame: String) {
        enqueue("""window.updateTimeFrame('${timeFrame}')""")
    }

    fun setTradeMarkers(markersJson: String) {
        val escapedJson = markersJson.replace("'", "\\'").replace("\"", "\\\"")
        enqueue("""window.setTradeMarkers('$escapedJson')""")
    }

    fun clearTradeMarkers() {
        enqueue("""window.clearTradeMarkers()""")
    }
    
    // 무한 히스토리 관련 메서드들
    @JavascriptInterface
    fun requestHistoricalData(barsToLoad: Int) {
        historicalDataListener?.onRequestHistoricalData(barsToLoad)
    }
    
    /**
     * 무한 히스토리 데이터 추가 (TradingView 권장: 기존 데이터와 병합 후 setData)
     * @param historicalCandles 과거 캔들 데이터 (시간순 정렬됨)
     * @param historicalVolumes 과거 거래량 데이터 (옵션)
     */
    fun prependHistoricalData(historicalCandles: List<CandleData>, historicalVolumes: List<VolumeData> = emptyList()) {
        val candlesJson = gson.toJson(historicalCandles)
        val volumesJson = gson.toJson(historicalVolumes)
        enqueue("""window.prependHistoricalData(${candlesJson.quote()}, ${volumesJson.quote()})""")
    }

    @Deprecated("Use prependHistoricalData instead")
    fun addHistoricalData(historicalDataJson: String) {
        val escapedJson = historicalDataJson.replace("'", "\\'").replace("\"", "\\\"")
        enqueue("""window.addHistoricalData('$escapedJson')""")
    }

    private fun enqueue(script: String) {
        if (!ready) {
            queue.addLast(script)
            return
        }
        eval(script)
    }

    private fun eval(script: String) {
        webView.post { webView.evaluateJavascript(script, null) }
    }

    private fun String.quote(): String = JSONObject.quote(this)

    companion object {
        /**
         * TradingView 차트 시간 포맷 변환 유틸리티
         */
        object TimeUtils {
            private val dailyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            
            /**
             * 일봉용 시간 변환: Unix timestamp → 'YYYY-MM-DD' 문자열
             */
            fun formatDailyTime(unixTimestamp: Long): String {
                return dailyFormat.format(Date(unixTimestamp))
            }
            
            /**
             * 분/초봉용 시간 변환: Unix milliseconds → Unix seconds
             */
            fun formatIntraTime(unixMillis: Long): Long {
                return unixMillis / 1000
            }
            
            /**
             * 타임프레임에 따른 자동 시간 변환
             * @param timestamp Unix milliseconds
             * @param timeFrame 차트 타임프레임 ("D", "1", "3", "5" 등)
             * @return 일봉이면 String, 분/초봉이면 Long
             */
            fun formatTimeForChart(timestamp: Long, timeFrame: String): Any {
                return when (timeFrame) {
                    "D", "W", "M", "Y" -> formatDailyTime(timestamp) // 일봉 이상
                    else -> formatIntraTime(timestamp) // 분/초봉
                }
            }
        }
    }
}