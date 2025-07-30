package com.lago.app.presentation.ui.chart

import android.view.ViewGroup
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.animation.core.tween
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.runtime.derivedStateOf
import kotlin.math.abs
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

// 애니메이션 상수들 - 반응성 향상
private object ChartAnimationConstants {
    const val ANIMATION_DURATION = 150  // 200 -> 150
    const val SLOW_ANIMATION_DURATION = 200  // 250 -> 200
    const val TITLE_TRANSLATION_X_MAX = -150f
    const val TITLE_TRANSLATION_Y_MAX = -60f
    const val STOCK_NAME_SIZE_MAX = 24f
    const val STOCK_NAME_SIZE_MID = 20f
    const val STOCK_NAME_SIZE_MIN = 16f
    const val STOCK_PRICE_SIZE_MAX = 32f
    const val STOCK_PRICE_SIZE_MID = 24f
    const val STOCK_PRICE_SIZE_MIN = 16f
    const val HEADER_HEIGHT_MAX = 120f
    const val HEADER_HEIGHT_MID = 96f
    const val HEADER_HEIGHT_MIN = 64f
    const val PROGRESS_THRESHOLD_LOW = 0.3f
    const val PROGRESS_THRESHOLD_MID = 0.6f
    const val PROGRESS_THRESHOLD_HIGH = 0.7f
}

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(
    viewModel: ChartViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // 커스텀 바텀시트 상태 - 3단계 지원
    val bottomSheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = false
        )
    )

    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    // 바텀시트 3단계 높이 정의
    val peekHeight = 150.dp
    val midHeight = with(density) { (screenHeight.toPx() * 0.55f).toDp() }
    val maxHeight = with(density) { (screenHeight.toPx() * 0.75f).toDp() }

    // 바텀시트 드래그 제한을 위한 LaunchedEffect
    LaunchedEffect(bottomSheetState.bottomSheetState) {
        // 하단 제한: peekHeight 아래로 못 내려가게 함
        try {
            val currentOffset = bottomSheetState.bottomSheetState.requireOffset()
            val peekOffsetPx = with(density) { (screenHeight - peekHeight).toPx() }

            if (currentOffset > peekOffsetPx) {
                bottomSheetState.bottomSheetState.partialExpand()
            }
        } catch (e: Exception) {
            // offset이 아직 초기화되지 않은 경우 무시
        }
    }

    // offset 기반 실시간 진행도 계산 - 성능 최적화
    val currentOffset = remember(bottomSheetState.bottomSheetState) {
        derivedStateOf {
            try {
                bottomSheetState.bottomSheetState.requireOffset()
            } catch (e: IllegalStateException) {
                with(density) { (screenHeight - peekHeight).toPx() }
            }
        }
    }.value

    val peekOffsetPx = with(density) { (screenHeight - peekHeight).toPx() }
    val midOffsetPx = with(density) { (screenHeight - midHeight).toPx() }
    val maxOffsetPx = with(density) { (screenHeight - maxHeight).toPx() }

    // 3단계 진행도 계산 수정 - 더 정확한 계산
    val sheetProgress = when {
        currentOffset >= peekOffsetPx -> 0f  // 하단 상태
        currentOffset <= maxOffsetPx -> 1f   // 최상단 상태
        currentOffset > midOffsetPx -> {
            // 하단→중간: 0.0 → 0.5
            val progress = (peekOffsetPx - currentOffset) / (peekOffsetPx - midOffsetPx)
            (progress * 0.5f).coerceIn(0f, 0.5f)
        }
        else -> {
            // 중간→상단: 0.5 → 1.0
            val progress = (midOffsetPx - currentOffset) / (midOffsetPx - maxOffsetPx)
            (0.5f + progress * 0.5f).coerceIn(0.5f, 1f)
        }
    }

    // 부드러운 애니메이션 진행도 - 즉각 반응으로 설정
    val animatedProgress by animateFloatAsState(
        targetValue = sheetProgress,
        animationSpec = tween(durationMillis = 0), // 즉각 반응
        label = "animated_progress"
    )

    // 주가 정보 애니메이션 값들 - 중단에서 완전히 올라가도록 조정
    val titleTranslationX by animateFloatAsState(
        targetValue = when {
            animatedProgress <= 0.5f -> 0f  // 중단까지는 그대로
            else -> ChartAnimationConstants.TITLE_TRANSLATION_X_MAX *
                    ((animatedProgress - 0.5f) / 0.5f)  // 중단에서 완전히 올라감
        },
        animationSpec = tween(durationMillis = 0), // 즉각 반응
        label = "title_translation_x"
    )

    val titleTranslationY by animateFloatAsState(
        targetValue = when {
            animatedProgress <= 0.5f -> 0f  // 중단까지는 그대로
            else -> ChartAnimationConstants.TITLE_TRANSLATION_Y_MAX *
                    ((animatedProgress - 0.5f) / 0.5f)  // 중단에서 완전히 올라감
        },
        animationSpec = tween(durationMillis = 0), // 즉각 반응
        label = "title_translation_y"
    )

    val stockNameSize by animateFloatAsState(
        targetValue = when {
            animatedProgress <= ChartAnimationConstants.PROGRESS_THRESHOLD_LOW ->
                ChartAnimationConstants.STOCK_NAME_SIZE_MAX -
                        (4f * animatedProgress / ChartAnimationConstants.PROGRESS_THRESHOLD_LOW)
            animatedProgress <= ChartAnimationConstants.PROGRESS_THRESHOLD_HIGH ->
                ChartAnimationConstants.STOCK_NAME_SIZE_MID -
                        (4f * (animatedProgress - ChartAnimationConstants.PROGRESS_THRESHOLD_LOW) / 0.4f)
            else -> ChartAnimationConstants.STOCK_NAME_SIZE_MIN
        },
        animationSpec = tween(durationMillis = 0), // 즉각 반응
        label = "stock_name_size"
    )

    val stockPriceSize by animateFloatAsState(
        targetValue = when {
            animatedProgress <= ChartAnimationConstants.PROGRESS_THRESHOLD_LOW ->
                ChartAnimationConstants.STOCK_PRICE_SIZE_MAX -
                        (8f * animatedProgress / ChartAnimationConstants.PROGRESS_THRESHOLD_LOW)
            animatedProgress <= ChartAnimationConstants.PROGRESS_THRESHOLD_HIGH ->
                ChartAnimationConstants.STOCK_PRICE_SIZE_MID -
                        (8f * (animatedProgress - ChartAnimationConstants.PROGRESS_THRESHOLD_LOW) / 0.4f)
            else -> ChartAnimationConstants.STOCK_PRICE_SIZE_MIN
        },
        animationSpec = tween(durationMillis = 0), // 즉각 반응
        label = "stock_price_size"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        BottomSheetScaffold(
            scaffoldState = bottomSheetState,
            sheetContent = {
                // 바텀시트 높이를 제한 - 하단/상단 브레이크
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(
                            min = peekHeight,  // 최소 높이 제한 (하단 제한)
                            max = maxHeight    // 최대 높이 제한 (상단 제한)
                        )
                ) {
                    BottomSheetContent(viewModel = viewModel)
                }
            },
            sheetPeekHeight = 150.dp,
            modifier = Modifier.padding(bottom = 76.dp),
            sheetContainerColor = Color.White,
            sheetContentColor = Color.Black,
            sheetShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            sheetDragHandle = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp, bottom = 4.dp)
                            .width(32.dp)
                            .height(5.dp)
                            .shadow(
                                elevation = 2.dp,
                                shape = RoundedCornerShape(12.dp),
                                spotColor = Color(0x1A000000)
                            )
                            .background(
                                color = Color(0xFFBDBDBD),
                                shape = RoundedCornerShape(12.dp)
                            )
                    )
                }
            },
            containerColor = Color.White
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(paddingValues)
                ) {
                    // 앱바
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(elevation = 4.dp, spotColor = Color(0x14000000))
                            .background(Color.White)
                    ) {
                        TopAppBar(
                            onBackClick = { viewModel.onEvent(ChartUiEvent.BackPressed) },
                            isFavorite = uiState.isFavorite,
                            onFavoriteClick = {
                                viewModel.onEvent(ChartUiEvent.ToggleFavorite)
                            },
                            onNotificationClick = { viewModel.onEvent(ChartUiEvent.NotificationClicked) },
                            onSettingsClick = { viewModel.onEvent(ChartUiEvent.SettingsClicked) },
                            stockInfo = uiState.currentStock,
                            animatedProgress = animatedProgress,
                            modifier = Modifier.fillMaxSize()
                        )

                        // 주가 정보가 앱바로 이동하는 애니메이션 - 부드러운 페이드인
                        if (animatedProgress > 0.5f) {  // 중단에서 앱바 표시 시작
                            val appBarAlpha by animateFloatAsState(
                                targetValue = ((animatedProgress - 0.5f) / 0.5f).coerceIn(0f, 1f),
                                animationSpec = tween(durationMillis = 0), // 즉각 반응
                                label = "appbar_alpha"
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = 50.dp)
                                    .alpha(appBarAlpha),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = uiState.currentStock.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${String.format("%.0f", uiState.currentStock.currentPrice)}원",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )

                                if (animatedProgress > 0.8f) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    val isPositive = uiState.currentStock.priceChange >= 0
                                    Text(
                                        text = "${if (isPositive) "+" else ""}${String.format("%.0f", uiState.currentStock.priceChange)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isPositive) Color(0xFFFF69B4) else Color(0xFF42A6FF)
                                    )
                                }
                            }
                        }
                    }

                    // 주식 정보 헤더 (바텀시트 진행도에 따라 크기 변화)
                    val headerHeight by animateFloatAsState(
                        targetValue = when {
                            animatedProgress <= ChartAnimationConstants.PROGRESS_THRESHOLD_LOW ->
                                ChartAnimationConstants.HEADER_HEIGHT_MAX * (1f - animatedProgress * 0.2f)
                            animatedProgress <= ChartAnimationConstants.PROGRESS_THRESHOLD_HIGH ->
                                ChartAnimationConstants.HEADER_HEIGHT_MID * (1f - (animatedProgress - ChartAnimationConstants.PROGRESS_THRESHOLD_LOW) * 0.8f)
                            else -> ChartAnimationConstants.HEADER_HEIGHT_MIN *
                                    (1f - (animatedProgress - ChartAnimationConstants.PROGRESS_THRESHOLD_HIGH) * 2f).coerceAtLeast(0f)
                        },
                        animationSpec = tween(durationMillis = 0), // 즉각 반응
                        label = "header_height"
                    )

                    if (headerHeight > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(headerHeight.dp)
                                .graphicsLayer {
                                    // 앱바로 이동하는 애니메이션
                                    translationX = titleTranslationX
                                    translationY = titleTranslationY
                                    alpha = (1f - animatedProgress * 1.2f).coerceAtLeast(0f)
                                }
                        ) {
                            StockInfoHeader(
                                stockName = uiState.currentStock.name,
                                stockCode = uiState.currentStock.code,
                                currentPrice = uiState.currentStock.currentPrice,
                                priceChange = uiState.currentStock.priceChange,
                                priceChangePercent = uiState.currentStock.priceChangePercent,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(
                                        start = 32.dp,
                                        end = 32.dp,
                                        top = 16.dp,
                                        bottom = 8.dp
                                    )
                            )
                        }
                    }

                    // 차트 + 시간 버튼을 하나의 박스로 감싸서 바텀시트가 침범하지 않도록 함
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                    ) {
                        // 차트 영역 - 고정 높이로 설정
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
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
                                        contentDescription = "주식 차트: 현재 가격 ${uiState.currentStock.currentPrice}원, " +
                                                "변동량 ${uiState.currentStock.priceChange}원"
                                    }
                            )
                        }

                        // 시간대 선택 - 항상 보이도록 배치
                        TimeFrameSelection(
                            selectedTimeFrame = uiState.config.timeFrame,
                            onTimeFrameChange = { timeFrame ->
                                viewModel.onEvent(ChartUiEvent.ChangeTimeFrame(timeFrame))
                            },
                            modifier = Modifier
                                .background(Color.White)
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 16.dp
                                )
                        )
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
    animatedProgress: Float = 0f,
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
                contentDescription = null, // semantics에서 처리
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
                contentDescription = null, // semantics에서 처리
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
                contentDescription = null, // semantics에서 처리
                tint = Color(0xFFFFE476),
                modifier = Modifier
                    .size(25.dp)
                    .clip(CircleShape)
                    .background(
                        color = Color(0xFF69E1F6)
                    )
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
private fun StockInfoHeader(
    stockName: String,
    stockCode: String,
    currentPrice: Float,
    priceChange: Float,
    priceChangePercent: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // 왼쪽: 종목명 + 가격 (세로 배치)
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stockName,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${String.format("%.0f", currentPrice)}원",
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                lineHeight = 36.sp
            )
        }

        // 오른쪽: 수익률 정보 (세로 배치, 우측 정렬)
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(start = 16.dp)
        ) {
            val isPositive = priceChange >= 0
            Text(
                text = "${if (isPositive) "▲" else "▼"}${String.format("%.0f", abs(priceChange))}",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isPositive) Color(0xFFFF69B4) else Color(0xFF42A6FF),
                modifier = Modifier.semantics {
                    contentDescription = if (isPositive) "상승 ${String.format("%.0f", priceChange)}원"
                    else "하락 ${String.format("%.0f", abs(priceChange))}원"
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "(${if (isPositive) "+" else ""}${String.format("%.2f", priceChangePercent)}%)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = if (isPositive) Color(0xFFFF69B4) else Color(0xFF42A6FF),
                modifier = Modifier.semantics {
                    contentDescription = if (isPositive) "상승률 ${String.format("%.2f", priceChangePercent)}퍼센트"
                    else "하락률 ${String.format("%.2f", abs(priceChangePercent))}퍼센트"
                }
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
                contentDescription = "주식 가격 차트"
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

    // 현재 선택된 것이 분봉인지 확인
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
                    .height(30.dp)
            )

            // 드롭다운 메뉴 - 흰색 배경으로 변경
            DropdownMenu(
                expanded = showMinuteDropdown,
                onDismissRequest = { showMinuteDropdown = false },
                modifier = Modifier
                    .background(Color.White)  // 흰색 배경
                    .width(120.dp)
            ) {
                minuteFrames.forEach { (code, name) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                name,
                                color = Color.Black,  // 검은색 텍스트
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
                    .height(30.dp)
            )
        }
    }
}

@Composable
private fun BottomSheetContent(
    viewModel: ChartViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTabIndex = uiState.selectedBottomTab
    val tabTitles = listOf("보유현황", "매매내역", "패턴분석")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                spotColor = Color(0x0F000000)
            )
            .background(
                color = Color.White,
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
            )
    ) {
        // 탭 (항상 표시) - 레퍼런스에 맞게 크기 조정
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
                            fontSize = 16.sp, // 20sp -> 16sp로 축소
                            fontWeight = FontWeight.Medium,
                            color = if (selectedTabIndex == index) Color(0xFF08090E) else Color(0xFF747476)
                        )
                    },
                    modifier = Modifier.height(44.dp) // 탭 높이 고정
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

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(holdings) { holding ->
            HoldingItemRow(holding)
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

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(history) { tradingItem ->
            TradingItemRow(tradingItem)
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

        // 분석 버튼
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

        // 최근 분석 결과
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
                // 차트 기본 옵션 설정
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

                // 캔들스틱 데이터 설정
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

                // 이동평균선 추가
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

                // 거래량 추가
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