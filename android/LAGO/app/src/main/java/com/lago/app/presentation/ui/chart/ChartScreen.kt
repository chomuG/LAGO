package com.lago.app.presentation.ui.chart

import android.view.ViewGroup
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.alpha
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.unit.times
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.absoluteValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.lago.app.domain.entity.CandlestickData
import com.lago.app.domain.entity.ChartConfig
import com.lago.app.domain.entity.LineData
import com.lago.app.domain.entity.VolumeData
import com.lago.app.domain.entity.StockInfo
import com.lago.app.presentation.viewmodel.chart.ChartViewModel
import com.lago.app.presentation.viewmodel.chart.ChartUiEvent
import com.lago.app.presentation.viewmodel.chart.HoldingItem
import com.lago.app.presentation.viewmodel.chart.TradingItem
import com.lago.app.presentation.viewmodel.chart.PatternAnalysisResult
import com.lago.app.presentation.ui.chart.ChartUtils
import com.tradingview.lightweightcharts.view.ChartsView
import com.tradingview.lightweightcharts.api.options.models.*
import com.tradingview.lightweightcharts.api.options.models.layoutOptions
import com.tradingview.lightweightcharts.api.options.models.gridOptions
import com.tradingview.lightweightcharts.api.options.models.gridLineOptions
import com.tradingview.lightweightcharts.api.series.enums.SeriesType
import com.tradingview.lightweightcharts.api.series.models.CandlestickData as TradingViewCandlestickData
import com.tradingview.lightweightcharts.api.series.models.Time
import com.tradingview.lightweightcharts.api.series.models.LineData as TradingViewLineData
import com.tradingview.lightweightcharts.api.series.enums.LineWidth
import com.tradingview.lightweightcharts.api.chart.models.color.surface.SolidColor
import com.tradingview.lightweightcharts.api.chart.models.color.toIntColor
import com.tradingview.lightweightcharts.api.series.enums.LineStyle
import com.tradingview.lightweightcharts.api.series.models.HistogramData
import com.tradingview.lightweightcharts.api.series.models.PriceScaleId
import com.tradingview.lightweightcharts.api.options.models.PriceScaleOptions
import com.tradingview.lightweightcharts.api.options.models.PriceScaleMargins
import com.tradingview.lightweightcharts.api.series.models.PriceFormat
import android.graphics.Color as AndroidColor

// 차트 색상 상수들
private object ChartColorConstants {
    const val CHART_UP_COLOR = "#FF99C5"
    const val CHART_DOWN_COLOR = "#42A6FF"
    const val SMA5_COLOR = "#F5A623"
    const val SMA20_COLOR = "#50E3C2"
    const val VOLUME_COLOR = "#666666"
    const val GRID_COLOR = "#E6E6E6"
    const val TEXT_COLOR = "#616161"
}

// 바텀시트 상태 열거형
enum class BottomSheetState {
    COLLAPSED,     // 하단 (225dp)
    HALF_EXPANDED, // 중단 (55%)
    EXPANDED       // 상단 (85%)
}

