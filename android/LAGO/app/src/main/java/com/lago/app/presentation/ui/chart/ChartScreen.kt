package com.lago.app.presentation.ui.chart

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.res.painterResource
import com.lago.app.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
// 커스텀 바텀시트를 위한 imports
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.derivedStateOf
import kotlinx.coroutines.launch
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.draw.clipToBounds
import kotlin.math.abs
// Theme imports
import com.lago.app.presentation.theme.*
// Widget imports
import com.lago.app.presentation.ui.widget.DockingHeader
import com.lago.app.presentation.ui.widget.AnimatedHeaderBox
// Domain imports
import com.lago.app.domain.entity.CandlestickData
import com.lago.app.domain.entity.ChartConfig
import com.lago.app.domain.entity.LineData
import com.lago.app.domain.entity.VolumeData
import com.lago.app.domain.entity.StockInfo
import com.lago.app.domain.entity.MACDResult
import com.lago.app.domain.entity.BollingerBandsResult
// ViewModel imports
import com.lago.app.presentation.viewmodel.chart.ChartViewModel
import com.lago.app.presentation.viewmodel.chart.ChartUiEvent
import com.lago.app.presentation.viewmodel.chart.HoldingItem
import com.lago.app.presentation.viewmodel.chart.TradingItem
import com.lago.app.presentation.viewmodel.chart.PatternAnalysisResult
import com.skydoves.flexible.core.pxToDp
// Chart imports - v5 Multi-Panel Chart
import com.lago.app.presentation.ui.chart.v5.MultiPanelChart
import com.lago.app.presentation.ui.chart.v5.DataConverter
import com.lago.app.presentation.ui.chart.v5.toEnabledIndicators
import kotlin.math.absoluteValue
// Character Dialog import
import com.lago.app.presentation.components.CharacterSelectionDialog
import com.lago.app.presentation.components.CharacterInfo


// 바텀시트 상태 열거형
enum class BottomSheetState {
    COLLAPSED,     // 하단 (200dp)
    HALF_EXPANDED, // 중단 (45%)
    EXPANDED       // 상단 (75%)
}


