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
data class CandleData(val time: Long, val open: Float, val high: Float, val low: Float, val close: Float)
data class VolumeData(val time: Long, val value: Long, val color: String? = null)

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
    
    // time 단위 정규화: ms(13자리) → sec(10자리)
    private fun normalizeSec(t: Long) = if (t > 9_999_999_999L) t / 1000 else t

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
        android.util.Log.d("JsBridge", "🔥 setInitialData 호출: ${candles.size}개 캔들, ${volumes.size}개 거래량")
        
        // time 정규화 적용
        val cc = candles.map { it.copy(time = normalizeSec(it.time)) }
        val vv = volumes.map { it.copy(time = normalizeSec(it.time)) }
        
        if (cc.isNotEmpty()) {
            android.util.Log.d("JsBridge", "🔥 첫 캔들(정규화): time=${cc.first().time}, close=${cc.first().close}")
            android.util.Log.d("JsBridge", "🔥 마지막 캔들(정규화): time=${cc.last().time}, close=${cc.last().close}")
        }
        
        val candlesJson = gson.toJson(cc)
        val volumesJson = gson.toJson(vv)
        
        android.util.Log.d("JsBridge", "🔥 JSON 변환 완료 - 캔들 JSON 길이: ${candlesJson.length}, 거래량 JSON 길이: ${volumesJson.length}")
        android.util.Log.v("JsBridge", "🔥 캔들 JSON 샘플: ${candlesJson.take(200)}...")
        
        val jsCommand = """window.setSeriesData(${candlesJson.quote()}, ${volumesJson.quote()})"""
        android.util.Log.d("JsBridge", "🔥 JavaScript 명령 실행: ${jsCommand.take(100)}...")
        enqueue(jsCommand)
    }

    /**
     * 실시간 데이터 업데이트 (TradingView 권장: series.update())
     * 동일 time = 현재 바 덮어쓰기, 새로운 time = 새 바 추가
     */
    fun updateRealTimeBar(bar: CandleData) {
        val b = bar.copy(time = normalizeSec(bar.time))
        val barJson = gson.toJson(b)
        enqueue("""window.updateRealTimeBar(${barJson.quote()})""")
    }

    fun updateRealTimeVolume(vol: VolumeData) {
        val v = vol.copy(time = normalizeSec(vol.time))
        val volJson = gson.toJson(v)
        enqueue("""window.updateRealTimeVolume(${volJson.quote()})""")
    }

    // 기존 호환성을 위한 래퍼 메서드들
    fun setLegacyInitialData(candles: List<Candle>, volumes: List<VolumeBar> = emptyList()) {
        val cc = candles.map { 
            CandleData(normalizeSec(it.time), it.open.toFloat(), it.high.toFloat(), it.low.toFloat(), it.close.toFloat()) 
        }
        val vv = volumes.map { VolumeData(normalizeSec(it.time), it.value) }
        setInitialData(cc, vv)
    }

    fun updateBar(bar: Candle) {
        // time을 epoch seconds로 변환 (JavaScript에서 숫자로 받을 수 있도록)
        val epochSeconds = if (bar.time > 9999999999L) bar.time / 1000 else bar.time
        updateRealTimeBar(CandleData(epochSeconds, bar.open.toFloat(), bar.high.toFloat(), bar.low.toFloat(), bar.close.toFloat()))
    }

    fun updateVolume(vol: VolumeBar) {
        // time을 epoch seconds로 변환 (JavaScript에서 숫자로 받을 수 있도록)
        val epochSeconds = if (vol.time > 9999999999L) vol.time / 1000 else vol.time
        updateRealTimeVolume(VolumeData(epochSeconds, vol.value))
    }

    fun updateSymbolName(symbolName: String) {
        enqueue("""window.updateSymbolName('${symbolName.replace("'", "\\'")}')""")
    }


    fun setTradeMarkers(markersJson: String) {
        val escapedJson = markersJson.replace("'", "\\'").replace("\"", "\\\"")
        enqueue("""window.setTradeMarkers('$escapedJson')""")
    }

    fun clearTradeMarkers() {
        enqueue("""window.clearTradeMarkers()""")
    }

    /**
     * 메인 패널 오버레이 지표 동적 업데이트 (재생성 없이)
     * 재생성 방식에서는 불필요하지만 향후 최적화를 위해 유지
     */
    fun updateSMA5(data: List<com.lago.app.presentation.ui.chart.v5.ChartData>) {
        val json = if (data.isEmpty()) "[]" else gson.toJson(data)
        enqueue("""window.seriesMap.sma5?.setData($json)""")
    }

    fun updateSMA20(data: List<com.lago.app.presentation.ui.chart.v5.ChartData>) {
        val json = if (data.isEmpty()) "[]" else gson.toJson(data)
        enqueue("""window.seriesMap.sma20?.setData($json)""")
    }

    fun updateBollingerBands(upperBand: List<com.lago.app.presentation.ui.chart.v5.ChartData>, middleBand: List<com.lago.app.presentation.ui.chart.v5.ChartData>, lowerBand: List<com.lago.app.presentation.ui.chart.v5.ChartData>) {
        val upperJson = if (upperBand.isEmpty()) "[]" else gson.toJson(upperBand)
        val middleJson = if (middleBand.isEmpty()) "[]" else gson.toJson(middleBand)
        val lowerJson = if (lowerBand.isEmpty()) "[]" else gson.toJson(lowerBand)
        
        enqueue("""
            window.seriesMap.bb_upper?.setData($upperJson);
            window.seriesMap.bb_middle?.setData($middleJson);
            window.seriesMap.bb_lower?.setData($lowerJson);
        """.trimIndent())
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
        // time 정규화 적용
        val cc = historicalCandles.map { it.copy(time = normalizeSec(it.time)) }
        val vv = historicalVolumes.map { it.copy(time = normalizeSec(it.time)) }
        
        val candlesJson = gson.toJson(cc)
        val volumesJson = gson.toJson(vv)
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