@Composable
fun ChartScreen(
    viewModel: ChartViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val coroutineScope = rememberCoroutineScope()

    // 3단계 높이 정의
    val collapsedHeight = 225.dp
    val halfExpandedHeight = screenHeight * 0.55f
    val expandedHeight = screenHeight * 0.85f
    val buttonBarHeight = 76.dp

    // 바텀시트 상태와 현재 오프셋
    var bottomSheetState by remember { mutableStateOf(BottomSheetState.COLLAPSED) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    // Y 위치를 픽셀로 계산 (화면 상단에서부터의 거리)
    val screenHeightPx = with(density) { screenHeight.toPx() }
    val buttonBarHeightPx = with(density) { buttonBarHeight.toPx() }

    // 각 상태별 Y 위치 (화면 상단에서부터의 거리)
    val collapsedY = screenHeightPx - with(density) { collapsedHeight.toPx() } - buttonBarHeightPx
    val halfExpandedY = screenHeightPx - with(density) { halfExpandedHeight.toPx() } - buttonBarHeightPx
    val expandedY = screenHeightPx - with(density) { expandedHeight.toPx() } - buttonBarHeightPx

    // 현재 목표 Y 위치
    val targetY = when (bottomSheetState) {
        BottomSheetState.COLLAPSED -> collapsedY
        BottomSheetState.HALF_EXPANDED -> halfExpandedY
        BottomSheetState.EXPANDED -> expandedY
    }

    // 실제 Y 위치 (드래그 적용)
    val currentY = if (isDragging) {
        (targetY + dragOffset).coerceIn(expandedY, collapsedY)
    } else {
        targetY
    }

    // 애니메이션된 Y 위치
    val animatedY by animateFloatAsState(
        targetValue = currentY,
        animationSpec = if (isDragging) {
            tween(durationMillis = 0)
        } else {
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            )
        },
        label = "bottom_sheet_y"
    )

    // 진행도 계산 (0.0 = 하단, 1.0 = 상단)
    val sheetProgress by remember {
        derivedStateOf {
            val progress = (collapsedY - animatedY) / (collapsedY - expandedY)
            progress.coerceIn(0f, 1f)
        }
    }

    // 드래그 처리 함수
    fun handleDragEnd(totalDragAmount: Float, duration: Long) {
        val velocity = if (duration > 0) totalDragAmount / duration * 1000 else 0f
        val fastSwipeThreshold = 800f

        when {
            // 빠른 아래 스와이프
            velocity > fastSwipeThreshold -> {
                bottomSheetState = when (bottomSheetState) {
                    BottomSheetState.EXPANDED -> BottomSheetState.HALF_EXPANDED
                    BottomSheetState.HALF_EXPANDED -> BottomSheetState.COLLAPSED
                    BottomSheetState.COLLAPSED -> BottomSheetState.COLLAPSED
                }
            }
            // 빠른 위 스와이프
            velocity < -fastSwipeThreshold -> {
                bottomSheetState = when (bottomSheetState) {
                    BottomSheetState.COLLAPSED -> BottomSheetState.HALF_EXPANDED
                    BottomSheetState.HALF_EXPANDED -> BottomSheetState.EXPANDED
                    BottomSheetState.EXPANDED -> BottomSheetState.EXPANDED
                }
            }
            // 느린 드래그 - 가장 가까운 상태로 스냅
            else -> {
                val currentPosition = targetY + dragOffset
                val distanceToCollapsed = abs(currentPosition - collapsedY)
                val distanceToHalfExpanded = abs(currentPosition - halfExpandedY)
                val distanceToExpanded = abs(currentPosition - expandedY)

                bottomSheetState = when {
                    distanceToCollapsed <= distanceToHalfExpanded && distanceToCollapsed <= distanceToExpanded ->
                        BottomSheetState.COLLAPSED
                    distanceToHalfExpanded <= distanceToExpanded ->
                        BottomSheetState.HALF_EXPANDED
                    else ->
                        BottomSheetState.EXPANDED
                }
            }
        }

        dragOffset = 0f
        isDragging = false
    }

    // 애니메이션 진행도 계산
    val animationProgress = ((sheetProgress - 0.2f) / 0.3f).coerceIn(0f, 1f) // 20~50% 구간

    // 헤더 박스 애니메이션 값들
    val boxTranslationY by animateFloatAsState(
        targetValue = animationProgress * (-72f), // 헤더(72dp)에서 앱바(0dp)로 이동
        animationSpec = tween(durationMillis = 250),
        label = "box_translation_y"
    )

    val boxHeight by animateFloatAsState(
        targetValue = 120f - (animationProgress * 64f), // 120dp → 56dp
        animationSpec = tween(durationMillis = 250),
        label = "box_height"
    )

    val boxCornerRadius by animateFloatAsState(
        targetValue = 16f * (1f - animationProgress), // 16dp → 0dp
        animationSpec = tween(durationMillis = 250),
        label = "box_corner_radius"
    )

    val boxPadding by animateFloatAsState(
        targetValue = 16f * (1f - animationProgress), // 16dp → 0dp
        animationSpec = tween(durationMillis = 250),
        label = "box_padding"
    )

    val boxAlpha by animateFloatAsState(
        targetValue = 0.95f + (animationProgress * 0.05f), // 95% → 100% 불투명도
        animationSpec = tween(durationMillis = 250),
        label = "box_alpha"
    )

    // 텍스트 애니메이션 값들
    val titleScale by animateFloatAsState(
        targetValue = 1f - (animationProgress * 0.33f),
        animationSpec = tween(durationMillis = 250),
        label = "title_scale"
    )

    val priceScale by animateFloatAsState(
        targetValue = 1f - (animationProgress * 0.5f),
        animationSpec = tween(durationMillis = 250),
        label = "price_scale"
    )

    val headerHeight by animateFloatAsState(
        targetValue = when {
            sheetProgress <= 0.2f -> 120f
            sheetProgress <= 0.5f -> 120f - ((sheetProgress - 0.2f) / 0.3f) * 64f // 120 → 56
            else -> 0f
        },
        animationSpec = tween(durationMillis = 150),
        label = "header_height"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // 배경 및 콘텐츠 영역
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(bottom = buttonBarHeight)
        ) {
            // 헤더 영역 (빈 공간 - 실제 콘텐츠는 박스에 포함)
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((176f - (animationProgress * 120f)).dp) // 176dp → 56dp
            )

            // 차트 + 시간 버튼 영역
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White)
            ) {
                // 차트 영역
                val chartHeight by animateFloatAsState(
                    targetValue = when {
                        sheetProgress <= 0.2f -> 300f
                        sheetProgress <= 0.5f -> 270f - ((sheetProgress - 0.2f) / 0.3f) * 30f
                        else -> 240f - ((sheetProgress - 0.5f) / 0.5f) * 30f
                    }.coerceAtLeast(210f),
                    animationSpec = tween(durationMillis = 150),
                    label = "chart_height"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(chartHeight.dp)
                        .padding(horizontal = 16.dp)
                        .background(Color.White)
                        .padding(horizontal = 8.dp)
                ) {
                    SingleChartView(
                        candlestickData = uiState.candlestickData,
                        volumeData = uiState.volumeData,
                        sma5Data = uiState.sma5Data,
                        sma20Data = uiState.sma20Data,
                        config = uiState.config,
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
                    onTimeFrameChange = { timeFrame ->
                        viewModel.onEvent(ChartUiEvent.ChangeTimeFrame(timeFrame))
                    },
                    modifier = Modifier
                        .background(Color.White)
                        .padding(
                            horizontal = 16.dp,
                            vertical = 12.dp
                        )
                )
            }
        }

        // 애니메이션되는 헤더 박스 (앱바 아이콘들 뒤에 위치)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = boxPadding.dp)
                .offset(y = (72f + boxTranslationY).dp) // 헤더 위치(72dp)에서 시작
                .height(boxHeight.dp)
                .shadow(
                    elevation = (animationProgress * 4f).dp,
                    shape = RoundedCornerShape(boxCornerRadius.dp)
                )
                .background(
                    color = Color.White.copy(alpha = boxAlpha),
                    shape = RoundedCornerShape(boxCornerRadius.dp)
                )
        ) {
            // 박스 안의 콘텐츠
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 16.dp + (animationProgress * 39.dp), // 16dp → 55dp (앱바 중앙으로 이동)
                        end = 16.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 왼쪽: 종목명과 가격
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer {
                            scaleX = titleScale
                            scaleY = titleScale
                        }
                ) {
                    if (animationProgress > 0.5f) {
                        // 중단 이후: 가로 배치
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.currentStock.name,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${String.format("%.0f", uiState.currentStock.currentPrice)}원",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                                modifier = Modifier.graphicsLayer {
                                    scaleX = priceScale / titleScale
                                    scaleY = priceScale / titleScale
                                }
                            )
                        }
                    } else {
                        // 초기~중단: 세로 배치
                        Text(
                            text = uiState.currentStock.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${String.format("%.0f", uiState.currentStock.currentPrice)}원",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            lineHeight = 36.sp,
                            modifier = Modifier.graphicsLayer {
                                scaleX = priceScale / titleScale
                                scaleY = priceScale / titleScale
                            }
                        )
                    }
                }

                // 오른쪽: 수익률 정보 (초기 상태에서만 표시)
                if (animationProgress < 0.3f) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.alpha(1f - (animationProgress * 3.33f))
                    ) {
                        val isPositive = uiState.currentStock.priceChange >= 0
                        Text(
                            text = "${if (isPositive) "▲" else "▼"}${String.format("%.0f", abs(uiState.currentStock.priceChange))}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isPositive) Color(0xFFFF69B4) else Color(0xFF42A6FF)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "(${if (isPositive) "+" else ""}${String.format("%.2f", uiState.currentStock.priceChangePercent)}%)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isPositive) Color(0xFFFF69B4) else Color(0xFF42A6FF)
                        )
                    }
                }
            }
        }

        // 앱바 (박스 위에 표시)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            TopAppBar(
                onBackClick = { viewModel.onEvent(ChartUiEvent.BackPressed) },
                isFavorite = uiState.isFavorite,
                onFavoriteClick = { viewModel.onEvent(ChartUiEvent.ToggleFavorite) },
                onNotificationClick = { viewModel.onEvent(ChartUiEvent.NotificationClicked) },
                onSettingsClick = { viewModel.onEvent(ChartUiEvent.SettingsClicked) },
                stockInfo = uiState.currentStock,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 3단계 바텀시트
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = with(density) { animatedY.toDp() })
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    spotColor = Color(0x0F000000)
                )
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .pointerInput(Unit) {
                    var totalDragAmount = 0f
                    var dragStartTime = 0L

                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                            totalDragAmount = 0f
                            dragStartTime = System.currentTimeMillis()
                        },
                        onDragEnd = {
                            val duration = System.currentTimeMillis() - dragStartTime
                            handleDragEnd(totalDragAmount, duration)
                        }
                    ) { _, dragAmount ->
                        totalDragAmount += dragAmount.y

                        // 드래그 오프셋 업데이트 (범위 제한)
                        val newDragOffset = dragOffset + dragAmount.y
                        val minOffset = expandedY - targetY  // 위로 드래그 한계
                        val maxOffset = collapsedY - targetY // 아래로 드래그 한계

                        dragOffset = newDragOffset.coerceIn(minOffset, maxOffset)
                    }
                }
        ) {
            Column {
                // 드래그 핸들
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 12.dp, bottom = 8.dp)
                            .width(40.dp)
                            .height(4.dp)
                            .background(
                                color = Color(0xFFBDBDBD),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                }

                // 바텀시트 콘텐츠
                BottomSheetContent(
                    viewModel = viewModel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            when (bottomSheetState) {
                                BottomSheetState.COLLAPSED -> 185.dp
                                BottomSheetState.HALF_EXPANDED -> 450.dp
                                BottomSheetState.EXPANDED -> 600.dp
                            }
                        )
                )
            }
        }

        // 화면 하단 고정 매수/매도 버튼
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    spotColor = Color(0x1A000000)
                )
                .background(Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.onEvent(ChartUiEvent.SellClicked) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF42A6FF)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "판매하기",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { viewModel.onEvent(ChartUiEvent.BuyClicked) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF69B4)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "구매하기",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 로딩 상태
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFF69B4))
            }
        }
    }
}

