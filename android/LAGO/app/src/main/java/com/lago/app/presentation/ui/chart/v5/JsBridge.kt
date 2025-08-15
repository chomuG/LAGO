package com.lago.app.presentation.ui.chart.v5

import android.webkit.WebView
import android.webkit.JavascriptInterface
import org.json.JSONObject
import com.google.gson.Gson
import java.util.ArrayDeque

data class Candle(val time: Long, val open: Int, var high: Int, var low: Int, var close: Int)
data class VolumeBar(val time: Long, val value: Long)

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

    fun setInitialData(candles: List<Candle>, volumes: List<VolumeBar> = emptyList()) {
        val c = gson.toJson(candles)
        val v = gson.toJson(volumes)
        enqueue("""window.setInitialData(${c.quote()}, ${v.quote()})""")
    }

    fun updateBar(bar: Candle) {
        val j = gson.toJson(bar)
        enqueue("""window.updateBar('main', ${j.quote()})""")
    }

    fun updateVolume(vol: VolumeBar) {
        val j = gson.toJson(vol)
        enqueue("""window.updateVolume(${j.quote()})""")
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
}