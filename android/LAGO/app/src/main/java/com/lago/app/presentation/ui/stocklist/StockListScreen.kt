package com.lago.app.presentation.ui.stocklist

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.res.painterResource
import com.lago.app.R
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lago.app.presentation.theme.*
import com.lago.app.presentation.viewmodel.stocklist.StockListViewModel
import com.lago.app.presentation.viewmodel.stocklist.SortType
import com.lago.app.domain.entity.StockItem
import com.lago.app.domain.entity.News
import com.lago.app.domain.entity.HistoryChallengeStock
import com.lago.app.domain.entity.HistoryChallengeNews
import com.lago.app.presentation.ui.components.NewsCard
import com.lago.app.presentation.ui.components.SimpleNewsCard
import com.lago.app.presentation.ui.components.CircularStockLogo

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StockListScreen(
    viewModel: StockListViewModel = hiltViewModel(),
    onStockClick: (String, String) -> Unit,
    onHistoryChallengeStockClick: (String) -> Unit = {},
    onNewsClick: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    
    // 가시 영역 종목 추적
    val visibleStocks by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            layoutInfo.visibleItemsInfo.mapNotNull { itemInfo ->
                if (itemInfo.index < uiState.filteredStocks.size) {
                    uiState.filteredStocks[itemInfo.index].code
                } else null
            }
        }
    }
    
    // 가시 영역이 변경될 때마다 WebSocket 구독 업데이트
    LaunchedEffect(visibleStocks) {
        if (uiState.selectedTab == 0 && visibleStocks.isNotEmpty()) {
            viewModel.updateVisibleStocks(visibleStocks)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            StockListTopBar(
                selectedTab = uiState.selectedTab,
                onTabChange = { viewModel.onTabChange(it) }
            )
            when (uiState.selectedTab) {
                0 -> {
                    // 모의 투자 화면 (기존 리스트)
                    SearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = { viewModel.onSearchQueryChange(it) },
                        modifier = Modifier.padding(Spacing.md)
                    )
                    FilterChips(
                        currentSortType = uiState.currentSortType,
                        showFavoritesOnly = uiState.showFavoritesOnly,
                        onFilterChange = { viewModel.onFilterChange(it) },
                        modifier = Modifier.padding(horizontal = Spacing.md)
                    )
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.filteredStocks.size) { index ->
                                val stock = uiState.filteredStocks[index]
                                StockItemCard(
                                    stock = stock,
                                    onFavoriteClick = { viewModel.toggleFavorite(stock.code) },
                                    onClick = { 
                                        onStockClick(
                                            stock.code,
                                            stock.name
                                        )
                                    }
                                )
                                
                                // 마지막 아이템이 아닌 경우에만 디바이더 표시
                                if (index < uiState.filteredStocks.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = Spacing.md),
                                        thickness = 1.dp,
                                        color = Gray200
                                    )
                                }
                            }
                        }
                        
                        // 로딩 상태
                        if (uiState.isLoading && uiState.filteredStocks.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        
                        // 에러 상태
                        if (uiState.errorMessage != null && uiState.filteredStocks.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "데이터를 불러오는데 실패했습니다",
                                        style = SB_18,
                                        color = Gray700
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "다시 시도해주세요",
                                        style = R_14,
                                        color = Gray500
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { viewModel.refreshStocks() }
                                    ) {
                                        Text("다시 시도")
                                    }
                                }
                            }
                        }
                        
                        // 빈 상태
                        if (!uiState.isLoading && uiState.errorMessage == null && uiState.filteredStocks.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "종목이 없습니다",
                                        style = SB_18,
                                        color = Gray700
                                    )
                                    if (uiState.showFavoritesOnly) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "관심종목을 추가해보세요",
                                            style = R_14,
                                            color = Gray500
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // 역사 챌린지 화면 - 종목 리스트 + 관련 뉴스
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 역사 챌린지 종목들
                        items(uiState.historyChallengeStocks.size) { index ->
                            HistoryChallengeStockItem(
                                stock = uiState.historyChallengeStocks[index],
                                onClick = { onHistoryChallengeStockClick(uiState.historyChallengeStocks[index].stockCode) }
                            )
                            
                            // 마지막 종목이 아닌 경우에만 디바이더 표시
                            if (index < uiState.historyChallengeStocks.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = Spacing.md),
                                    thickness = 1.dp,
                                    color = Gray200
                                )
                            }
                        }
                        
                        // 종목과 뉴스 사이 구분선
                        if (uiState.historyChallengeStocks.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = Spacing.md),
                                    thickness = 1.dp,
                                    color = Gray300
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                        
                        // 관련 뉴스 섹션 제목
                        item {
                            Text(
                                text = "관련 뉴스",
                                style = SB_18,
                                color = Gray900,
                                modifier = Modifier.padding(horizontal = Spacing.md, vertical = 8.dp)
                            )
                        }
                        
                        // 역사적 챌린지 뉴스 데이터
                        if (uiState.isNewsLoading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(Spacing.md),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = MainBlue)
                                }
                            }
                        } else if (uiState.historyChallengeNews.isNotEmpty()) {
                            items(uiState.historyChallengeNews.size) { index ->
                                val challengeNews = uiState.historyChallengeNews[index]
                                Box(modifier = Modifier.padding(horizontal = Spacing.md)) {
                                    HistoryChallengeNewsCard(
                                        news = challengeNews,
                                        onClick = { onNewsClick(challengeNews.challengeNewsId) }
                                    )
                                }
                                
                                // 뉴스 카드 간격
                                if (index < uiState.historyChallengeNews.size - 1) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                            
                            // 마지막 뉴스 하단 여백
                            if (uiState.historyChallengeNews.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        } else {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(Spacing.md),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "관련 뉴스가 없습니다",
                                        style = BodyR14,
                                        color = Gray600
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StockListTopBar(
    selectedTab: Int,
    onTabChange: (Int) -> Unit
) {
    TabRow(
        modifier = Modifier.height(60.dp),
        selectedTabIndex = selectedTab,
        containerColor = AppBackground,
        indicator = { tabPositions ->
            Box(
                modifier = Modifier
                    .tabIndicatorOffset(tabPositions[selectedTab])
                    .height(2.dp)
                    .background(MainBlue)
            )
        }
    ) {
        Tab(
            selected = selectedTab == 0,
            onClick = { onTabChange(0) },
            text = {
                Text(
                    text = "모의 투자",
                    style = TitleB18,
                    color = if (selectedTab == 0) Color.Black else Gray600
                )
            }
        )
        Tab(
            selected = selectedTab == 1,
            onClick = { onTabChange(1) },
            text = {
                Text(
                    text = "역사 챌린지",
                    style = TitleB18,
                    color = if (selectedTab == 1) Color.Black else Gray600
                )
            }
        )
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),  // 검색창 높이 증가
        placeholder = {
            Text(
                "검색하기...",
                style = BodyR18.copy(lineHeight = 24.sp),  // 줄 높이 추가
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "검색",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedBorderColor = MainBlue
        ),
        singleLine = true
    )
}

@Composable
private fun FilterChips(
    currentSortType: SortType,
    showFavoritesOnly: Boolean,
    onFilterChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = listOf("이름", "현재가", "등락률")

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
        filters.forEach { filter ->
            when (filter) {
                else -> {
                    // 정렬 버튼 (화살표 포함)
                    val sortDirection = when (filter) {
                        "이름" -> when (currentSortType) {
                            SortType.NAME_ASC -> SortDirection.ASC
                            SortType.NAME_DESC -> SortDirection.DESC
                            else -> SortDirection.NONE
                        }
                        "현재가" -> when (currentSortType) {
                            SortType.PRICE_ASC -> SortDirection.ASC
                            SortType.PRICE_DESC -> SortDirection.DESC
                            else -> SortDirection.NONE
                        }
                        "등락률" -> when (currentSortType) {
                            SortType.CHANGE_ASC -> SortDirection.ASC
                            SortType.CHANGE_DESC -> SortDirection.DESC
                            else -> SortDirection.NONE
                        }
                        else -> SortDirection.NONE
                    }
                    
                    SortChip(
                        label = filter,
                        sortDirection = sortDirection,
                        onClick = { onFilterChange(filter) }
                    )
                }
            }
        }
        }
        
        // 오른쪽: 관심목록 필터 하트 아이콘
        IconButton(
            onClick = { onFilterChange("관심목록") }
        ) {
            Icon(
                painter = if (showFavoritesOnly) painterResource(R.drawable.pink_heart) else painterResource(R.drawable.blank_heart),
                contentDescription = "관심목록 필터",
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

enum class SortDirection {
    NONE, ASC, DESC
}

@Composable
private fun SortChip(
    label: String,
    sortDirection: SortDirection,
    onClick: () -> Unit
) {
    FilterChip(
        selected = false,  // 색상 변경 없음
        onClick = onClick,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = label,
                    style = SubtitleSb14,
                    color = Gray700  // 텍스트 색상은 항상 동일
                )
                
                // 화살표 컬럼 - 정렬 중일 때만 표시
                if (sortDirection != SortDirection.NONE) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.height(16.dp)
                    ) {
                        // 위 화살표 (오름차순)
                        Icon(
                            painter = painterResource(
                                if (sortDirection == SortDirection.ASC) 
                                    R.drawable.order_arrowup_black 
                                else 
                                    R.drawable.order_arrowup_gray
                            ),
                            contentDescription = "오름차순",
                            modifier = Modifier.size(8.dp),
                            tint = Color.Unspecified
                        )
                        // 아래 화살표 (내림차순)
                        Icon(
                            painter = painterResource(
                                if (sortDirection == SortDirection.DESC) 
                                    R.drawable.order_arrowdown_black 
                                else 
                                    R.drawable.order_arrowdown_gray
                            ),
                            contentDescription = "내림차순",
                            modifier = Modifier.size(8.dp),
                            tint = Color.Unspecified
                        )
                    }
                }
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun StockItemCard(
    stock: StockItem,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 종목 로고
        CircularStockLogo(
            stockCode = stock.code,
            stockName = stock.name,
            size = 48.dp
        )

        Spacer(modifier = Modifier.width(Spacing.sm))

        // 종목 정보
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stock.name,
                style = SB_18,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (stock.currentPrice == 0) {
                    // 🚀 가격 로딩 중 표시
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 1.5.dp,
                            color = Gray500
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "가격 로딩 중...",
                            style = R_12,
                            color = Gray500
                        )
                    }
                } else {
                    // 실제 가격 정보 표시
                    Text(
                        text = "${String.format("%,d", stock.currentPrice)}원",
                        style = R_14,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // 변동률과 변동금액 (현재 금액 바로 오른쪽)
                    val isPositive = stock.priceChangePercent >= 0
                    val changeColor = if (isPositive) MainPink else MainBlue
                    val changeSign = if (isPositive) "+" else ""
                    
                    Text(
                        text = "${changeSign}${String.format("%,d", stock.priceChange)}(${String.format("%.2f", kotlin.math.abs(stock.priceChangePercent))}%)",
                        style = R_12,
                        color = changeColor
                    )
                }
            }
        }

        // 즐겨찾기 버튼
        IconButton(onClick = onFavoriteClick) {
            Icon(
                painter = if (stock.isFavorite) painterResource(R.drawable.pink_heart) else painterResource(R.drawable.blank_heart),
                contentDescription = "관심종목",
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun HistoryChallengeStockItem(
    stock: HistoryChallengeStock,
    onClick: () -> Unit,
    onFavoriteClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 종목 로고
        CircularStockLogo(
            stockCode = stock.stockCode,
            stockName = stock.stockName,
            size = 48.dp
        )

        Spacer(modifier = Modifier.width(Spacing.sm))

        // 종목 정보
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stock.stockName,
                style = SB_18,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${String.format("%,d", stock.currentPrice.toInt())}원",
                    style = R_14,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // 변동률과 변동금액 (WebSocket 실제 데이터 사용)
                val isPositive = stock.changePrice >= 0
                val changeColor = if (isPositive) MainPink else MainBlue
                val changeSign = if (isPositive) "+" else ""
                
                Text(
                    text = "${changeSign}${String.format("%,d", stock.changePrice.toInt())}(${String.format("%.2f", kotlin.math.abs(stock.fluctuationRate))}%)",
                    style = R_12,
                    color = changeColor
                )
            }
        }

        // 역사챌린지에서는 하트 아이콘 없음
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun HistoryChallengeNewsCard(
    news: HistoryChallengeNews,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = ShadowColor,
                ambientColor = ShadowColor
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 뉴스 이미지
            if (news.imageUrl.isNotEmpty()) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Gray100)
                ) {
                    coil.compose.AsyncImage(
                        model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                            .data(news.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "뉴스 이미지",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        onError = {
                            // 이미지 로드 실패 시 기본 배경색만 표시
                        }
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
            }
            
            // 뉴스 텍스트 정보
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = news.title,
                    style = TitleB16,
                    color = Black,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = formatHistoryChallengeDateTime(news.publishedAt),
                    style = BodyR12,
                    color = Gray600
                )
            }
        }
    }
}

private fun formatHistoryChallengeDateTime(dateTimeStr: String): String {
    return try {
        val inputFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val outputFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy년 M월 d일")
        val dateTime = java.time.LocalDateTime.parse(dateTimeStr, inputFormatter)
        dateTime.format(outputFormatter)
    } catch (e: Exception) {
        dateTimeStr // 파싱 실패 시 원본 반환
    }
}