@Composable
private fun TopAppBar(
    onBackClick: () -> Unit,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    stockInfo: StockInfo,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(start = 7.dp)
                .semantics {
                    contentDescription = "이전 화면으로 돌아가기"
                }
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null,
                tint = Color(0xFF404040)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = onFavoriteClick,
            modifier = Modifier.semantics {
                contentDescription = if (isFavorite) "관심종목에서 제거" else "관심종목에 추가"
            }
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = if (isFavorite) Color(0xFFFF69B4) else Color.Black
            )
        }

        IconButton(
            onClick = onNotificationClick,
            modifier = Modifier.semantics {
                contentDescription = "주식 알림 설정"
            }
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = Color(0xFFFFE476),
                modifier = Modifier
                    .size(25.dp)
                    .clip(CircleShape)
                    .background(color = Color(0xFF69E1F6))
                    .padding(4.dp)
            )
        }

        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.padding(end = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "더보기",
                tint = Color.Black
            )
        }
    }
}

@Composable
private fun SingleChartView(
    candlestickData: List<CandlestickData>,
    volumeData: List<VolumeData>,
    sma5Data: List<LineData>,
    sma20Data: List<LineData>,
    config: ChartConfig,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            ChartsView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        modifier = modifier,
        update = { chartsView ->
            setupChart(chartsView, candlestickData, volumeData, sma5Data, sma20Data, config)
        }
    )
}

