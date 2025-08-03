package com.lago.app.presentation.ui.chart

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.lago.app.domain.entity.ChartConfig
import com.lago.app.domain.entity.CandlestickData
import com.lago.app.domain.entity.VolumeData
import com.lago.app.domain.entity.LineData
import com.lago.app.domain.entity.MACDResult
import com.lago.app.domain.entity.BollingerBandsResult
import kotlin.math.roundToInt

// 실제 길이 기반 패널 크기 (픽셀 단위)
data class PanelSizesInPx(
    val mainChart: Int = 250,      // 250px
    val volume: Int = 80,          // 80px
    val rsi: Int = 60,            // 60px
    val macd: Int = 60            // 60px
) {
    fun totalHeight(): Int = mainChart + volume + rsi + macd
    
    fun toNormalizedPanelSizes(): PanelSizes {
        val total = totalHeight().toFloat()
        return PanelSizes(
            mainChartHeight = mainChart / total,
            volumeHeight = volume / total,
            rsiHeight = rsi / total,
            macdHeight = macd / total
        )
    }
}

@Composable
fun ImprovedSmoothDragChart(
    candlestickData: List<CandlestickData>,
    volumeData: List<VolumeData>,
    sma5Data: List<LineData>,
    sma20Data: List<LineData>,
    config: ChartConfig,
    rsiData: List<LineData> = emptyList(),
    macdData: MACDResult? = null,
    bollingerBands: BollingerBandsResult? = null,
    modifier: Modifier = Modifier
) {
    // 실제 길이 기반 패널 크기 상태
    var panelSizesInPx by remember { mutableStateOf(PanelSizesInPx()) }
    var totalHeightPx by remember { mutableStateOf(1f) }
    var isDragging by remember { mutableStateOf(false) }
    var dragTargetIndex by remember { mutableStateOf(-1) }
    
    // 디바운스된 패널 크기 (차트 업데이트용)
    var debouncedPanelSizes by remember { mutableStateOf(panelSizesInPx.toNormalizedPanelSizes()) }
    val coroutineScope = rememberCoroutineScope()
    
    // 애니메이션된 디바이더 위치
    val animatedMainDividerY = remember { Animatable(panelSizesInPx.mainChart.toFloat()) }
    val animatedVolumeDividerY = remember { Animatable((panelSizesInPx.mainChart + panelSizesInPx.volume).toFloat()) }
    val animatedRsiDividerY = remember { Animatable((panelSizesInPx.mainChart + panelSizesInPx.volume + panelSizesInPx.rsi).toFloat()) }
    
    // 패널 크기 변경 시 애니메이션
    LaunchedEffect(panelSizesInPx) {
        animatedMainDividerY.animateTo(
            panelSizesInPx.mainChart.toFloat(),
            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
        )
        animatedVolumeDividerY.animateTo(
            (panelSizesInPx.mainChart + panelSizesInPx.volume).toFloat(),
            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
        )
        animatedRsiDividerY.animateTo(
            (panelSizesInPx.mainChart + panelSizesInPx.volume + panelSizesInPx.rsi).toFloat(),
            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
        )
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                totalHeightPx = coordinates.size.height.toFloat()
                Log.d("SmoothDrag", "Total height: ${totalHeightPx}px")
            }
    ) {
        // 기존 OptimizedChartView (시간축 공유 보장)
        OptimizedChartView(
            candlestickData = candlestickData,
            volumeData = volumeData,
            sma5Data = sma5Data,
            sma20Data = sma20Data,
            config = config,
            rsiData = rsiData,
            macdData = macdData,
            bollingerBands = bollingerBands,
            panelSizes = debouncedPanelSizes,
            modifier = Modifier.fillMaxSize()
        )
        
        // 부드러운 드래그 디바이더들
        if (config.indicators.volume) {
            SmoothDragDivider(
                positionY = animatedMainDividerY.value.roundToInt(),
                isDragging = isDragging && dragTargetIndex == 0,
                onDragStart = { 
                    isDragging = true
                    dragTargetIndex = 0
                    Log.d("SmoothDrag", "Main-Volume divider drag started")
                },
                onDragEnd = {
                    isDragging = false
                    dragTargetIndex = -1
                    // 500ms 디바운스 후 차트 업데이트
                    coroutineScope.launch {
                        delay(500)
                        debouncedPanelSizes = panelSizesInPx.toNormalizedPanelSizes()
                        Log.d("SmoothDrag", "Chart updated with new panel sizes")
                    }
                },
                onDrag = { deltaY ->
                    val newMainHeight = (panelSizesInPx.mainChart + deltaY.roundToInt()).coerceIn(150, 400)
                    val newVolumeHeight = (panelSizesInPx.volume - deltaY.roundToInt()).coerceIn(50, 150)
                    
                    panelSizesInPx = panelSizesInPx.copy(
                        mainChart = newMainHeight,
                        volume = newVolumeHeight
                    )
                    Log.d("SmoothDrag", "Main: ${newMainHeight}px, Volume: ${newVolumeHeight}px")
                },
                modifier = Modifier.zIndex(10f)
            )
        }
        
        if (config.indicators.volume && config.indicators.rsi) {
            SmoothDragDivider(
                positionY = animatedVolumeDividerY.value.roundToInt(),
                isDragging = isDragging && dragTargetIndex == 1,
                onDragStart = { 
                    isDragging = true
                    dragTargetIndex = 1
                },
                onDragEnd = {
                    isDragging = false
                    dragTargetIndex = -1
                    coroutineScope.launch {
                        delay(500)
                        debouncedPanelSizes = panelSizesInPx.toNormalizedPanelSizes()
                    }
                },
                onDrag = { deltaY ->
                    val newVolumeHeight = (panelSizesInPx.volume + deltaY.roundToInt()).coerceIn(50, 150)
                    val newRsiHeight = (panelSizesInPx.rsi - deltaY.roundToInt()).coerceIn(40, 120)
                    
                    panelSizesInPx = panelSizesInPx.copy(
                        volume = newVolumeHeight,
                        rsi = newRsiHeight
                    )
                },
                modifier = Modifier.zIndex(10f)
            )
        }
        
        if (config.indicators.rsi && config.indicators.macd) {
            SmoothDragDivider(
                positionY = animatedRsiDividerY.value.roundToInt(),
                isDragging = isDragging && dragTargetIndex == 2,
                onDragStart = { 
                    isDragging = true
                    dragTargetIndex = 2
                },
                onDragEnd = {
                    isDragging = false 
                    dragTargetIndex = -1
                    coroutineScope.launch {
                        delay(500)
                        debouncedPanelSizes = panelSizesInPx.toNormalizedPanelSizes()
                    }
                },
                onDrag = { deltaY ->
                    val newRsiHeight = (panelSizesInPx.rsi + deltaY.roundToInt()).coerceIn(40, 120)
                    val newMacdHeight = (panelSizesInPx.macd - deltaY.roundToInt()).coerceIn(40, 120)
                    
                    panelSizesInPx = panelSizesInPx.copy(
                        rsi = newRsiHeight,
                        macd = newMacdHeight
                    )
                },
                modifier = Modifier.zIndex(10f)
            )
        }
        
        // 토스 스타일 우측 지표 정보
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
        ) {
            // 메인 차트 가격 정보 (애니메이션된 위치)
            Box(
                modifier = Modifier
                    .height(with(LocalDensity.current) { animatedMainDividerY.value.roundToInt().toDp() }),
                contentAlignment = Alignment.Center
            ) {
                PriceInfoCard(
                    currentPrice = candlestickData.lastOrNull()?.close ?: 0f,
                    priceChange = 0f // TODO: 계산
                )
            }
            
            // 거래량 정보
            if (config.indicators.volume) {
                Box(
                    modifier = Modifier
                        .height(with(LocalDensity.current) { panelSizesInPx.volume.toDp() }),
                    contentAlignment = Alignment.Center
                ) {
                    VolumeInfoCard(
                        currentVolume = volumeData.lastOrNull()?.value ?: 0f
                    )
                }
            }
            
            // RSI 정보
            if (config.indicators.rsi) {
                Box(
                    modifier = Modifier
                        .height(with(LocalDensity.current) { panelSizesInPx.rsi.toDp() }),
                    contentAlignment = Alignment.Center
                ) {
                    RsiInfoCard(
                        currentRsi = rsiData.lastOrNull()?.value ?: 0f
                    )
                }
            }
            
            // MACD 정보
            if (config.indicators.macd) {
                Box(
                    modifier = Modifier
                        .height(with(LocalDensity.current) { panelSizesInPx.macd.toDp() }),
                    contentAlignment = Alignment.Center
                ) {
                    MacdInfoCard(
                        macdData = macdData
                    )
                }
            }
        }
    }
}

