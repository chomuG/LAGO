package com.lago.app.presentation.ui.chart

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import com.lago.app.domain.entity.ChartConfig
import com.lago.app.domain.entity.CandlestickData
import com.lago.app.domain.entity.VolumeData
import com.lago.app.domain.entity.LineData
import com.lago.app.domain.entity.MACDResult
import com.lago.app.domain.entity.BollingerBandsResult

@Composable
fun ImprovedResizablePanels(
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
    // 부드러운 드래그를 위한 상태
    var totalHeightPx by remember { mutableStateOf(1f) }
    var mainChartWeight by remember { mutableStateOf(0.6f) }
    var volumeWeight by remember { mutableStateOf(0.2f) }
    var rsiWeight by remember { mutableStateOf(0.1f) }
    var macdWeight by remember { mutableStateOf(0.1f) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                totalHeightPx = coordinates.size.height.toFloat()
            }
    ) {
        // 1. 메인 차트 패널
        Box(
            modifier = Modifier
                .weight(mainChartWeight)
                .fillMaxWidth()
        ) {
            // 기존 OptimizedChartView를 메인 차트 전용으로 사용
            OptimizedChartView(
                candlestickData = candlestickData,
                volumeData = emptyList(), // 메인 패널에서는 볼륨 제외
                sma5Data = sma5Data,
                sma20Data = sma20Data,
                config = config.copy(
                    indicators = config.indicators.copy(
                        volume = false, // 볼륨은 별도 패널
                        rsi = false,    // RSI는 별도 패널
                        macd = false    // MACD는 별도 패널
                    )
                ),
                bollingerBands = bollingerBands,
                panelSizes = PanelSizes(
                    mainChartHeight = 1.0f, // 전체 높이 사용
                    volumeHeight = 0f,
                    rsiHeight = 0f,
                    macdHeight = 0f
                ),
                modifier = Modifier.fillMaxSize()
            )
            
            // 우측 현재 가격 표시 (토스 스타일)
            CurrentPriceIndicator(
                currentPrice = candlestickData.lastOrNull()?.close ?: 0f,
                priceChange = 0f, // TODO: 가격 변화 계산
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
        
        // 메인 차트 - 거래량 디바이더
        if (config.indicators.volume) {
            SmoothResizableDivider(
                onDragDelta = { deltaY ->
                    val deltaWeight = deltaY / totalHeightPx
                    val newMainWeight = (mainChartWeight - deltaWeight).coerceIn(0.3f, 0.8f)
                    val newVolumeWeight = (volumeWeight + deltaWeight).coerceIn(0.1f, 0.4f)
                    
                    // 정규화하여 전체 합이 1.0이 되도록
                    val total = newMainWeight + newVolumeWeight + rsiWeight + macdWeight
                    mainChartWeight = newMainWeight / total
                    volumeWeight = newVolumeWeight / total
                    rsiWeight = rsiWeight / total
                    macdWeight = macdWeight / total
                }
            )
            
            // 2. 거래량 패널
            Box(
                modifier = Modifier
                    .weight(volumeWeight)
                    .fillMaxWidth()
            ) {
                OptimizedChartView(
                    candlestickData = emptyList(),
                    volumeData = volumeData,
                    sma5Data = emptyList(),
                    sma20Data = emptyList(),
                    config = config.copy(
                        indicators = config.indicators.copy(
                            volume = true,
                            rsi = false,
                            macd = false,
                            sma5 = false,
                            sma20 = false,
                            bollingerBands = false
                        )
                    ),
                    panelSizes = PanelSizes(
                        mainChartHeight = 0f,
                        volumeHeight = 1.0f, // 전체 높이 사용
                        rsiHeight = 0f,
                        macdHeight = 0f
                    ),
                    modifier = Modifier.fillMaxSize()
                )
                
                // 우측 거래량 정보
                VolumeIndicator(
                    currentVolume = volumeData.lastOrNull()?.value ?: 0f,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
        
        // 거래량 - RSI 디바이더
        if (config.indicators.volume && config.indicators.rsi) {
            SmoothResizableDivider(
                onDragDelta = { deltaY ->
                    val deltaWeight = deltaY / totalHeightPx
                    val newVolumeWeight = (volumeWeight - deltaWeight).coerceIn(0.1f, 0.4f)
                    val newRsiWeight = (rsiWeight + deltaWeight).coerceIn(0.05f, 0.3f)
                    
                    val total = mainChartWeight + newVolumeWeight + newRsiWeight + macdWeight
                    mainChartWeight = mainChartWeight / total
                    volumeWeight = newVolumeWeight / total
                    rsiWeight = newRsiWeight / total
                    macdWeight = macdWeight / total
                }
            )
        }
        
        // 3. RSI 패널
        if (config.indicators.rsi) {
            Box(
                modifier = Modifier
                    .weight(rsiWeight)
                    .fillMaxWidth()
            ) {
                OptimizedChartView(
                    candlestickData = emptyList(),
                    volumeData = emptyList(),
                    sma5Data = emptyList(),
                    sma20Data = emptyList(),
                    config = config.copy(
                        indicators = config.indicators.copy(
                            volume = false,
                            rsi = true,
                            macd = false,
                            sma5 = false,
                            sma20 = false,
                            bollingerBands = false
                        )
                    ),
                    rsiData = rsiData,
                    panelSizes = PanelSizes(
                        mainChartHeight = 0f,
                        volumeHeight = 0f,
                        rsiHeight = 1.0f, // 전체 높이 사용
                        macdHeight = 0f
                    ),
                    modifier = Modifier.fillMaxSize()
                )
                
                // 우측 RSI 정보
                RsiIndicator(
                    currentRsi = rsiData.lastOrNull()?.value ?: 0f,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
        
        // RSI - MACD 디바이더
        if (config.indicators.rsi && config.indicators.macd) {
            SmoothResizableDivider(
                onDragDelta = { deltaY ->
                    val deltaWeight = deltaY / totalHeightPx
                    val newRsiWeight = (rsiWeight - deltaWeight).coerceIn(0.05f, 0.3f)
                    val newMacdWeight = (macdWeight + deltaWeight).coerceIn(0.05f, 0.3f)
                    
                    val total = mainChartWeight + volumeWeight + newRsiWeight + newMacdWeight
                    mainChartWeight = mainChartWeight / total
                    volumeWeight = volumeWeight / total
                    rsiWeight = newRsiWeight / total
                    macdWeight = newMacdWeight / total
                }
            )
        }
        
        // 4. MACD 패널
        if (config.indicators.macd) {
            Box(
                modifier = Modifier
                    .weight(macdWeight)
                    .fillMaxWidth()
            ) {
                OptimizedChartView(
                    candlestickData = emptyList(),
                    volumeData = emptyList(),
                    sma5Data = emptyList(),
                    sma20Data = emptyList(),
                    config = config.copy(
                        indicators = config.indicators.copy(
                            volume = false,
                            rsi = false,
                            macd = true,
                            sma5 = false,
                            sma20 = false,
                            bollingerBands = false
                        )
                    ),
                    macdData = macdData,
                    panelSizes = PanelSizes(
                        mainChartHeight = 0f,
                        volumeHeight = 0f,
                        rsiHeight = 0f,
                        macdHeight = 1.0f // 전체 높이 사용
                    ),
                    modifier = Modifier.fillMaxSize()
                )
                
                // 우측 MACD 정보
                MacdIndicator(
                    macdData = macdData,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
    }
}

@Composable
private fun SmoothResizableDivider(
    onDragDelta: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isDragging by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(12.dp) // 터치 영역을 더 크게
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { 
                        isDragging = true 
                    },
                    onDragEnd = { 
                        isDragging = false 
                    }
                ) { _, dragAmount ->
                    coroutineScope.launch {
                        onDragDelta(dragAmount.y)
                    }
                }
            }
    ) {
        // 디바이더 선 (토스 스타일)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isDragging) 2.dp else 1.dp)
                .background(
                    if (isDragging) Color(0xFF4CAF50) else Color(0xFFE0E0E0)
                )
                .align(Alignment.Center)
        )
        
        // 드래그 중일 때 중앙 핸들 표시
        if (isDragging) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(
                        Color(0xFF4CAF50),
                        RoundedCornerShape(2.dp)
                    )
                    .align(Alignment.Center)
            )
        }
    }
}

// 토스 스타일 우측 지표 정보들
@Composable
private fun CurrentPriceIndicator(
    currentPrice: Float,
    priceChange: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(end = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = String.format("%,.0f", currentPrice),
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun VolumeIndicator(
    currentVolume: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(end = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "Vol",
                color = Color.White,
                fontSize = 9.sp
            )
            Text(
                text = String.format("%.0f", currentVolume),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RsiIndicator(
    currentRsi: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(end = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "RSI",
                color = Color.White,
                fontSize = 9.sp
            )
            Text(
                text = String.format("%.1f", currentRsi),
                color = when {
                    currentRsi > 70 -> Color(0xFFFF5252) // 과매수 - 빨강
                    currentRsi < 30 -> Color(0xFF4CAF50) // 과매도 - 초록
                    else -> Color.White
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MacdIndicator(
    macdData: MACDResult?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(end = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "MACD",
                color = Color.White,
                fontSize = 9.sp
            )
            
            val macdValue = macdData?.macdLine?.lastOrNull()?.value ?: 0f
            val histogramValue = macdData?.histogram?.lastOrNull()?.value ?: 0f
            
            Text(
                text = String.format("%.3f", macdValue),
                color = Color(0xFF4CAF50),
                fontSize = 9.sp
            )
            Text(
                text = String.format("%.3f", histogramValue),
                color = if (histogramValue >= 0) Color(0xFF2196F3) else Color(0xFFFF5252),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}