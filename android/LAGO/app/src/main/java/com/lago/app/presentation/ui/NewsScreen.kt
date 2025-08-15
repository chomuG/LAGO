package com.lago.app.presentation.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lago.app.R
import com.lago.app.domain.entity.News
import com.lago.app.presentation.theme.*
import com.lago.app.presentation.ui.components.NewsCard
import com.lago.app.presentation.viewmodel.NewsUiState
import com.lago.app.presentation.viewmodel.NewsViewModel
import com.lago.app.util.formatTimeAgo

data class NewsItem(
    val id: String,
    val category: String,
    val title: String,
    val timeAgo: String,
    val imageRes: Int
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    onNewsClick: (Int) -> Unit = {},
    viewModel: NewsViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf("실시간 뉴스") }
    
    val newsState by viewModel.newsState.collectAsStateWithLifecycle()
    val interestNewsState by viewModel.interestNewsState.collectAsStateWithLifecycle()
    
    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            "실시간 뉴스" -> viewModel.loadNews()
            "관심 종목 뉴스" -> viewModel.loadInterestNews()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // Tab Row
        TabRow(
            modifier = Modifier.height(60.dp),
            selectedTabIndex = if (selectedTab == "실시간 뉴스") 0 else 1,
            containerColor = AppBackground,
            indicator = { tabPositions ->
                Box(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[if (selectedTab == "실시간 뉴스") 0 else 1])
                        .height(2.dp)
                        .background(MainBlue)
                )
            }
        ) {
            Tab(
                selected = selectedTab == "실시간 뉴스",
                onClick = { selectedTab = "실시간 뉴스" },
                text = {
                    Text(
                        text = "실시간 뉴스",
                        style = TitleB18,
                        color = if (selectedTab == "실시간 뉴스") Color.Black else Gray600
                    )
                }
            )
            Tab(
                selected = selectedTab == "관심 종목 뉴스",
                onClick = { selectedTab = "관심 종목 뉴스" },
                text = {
                    Text(
                        text = "관심 종목 뉴스",
                        style = TitleB18,
                        color = if (selectedTab == "관심 종목 뉴스") Color.Black else Gray600
                    )
                }
            )
        }
        
        // News List
        val currentState = if (selectedTab == "실시간 뉴스") newsState else interestNewsState
        
        when (currentState) {
            is NewsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MainBlue)
                }
            }
            is NewsUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(currentState.newsList) { news ->
                        NewsCard(
                            news = news,
                            onClick = { onNewsClick(news.newsId) }
                        )
                    }
                }
            }
            is NewsUiState.Error -> {
                // 에러 상황에서도 레이아웃 확인을 위한 더미 데이터
                val dummyNews = listOf(
                    News(
                        newsId = 1,
                        title = "삼성전자, 3분기 영업이익 전년 동기 대비 277% 증가",
                        content = "삼성전자가 3분기 실적을 발표하며...",
                        summary = "{3분기 실적, 영업이익 증가, 반도체 회복}",
                        publishedAt = "2024-10-31T10:30:00Z",
                        sentiment = "호재",
                        type = "da"
                    ),
                    News(
                        newsId = 2,
                        title = "SK하이닉스, HBM 시장 확대로 주가 상승 전망",
                        content = "SK하이닉스가 HBM 메모리 시장에서...",
                        summary = "{HBM 시장, 주가 상승, 메모리 반도체}",
                        publishedAt = "2024-10-31T09:15:00Z",
                        sentiment = "호재",
                        type = "da"
                    ),
                    News(
                        newsId = 3,
                        title = "현대차, 전기차 판매 부진으로 실적 우려",
                        content = "현대차의 전기차 판매량이 예상보다...",
                        summary = "{전기차 판매, 실적 우려, 자동차 산업}",
                        publishedAt = "2024-10-31T08:45:00Z",
                        sentiment = "악재",
                        type = "da"
                    )
                )
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "⚠️ 데이터 로드 실패",
                                    style = TitleB16,
                                    color = Color.Red
                                )
                                Text(
                                    text = "아래는 레이아웃 확인용 더미 데이터입니다",
                                    style = BodyR12,
                                    color = Gray600
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { 
                                        when (selectedTab) {
                                            "실시간 뉴스" -> viewModel.loadNews()
                                            "관심 종목 뉴스" -> viewModel.loadInterestNews()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MainBlue)
                                ) {
                                    Text("다시 시도", color = Color.White)
                                }
                            }
                        }
                    }
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


@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun NewsScreenPreview() {
    LagoTheme {
        NewsScreen()
    }
}