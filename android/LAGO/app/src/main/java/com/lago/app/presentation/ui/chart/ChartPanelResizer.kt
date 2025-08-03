package com.lago.app.presentation.ui.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch

// 패널 크기 상태
data class PanelSizes(
    val mainChartHeight: Float = 0.6f,
    val volumeHeight: Float = 0.2f,
    val rsiHeight: Float = 0.1f,
    val macdHeight: Float = 0.1f
)

@Composable
fun ChartPanelDivider(
    modifier: Modifier = Modifier,
    onDrag: (Float) -> Unit = {}
) {
    val density = LocalDensity.current
    var isDragging by remember { mutableStateOf(false) }
    
    // 터치 영역을 더 크게 만들기 위한 컨테이너
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(30.dp) // 터치 영역을 30dp로 확대
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { 
                        isDragging = true
                        println("🔴 Panel divider drag started")
                    },
                    onDragEnd = { 
                        isDragging = false
                        println("🔴 Panel divider drag ended")
                    }
                ) { _, dragAmount ->
                    // 더 직관적인 드래그 감도 조정
                    val dragPercentage = with(density) {
                        dragAmount.y.toDp().value / 1000f // 더 세밀한 조정을 위한 감도 조정
                    }
                    println("🔴 Panel divider dragging: $dragAmount, percentage: $dragPercentage")
                    onDrag(dragPercentage)
                }
            }
    ) {
        // 실제 디바이더 선
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isDragging) 6.dp else 3.dp)
                .background(
                    if (isDragging) Color(0xFF2196F3) else Color(0xFF888888), // 정상 색상으로 되돌림
                    RoundedCornerShape(1.dp)
                )
                .align(Alignment.Center)
        )
        
        // 항상 보이는 그립 표시 (더 명확하게)
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .background(
                    if (isDragging) Color(0xFF2196F3) else Color(0xFF888888),
                    RoundedCornerShape(2.dp)
                )
                .padding(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            repeat(5) {
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .background(Color.White, CircleShape)
                )
            }
        }
    }
}

// 패널 크기 관리 함수
@Composable
fun rememberPanelSizeState(): MutableState<PanelSizes> {
    return remember { mutableStateOf(PanelSizes()) }
}

// 패널 크기 조정 함수
fun adjustPanelSizes(
    currentSizes: PanelSizes,
    dividerIndex: Int,
    delta: Float
): PanelSizes {
    val minSize = 0.05f // 최소 5%
    val maxSize = 0.8f  // 최대 80%
    
    return when (dividerIndex) {
        0 -> {
            // 메인 차트와 Volume 사이
            val newMainHeight = (currentSizes.mainChartHeight + delta).coerceIn(minSize, maxSize)
            val newVolumeHeight = (currentSizes.volumeHeight - delta).coerceIn(minSize, maxSize)
            currentSizes.copy(
                mainChartHeight = newMainHeight,
                volumeHeight = newVolumeHeight
            )
        }
        1 -> {
            // Volume과 RSI 사이
            val newVolumeHeight = (currentSizes.volumeHeight + delta).coerceIn(minSize, maxSize)
            val newRsiHeight = (currentSizes.rsiHeight - delta).coerceIn(minSize, maxSize)
            currentSizes.copy(
                volumeHeight = newVolumeHeight,
                rsiHeight = newRsiHeight
            )
        }
        2 -> {
            // RSI와 MACD 사이
            val newRsiHeight = (currentSizes.rsiHeight + delta).coerceIn(minSize, maxSize)
            val newMacdHeight = (currentSizes.macdHeight - delta).coerceIn(minSize, maxSize)
            currentSizes.copy(
                rsiHeight = newRsiHeight,
                macdHeight = newMacdHeight
            )
        }
        else -> currentSizes
    }
}

@Composable
fun ChartPanelDividers(
    panelSizes: PanelSizes,
    config: com.lago.app.domain.entity.ChartConfig,
    onPanelSizeChange: (PanelSizes) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentSizes by remember(panelSizes) { mutableStateOf(panelSizes) }
    
    BoxWithConstraints(modifier = modifier) {
        val totalHeight = maxHeight
        
        // 동적 위치 계산
        val mainChartEnd = currentSizes.mainChartHeight
        val volumeEnd = mainChartEnd + currentSizes.volumeHeight
        val rsiEnd = volumeEnd + currentSizes.rsiHeight
        
        // 1. 메인 차트와 Volume 사이 디바이더
        if (config.indicators.volume) {
            ChartPanelDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = totalHeight * mainChartEnd)
                    .zIndex(100f),
                onDrag = { delta ->
                    val newSizes = adjustPanelSizes(currentSizes, 0, delta)
                    currentSizes = newSizes
                    println("🟢 Panel size changed - main: ${newSizes.mainChartHeight}, volume: ${newSizes.volumeHeight}")
                    onPanelSizeChange(newSizes)
                }
            )
        }
        
        // 2. Volume과 RSI 사이 디바이더
        if (config.indicators.volume && config.indicators.rsi) {
            ChartPanelDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = totalHeight * volumeEnd)
                    .zIndex(100f),
                onDrag = { delta ->
                    val newSizes = adjustPanelSizes(currentSizes, 1, delta)
                    currentSizes = newSizes
                    onPanelSizeChange(newSizes)
                }
            )
        }
        
        // 3. RSI와 MACD 사이 디바이더
        if (config.indicators.rsi && config.indicators.macd) {
            ChartPanelDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = totalHeight * rsiEnd)
                    .zIndex(100f),
                onDrag = { delta ->
                    val newSizes = adjustPanelSizes(currentSizes, 2, delta)
                    currentSizes = newSizes
                    onPanelSizeChange(newSizes)
                }
            )
        }
    }
}