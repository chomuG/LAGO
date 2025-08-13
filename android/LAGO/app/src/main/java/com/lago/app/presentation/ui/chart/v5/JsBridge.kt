package com.lago.app.presentation.ui.chart.v5

import android.webkit.WebView
import org.json.JSONObject
import com.google.gson.Gson
import java.util.ArrayDeque

data class Candle(val time: Long, val open: Int, var high: Int, var low: Int, var close: Int)
data class VolumeBar(val time: Long, val value: Long)

class JsBridge(private val webView: WebView, private val gson: Gson = Gson()) {
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
        enqueue("""window.updateBar(${j.quote()})""")
    }

    fun updateVolume(vol: VolumeBar) {
        val j = gson.toJson(vol)
        enqueue("""window.updateVolume(${j.quote()})""")
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