@Composable
fun ChartScreen(
    stockCode: String? = null,
    viewModel: ChartViewModel = hiltViewModel(),
    onNavigateToStockPurchase: (String, String) -> Unit = { _, _ -> },
    onNavigateToAIDialog: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val coroutineScope = rememberCoroutineScope()
    
    // Character selection dialog state
    var showCharacterDialog by remember { mutableStateOf(false) }

    // 투자 탭에서 선택된 주식 코드로 차트 데이터 로드
    LaunchedEffect(stockCode) {
        stockCode?.let { code ->
            viewModel.onEvent(ChartUiEvent.ChangeStock(code))
        }
    }

    // 3단계 높이 정의 (원래 ChartSample.kt 참고)
    val collapsedHeight = 225.dp
    val halfExpandedHeight = screenHeight * 0.50f  // 50%
    val expandedHeight = screenHeight * 0.85f
    val buttonBarHeight = 76.dp

    // 바텀시트 상태와 현재 오프셋
    var bottomSheetState by rememberSaveable { mutableStateOf(BottomSheetState.COLLAPSED) }
    var isDragging by remember { mutableStateOf(false) }

    // Y 위치를 픽셀로 계산
    val screenHeightPx = with(density) { screenHeight.toPx() }
    val buttonBarHeightPx = with(density) { buttonBarHeight.toPx() }

    // 각 상태별 Y 위치
    val sheetPositions = remember(screenHeightPx, density) {
        object {
            val collapsed = screenHeightPx - with(density) { collapsedHeight.toPx() } - buttonBarHeightPx
            val halfExpanded = screenHeightPx - with(density) { halfExpandedHeight.toPx() } - buttonBarHeightPx
            val expanded = screenHeightPx - with(density) { expandedHeight.toPx() } - buttonBarHeightPx
        }
    }

    // 현재 목표 Y 위치
    val targetY = remember(bottomSheetState, sheetPositions) {
        when (bottomSheetState) {
            BottomSheetState.COLLAPSED -> sheetPositions.collapsed
            BottomSheetState.HALF_EXPANDED -> sheetPositions.halfExpanded
            BottomSheetState.EXPANDED -> sheetPositions.expanded
        }
    }

    // Animatable로 부드러운 애니메이션 처리
    val sheetAnimY = remember { Animatable(sheetPositions.collapsed) }

    // 바텀시트 상태가 변경될 때 애니메이션
    LaunchedEffect(bottomSheetState) {
        sheetAnimY.animateTo(
            targetValue = targetY,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    // 진행도 계산 (0.0 = 하단, 1.0 = 상단)
    val sheetProgress by remember(sheetPositions) {
        derivedStateOf {
            val progress = (sheetPositions.collapsed - sheetAnimY.value) / (sheetPositions.collapsed - sheetPositions.expanded)
            progress.coerceIn(0f, 1f)
        }
    }

    // 헤더 정렬 진행도 계산 (원래 ChartSample.kt 방식)
    val headerAlignmentProgress = when {
        sheetProgress <= 0.3f -> 0f
        sheetProgress >= 0.4f -> 1f
        else -> (sheetProgress - 0.3f) / 0.1f // 30~40% 구간에서만 애니메이션
    }

    // 바텀시트 위치에 따른 콘텐츠 오프셋 (헤더용 - 원래대로)
    val contentOffsetY = with(density) {
        val maxOffset = -40.dp.toPx()
        val currentOffset = (sheetAnimY.value - sheetPositions.collapsed).coerceAtMost(0f)
        (currentOffset * 0.3f).coerceAtLeast(maxOffset).toDp()
    }

    // 시간버튼만 올라가도록 오프셋 설정
    val timeButtonOffsetY = with(density) {
        val currentOffset = (sheetAnimY.value - sheetPositions.collapsed).coerceAtMost(0f)
        currentOffset.toDp()
    }

    // 드래그 제스처 처리 함수
    fun handleDragEnd(velocity: Float, currentPosition: Float) {
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
                val distances = listOf(
                    BottomSheetState.COLLAPSED to abs(currentPosition - sheetPositions.collapsed),
                    BottomSheetState.HALF_EXPANDED to abs(currentPosition - sheetPositions.halfExpanded),
                    BottomSheetState.EXPANDED to abs(currentPosition - sheetPositions.expanded)
                )

                bottomSheetState = distances.minByOrNull { it.second }?.first ?: BottomSheetState.COLLAPSED
            }
        }

        isDragging = false
    }

    // LazyListState들을 상위에서 관리
    val holdingsListState = rememberLazyListState()
    val tradingHistoryListState = rememberLazyListState()

    // NestedScrollConnection
    val nestedScrollConnection = remember(bottomSheetState, sheetAnimY, coroutineScope) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (bottomSheetState == BottomSheetState.COLLAPSED && available.y < 0) {
                    coroutineScope.launch {
                        val newY = (sheetAnimY.value + available.y).coerceIn(
                            sheetPositions.expanded,
                            sheetPositions.collapsed
                        )
                        sheetAnimY.snapTo(newY)
                    }
                    return available
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val currentListState = when (uiState.selectedBottomTab) {
                    0 -> holdingsListState
                    1 -> tradingHistoryListState
                    else -> null
                }

                val isAtTop = currentListState?.firstVisibleItemIndex == 0 &&
                        currentListState.firstVisibleItemScrollOffset == 0

                if (isAtTop && available.y > 0 && consumed.y == 0f && bottomSheetState == BottomSheetState.EXPANDED) {
                    coroutineScope.launch {
                        val newY = (sheetAnimY.value + available.y).coerceIn(
                            sheetPositions.expanded,
                            sheetPositions.collapsed
                        )
                        sheetAnimY.snapTo(newY)

                        if (newY > sheetPositions.expanded + 100) {
                            bottomSheetState = BottomSheetState.HALF_EXPANDED
                        }
                    }
                    return available
                }
                return Offset.Zero
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. 메인 콘텐츠 (앱바 아래까지만 올라감) - 바텀시트 높이에 따른 패딩 조정
        // 시간 버튼이 바텀시트로 이동했으므로 기본 패딩만 사용

        // 1. 배경 영역
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )

        // 차트 + 시간버튼 분리된 구조 - 빈 공간 최소화
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = buttonBarHeight)
                .offset(y = with(density) {
                    val maxUpwardOffset = -56.dp.toPx()
                    val offset = sheetAnimY.value - sheetPositions.collapsed
                    val halfExpandedOffset = sheetPositions.halfExpanded - sheetPositions.collapsed
                    val initialDownwardOffset = 30.dp.toPx() // 초기 위치를 30dp 아래로

                    // 중단까지만 움직이고, 중단->상단에서는 고정
                    (offset.coerceIn(halfExpandedOffset, 0f).coerceAtLeast(maxUpwardOffset) + initialDownwardOffset).toDp()
                })
        ) {
            // 앱바 영역 (고정)
            Spacer(modifier = Modifier.height(56.dp))

            // 헤더 영역 (최소화)
            Spacer(modifier = Modifier.height((80f - (headerAlignmentProgress * 40f)).dp))

            // 차트 영역 (패널 디바이더 포함)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        with(density) {
                            val baseHeight = 350.dp
                            val maxUpwardOffset = -56.dp.toPx()
                            val currentOffset = sheetAnimY.value - sheetPositions.collapsed
                            val halfExpandedOffset = sheetPositions.halfExpanded - sheetPositions.collapsed

                            when {
                                // 중단->상단: 중단에서의 압축된 높이 유지
                                currentOffset <= halfExpandedOffset -> {
                                    val compressionAtHalf = -(halfExpandedOffset - maxUpwardOffset)
                                    (baseHeight - compressionAtHalf.toDp()).coerceAtLeast(150.dp)
                                }
                                // 하단->중단: 앱바에 닿으면 압축 시작
                                currentOffset <= maxUpwardOffset -> {
                                    val compression = -(currentOffset - maxUpwardOffset)
                                    (baseHeight - compression.toDp()).coerceAtLeast(150.dp)
                                }
                                // 하단: 기본 높이
                                else -> baseHeight
                            }
                        }
                    )
            ) {
                // TradingView v5 Multi-Panel Chart with Native API
                val multiPanelData = DataConverter.createMultiPanelData(
                    candlestickData = uiState.candlestickData,
                    volumeData = uiState.volumeData,
                    sma5Data = uiState.sma5Data,
                    sma20Data = uiState.sma20Data,
                    rsiData = uiState.rsiData,
                    macdData = uiState.macdData,
                    bollingerBands = uiState.bollingerBands,
                    enabledIndicators = uiState.config.indicators.toEnabledIndicators(),
                    timeFrame = uiState.config.timeFrame
                )

                MultiPanelChart(
                    data = multiPanelData,
                    timeFrame = uiState.config.timeFrame,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Spacing.md),
                    onChartReady = {
                        // Chart ready callback
                    },
                    onDataPointClick = { time, value, panelId ->
                        // Handle data point click
                    },
                    onCrosshairMove = { time, value, panelId ->
                        // Handle crosshair move
                    }
                )
            }

            // 차트와 시간버튼 사이 간격 최소화
            Spacer(modifier = Modifier.height(Spacing.sm))

            // 시간버튼 영역
            TimeFrameSelection(
                selectedTimeFrame = uiState.config.timeFrame,
                onTimeFrameChange = { viewModel.onEvent(ChartUiEvent.ChangeTimeFrame(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = Spacing.md, vertical = Spacing.xs)
            )

            // 시간버튼과 바텀시트 사이 간격 최소화
            Spacer(modifier = Modifier.height(Spacing.sm))

        }

        // 2. 앱바 (중간 레이어)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(MaterialTheme.colorScheme.surface)
                .zIndex(1f)
        ) {
            TopAppBar(
                onBackClick = {
                    viewModel.onEvent(ChartUiEvent.BackPressed)
                    onNavigateBack()
                },
                isFavorite = uiState.isFavorite,
                onFavoriteClick = { viewModel.onEvent(ChartUiEvent.ToggleFavorite) },
                stockInfo = uiState.currentStock,
                onSettingsClick = { viewModel.onEvent(ChartUiEvent.ToggleIndicatorSettings) },
                onNavigateToAIDialog = { showCharacterDialog = true },
                modifier = Modifier.fillMaxSize()
            )
        }

        // 3. 애니메이션되는 헤더 박스 (앱바 위로 올라감)
        AnimatedHeaderBox(
            stockInfo = uiState.currentStock,
            headerAlignmentProgress = headerAlignmentProgress,
            contentOffsetY = contentOffsetY.value // 헤더는 원래대로 contentOffsetY 적용
        )

        // 4. 커스텀 3단계 바텀시트
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = with(density) { sheetAnimY.value.toDp() })
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    spotColor = Color.Black.copy(alpha = 0.1f)
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
                            val velocity = if (duration > 0) totalDragAmount / duration * 1000 else 0f
                            handleDragEnd(velocity, sheetAnimY.value)
                        }
                    ) { _, dragAmount ->
                        totalDragAmount += dragAmount.y

                        // 드래그 중 실시간 업데이트
                        coroutineScope.launch {
                            val newY = (sheetAnimY.value + dragAmount.y).coerceIn(
                                sheetPositions.expanded,
                                sheetPositions.collapsed
                            )
                            sheetAnimY.snapTo(newY)
                        }
                    }
                }
        ) {
            Column {
                // 드래그 핸들
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(
                                color = Gray300,
                                shape = RoundedCornerShape(2.dp)
                            )
                            .semantics {
                                contentDescription = "바텀시트 드래그 핸들"
                            }
                    )
                }

                // 동적 높이 바텀시트 콘텐츠
                BottomSheetContent(
                    viewModel = viewModel,
                    nestedScrollConnection = nestedScrollConnection,
                    holdingsListState = holdingsListState,
                    tradingHistoryListState = tradingHistoryListState,
                    bottomSheetState = bottomSheetState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            with(density) {
                                val currentProgress = (sheetPositions.collapsed - sheetAnimY.value) / (sheetPositions.collapsed - sheetPositions.expanded)
                                val progress = currentProgress.coerceIn(0f, 1f)

                                // 160dp(collapsed) ~ 550dp(expanded) 사이에서 실시간 보간
                                val minHeight = 160.dp
                                val maxHeight = 550.dp
                                minHeight + (maxHeight - minHeight) * progress
                            }
                        )
                )
            }
        }

        // 5. 화면 하단 고정 매수/매도 버튼
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,  // 바텀시트보다 높은 elevation
                    spotColor = Color(0x1A000000)
                )
                .background(Color.White)
                .zIndex(10f)  // 바텀시트보다 위에 표시
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm + Spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm + Spacing.xs)
            ) {
                Button(
                    onClick = {
                        viewModel.onEvent(ChartUiEvent.SellClicked)
                        onNavigateToStockPurchase(uiState.currentStock.code, "sell")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .semantics {
                            contentDescription = "${uiState.currentStock.name} 판매하기 버튼"
                        },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "판매하기",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = TitleB16
                    )
                }

                Button(
                    onClick = {
                        viewModel.onEvent(ChartUiEvent.BuyClicked)
                        onNavigateToStockPurchase(uiState.currentStock.code, "buy")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .semantics {
                            contentDescription = "${uiState.currentStock.name} 구매하기 버튼"
                        },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "구매하기",
                        color = MaterialTheme.colorScheme.onSecondary,
                        style = TitleB16
                    )
                }
            }
        }

        // 지표 설정 다이얼로그
        if (uiState.showIndicatorSettings) {
            IndicatorSettingsDialog(
                config = uiState.config,
                onIndicatorToggle = { indicatorType, enabled ->
                    viewModel.onEvent(ChartUiEvent.ToggleIndicator(indicatorType, enabled))
                },
                onDismiss = {
                    viewModel.onEvent(ChartUiEvent.HideIndicatorSettings)
                }
            )
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
        
        // Character Selection Dialog
        if (showCharacterDialog) {
            CharacterSelectionDialog(
                onDismiss = { showCharacterDialog = false },
                onConfirm = { character ->
                    // 선택한 캐릭터 처리
                    println("선택된 캐릭터: ${character.name}")
                    showCharacterDialog = false
                }
            )
        }
    }
}