@Composable
private fun TimeFrameSelection(
    selectedTimeFrame: String,
    onTimeFrameChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMinuteDropdown by remember { mutableStateOf(false) }

    val timeFrames = listOf(
        "D" to "일",
        "W" to "주",
        "M" to "월",
        "Y" to "년"
    )

    val minuteFrames = listOf(
        "1" to "1분",
        "3" to "3분",
        "5" to "5분",
        "10" to "10분",
        "15" to "15분",
        "30" to "30분",
        "60" to "60분"
    )

    val selectedMinute = minuteFrames.find { it.first == selectedTimeFrame }
    val isMinuteSelected = selectedMinute != null

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        // 분봉 드롭다운 버튼
        Box {
            FilterChip(
                onClick = { showMinuteDropdown = !showMinuteDropdown },
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            selectedMinute?.second ?: "10분",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isMinuteSelected) Color.White else Color.Black
                        )
                        Icon(
                            imageVector = if (showMinuteDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "분봉 선택",
                            tint = if (isMinuteSelected) Color.White else Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                selected = isMinuteSelected,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFF69B4),
                    selectedLabelColor = Color.White,
                    containerColor = Color.Transparent,
                    labelColor = Color.Black
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isMinuteSelected,
                    borderColor = Color.Transparent,
                    selectedBorderColor = Color.Transparent
                ),
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .height(36.dp)
            )

            DropdownMenu(
                expanded = showMinuteDropdown,
                onDismissRequest = { showMinuteDropdown = false },
                modifier = Modifier
                    .background(Color.White)
                    .width(120.dp)
            ) {
                minuteFrames.forEach { (code, name) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                name,
                                color = Color.Black,
                                fontSize = 14.sp
                            )
                        },
                        onClick = {
                            onTimeFrameChange(code)
                            showMinuteDropdown = false
                        },
                        modifier = Modifier.height(40.dp)
                    )
                }
            }
        }

        // 나머지 시간대 버튼들
        timeFrames.forEach { (code, name) ->
            FilterChip(
                onClick = { onTimeFrameChange(code) },
                label = {
                    Text(
                        name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (selectedTimeFrame == code) Color.White else Color.Black
                    )
                },
                selected = selectedTimeFrame == code,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFF69B4),
                    selectedLabelColor = Color.White,
                    containerColor = Color.Transparent,
                    labelColor = Color.Black
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedTimeFrame == code,
                    borderColor = Color.Transparent,
                    selectedBorderColor = Color.Transparent
                ),
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .height(36.dp)
            )
        }
    }
}

