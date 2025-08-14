package com.lago.app.presentation.ui.chart.v5

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.lago.app.presentation.ui.chart.WebChartScreen

/**
 * Minimal realtime chart that loads fixed HTML and exposes JsBridge onReady.
 * Properly separates WebView ready and Chart ready events.
 */
@Composable
fun MultiPanelChartRealtime(
    modifier: Modifier = Modifier,
    onReady: (JsBridge) -> Unit
) {
    val html = remember { ChartHtmlTemplate.get() }
    var bridge by remember { mutableStateOf<JsBridge?>(null) }

    WebChartScreen(
        htmlContent = html,
        modifier = modifier.fillMaxSize(),
        onWebViewReady = { webView ->
            // ① WebView 핸들 확보 → 브릿지 생성 (차트 준비는 아직 안됨)
            bridge = JsBridge(webView)
        },
        onChartReady = {
            // ② JS가 Android.onChartReady() 호출 후
            bridge?.markReady()                  // 준비 완료 → 큐 flush
            bridge?.let(onReady)                 // 이제부터 호출 안전
        }
    )
}