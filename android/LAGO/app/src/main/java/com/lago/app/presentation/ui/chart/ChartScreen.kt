package com.lago.app.presentation.ui.chart

import android.view.ViewGroup
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.alpha
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
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
import com.tradingview.lightweightcharts.view.ChartsView
import com.tradingview.lightweightcharts.api.options.models.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(
    viewModel: ChartViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
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
    val peekHeight = 70.dp
    val midHeight = screenHeight * 0.6f
    val maxHeight = screenHeight * 0.9f
    
    // offset 기반 실시간 진행도 계산
    val currentOffset = try {
        bottomSheetState.bottomSheetState.requireOffset()
    } catch (e: Exception) {
        with(density) { (screenHeight - peekHeight).toPx() }
    }
    
    val peekOffsetPx = with(density) { (screenHeight - peekHeight).toPx() }
    val midOffsetPx = with(density) { (screenHeight - midHeight).toPx() }
    val maxOffsetPx = with(density) { (screenHeight - maxHeight).toPx() }
    
    // 3단계 진행도 계산 (0.0 = 하단, 0.5 = 중단, 1.0 = 상단)
    val sheetProgress = when {
        currentOffset >= peekOffsetPx -> 0f
        currentOffset <= maxOffsetPx -> 1f
        currentOffset > midOffsetPx -> {
            // 하단(peek) → 중단(mid): 0.0 → 0.5
            (peekOffsetPx - currentOffset) / (peekOffsetPx - midOffsetPx) * 0.5f
        }
        else -> {
            // 중단(mid) → 상단(max): 0.5 → 1.0
            0.5f + (midOffsetPx - currentOffset) / (midOffsetPx - maxOffsetPx) * 0.5f
        }
    }
    
    // 부드러운 애니메이션 진행도
    val animatedProgress by animateFloatAsState(
        targetValue = sheetProgress,
        animationSpec = tween(durationMillis = 200),
        label = "animated_progress"
    )
    
    // 주가 정보 애니메이션 값들 - progress 기반으로 부드럽게 변화
    val titleTranslationX by animateFloatAsState(
        targetValue = when {
            animatedProgress <= 0.6f -> 0f
            else -> -150f * ((animatedProgress - 0.6f) / 0.4f)  // 0.6~1.0 구간에서 -150px까지
        },
        animationSpec = tween(durationMillis = 250),
        label = "title_translation_x"
    )
    
    val titleTranslationY by animateFloatAsState(
        targetValue = when {
            animatedProgress <= 0.6f -> 0f
            else -> -60f * ((animatedProgress - 0.6f) / 0.4f)  // 0.6~1.0 구간에서 -60px까지
        },
        animationSpec = tween(durationMillis = 250),
        label = "title_translation_y"
    )
    
    val stockNameSize by animateFloatAsState(
        targetValue = when {
            animatedProgress <= 0.3f -> 24f - (4f * animatedProgress / 0.3f)  // 24 → 20
            animatedProgress <= 0.7f -> 20f - (4f * (animatedProgress - 0.3f) / 0.4f)  // 20 → 16
            else -> 16f
        },
        animationSpec = tween(durationMillis = 200),
        label = "stock_name_size"
    )
    
    val stockPriceSize by animateFloatAsState(
        targetValue = when {
            animatedProgress <= 0.3f -> 32f - (8f * animatedProgress / 0.3f)  // 32 → 24
            animatedProgress <= 0.7f -> 24f - (8f * (animatedProgress - 0.3f) / 0.4f)  // 24 → 16
            else -> 16f
        },
        animationSpec = tween(durationMillis = 200),
        label = "stock_price_size"
    )

    BottomSheetScaffold(
        scaffoldState = bottomSheetState,
        sheetContent = {
            BottomSheetContent(viewModel = viewModel)
        },
        sheetPeekHeight = 70.dp,  // 탭만 보이는 높이
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
                    if (animatedProgress > 0.6f) {
                        val appBarAlpha by animateFloatAsState(
                            targetValue = ((animatedProgress - 0.6f) / 0.4f).coerceIn(0f, 1f),
                            animationSpec = tween(durationMillis = 200),
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
                        animatedProgress <= 0.3f -> 120f * (1f - animatedProgress * 0.2f)  // 120 → 96
                        animatedProgress <= 0.7f -> 96f * (1f - (animatedProgress - 0.3f) * 0.8f)  // 96 → 64
                        else -> 64f * (1f - (animatedProgress - 0.7f) * 2f).coerceAtLeast(0f)  // 64 → 0
                    },
                    animationSpec = tween(durationMillis = 200),
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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                start = 32.dp,
                                end = 32.dp,
                                top = 16.dp,
                                bottom = 8.dp
                            ),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.currentStock.name,
                            fontSize = stockNameSize.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        if (animatedProgress < 0.8f) {
                            Text(
                                text = "${String.format("%.0f", uiState.currentStock.currentPrice)}원",
                                fontSize = stockPriceSize.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                                lineHeight = (stockPriceSize * 0.9f).sp
                            )
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            val isPositive = uiState.currentStock.priceChange >= 0
                            Text(
                                text = "${if (isPositive) "▲" else "▼"}${String.format("%.0f", Math.abs(uiState.currentStock.priceChange))} (${String.format("%.2f", Math.abs(uiState.currentStock.priceChangePercent))}%)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isPositive) Color(0xFFFF69B4) else Color(0xFF42A6FF)
                            )
                        }
                    }
                    }
                }

                // 차트 영역 - 바텀시트 위치에 따라 동적 크기 조정
                val chartHeight = with(density) {
                    val currentSheetHeight = when {
                        animatedProgress <= 0.5f -> {
                            // 하단→중단: peekHeight → midHeight
                            peekHeight + (midHeight - peekHeight) * (animatedProgress / 0.5f)
                        }
                        else -> {
                            // 중단→상단: midHeight → maxHeight  
                            midHeight + (maxHeight - midHeight) * ((animatedProgress - 0.5f) / 0.5f)
                        }
                    }
                    (screenHeight - currentSheetHeight - 120.dp).coerceAtLeast(200.dp)  // 헤더 높이 고려
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(chartHeight)
                        .padding(horizontal = 16.dp)
                        .background(Color.White)
                        .padding(horizontal = 16.dp)
                ) {
                    SingleChartView(
                        candlestickData = uiState.candlestickData,
                        volumeData = uiState.volumeData,
                        sma5Data = uiState.sma5Data,
                        sma20Data = uiState.sma20Data,
                        config = uiState.config
                    )
                }

                // 시간대 선택
                TimeFrameSelection(
                    selectedTimeFrame = uiState.config.timeFrame,
                    onTimeFrameChange = { timeFrame ->
                        viewModel.onEvent(ChartUiEvent.ChangeTimeFrame(timeFrame))
                    },
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    )
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
            modifier = Modifier.padding(start = 7.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "뒤로가기",
                tint = Color(0xFF404040)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = onFavoriteClick) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "관심종목",
                tint = if (isFavorite) Color(0xFFFF69B4) else Color.Black
            )
        }

        IconButton(onClick = onNotificationClick) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "알림",
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
    Column(modifier = modifier) {
        Text(
            text = stockName,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${String.format("%.0f", currentPrice)}원",
            fontSize = 40.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            lineHeight = 28.sp
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            val isPositive = priceChange >= 0
            Text(
                text = "${if (isPositive) "▲" else "▼"}${String.format("%.0f", Math.abs(priceChange))} (${String.format("%.2f", Math.abs(priceChangePercent))}%)",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isPositive) Color(0xFFFF69B4) else Color(0xFF42A6FF)
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
    config: ChartConfig
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
        modifier = Modifier.fillMaxSize(),
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

    val timeFrames = listOf(
        "10" to "10분",
        "D" to "일",
        "W" to "주",
        "M" to "월", 
        "Y" to "년"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
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
        // 탭 (항상 표시)
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
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (selectedTabIndex == index) Color(0xFF08090E) else Color(0xFF747476)
                        )
                    }
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
        
        // 매수/매도 버튼
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

@Composable
private fun HoldingsContent(holdings: List<HoldingItem>) {

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(holdings.size) { index ->
            HoldingItemRow(holdings[index])
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
        items(history.size) { index ->
            TradingItemRow(history[index])
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

@Composable
private fun TradingButtons(
    modifier: Modifier = Modifier,
    viewModel: ChartViewModel
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = { viewModel.onEvent(ChartUiEvent.SellClicked) },
            modifier = Modifier
                .weight(1f)
                .height(44.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF42A6FF)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                "매도하기",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Button(
            onClick = { viewModel.onEvent(ChartUiEvent.BuyClicked) },
            modifier = Modifier
                .weight(1f)
                .height(44.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF99C5)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                "매수하기",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
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
                        textColor = AndroidColor.parseColor("#616161").toIntColor()
                    }
                    grid = gridOptions {
                        vertLines = gridLineOptions {
                            color = AndroidColor.parseColor("#E6E6E6").toIntColor()
                            style = LineStyle.DASHED
                        }
                        horzLines = gridLineOptions {
                            color = AndroidColor.parseColor("#E6E6E6").toIntColor()
                            style = LineStyle.DASHED
                        }
                    }
                }

                // 캔들스틱 데이터 설정
                if (candlestickData.isNotEmpty()) {
                    val chartData = candlestickData.map { data ->
                        com.tradingview.lightweightcharts.api.series.models.CandlestickData(
                            time = ChartUtils.timestampToBusinessDay(data.time),
                            open = data.open,
                            high = data.high,
                            low = data.low,
                            close = data.close
                        )
                    }

                    chartsView.api.addCandlestickSeries(
                        options = CandlestickSeriesOptions(
                            upColor = AndroidColor.parseColor("#FF99C5").toIntColor(),
                            downColor = AndroidColor.parseColor("#42A6FF").toIntColor(),
                            borderVisible = false,
                            wickUpColor = AndroidColor.parseColor("#FF99C5").toIntColor(),
                            wickDownColor = AndroidColor.parseColor("#42A6FF").toIntColor()
                        )
                    ) { api ->
                        api.setData(chartData)
                    }
                }

                // 이동평균선 추가
                if (config.indicators.sma5 && sma5Data.isNotEmpty()) {
                    val sma5ChartData = sma5Data.map { data ->
                        com.tradingview.lightweightcharts.api.series.models.LineData(
                            time = ChartUtils.timestampToBusinessDay(data.time),
                            value = data.value
                        )
                    }

                    chartsView.api.addLineSeries(
                        options = LineSeriesOptions(
                            color = AndroidColor.parseColor("#F5A623").toIntColor(),
                            lineWidth = LineWidth.TWO
                        )
                    ) { api ->
                        api.setData(sma5ChartData)
                    }
                }

                if (config.indicators.sma20 && sma20Data.isNotEmpty()) {
                    val sma20ChartData = sma20Data.map { data ->
                        com.tradingview.lightweightcharts.api.series.models.LineData(
                            time = ChartUtils.timestampToBusinessDay(data.time),
                            value = data.value
                        )
                    }

                    chartsView.api.addLineSeries(
                        options = LineSeriesOptions(
                            color = AndroidColor.parseColor("#50E3C2").toIntColor(),
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
                                AndroidColor.parseColor("#FF99C5").toIntColor()
                            else
                                AndroidColor.parseColor("#42A6FF").toIntColor()
                        )
                    }

                    chartsView.api.addHistogramSeries(
                        options = HistogramSeriesOptions(
                            priceScaleId = PriceScaleId("volume"),
                            color = AndroidColor.parseColor("#666666").toIntColor(),
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

// 데이터 클래스들은 ChartUiState.kt로 이동됨