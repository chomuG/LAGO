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
import androidx.compose.runtime.saveable.rememberSaveable
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
// Theme imports
import com.lago.app.presentation.theme.*
// Animation imports for smoothing
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.LaunchedEffect
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
    HALF_EXPANDED, // 중단 (40%)
    EXPANDED       // 상단 (85%)
}

// 커스텀 lerp 함수
private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + fraction * (stop - start)
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

    // 3단계 높이 정의 (메모이제이션으로 성능 최적화)
    val bottomSheetHeights = remember(screenHeight) {
        object {
            val collapsed = 225.dp
            val halfExpanded = screenHeight * 0.50f
            val expanded = screenHeight * 0.85f
            val buttonBar = 76.dp
        }
    }

    // 바텀시트 상태와 현재 오프셋 (상태 보존 개선)
    var bottomSheetState by rememberSaveable { mutableStateOf(BottomSheetState.COLLAPSED) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    // Y 위치를 픽셀로 계산 (화면 상단에서부터의 거리)
    val screenHeightPx = with(density) { screenHeight.toPx() }

    // 각 상태별 Y 위치 (화면 상단에서부터의 거리) - 메모이제이션 최적화
    val sheetPositions = remember(screenHeightPx, density) {
        val buttonBarHeightPx = with(density) { bottomSheetHeights.buttonBar.toPx() }
        object {
            val collapsed = screenHeightPx - with(density) { bottomSheetHeights.collapsed.toPx() } - buttonBarHeightPx
            val halfExpanded = screenHeightPx - with(density) { bottomSheetHeights.halfExpanded.toPx() } - buttonBarHeightPx
            val expanded = screenHeightPx - with(density) { bottomSheetHeights.expanded.toPx() } - buttonBarHeightPx
        }
    }

    // 현재 목표 Y 위치 (성능 최적화된 계산)
    val targetY = remember(bottomSheetState, sheetPositions) {
        when (bottomSheetState) {
            BottomSheetState.COLLAPSED -> sheetPositions.collapsed
            BottomSheetState.HALF_EXPANDED -> sheetPositions.halfExpanded
            BottomSheetState.EXPANDED -> sheetPositions.expanded
        }
    }

    // 실제 Y 위치 (드래그 적용) - 계산 최적화
    val currentY = remember(isDragging, targetY, dragOffset, sheetPositions) {
        if (isDragging) {
            (targetY + dragOffset).coerceIn(sheetPositions.expanded, sheetPositions.collapsed)
        } else {
            targetY
        }
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

    // 진행도 계산 (0.0 = 하단, 1.0 = 상단) - 성능 최적화
    val sheetProgress by remember(sheetPositions) {
        derivedStateOf {
            val progress = (sheetPositions.collapsed - animatedY) / (sheetPositions.collapsed - sheetPositions.expanded)
            progress.coerceIn(0f, 1f)
        }
    }

    // 1) sheetProgress 스무딩 적용 - 드래그 상태에 따른 동적 스프링 조절
    val smoothProgress = remember { Animatable(0f) }
    LaunchedEffect(sheetProgress, isDragging) {
        smoothProgress.animateTo(
            targetValue = sheetProgress,
            animationSpec = if (isDragging) {
                // 드래그 중: 매우 빠르고 반응적인 스프링 (거의 즉시 반응)
                spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessHigh * 3f // 3배 빠른 반응
                )
            } else {
                // 평상시: 부드러운 스프링 (자연스러운 마무리)
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            }
        )
    }

    // 드래그 처리 함수
    fun handleDragEnd(totalDragAmount: Float, duration: Long) {
        val velocity = if (duration > 0) totalDragAmount / duration * 1000 else 0f
        val fastSwipeThreshold = 800f

        when {
            // 빠른 아래 스와이프 (상태 전환 최적화)
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
            // 느린 드래그 - 가장 가까운 상태로 스냅 (성능 최적화)
            else -> {
                val currentPosition = targetY + dragOffset
                val distances = listOf(
                    BottomSheetState.COLLAPSED to abs(currentPosition - sheetPositions.collapsed),
                    BottomSheetState.HALF_EXPANDED to abs(currentPosition - sheetPositions.halfExpanded),
                    BottomSheetState.EXPANDED to abs(currentPosition - sheetPositions.expanded)
                )

                bottomSheetState = distances.minByOrNull { it.second }?.first ?: BottomSheetState.COLLAPSED
            }
        }

        dragOffset = 0f
        isDragging = false
    }

    // 애니메이션 진행도 계산 (20~50% 구간에서 헤더 변환)
    val animationProgress = ((sheetProgress - 0.2f) / 0.3f).coerceIn(0f, 1f)

    // 중간 위치(50%)에서 완전히 앱바에 정렬되도록 조정 - smoothProgress 사용
    val headerAlignmentProgress = when {
        smoothProgress.value <= 0.3f -> 0f
        smoothProgress.value >= 0.4f -> 1f
        else -> (smoothProgress.value - 0.3f) / 0.1f // 30~40% 구간에서만 애니메이션
    }

    // 2) 애니메이션 값들 - lerp 사용으로 더 부드럽게
    val headerOffsetY = lerp(0f, -72f, headerAlignmentProgress)
    val headerScale = lerp(1f, 0.75f, headerAlignmentProgress)
    val chartScale = lerp(1f, 0.85f, smoothProgress.value) // 전체 진행도에 따른 차트 스케일
    val chartTranslationY = lerp(0f, -30f, smoothProgress.value)

    // 헤더 박스 애니메이션 값들 - lerp 기반으로 더 부드럽게
    val boxTranslationY = headerOffsetY
    val boxHeight = lerp(120f, 56f, headerAlignmentProgress)
    val boxCornerRadius = lerp(16f, 0f, headerAlignmentProgress)
    val boxPadding = lerp(16f, 0f, headerAlignmentProgress)
    val boxAlpha = lerp(0.95f, 1.0f, headerAlignmentProgress)

    // 텍스트 애니메이션 값들 - lerp 기반으로 부드럽게
    val titleScale = lerp(1f, 0.67f, headerAlignmentProgress) // 1f - 0.33f
    val priceScale = lerp(1f, 0.5f, headerAlignmentProgress)
    val titleFontSize = lerp(24f, 18f, headerAlignmentProgress)
    val priceFontSize = lerp(32f, 16f, headerAlignmentProgress)
    val layoutTransition = headerAlignmentProgress

    val headerHeight = when {
        smoothProgress.value <= 0.2f -> 120f
        smoothProgress.value <= 0.5f -> lerp(120f, 56f, (smoothProgress.value - 0.2f) / 0.3f)
        else -> 56f
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 배경 및 콘텐츠 영역
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(bottom = bottomSheetHeights.buttonBar)
        ) {
            // 헤더 영역 (빈 공간 - 실제 콘텐츠는 박스에 포함)
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((176f - (headerAlignmentProgress * 120f)).dp) // 176dp → 56dp
            )

            // 차트 + 시간 버튼 영역
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White)
            ) {
                // 차트 영역 - 4) 부드러운 차트 애니메이션
                val chartHeight = when {
                    smoothProgress.value <= 0.2f -> 300f
                    smoothProgress.value <= 0.5f -> lerp(300f, 270f, (smoothProgress.value - 0.2f) / 0.3f)
                    else -> lerp(270f, 240f, (smoothProgress.value - 0.5f) / 0.5f)
                }.coerceAtLeast(210f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(chartHeight.dp)
                        .graphicsLayer {
                            scaleX = chartScale
                            scaleY = chartScale
                            translationY = chartTranslationY.dp.toPx()
                        }
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

        // 애니메이션되는 헤더 박스 (앱바 아이콘들 뒤에 위치) - 3) 부드러운 헤더 애니메이션
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = boxPadding.dp)
                .offset(y = (72f + boxTranslationY).dp)
                .height(boxHeight.dp)
                .graphicsLayer {
                    scaleX = headerScale
                    scaleY = headerScale
                }
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
                        start = 8.dp, // + (headerAlignmentProgress * 39.dp), // 16dp → 55dp (앱바 중앙으로 이동)
                        end = 16.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 왼쪽: 종목명과 가격 - 부드러운 애니메이션 전환
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer {
                            scaleX = titleScale
                            scaleY = titleScale
                        }
                ) {
                    // 초기 상태 (세로 배치) - 점진적으로 사라짐
                    Column(
                        modifier = Modifier
                            .alpha(1f - layoutTransition)
                            .graphicsLayer {
                                // 레이아웃 전환 시 위치 조정
                                translationY = -layoutTransition * 20f
                            }
                    ) {
                        Text(
                            text = uiState.currentStock.name,
                            fontSize = titleFontSize.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray900
                        )

                        Spacer(modifier = Modifier.height((8f * (1f - layoutTransition)).dp))

                        Text(
                            text = "${String.format("%.0f", uiState.currentStock.currentPrice)}원",
                            fontSize = priceFontSize.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Gray900,
                            modifier = Modifier.graphicsLayer {
                                scaleX = priceScale / titleScale
                                scaleY = priceScale / titleScale
                            }
                        )
                    }

                    // 최종 상태 (가로 배치) - 점진적으로 나타남
                    Column(
                        modifier = Modifier
                            .alpha(layoutTransition)
                            .graphicsLayer {
                                // 레이아웃 전환 시 위치 조정
                                translationY = (1f - layoutTransition) * 20f
                            },
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = uiState.currentStock.name,
                            fontSize = (18f + (1f - layoutTransition) * 6f).sp, // 24sp -> 18sp
                            fontWeight = FontWeight.SemiBold,
                            color = Gray900
                        )

                        Spacer(modifier = Modifier.height((4f * layoutTransition).dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${String.format("%.0f", uiState.currentStock.currentPrice)}원",
                                fontSize = (16f + (1f - layoutTransition) * 16f).sp, // 32sp -> 16sp
                                fontWeight = if (layoutTransition > 0.5f) FontWeight.Bold else FontWeight.ExtraBold,
                                color = Gray900
                            )

                            Spacer(modifier = Modifier.width((4f * layoutTransition).dp))

                            val isPositive = uiState.currentStock.priceChange >= 0
                            Text(
                                text = "${if (isPositive) "+" else ""}${String.format("%.2f", uiState.currentStock.priceChangePercent)}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isPositive) MainPink else MainBlue,
                                modifier = Modifier.alpha(layoutTransition)
                            )
                        }
                    }
                }

                // 오른쪽: 수익률 정보 (초기 상태에서 점진적으로 사라짐)
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .alpha(1f - layoutTransition)
                        .graphicsLayer {
                            // 페이드아웃과 함께 살짝 위로 이동
                            translationY = -layoutTransition * 10f
                            scaleX = 1f - (layoutTransition * 0.2f)
                            scaleY = 1f - (layoutTransition * 0.2f)
                        }
                ) {
                    val isPositive = uiState.currentStock.priceChange >= 0
                    Text(
                        text = "${if (isPositive) "▲" else "▼"}${String.format("%.0f", abs(uiState.currentStock.priceChange))}원",
                        style = TitleB16,
                        color = if (isPositive) MainPink else MainBlue
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "(${if (isPositive) "+" else ""}${String.format("%.2f", uiState.currentStock.priceChangePercent)}%)",
                        style = SubtitleSb14,
                        color = if (isPositive) MainPink else MainBlue
                    )
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

                        // 드래그 오프셋 업데이트 (범위 제한) - 성능 최적화
                        val newDragOffset = dragOffset + dragAmount.y
                        val minOffset = sheetPositions.expanded - targetY  // 위로 드래그 한계
                        val maxOffset = sheetPositions.collapsed - targetY // 아래로 드래그 한계

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
                            .semantics {
                                contentDescription = "바텀시트 드래그 핸들"
                            }
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
                        .height(48.dp)
                        .semantics {
                            contentDescription = "${uiState.currentStock.name} 판매하기 버튼"
                        },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MainBlue
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
                        .height(48.dp)
                        .semantics {
                            contentDescription = "${uiState.currentStock.name} 구매하기 버튼"
                        },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MainPink
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
                tint = Gray700
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
                tint = if (isFavorite) MainPink else Gray900
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
                tint = Gray900
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
                modifier = Modifier
                    .semantics {
                        contentDescription = "분봉 선택: 현재 ${selectedMinute?.second ?: "10분"}"
                    }
                    .padding(horizontal = 6.dp)
                    .height(36.dp),
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            selectedMinute?.second ?: "10분",
                            style = SubtitleSb14,
                            color = if (isMinuteSelected) Color.White else Gray900
                        )
                        Icon(
                            imageVector = if (showMinuteDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "분봉 선택",
                            tint = if (isMinuteSelected) Color.White else Gray900,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                selected = isMinuteSelected,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MainPink,
                    selectedLabelColor = Color.White,
                    containerColor = Color.Transparent,
                    labelColor = Gray900
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isMinuteSelected,
                    borderColor = Color.Transparent,
                    selectedBorderColor = Color.Transparent
                ),
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
                                style = BodyR14,
                                color = Gray900
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
                modifier = Modifier
                    .semantics {
                        contentDescription = "시간대 선택: ${name}${if (selectedTimeFrame == code) ", 선택됨" else ""}"
                    }
                    .padding(horizontal = 6.dp)
                    .height(36.dp),
                label = {
                    Text(
                        name,
                        style = SubtitleSb14,
                        color = if (selectedTimeFrame == code) Color.White else Gray900
                    )
                },
                selected = selectedTimeFrame == code,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MainPink,
                    selectedLabelColor = Color.White,
                    containerColor = Color.Transparent,
                    labelColor = Gray900
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedTimeFrame == code,
                    borderColor = Color.Transparent,
                    selectedBorderColor = Color.Transparent
                ),

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
                            style = TitleB16,
                            color = if (selectedTabIndex == index) Gray900 else Gray600
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
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
            // 수익률 표시 (가격 밑에)
            Text(
                "${if (item.change >= 0) "+" else ""}${String.format("%.2f", item.change)}% (${String.format("%+,d", (item.value * item.change / 100).toInt())}원)",
                fontSize = 12.sp,
                color = if (item.change >= 0) Color(0xFFFF69B4) else Color(0xFF42A6FF),
                fontWeight = FontWeight.Medium
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

        Column(horizontalAlignment = Alignment.End) {
            Text(
                "${String.format("%,d", item.amount)}원",
                fontSize = 14.sp,
                color = Color.Black
            )
            val quantityNumber = item.quantity.replace("주", "").trim().toIntOrNull() ?: 1
            Text(
                "주당 ${String.format("%,d", item.amount / quantityNumber)}원",
                fontSize = 12.sp,
                color = Color(0xFF616161)
            )
        }
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
        // 설명 텍스트
        Text(
            "차트에서 인식된 패턴을\n분석해드려요.\n분석은 하루 최대 3회까지 가능합니다.",
            color = Color.Black,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // 분석 버튼
        Button(
            onClick = onAnalyzeClick,
            enabled = patternAnalysisCount < maxPatternAnalysisCount,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50),
                disabledContainerColor = Color(0xFFE0E0E0)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 24.dp)
                .semantics {
                    contentDescription = if (patternAnalysisCount < maxPatternAnalysisCount) {
                        "차트 패턴 분석하기 버튼, 남은 횟수: ${maxPatternAnalysisCount - patternAnalysisCount}번"
                    } else {
                        "오늘 분석 횟수를 모두 사용하셨습니다"
                    }
                }
        ) {
            Text(
                "차트 패턴 분석하기 ($patternAnalysisCount/$maxPatternAnalysisCount)",
                color = if (patternAnalysisCount < maxPatternAnalysisCount) Color.White else Color(0xFF9E9E9E),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // 최근 분석 결과가 있을 때만 표시
        lastPatternAnalysis?.let { analysis ->
            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "최근 분석 결과",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        Text(
                            analysis.analysisTime,
                            fontSize = 12.sp,
                            color = Color(0xFF616161)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        analysis.patternName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        analysis.description,
                        fontSize = 14.sp,
                        color = Color(0xFF424242),
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