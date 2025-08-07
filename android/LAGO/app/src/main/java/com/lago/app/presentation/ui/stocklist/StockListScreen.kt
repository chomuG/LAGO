package com.lago.app.presentation.ui.stocklist

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.res.painterResource
import com.lago.app.R
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.lago.app.presentation.ui.components.NewsCard

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StockListScreen(
    viewModel: StockListViewModel = hiltViewModel(),
    onStockClick: (String) -> Unit,
    onNewsClick: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

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
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(Spacing.md),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm + Spacing.xs)
                    ) {
                        items(uiState.filteredStocks) { stock ->
                            StockItemCard(
                                stock = stock,
                                onFavoriteClick = { viewModel.toggleFavorite(stock.code) },
                                onClick = { onStockClick(stock.code) }
                            )
                        }
                    }
                }
                1 -> {
                    // 역사 챌린지 화면 - 뉴스 탭과 동일한 스타일
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        // 더미 뉴스 데이터
                        val dummyNews = listOf(
                            News(
                                newsId = 1,
                                title = "삼성전자, 3분기 영업이익 전년 동기 대비 277% 증가",
                                publishedAt = "2024-10-31T10:30:00Z",
                                sentiment = "호재",
                            ),
                            News(
                                newsId = 2,
                                title = "SK하이닉스, HBM 시장 확대로 주가 상승 전망",
                                publishedAt = "2024-10-31T09:15:00Z",
                                sentiment = "호재",
                            ),
                            News(
                                newsId = 3,
                                title = "현대차, 전기차 판매 부진으로 실적 우려",
                                publishedAt = "2024-10-31T08:00:00Z",
                                sentiment = "악재",
                            ),
                            News(
                                newsId = 4,
                                title = "LG에너지솔루션, 북미 배터리 공장 가동 시작",
                                publishedAt = "2024-10-31T07:30:00Z",
                                sentiment = "중립",
                            )
                        )
                        
                        items(dummyNews) { news ->
                            NewsCard(
                                news = news,
                                onClick = { onNewsClick(news.newsId) }
                            )
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
    val filters = listOf("이름", "현재가", "등락률", "관심목록")

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        filters.forEach { filter ->
            when (filter) {
                "관심목록" -> {
                    // 관심목록은 화살표 없이 필터링만
                    FilterChip(
                        selected = showFavoritesOnly,
                        onClick = { onFilterChange(filter) },
                        border = BorderStroke(1.dp, Gray600),
                        label = {
                            Text(
                                filter,
                                style = SubtitleSb14
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MainPink,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                    )
                }
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 로고
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stock.name.take(2),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = TitleB16
                )
            }

            Spacer(modifier = Modifier.width(Spacing.sm + Spacing.xs))

            // 종목 정보
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stock.name,
                    style = TitleB16,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${String.format("%,d", stock.currentPrice)}원",
                    style = SubtitleSb14,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // 변동률
            Column(horizontalAlignment = Alignment.End) {
                val isPositive = stock.priceChangePercent >= 0
                val changeColor = if (isPositive) MainPink else MainBlue

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(if (isPositive) R.drawable.up_triangle else R.drawable.down_triangle),
                        contentDescription = null,
                        tint = changeColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = "${String.format("%,d", kotlin.math.abs(stock.priceChange))}원",
                        style = SubtitleSb14,
                        color = changeColor
                    )
                }
                Text(
                    text = "(${if (isPositive) "+" else ""}${String.format("%.2f", stock.priceChangePercent)}%)",
                    style = BodyR12,
                    color = changeColor
                )
            }

            Spacer(modifier = Modifier.width(Spacing.sm))

            // 즐겨찾기 버튼
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    painter = if (stock.isFavorite) painterResource(R.drawable.pink_heart) else painterResource(R.drawable.blank_heart),
                    contentDescription = "관심종목",
                    tint = if (stock.isFavorite) Color.Unspecified else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}