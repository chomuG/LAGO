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
import android.graphics.Color as AndroidColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(
    viewModel: ChartViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = true
    )
    
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = bottomSheetState
    )

    // 바텀시트 확장 정도 계산 (0 = 접힘, 1 = 완전 확장)
    val sheetProgress by animateFloatAsState(
        targetValue = if (bottomSheetState.currentValue == SheetValue.Expanded) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "sheet_progress"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetContent = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f)
                        .background(Color.White)
                ) {
                    // 바텀시트 핸들
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 12.dp, bottom = 8.dp)
                                .align(Alignment.CenterHorizontally)
                                .width(37.dp)
                                .height(4.dp)
                                .background(
                                    color = Color(0xFFD9D9D9),
                                    shape = RoundedCornerShape(10.dp)
                                )
                        )
                    }

                    // 바텀시트 내용
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        BottomSheetContent(viewModel)
                    }
                }
            },
            sheetPeekHeight = 160.dp,
            sheetContainerColor = Color.White,
            sheetShadowElevation = 4.dp,
            sheetShape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            containerColor = Color.White,
            sheetSwipeEnabled = true
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(paddingValues)
                    .padding(bottom = 76.dp)  // 매매 버튼 높이
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
                        animatedProgress = sheetProgress,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // 주식 정보 헤더
                StockInfoHeader(
                    stockName = uiState.currentStock.name,
                    stockCode = uiState.currentStock.code,
                    currentPrice = uiState.currentStock.currentPrice,
                    priceChange = uiState.currentStock.priceChange,
                    priceChangePercent = uiState.currentStock.priceChangePercent,
                    modifier = Modifier.padding(
                        start = 32.dp,
                        end = 32.dp,
                        top = 16.dp,
                        bottom = 8.dp
                    )
                )

                // 차트 영역
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 31.dp)
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
                        horizontal = 34.dp,
                        vertical = 16.dp
                    )
                )
            }
        }
        
        // 매매 버튼 (화면 최하단 고정)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
        ) {
            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
            TradingButtons(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                viewModel = viewModel
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
    var isMinuteDropdownExpanded by remember { mutableStateOf(false) }

    val minuteTimeFrames = listOf(
        "10" to "10분",
        "01" to "1분",
        "03" to "3분",
        "05" to "5분",
        "15" to "15분",
        "30" to "30분",
        "60" to "60분"
    )

    val otherTimeFrames = listOf(
        "D" to "일",
        "W" to "주",
        "M" to "월",
        "Y" to "년"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // 분봉 드롭다운
        Box {
            val currentMinuteFrame = minuteTimeFrames.find { it.first == selectedTimeFrame }
            val displayText = currentMinuteFrame?.second ?: "10분"

            FilterChip(
                onClick = { isMinuteDropdownExpanded = !isMinuteDropdownExpanded },
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = displayText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (currentMinuteFrame != null) Color.White else Color.Black
                        )
                        Icon(
                            imageVector = if (isMinuteDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "드롭다운",
                            modifier = Modifier.size(16.dp),
                            tint = if (currentMinuteFrame != null) Color.White else Color.Black
                        )
                    }
                },
                selected = currentMinuteFrame != null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFF99C5),
                    selectedLabelColor = Color.White,
                    containerColor = Color.Transparent,
                    labelColor = Color.Black
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = currentMinuteFrame != null,
                    borderColor = Color.Transparent,
                    selectedBorderColor = Color.Transparent
                ),
                modifier = Modifier
                    .width(48.dp)
                    .height(24.dp)
            )

            DropdownMenu(
                expanded = isMinuteDropdownExpanded,
                onDismissRequest = { isMinuteDropdownExpanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                minuteTimeFrames.forEach { (code, name) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                name,
                                color = Color.Black,
                                fontSize = 12.sp
                            )
                        },
                        onClick = {
                            onTimeFrameChange(code)
                            isMinuteDropdownExpanded = false
                        }
                    )
                }
            }
        }

        // 다른 시간봉들
        otherTimeFrames.forEach { (code, name) ->
            FilterChip(
                onClick = { onTimeFrameChange(code) },
                label = {
                    Text(
                        name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selectedTimeFrame == code) Color.White else Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                selected = selectedTimeFrame == code,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFF99C5),
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
                    .width(48.dp)
                    .height(24.dp)
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
            .fillMaxHeight()
            .background(Color.White)
    ) {
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
                .height(44.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF99C5)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                "구매하기",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
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
                            color = AndroidColor.parseColor("#64B5F6").toIntColor()
                        )
                    ) { api ->
                        api.setData(volumeChartData)
                    }
                }
            }
            else -> {}
        }
    }
}

// 데이터 클래스들은 ChartUiState.kt로 이동됨