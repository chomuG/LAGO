package com.lago.app.presentation.ui.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.lago.app.domain.entity.CandlestickData
import com.lago.app.domain.entity.ChartConfig
import com.lago.app.domain.entity.LineData
import com.lago.app.domain.entity.VolumeData
import com.lago.app.domain.entity.MACDResult
import com.lago.app.domain.entity.BollingerBandsResult
import com.lago.app.presentation.viewmodel.chart.ChartUiState

@Composable
fun ChartAndTimeFrameSelection(
    uiState: ChartUiState,
    onTimeFrameChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 차트 영역
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)  // 높이를 더 크게 설정
                .padding(horizontal = 16.dp)
                .background(Color(0xFFF5F5F5)) // 임시 배경색으로 차트 영역 확인
                .padding(horizontal = 8.dp)
        ) {
            OptimizedChartView(
                candlestickData = uiState.candlestickData,
                volumeData = uiState.volumeData,
                sma5Data = uiState.sma5Data,
                sma20Data = uiState.sma20Data,
                config = uiState.config,
                rsiData = uiState.rsiData,
                macdData = uiState.macdData,
                bollingerBands = uiState.bollingerBands,
                panelSizes = PanelSizes(),
                modifier = Modifier
                    .fillMaxSize()
                    .semantics {
                        contentDescription = "주식 차트: 현재 가격 ${uiState.currentStock.currentPrice}원"
                    }
            )
        }
        
        // 시간대 선택
        TimeFrameSelection(
            selectedTimeFrame = uiState.config.timeFrame,
            onTimeFrameChange = onTimeFrameChange,
            modifier = Modifier
                .background(Color.White)
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                )
        )
    }
}