@Composable
private fun SmoothDragDivider(
    positionY: Int,
    isDragging: Boolean,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDrag: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp) // 큰 터치 영역
            .offset { IntOffset(0, positionY - with(density) { 10.dp.roundToPx() }) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { 
                        onDragStart()
                        Log.d("SmoothDrag", "Drag started at Y: $positionY")
                    },
                    onDragEnd = { 
                        onDragEnd()
                        Log.d("SmoothDrag", "Drag ended")
                    }
                ) { _, dragAmount ->
                    onDrag(dragAmount.y)
                }
            }
    ) {
        // 시각적 디바이더 라인
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isDragging) 3.dp else 1.dp)
                .background(
                    if (isDragging) Color(0xFF4CAF50) else Color(0xFFE0E0E0)
                )
                .align(Alignment.Center)
        )
        
        // 드래그 중 핸들 표시
        if (isDragging) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(8.dp)
                    .background(
                        Color(0xFF4CAF50),
                        RoundedCornerShape(4.dp)
                    )
                    .align(Alignment.Center)
            )
        }
    }
}

// 토스 스타일 지표 카드들
@Composable
private fun PriceInfoCard(
    currentPrice: Float,
    priceChange: Float
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = String.format("%,.0f", currentPrice),
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            if (priceChange != 0f) {
                Text(
                    text = String.format("%+.1f", priceChange),
                    color = if (priceChange > 0) Color(0xFF4CAF50) else Color(0xFFFF5252),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun VolumeInfoCard(
    currentVolume: Float
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text("Vol", color = Color.White, fontSize = 10.sp)
            Text(
                text = String.format("%.0f", currentVolume),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RsiInfoCard(
    currentRsi: Float
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text("RSI", color = Color.White, fontSize = 10.sp)
            Text(
                text = String.format("%.1f", currentRsi),
                color = when {
                    currentRsi > 70 -> Color(0xFFFF5252) // 과매수
                    currentRsi < 30 -> Color(0xFF4CAF50) // 과매도
                    else -> Color.White
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MacdInfoCard(
    macdData: MACDResult?
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text("MACD", color = Color.White, fontSize = 10.sp)
            
            val macdValue = macdData?.macdLine?.lastOrNull()?.value ?: 0f
            val histogramValue = macdData?.histogram?.lastOrNull()?.value ?: 0f
            
            Text(
                text = String.format("%.3f", macdValue),
                color = Color(0xFF4CAF50),
                fontSize = 10.sp
            )
            Text(
                text = String.format("%.3f", histogramValue),
                color = if (histogramValue >= 0) Color(0xFF2196F3) else Color(0xFFFF5252),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}