@Composable
private fun TopAppBar(
    onBackClick: () -> Unit,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    stockInfo: StockInfo,
    onSettingsClick: () -> Unit = {},
    onNavigateToAIDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(start = Spacing.xs + 3.dp)
                .semantics {
                    contentDescription = "이전 화면으로 돌아가기"
                }
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                painter = if (isFavorite) painterResource(R.drawable.pink_heart) else painterResource(R.drawable.blank_heart),
                contentDescription = null,
                tint = if (isFavorite) Color.Unspecified else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }

        IconButton(
            onClick = { onNavigateToAIDialog() },
            modifier = Modifier.semantics {
                contentDescription = "AI 차트 분석"
            }
        ) {
            Icon(
                painter = painterResource(R.drawable.ai_button),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
        }

        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.semantics {
                contentDescription = "지표 설정"
            }
        ) {
            Icon(
                painter = painterResource(R.drawable.setting),
                contentDescription = null,
                tint = Gray900,
                modifier = Modifier.size(24.dp)
            )
        }

    }
}

// Legacy v4 OptimizedChartView removed - replaced with v5 MultiPanelChart

@Composable
fun TimeFrameSelection(
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
                shape = RoundedCornerShape(8.dp),
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
                shape = RoundedCornerShape(8.dp),
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
    nestedScrollConnection: NestedScrollConnection,
    holdingsListState: LazyListState,
    tradingHistoryListState: LazyListState,
    bottomSheetState: BottomSheetState,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTabIndex = uiState.selectedBottomTab
    val tabTitles = listOf("보유현황", "매매내역", "차트패턴")

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
                    onClick = {
                        viewModel.onEvent(ChartUiEvent.ChangeBottomTab(index))
                    },
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
                .padding(
                    bottom = when (bottomSheetState) {
                        BottomSheetState.EXPANDED -> 0.dp // 전체 화면일 때는 패딩 없음
                        else -> 40.dp // COLLAPSED, HALF_EXPANDED일 때만 패딩
                    }
                )
        ) {
            when (selectedTabIndex) {
                0 -> HoldingsContent(
                    holdings = uiState.holdingItems,
                    listState = holdingsListState,
                    nestedScrollConnection = nestedScrollConnection,
                    bottomSheetState = bottomSheetState
                )
                1 -> TradingHistoryContent(
                    history = uiState.tradingHistory,
                    currentStockCode = uiState.currentStock.code,
                    listState = tradingHistoryListState,
                    nestedScrollConnection = nestedScrollConnection,
                    bottomSheetState = bottomSheetState
                )
                2 -> PatternAnalysisContent(
                    patternAnalysisCount = uiState.patternAnalysisCount,
                    maxPatternAnalysisCount = uiState.maxPatternAnalysisCount,
                    lastPatternAnalysis = uiState.patternAnalysis,
                    onAnalyzeClick = { viewModel.onEvent(ChartUiEvent.AnalyzePattern) }
                )
            }
        }
    }
}