@Composable
private fun BottomSheetContent(
    viewModel: ChartViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTabIndex = uiState.selectedBottomTab
    val tabTitles = listOf("보유현황", "매매내역", "패턴분석")

    Column(modifier = modifier) {
        // 탭
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.White,
            contentColor = Color(0xFF08090E),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = Color.Black,
                    height = 2.dp
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { viewModel.onEvent(ChartUiEvent.ChangeBottomTab(index)) },
                    text = {
                        Text(
                            title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (selectedTabIndex == index) Color(0xFF08090E) else Color(0xFF747476)
                        )
                    },
                    modifier = Modifier.height(44.dp)
                )
            }
        }

        // 탭 내용
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 27.dp, vertical = 16.dp)
        ) {
            when (selectedTabIndex) {
                0 -> HoldingsContent(uiState.holdings)
                1 -> TradingHistoryContent(uiState.tradingHistory)
                2 -> PatternAnalysisContent(
                    patternAnalysisCount = uiState.patternAnalysisCount,
                    maxPatternAnalysisCount = uiState.maxPatternAnalysisCount,
                    lastPatternAnalysis = uiState.lastPatternAnalysis,
                    onAnalyzeClick = { viewModel.onEvent(ChartUiEvent.AnalyzePattern) }
                )
            }
        }
    }
}

