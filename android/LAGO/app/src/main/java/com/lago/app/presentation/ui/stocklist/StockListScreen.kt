package com.lago.app.presentation.ui.stocklist

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
import com.lago.app.domain.entity.StockItem

@Composable
fun StockListScreen(
    viewModel: StockListViewModel = hiltViewModel(),
    onStockClick: (String) -> Unit
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
                        selectedFilters = uiState.selectedFilters,
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
                    // 역사 챌린지 화면: 예시로 종목 관련 뉴스 등
                    // 필요하다면 viewModel에서 관련 뉴스 로딩 로직 추가
                    Text(
                        text = "관련 뉴스",
                        style = TitleB20,
                        modifier = Modifier.padding(Spacing.md)
                    )
                    // 뉴스 카드 리스트 (가짜 데이터 예시)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(Spacing.md),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm + Spacing.xs)
                    ) {
                        item {
                            // 첫 번째 큰 뉴스 카드
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(Spacing.md)) {
                                    Text("호재", style = SubtitleSb14, color = MainPink)
                                    Spacer(Modifier.height(Spacing.sm))
                                    Text(
                                        "서정진 “셀트리온, 관세 리스크 완전 해소…연 4.6조 매출 예상”",
                                        style = TitleB16
                                    )
                                    Spacer(Modifier.height(Spacing.xs))
                                    Text("27분전", style = BodyR12, color = Gray600)
                                }
                            }
                        }
                        // 나머지 뉴스들
                        items(3) { idx ->
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(Spacing.sm + Spacing.xs)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("중립", style = SubtitleSb14, color = Gray900)
                                        Spacer(Modifier.height(Spacing.xs))
                                        Text(
                                            "코스피 하락세 지속, 외국인 매도물량 증가로 인한 시장 불안감 확산",
                                            style = BodyR14
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text("27분전", style = BodyR12, color = Gray600)
                                    }
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
    Column {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.onSurface,
                    height = 2.dp
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { onTabChange(0) },
                text = {
                    Text(
                        "모의 투자",
                        style = TitleB16,
                        color = if (selectedTab == 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { onTabChange(1) },
                text = {
                    Text(
                        "역사 챌린지",
                        style = TitleB16,
                        color = if (selectedTab == 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
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
            .height(48.dp),
        placeholder = {
            Text(
                "검색하기...",
                style = BodyR14,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "검색",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedBorderColor = MainPink
        ),
        singleLine = true
    )
}

@Composable
private fun FilterChips(
    selectedFilters: List<String>,
    onFilterChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = listOf("이름", "현재가", "등락률", "관심목록")

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        filters.forEach { filter ->
            FilterChip(
                selected = selectedFilters.contains(filter),
                onClick = { onFilterChange(filter) },
                label = {
                    Text(
                        filter,
                        style = SubtitleSb14
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MainPink,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
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