@Composable
private fun HoldingsContent(
    holdings: List<HoldingItem>,
    listState: LazyListState,
    nestedScrollConnection: NestedScrollConnection,
    bottomSheetState: BottomSheetState
) {
    if (holdings.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "보유 주식이 없어요!",
                fontSize = 14.sp,
                color = Gray600
            )
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            userScrollEnabled = bottomSheetState != BottomSheetState.COLLAPSED
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 종목별 컬러 아이콘
            val stockColor = when (item.name) {
                "삼성전자" -> Color(0xFF1428A0)
                "GS리테일" -> Color(0xFF00A651)
                "한화생명" -> Color(0xFFE8501A)
                "LG전자" -> Color(0xFFA50034)
                "하이트진로맥주" -> Color(0xFFED1C24)
                else -> Color(0xFF666666)
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(stockColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (item.name) {
                        "삼성전자" -> "삼성"
                        "GS리테일" -> "GS"
                        "한화생명" -> "한화"
                        "LG전자" -> "LG"
                        "하이트진로맥주" -> "진로"
                        else -> "종목"
                    },
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = item.name,
                    style = TitleB20,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${item.quantity} · 주당 ${String.format("%,d", item.value / (item.quantity.replace("주", "").trim().toIntOrNull() ?: 1))}원",
                    style = BodyR12,
                    color = Gray500
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${String.format("%,d", item.value)}원",
                style = TitleB18,
                color = MaterialTheme.colorScheme.onSurface
            )

            val isPositive = item.change >= 0

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(if (isPositive) R.drawable.up_triangle else R.drawable.down_triangle),
                    contentDescription = null,
                    tint = MainPink,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text(
                    text = "${String.format("%+,d", (item.value * item.change / 100).toInt())}원 (${String.format("%.2f", item.change.absoluteValue)}%)",
                    style = BodyR14,
                    color = MainPink
                )
            }
        }
    }
}

