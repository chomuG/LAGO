package com.lago.app.presentation.ui

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
import com.lago.app.R
import com.lago.app.presentation.theme.*

data class NewsItem(
    val id: String,
    val category: String,
    val title: String,
    val timeAgo: String,
    val imageRes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    onNewsClick: (String) -> Unit = {}
) {
    val newsList = listOf(
        NewsItem(
            id = "1",
            category = "악재",
            title = "코스피 하락세 지속, 외국인 매도 물량 증가로 인한 시장 불안",
            timeAgo = "2주전",
            imageRes = R.drawable.chart_study_image
        ),
        NewsItem(
            id = "2",
            category = "호재",
            title = "삼성전자 신제품 발표, 반도체 부문 실적 개선 전망으로 주가 상승 기대",
            timeAgo = "3시간 전",
            imageRes = R.drawable.megaphone_image
        ),
        NewsItem(
            id = "3",
            category = "중립",
            title = "주식시장에 부는 훈풍에 2분기 증권 업계 실적 기대감 고조",
            timeAgo = "3시간 전",
            imageRes = R.drawable.double_top_chart
        ),
        NewsItem(
            id = "4",
            category = "호재",
            title = "국제뱅, '하이브' 비정기 세무조사... 추가시장 '교량비' 일별 협업?",
            timeAgo = "3시간 전",
            imageRes = R.drawable.wordbook_image
        )
    )
    
    var selectedTab by remember { mutableStateOf("실시간 뉴스") }
    
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(newsList) { news ->
                NewsCard(
                    newsItem = news,
                    onClick = { onNewsClick(news.id) }
                )
            }
        }
    }
}

@Composable
fun NewsCard(
    newsItem: NewsItem,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = ShadowColor,
                ambientColor = ShadowColor
            )
            .height(128.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                // Category Badge
                Box(
                    modifier = Modifier
                        .background(
                            color = when (newsItem.category) {
                                "호재" -> Color(0xFFFFE9F2)
                                "악재" -> BlueLightHover
                                else -> Gray100
                            },
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = newsItem.category,
                        style = TitleB14,
                        color = when (newsItem.category) {
                            "호재" -> Color(0xFFFF6DAC)
                            "악재" -> BlueNormalHover
                            else -> Gray600
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Title
                Text(
                    text = newsItem.title,
                    style = SubtitleSb14,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Time
                Text(
                    text = newsItem.timeAgo,
                    style = BodyR12,
                    color = Gray700
                )
            }
            
            // News Image
            Image(
                painter = painterResource(id = newsItem.imageRes),
                contentDescription = "뉴스 이미지",
                modifier = Modifier
                    .fillMaxHeight()
                    .width(80.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewsScreenPreview() {
    LagoTheme {
        NewsScreen()
    }
}