package com.lago.app.presentation.ui.chart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// 패널 레이아웃 비율 상태 (기존 PanelSizes 기반)
data class PanelRatios(
    val mainChart: Float = 0.6f,      // 60% - 메인 캔들차트
    val volume: Float = 0.25f,        // 25% - 거래량
    val indicators: Float = 0.15f     // 15% - RSI 등 지표들
) {
    // 전체 합이 1.0이 되도록 정규화
    fun normalized(): PanelRatios {
        val total = mainChart + volume + indicators
        return PanelRatios(
            mainChart = mainChart / total,
            volume = volume / total,
            indicators = indicators / total
        )
    }
}

// 미리 정의된 레이아웃 프리셋
object ChartLayoutPresets {
    val CHART_FOCUSED = PanelRatios(0.75f, 0.2f, 0.05f)    // 차트 위주
    val BALANCED = PanelRatios(0.6f, 0.25f, 0.15f)         // 균등 분할
    val INDICATOR_FOCUSED = PanelRatios(0.5f, 0.2f, 0.3f)  // 지표 위주
    val VOLUME_FOCUSED = PanelRatios(0.55f, 0.35f, 0.1f)   // 거래량 위주
}

@Composable
fun ChartLayoutController(
    currentRatios: PanelRatios,
    onRatiosChange: (PanelRatios) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "차트 레이아웃",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF333333)
            )
            
            // 프리셋 버튼들
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                LayoutPresetButton(
                    text = "차트위주",
                    preset = ChartLayoutPresets.CHART_FOCUSED,
                    currentRatios = currentRatios,
                    onSelect = onRatiosChange,
                    modifier = Modifier.weight(1f)
                )
                
                LayoutPresetButton(
                    text = "균등분할",
                    preset = ChartLayoutPresets.BALANCED,
                    currentRatios = currentRatios,
                    onSelect = onRatiosChange,
                    modifier = Modifier.weight(1f)
                )
                
                LayoutPresetButton(
                    text = "지표위주",
                    preset = ChartLayoutPresets.INDICATOR_FOCUSED,
                    currentRatios = currentRatios,
                    onSelect = onRatiosChange,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // 현재 비율 표시
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                RatioIndicator("차트", currentRatios.mainChart, Color(0xFF4CAF50))
                RatioIndicator("거래량", currentRatios.volume, Color(0xFF2196F3))
                RatioIndicator("지표", currentRatios.indicators, Color(0xFF9C27B0))
            }
        }
    }
}

@Composable
private fun LayoutPresetButton(
    text: String,
    preset: PanelRatios,
    currentRatios: PanelRatios,
    onSelect: (PanelRatios) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = kotlin.math.abs(currentRatios.mainChart - preset.mainChart) < 0.01f &&
                    kotlin.math.abs(currentRatios.volume - preset.volume) < 0.01f &&
                    kotlin.math.abs(currentRatios.indicators - preset.indicators) < 0.01f
    
    Button(
        onClick = { onSelect(preset.normalized()) },
        modifier = modifier.height(36.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF4CAF50) else Color(0xFFE0E0E0),
            contentColor = if (isSelected) Color.White else Color(0xFF666666)
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun RatioIndicator(
    label: String,
    ratio: Float,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${(ratio * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF888888)
        )
    }
}

// 패널 비율 상태 관리
@Composable
fun rememberPanelRatios(
    initial: PanelRatios = ChartLayoutPresets.BALANCED
): MutableState<PanelRatios> {
    return remember { mutableStateOf(initial.normalized()) }
}