@Composable
private fun HoldingsContent(holdings: List<HoldingItem>) {
    if (holdings.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "보유 중인 종목이 없습니다",
                fontSize = 14.sp,
                color = Color(0xFF616161)
            )
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(holdings) { holding ->
                HoldingItemRow(holding)
            }
        }
    }
}

@Composable
private fun HoldingItemRow(item: HoldingItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFF333333), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "종목",
                    color = Color.White,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    item.name,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    item.quantity,
                    fontSize = 12.sp,
                    color = Color(0xFF616161)
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                "${String.format("%,d", item.value)}원",
                fontSize = 14.sp,
                color = Color.Black
            )
            Text(
                "${if (item.change >= 0) "+" else ""}${String.format("%.1f", item.change)}%",
                fontSize = 12.sp,
                color = if (item.change >= 0) Color(0xFFFF69B4) else Color(0xFF42A6FF)
            )
        }
    }
}

@Composable
private fun TradingHistoryContent(history: List<TradingItem>) {
    if (history.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "매매 내역이 없습니다",
                fontSize = 14.sp,
                color = Color(0xFF616161)
            )
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(history) { tradingItem ->
                TradingItemRow(tradingItem)
            }
        }
    }
}

@Composable
private fun TradingItemRow(item: TradingItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.Black, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    item.type,
                    color = Color.White,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    item.quantity,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    item.date,
                    fontSize = 12.sp,
                    color = Color(0xFF616161)
                )
            }
        }

        Text(
            "${String.format("%,d", item.amount)}원",
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}

