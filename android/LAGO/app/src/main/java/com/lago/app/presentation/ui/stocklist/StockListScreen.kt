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
import com.lago.app.presentation.viewmodel.stocklist.StockItem

@Composable
fun StockListScreen(
    viewModel: StockListViewModel = hiltViewModel(),
    onStockClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 상단 탭바
            StockListTopBar(
                selectedTab = uiState.selectedTab,
                onTabChange = { viewModel.onTabChange(it) }
            )
            // 검색바
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier.padding(16.dp)
            )

            // 필터 칩들
            FilterChips(
                selectedFilters = uiState.selectedFilters,
                onFilterChange = { viewModel.onFilterChange(it) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // 주식 리스트
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
            containerColor = Color.White,
            contentColor = Gray900,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Gray900,
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
                        color = if (selectedTab == 0) Gray900 else Gray600
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
                        color = if (selectedTab == 1) Gray900 else Gray600
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
                color = Gray600
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "검색",
                tint = Gray600
            )
        },
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Gray300,
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                    selectedLabelColor = Color.White
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 로고
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MainBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stock.name.take(2),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 종목 정보
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stock.name,
                    style = TitleB16,
                    color = Gray900
                )
                Text(
                    text = "${String.format("%,d", stock.currentPrice)}원",
                    style = SubtitleSb14,
                    color = Gray900
                )
            }

            // 변동률
            Column(horizontalAlignment = Alignment.End) {
                val isPositive = stock.changeRate >= 0
                val changeColor = if (isPositive) MainPink else MainBlue

                Text(
                    text = "${if (isPositive) "▲" else "▼"}${String.format("%,d", kotlin.math.abs(stock.change))}원",
                    style = SubtitleSb14,
                    color = changeColor
                )
                Text(
                    text = "(${if (isPositive) "+" else ""}${String.format("%.2f", stock.changeRate)}%)",
                    style = BodyR12,
                    color = changeColor
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 즐겨찾기 버튼
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (stock.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "관심종목",
                    tint = if (stock.isFavorite) MainPink else Gray600
                )
            }
        }
    }
}