@Composable
private fun TradingHistoryContent(
    history: List<TradingItem>,
    currentStockCode: String,
    listState: LazyListState,
    nestedScrollConnection: NestedScrollConnection,
    bottomSheetState: BottomSheetState
) {
    // 현재 주식 코드와 일치하는 매매내역만 필터링
    val filteredHistory = history.filter { tradingItem ->
        tradingItem.stockCode == currentStockCode
    }

    if (filteredHistory.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "매매 내역이 없어요",
                fontSize = 14.sp,
                color = Gray600
            )
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            userScrollEnabled = bottomSheetState != BottomSheetState.COLLAPSED
        ) {
            items(filteredHistory) { tradingItem ->
                TradingItemRow(tradingItem)
            }
        }
    }
}

@Composable
private fun TradingItemRow(item: TradingItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 구매/판매 구분 아이콘
            val isBuy = item.type == "구매"
            val iconColor = if (isBuy) MainPink else MainBlue
            val iconText = if (isBuy) "구매" else "판매"

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iconText,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = item.quantity,
                    fontSize = 14.sp,
                    color = Gray900,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = item.date,
                    fontSize = 12.sp,
                    color = Gray600
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${String.format("%,d", item.amount)}원",
                fontSize = 14.sp,
                color = Gray900,
                fontWeight = FontWeight.SemiBold
            )
            val quantityNumber = item.quantity.replace("주", "").trim().toIntOrNull() ?: 1
            Text(
                text = "주당 ${String.format("%,d", item.amount / quantityNumber)}원",
                fontSize = 12.sp,
                color = Gray600
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
        // 분석 결과 표시 - 결과가 있으면 표시, 없으면 "분석 결과가 없어요" 표시
        if (lastPatternAnalysis != null || patternAnalysisCount > 0) {
            // 분석 결과가 있는 경우
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(8.dp)
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
                            "2025-07-28 오후 1시 35분",
                            fontSize = 12.sp,
                            color = Color(0xFF616161)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "상승 삼각형, 헤드앤숄더",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "지향선을 여러 차례 돌파 시도했지 때문에 상승 가능성이 높습니다.",
                        fontSize = 14.sp,
                        color = Color(0xFF424242),
                        lineHeight = 20.sp
                    )
                }
            }
        } else {
            // 분석 결과가 없는 경우
            Text(
                "최근 분석 결과가 없어요.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Gray600,
                modifier = Modifier.padding(vertical = 32.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 분석 버튼
        Button(
            onClick = onAnalyzeClick,
            enabled = patternAnalysisCount < maxPatternAnalysisCount,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3), // 파란색으로 변경
                disabledContainerColor = Color(0xFFE0E0E0)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 24.dp)
                .semantics {
                    contentDescription = if (patternAnalysisCount < maxPatternAnalysisCount) {
                        "다시 분석하기 버튼, 남은 횟수: ${maxPatternAnalysisCount - patternAnalysisCount}번"
                    } else {
                        "오늘 분석 횟수를 모두 사용하셨습니다"
                    }
                }
        ) {
            Text(
                "다시 분석하기 ($patternAnalysisCount/$maxPatternAnalysisCount)",
                color = if (patternAnalysisCount < maxPatternAnalysisCount) Color.White else Color(0xFF9E9E9E),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun IndicatorSettingsDialog(
    config: ChartConfig,
    onIndicatorToggle: (String, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "지표 설정",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Volume 지표
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "거래량 (Volume)",
                        fontSize = 16.sp
                    )
                    Switch(
                        checked = config.indicators.volume,
                        onCheckedChange = { enabled ->
                            onIndicatorToggle("volume", enabled)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MainPink,
                            checkedTrackColor = MainPink.copy(alpha = 0.5f)
                        )
                    )
                }

                // RSI 지표
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RSI",
                        fontSize = 16.sp
                    )
                    Switch(
                        checked = config.indicators.rsi,
                        onCheckedChange = { enabled ->
                            onIndicatorToggle("rsi", enabled)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MainPink,
                            checkedTrackColor = MainPink.copy(alpha = 0.5f)
                        )
                    )
                }

                // MACD 지표
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MACD",
                        fontSize = 16.sp
                    )
                    Switch(
                        checked = config.indicators.macd,
                        onCheckedChange = { enabled ->
                            onIndicatorToggle("macd", enabled)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MainPink,
                            checkedTrackColor = MainPink.copy(alpha = 0.5f)
                        )
                    )
                }

                // SMA5 지표
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SMA5",
                        fontSize = 16.sp
                    )
                    Switch(
                        checked = config.indicators.sma5,
                        onCheckedChange = { enabled ->
                            onIndicatorToggle("sma5", enabled)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MainPink,
                            checkedTrackColor = MainPink.copy(alpha = 0.5f)
                        )
                    )
                }

                // SMA20 지표
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SMA20",
                        fontSize = 16.sp
                    )
                    Switch(
                        checked = config.indicators.sma20,
                        onCheckedChange = { enabled ->
                            onIndicatorToggle("sma20", enabled)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MainPink,
                            checkedTrackColor = MainPink.copy(alpha = 0.5f)
                        )
                    )
                }

                // Bollinger Bands 지표
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "볼린저 밴드",
                        fontSize = 16.sp
                    )
                    Switch(
                        checked = config.indicators.bollingerBands,
                        onCheckedChange = { enabled ->
                            onIndicatorToggle("bollingerBands", enabled)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MainPink,
                            checkedTrackColor = MainPink.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "확인",
                    color = MainPink,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}