@Composable
private fun PatternAnalysisContent(
    patternAnalysisCount: Int,
    maxPatternAnalysisCount: Int,
    lastPatternAnalysis: PatternAnalysisResult?,
    onAnalyzeClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "차트에서 인식된 패턴을 분석해드려요.\n분석은 하루 최대 3회까지 가능합니다.",
            color = Color.Black,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onAnalyzeClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(44.dp)
        ) {
            Text(
                "차트 패턴 분석하기 ($patternAnalysisCount/$maxPatternAnalysisCount)",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        lastPatternAnalysis?.let { analysis ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "최근 분석 결과",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        Text(
                            "($patternAnalysisCount/$maxPatternAnalysisCount)",
                            fontSize = 12.sp,
                            color = Color(0xFF616161)
                        )
                    }

                    Text(
                        analysis.analysisTime,
                        fontSize = 12.sp,
                        color = Color(0xFF616161),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        analysis.patternName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )

                    Text(
                        analysis.description,
                        fontSize = 14.sp,
                        color = Color(0xFF616161),
                        modifier = Modifier.padding(top = 4.dp),
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

// 차트 설정 함수
private fun setupChart(
    chartsView: ChartsView,
    candlestickData: List<CandlestickData>,
    volumeData: List<VolumeData>,
    sma5Data: List<LineData>,
    sma20Data: List<LineData>,
    config: ChartConfig
) {
    chartsView.subscribeOnChartStateChange { state ->
        when (state) {
            is ChartsView.State.Ready -> {
                chartsView.api.applyOptions {
                    layout = layoutOptions {
                        background = SolidColor(AndroidColor.WHITE)
                        textColor = AndroidColor.parseColor(ChartColorConstants.TEXT_COLOR).toIntColor()
                    }
                    grid = gridOptions {
                        vertLines = gridLineOptions {
                            color = AndroidColor.parseColor(ChartColorConstants.GRID_COLOR).toIntColor()
                            style = LineStyle.DASHED
                        }
                        horzLines = gridLineOptions {
                            color = AndroidColor.parseColor(ChartColorConstants.GRID_COLOR).toIntColor()
                            style = LineStyle.DASHED
                        }
                    }
                }

                if (candlestickData.isNotEmpty()) {
                    val chartData = candlestickData.map { data ->
                        TradingViewCandlestickData(
                            time = ChartUtils.timestampToBusinessDay(data.time),
                            open = data.open,
                            high = data.high,
                            low = data.low,
                            close = data.close
                        )
                    }

                    chartsView.api.addCandlestickSeries(
                        options = CandlestickSeriesOptions(
                            upColor = AndroidColor.parseColor(ChartColorConstants.CHART_UP_COLOR).toIntColor(),
                            downColor = AndroidColor.parseColor(ChartColorConstants.CHART_DOWN_COLOR).toIntColor(),
                            borderVisible = false,
                            wickUpColor = AndroidColor.parseColor(ChartColorConstants.CHART_UP_COLOR).toIntColor(),
                            wickDownColor = AndroidColor.parseColor(ChartColorConstants.CHART_DOWN_COLOR).toIntColor()
                        )
                    ) { api ->
                        api.setData(chartData)
                    }
                }

                if (config.indicators.sma5 && sma5Data.isNotEmpty()) {
                    val sma5ChartData = sma5Data.map { data ->
                        TradingViewLineData(
                            time = ChartUtils.timestampToBusinessDay(data.time),
                            value = data.value
                        )
                    }

                    chartsView.api.addLineSeries(
                        options = LineSeriesOptions(
                            color = AndroidColor.parseColor(ChartColorConstants.SMA5_COLOR).toIntColor(),
                            lineWidth = LineWidth.TWO
                        )
                    ) { api ->
                        api.setData(sma5ChartData)
                    }
                }

                if (config.indicators.sma20 && sma20Data.isNotEmpty()) {
                    val sma20ChartData = sma20Data.map { data ->
                        TradingViewLineData(
                            time = ChartUtils.timestampToBusinessDay(data.time),
                            value = data.value
                        )
                    }

                    chartsView.api.addLineSeries(
                        options = LineSeriesOptions(
                            color = AndroidColor.parseColor(ChartColorConstants.SMA20_COLOR).toIntColor(),
                            lineWidth = LineWidth.TWO
                        )
                    ) { api ->
                        api.setData(sma20ChartData)
                    }
                }

                if (config.indicators.volume && volumeData.isNotEmpty()) {
                    val volumeChartData = volumeData.map { data ->
                        HistogramData(
                            time = ChartUtils.timestampToBusinessDay(data.time),
                            value = data.value,
                            color = if (data.value > 0)
                                AndroidColor.parseColor(ChartColorConstants.CHART_UP_COLOR).toIntColor()
                            else
                                AndroidColor.parseColor(ChartColorConstants.CHART_DOWN_COLOR).toIntColor()
                        )
                    }

                    chartsView.api.addHistogramSeries(
                        options = HistogramSeriesOptions(
                            priceScaleId = PriceScaleId("volume"),
                            color = AndroidColor.parseColor(ChartColorConstants.VOLUME_COLOR).toIntColor(),
                            priceFormat = PriceFormat.priceFormatBuiltIn(
                                type = PriceFormat.Type.VOLUME,
                                precision = 0,
                                minMove = 1.0f
                            )
                        )
                    ) { api ->
                        api.setData(volumeChartData)
                        api.priceScale().applyOptions(
                            PriceScaleOptions(
                                scaleMargins = PriceScaleMargins(
                                    top = 0.8f,
                                    bottom = 0.0f
                                )
                            )
                        )
                    }
                }
            }
            else -> {}
        }
    }
}