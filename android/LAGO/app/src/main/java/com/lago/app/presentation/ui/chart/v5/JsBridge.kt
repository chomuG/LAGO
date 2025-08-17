package com.lago.app.presentation.ui.chart.v5

import android.webkit.WebView
import android.webkit.JavascriptInterface
import org.json.JSONObject
import com.google.gson.Gson
import java.util.ArrayDeque

// 기존 호환성을 위한 원래 데이터 구조 유지
data class Candle(val time: Long, val open: Int, var high: Int, var low: Int, var close: Int)
data class VolumeBar(val time: Long, val value: Long)

// TradingView 차트 전용 데이터 구조 (새로 추가)
data class CandleData(val time: Long, val open: Float, val high: Float, val low: Float, val close: Float)
data class VolumeData(val time: Long, val value: Long, val color: String? = null)

// 패턴 분석 리스너 (새 방식)
interface PatternListener {
    fun onPatternVisibleRange(fromEpochSec: Long, toEpochSec: Long)
    fun onPatternVisibleRangeError(msg: String)
}

// 무한 히스토리 데이터 요청 리스너
interface HistoricalDataRequestListener {
    fun onRequestHistoricalData(barsToLoad: Int)
}

class JsBridge(
    private val webView: WebView,
    private val gson: Gson = Gson(),
    private val historicalDataListener: HistoricalDataRequestListener? = null,
    private val patternListener: PatternListener? = null
) {
    private val queue = ArrayDeque<String>()
    private var ready = false
    private var version: Long = 0

    fun markReady() {
        ready = true
        version = System.currentTimeMillis()
        android.util.Log.d("JsBridge", "markReady() called - version: $version, queue size: ${queue.size}")
        while (queue.isNotEmpty()) {
            eval(queue.removeFirst())
        }
    }

    /**
     * 차트 초기 데이터 설정 (TradingView 권장: series.setData())
     * ChartTimeManager를 사용하여 시간 정규화
     */
    fun setInitialData(candles: List<CandleData>, volumes: List<VolumeData> = emptyList()) {
        android.util.Log.d("JsBridge", "🔥 setInitialData 호출: ${candles.size}개 캔들, ${volumes.size}개 거래량")
        android.util.Log.d("JsBridge", "🔥 ready 상태: $ready")

        // ChartTimeManager를 사용하여 시간 정규화
        val normalizedCandles = candles.map { candle ->
            val normalizedTime = ChartTimeManager.normalizeToEpochSeconds(candle.time)
            ChartTimeManager.debugTimeInfo("Initial Candle", candle.time)
            candle.copy(time = normalizedTime)
        }

        val normalizedVolumes = volumes.map { volume ->
            val normalizedTime = ChartTimeManager.normalizeToEpochSeconds(volume.time)
            volume.copy(time = normalizedTime)
        }

        if (normalizedCandles.isNotEmpty()) {
            android.util.Log.d("JsBridge", "🔥 첫 캔들(정규화): time=${normalizedCandles.first().time}, close=${normalizedCandles.first().close}")
            android.util.Log.d("JsBridge", "🔥 마지막 캔들(정규화): time=${normalizedCandles.last().time}, close=${normalizedCandles.last().close}")
        }

        val candlesJson = gson.toJson(normalizedCandles)
        val volumesJson = gson.toJson(normalizedVolumes)

        android.util.Log.d("JsBridge", "🔥 JSON 변환 완료 - 캔들 JSON 길이: ${candlesJson.length}, 거래량 JSON 길이: ${volumesJson.length}")

        val jsCommand = """window.setSeriesData(${candlesJson.quote()}, ${volumesJson.quote()})"""
        enqueueOrEval(jsCommand)
    }

    /**
     * 실시간 데이터 업데이트 (TradingView 권장: series.update())
     * 동일 time = 현재 바 덮어쓰기, 새로운 time = 새 바 추가
     * ChartTimeManager를 사용하여 버킷 시간 계산
     */
    fun updateRealTimeBar(bar: CandleData, timeFrame: String) {
        val normalizedTime = ChartTimeManager.normalizeToEpochSeconds(bar.time)
        val bucketTime = ChartTimeManager.getBucketStartTime(normalizedTime, timeFrame)

        ChartTimeManager.debugTimeInfo("RealTime Bar", bar.time, timeFrame)

        val bucketBar = bar.copy(time = bucketTime)
        val barJson = gson.toJson(bucketBar)
        enqueueOrEval("""window.updateRealTimeBar(${barJson.quote()})""")
    }

    fun updateRealTimeVolume(vol: VolumeData, timeFrame: String) {
        val normalizedTime = ChartTimeManager.normalizeToEpochSeconds(vol.time)
        val bucketTime = ChartTimeManager.getBucketStartTime(normalizedTime, timeFrame)

        val bucketVolume = vol.copy(time = bucketTime)
        val volJson = gson.toJson(bucketVolume)
        enqueueOrEval("""window.updateRealTimeVolume(${volJson.quote()})""")
    }

    // 기존 호환성을 위한 래퍼 메서드들 (ChartTimeManager 적용)
    fun setLegacyInitialData(candles: List<Candle>, volumes: List<VolumeBar> = emptyList()) {
        val convertedCandles = candles.map { candle ->
            val normalizedTime = ChartTimeManager.normalizeToEpochSeconds(candle.time)
            CandleData(normalizedTime, candle.open.toFloat(), candle.high.toFloat(), candle.low.toFloat(), candle.close.toFloat())
        }
        val convertedVolumes = volumes.map { volume ->
            val normalizedTime = ChartTimeManager.normalizeToEpochSeconds(volume.time)
            VolumeData(normalizedTime, volume.value)
        }
        setInitialData(convertedCandles, convertedVolumes)
    }

    fun updateBar(bar: Candle, timeFrame: String = "D") {
        val normalizedTime = ChartTimeManager.normalizeToEpochSeconds(bar.time)
        val bucketTime = ChartTimeManager.getBucketStartTime(normalizedTime, timeFrame)

        updateRealTimeBar(
            CandleData(bucketTime, bar.open.toFloat(), bar.high.toFloat(), bar.low.toFloat(), bar.close.toFloat()),
            timeFrame
        )
    }

    fun updateVolume(vol: VolumeBar, timeFrame: String = "D") {
        val normalizedTime = ChartTimeManager.normalizeToEpochSeconds(vol.time)
        val bucketTime = ChartTimeManager.getBucketStartTime(normalizedTime, timeFrame)

        updateRealTimeVolume(VolumeData(bucketTime, vol.value), timeFrame)
    }

    fun updateSymbolName(symbolName: String) {
        enqueueOrEval("""window.updateSymbolName('${symbolName.replace("'", "\\'")}')""")
    }

    fun setTradeMarkers(markersJson: String) {
        val escapedJson = markersJson.replace("'", "\\'").replace("\"", "\\\"")
        enqueueOrEval("""window.setTradeMarkers('$escapedJson')""")
    }

    fun clearTradeMarkers() {
        enqueueOrEval("""window.clearTradeMarkers()""")
    }

    // 메인 패널 오버레이 지표 동적 업데이트
    fun updateSMA5(data: List<ChartData>) {
        val normalizedData = data.map { item ->
            item.copy(time = ChartTimeManager.normalizeToEpochSeconds(item.time))
        }
        val json = if (normalizedData.isEmpty()) "[]" else gson.toJson(normalizedData)
        enqueueOrEval("""window.seriesMap.sma5?.setData($json)""")
    }

    fun updateSMA20(data: List<ChartData>) {
        val normalizedData = data.map { item ->
            item.copy(time = ChartTimeManager.normalizeToEpochSeconds(item.time))
        }
        val json = if (normalizedData.isEmpty()) "[]" else gson.toJson(normalizedData)
        enqueueOrEval("""window.seriesMap.sma20?.setData($json)""")
    }

    fun updateBollingerBands(upperBand: List<ChartData>, middleBand: List<ChartData>, lowerBand: List<ChartData>) {
        val normalizedUpper = upperBand.map { item ->
            item.copy(time = ChartTimeManager.normalizeToEpochSeconds(item.time))
        }
        val normalizedMiddle = middleBand.map { item ->
            item.copy(time = ChartTimeManager.normalizeToEpochSeconds(item.time))
        }
        val normalizedLower = lowerBand.map { item ->
            item.copy(time = ChartTimeManager.normalizeToEpochSeconds(item.time))
        }

        val upperJson = if (normalizedUpper.isEmpty()) "[]" else gson.toJson(normalizedUpper)
        val middleJson = if (normalizedMiddle.isEmpty()) "[]" else gson.toJson(normalizedMiddle)
        val lowerJson = if (normalizedLower.isEmpty()) "[]" else gson.toJson(normalizedLower)

        enqueueOrEval("""
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

    // ===== 패턴 분석 관련 메서드들 =====

    interface PatternAnalysisListener {
        fun onAnalyzePatternInRange(fromTime: String, toTime: String)
        fun onPatternAnalysisError(message: String)
        fun onPatternAnalysisComplete(patternName: String, description: String)
    }

    private var patternAnalysisListener: PatternAnalysisListener? = null

    fun setPatternAnalysisListener(listener: PatternAnalysisListener?) {
        this.patternAnalysisListener = listener
    }

    @JavascriptInterface
    fun analyzePatternInRange(fromTime: String, toTime: String) {
        android.util.Log.d("JsBridge", "📊 패턴 분석 요청: $fromTime ~ $toTime")
        patternAnalysisListener?.onAnalyzePatternInRange(fromTime, toTime)
    }

    @JavascriptInterface
    fun onPatternAnalysisError(message: String) {
        android.util.Log.w("JsBridge", "📊 패턴 분석 에러: $message")
        patternAnalysisListener?.onPatternAnalysisError(message)
    }

    @JavascriptInterface
    fun onPatternAnalysisComplete(patternName: String, description: String) {
        android.util.Log.d("JsBridge", "📊 패턴 분석 완료: $patternName - $description")
        patternAnalysisListener?.onPatternAnalysisComplete(patternName, description)
    }

    /**
     * 차트에서 보이는 영역의 시간 범위를 요청하는 개선된 메서드
     * ChartTimeManager를 사용하여 시간 처리 통일화
     */
    fun requestVisibleRange(callback: (fromTime: String?, toTime: String?) -> Unit) {
        val jsCommand = """
            (function() {
                try {
                    const range = window.getVisibleRange();
                    if (range && range.from && range.to) {
                        return JSON.stringify(range);
                    } else {
                        return null;
                    }
                } catch (error) {
                    console.error('getVisibleRange 실패:', error);
                    return null;
                }
            })();
        """.trimIndent()

        webView.post {
            webView.evaluateJavascript(jsCommand) { result ->
                try {
                    if (result != null && result != "null" && result.startsWith("{")) {
                        val cleanResult = result.replace("\"", "")
                        val jsonObject = org.json.JSONObject(cleanResult)
                        val fromTime = jsonObject.optString("from")
                        val toTime = jsonObject.optString("to")

                        if (fromTime.isNotEmpty() && toTime.isNotEmpty()) {
                            // ChartTimeManager를 사용하여 시간 검증
                            try {
                                val fromEpoch = ChartTimeManager.normalizeToEpochSeconds(fromTime.toLong())
                                val toEpoch = ChartTimeManager.normalizeToEpochSeconds(toTime.toLong())
                                ChartTimeManager.debugTimeInfo("Visible Range From", fromEpoch)
                                ChartTimeManager.debugTimeInfo("Visible Range To", toEpoch)
                                callback(fromEpoch.toString(), toEpoch.toString())
                            } catch (e: NumberFormatException) {
                                android.util.Log.e("JsBridge", "시간 파싱 실패: $fromTime, $toTime", e)
                                callback(null, null)
                            }
                        } else {
                            callback(null, null)
                        }
                    } else {
                        callback(null, null)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("JsBridge", "보이는 영역 파싱 실패", e)
                    callback(null, null)
                }
            }
        }
    }

    fun triggerPatternAnalysis() {
        enqueueOrEval("window.requestPatternAnalysis()")
    }

    /**
     * 차트의 보이는 영역에서 패턴 분석을 실행 (개선된 버전)
     */
    fun analyzePatternInVisibleRange() {
        android.util.Log.d("JsBridge", "analyzePatternInVisibleRange() called")
        android.util.Log.d("JsBridge", "ready state: $ready")

        val jsFunction = """
            (function() {
                try {
                    console.log('LAGO: getVisibleTimeRange 시작');
                    var c = window.lightweightChart || window.chart; 
                    if (!c || !c.timeScale) {
                        console.log('LAGO: 차트 또는 timeScale이 없음');
                        return null;
                    }
                    var r = c.timeScale().getVisibleTimeRange();
                    if (!r || !r.from || !r.to) {
                        console.log('LAGO: visible range가 없음');
                        return null;
                    }
                    console.log('LAGO: visible range found:', r.from, r.to);
                    // 안드로이드 콜백 호출
                    if (window.ChartBridge && window.ChartBridge.onPatternVisibleRange) {
                        window.ChartBridge.onPatternVisibleRange(r.from.toString(), r.to.toString());
                    }
                    return [r.from, r.to];
                } catch(e) { 
                    console.error('LAGO: getVisibleTimeRange 에러:', e);
                    if (window.ChartBridge && window.ChartBridge.onPatternVisibleRangeError) {
                        window.ChartBridge.onPatternVisibleRangeError('JavaScript 에러: ' + e.message);
                    }
                    return null; 
                }
            })();
        """.trimIndent()

        enqueueOrEval(jsFunction)
    }

    @JavascriptInterface
    fun onPatternVisibleRange(fromTime: String, toTime: String) {
        android.util.Log.d("JsBridge", "onPatternVisibleRange callback: $fromTime ~ $toTime")
        try {
            val fromEpoch = ChartTimeManager.normalizeToEpochSeconds(fromTime.toLong())
            val toEpoch = ChartTimeManager.normalizeToEpochSeconds(toTime.toLong())

            ChartTimeManager.debugTimeInfo("Pattern Analysis From", fromEpoch)
            ChartTimeManager.debugTimeInfo("Pattern Analysis To", toEpoch)

            patternListener?.onPatternVisibleRange(fromEpoch, toEpoch)
        } catch (e: Exception) {
            android.util.Log.e("JsBridge", "시간 파싱 에러", e)
            patternListener?.onPatternVisibleRangeError("시간 파싱 에러: ${e.message}")
        }
    }

    @JavascriptInterface
    fun onPatternVisibleRangeError(message: String) {
        android.util.Log.w("JsBridge", "onPatternVisibleRangeError: $message")
        patternListener?.onPatternVisibleRangeError(message)
    }

    @JavascriptInterface
    fun onVisibleRangeAnalysis(fromTime: String, toTime: String) {
        android.util.Log.d("JsBridge", "📊 [4단계] onVisibleRangeAnalysis 콜백 진입: $fromTime ~ $toTime")

        patternAnalysisListener?.let { listener ->
            android.util.Log.d("JsBridge", "📊 [4단계] listener 존재 - onAnalyzePatternInRange 호출")
            listener.onAnalyzePatternInRange(fromTime, toTime)
        } ?: run {
            android.util.Log.w("JsBridge", "📊 [4단계] patternAnalysisListener가 null - 분석 진행 불가")
        }
    }

    fun displayPatternResult(result: com.lago.app.domain.entity.PatternAnalysisResult) {
        val resultJson = gson.toJson(mapOf(
            "stockCode" to result.stockCode,
            "patterns" to result.patterns,
            "analysisTime" to result.analysisTime,
            "chartMode" to result.chartMode,
            "timeFrame" to result.timeFrame
        ))

        enqueueOrEval("window.displayPatternResult && window.displayPatternResult(${resultJson.quote()})")
    }

    /**
     * 무한 히스토리 데이터 추가 (개선된 버전)
     * ChartTimeManager를 사용하여 시간 정규화
     */
    fun prependHistoricalData(historicalCandles: List<CandleData>, historicalVolumes: List<VolumeData> = emptyList()) {
        val normalizedCandles = historicalCandles.map { candle ->
            val normalizedTime = ChartTimeManager.normalizeToEpochSeconds(candle.time)
            candle.copy(time = normalizedTime)
        }

        val normalizedVolumes = historicalVolumes.map { volume ->
            val normalizedTime = ChartTimeManager.normalizeToEpochSeconds(volume.time)
            volume.copy(time = normalizedTime)
        }

        val candlesJson = gson.toJson(normalizedCandles)
        val volumesJson = gson.toJson(normalizedVolumes)
        enqueueOrEval("""window.prependHistoricalData(${candlesJson.quote()}, ${volumesJson.quote()})""")
    }

    @Deprecated("Use prependHistoricalData instead")
    fun addHistoricalData(historicalDataJson: String) {
        val escapedJson = historicalDataJson.replace("'", "\\'").replace("\"", "\\\"")
        enqueueOrEval("""window.addHistoricalData('$escapedJson')""")
    }

    /**
     * JS 실행 전 ready 상태 확인 및 큐 관리
     */
    private fun enqueueOrEval(script: String) {
        if (!ready) {
            queue.addLast(script)
            android.util.Log.d("JsBridge", "큐에 저장: ${script.take(50)}... (큐 크기: ${queue.size})")
            return
        }
        eval(script)
    }

    private fun eval(script: String) {
        android.util.Log.v("JsBridge", "JS 실행: ${script.take(100)}...")
        webView.post { webView.evaluateJavascript(script, null) }
    }

    private fun String.quote(): String = JSONObject.quote(this)

    companion object {
        /**
         * TradingView 차트 시간 포맷 변환 유틸리티 (Deprecated)
         * ChartTimeManager 사용 권장
         */
        @Deprecated("Use ChartTimeManager instead")
        object TimeUtils {
            fun formatDailyTime(unixTimestamp: Long): String {
                return ChartTimeManager.formatForDisplay(
                    ChartTimeManager.normalizeToEpochSeconds(unixTimestamp),
                    false
                )
            }

            fun formatIntraTime(unixMillis: Long): Long {
                return ChartTimeManager.normalizeToEpochSeconds(unixMillis)
            }

            fun formatTimeForChart(timestamp: Long, timeFrame: String): Any {
                val normalized = ChartTimeManager.normalizeToEpochSeconds(timestamp)
                return if (ChartTimeManager.isDailyOrAbove(timeFrame)) {
                    ChartTimeManager.formatForDisplay(normalized, false)
                } else {
                    normalized
                }
            }
        }
    }
}
