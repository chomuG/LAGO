package com.lago.app.presentation.ui.news

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lago.app.R
import com.lago.app.presentation.theme.*
import com.lago.app.presentation.ui.components.CommonTopAppBar
import com.lago.app.presentation.viewmodel.NewsDetailViewModel
import com.lago.app.presentation.viewmodel.NewsDetailUiState
import com.lago.app.domain.entity.News
import com.lago.app.util.formatTimeAgo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    newsId: String = "1",
    onBackClick: () -> Unit = {},
    viewModel: NewsDetailViewModel = hiltViewModel()
) {
    var isAiSummaryExpanded by remember { mutableStateOf(true) }
    
    val newsDetailState by viewModel.newsDetailState.collectAsStateWithLifecycle()
    
    LaunchedEffect(newsId) {
        val id = newsId.toIntOrNull() ?: 1
        viewModel.loadNewsDetail(id)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // Top App Bar
        CommonTopAppBar(
            title = "실시간 뉴스",
            onBackClick = onBackClick
        )
        
        when (newsDetailState) {
            is NewsDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MainBlue)
                }
            }
            is NewsDetailUiState.Success -> {
                NewsDetailContent(
                    news = (newsDetailState as NewsDetailUiState.Success).news,
                    isAiSummaryExpanded = isAiSummaryExpanded,
                    onToggleAiSummary = { isAiSummaryExpanded = !isAiSummaryExpanded }
                )
            }
            is NewsDetailUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "뉴스를 불러올 수 없습니다",
                            style = TitleB16,
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { 
                                val id = newsId.toIntOrNull() ?: 1
                                viewModel.loadNewsDetail(id)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MainBlue)
                        ) {
                            Text("다시 시도", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewsDetailContent(
    news: News,
    isAiSummaryExpanded: Boolean,
    onToggleAiSummary: () -> Unit
) {
    // Scrollable Content
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
            // News Title
            Text(
                text = news.title,
                style = TitleB20,
                color = Color.Black,
                lineHeight = 24.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Date
            Text(
                text = formatDateTime(news.publishedAt),
                style = BodyR14,
                color = Gray700,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // AI Summary Section
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // AI Summary Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleAiSummary() },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.robot_icon),
                                contentDescription = "AI",
                                tint = MainBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI 요약 보기",
                                style = SubtitleSb14,
                                color = MainBlue
                            )
                        }
                        Icon(
                            imageVector = if (isAiSummaryExpanded) 
                                Icons.Default.KeyboardArrowUp 
                            else 
                                Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isAiSummaryExpanded) "접기" else "펼치기",
                            tint = MainBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    // AI Summary Content
                    if (isAiSummaryExpanded) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // summary 필드를 파싱해서 표시
                            val summaryText = news.summary.removePrefix("{").removeSuffix("}")
                            val summaryItems = summaryText.split(",").map { it.trim() }
                            
                            summaryItems.forEach { item ->
                                if (item.isNotBlank()) {
                                    AiSummaryItem(text = item)
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // News Image
            Image(
                painter = painterResource(id = R.drawable.megaphone_image),
                contentDescription = "뉴스 이미지",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(8.dp)
                    ),
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Image Caption
            Text(
                text = "이재용 삼성전자 회장. (사진=연합뉴스)",
                style = BodyR12,
                color = Gray700,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // News Content
            Text(
                text = news.content,
                style = BodyR16,
                color = Color.Black,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

@Composable
fun AiSummaryItem(
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Image(
            painter = painterResource(id = R.drawable.news_sum_icon),
            contentDescription = "AI 요약 아이콘",
            modifier = Modifier
                .size(16.dp)
                .offset(y = 2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = BodyR14,
            color = Color.Black,
            lineHeight = 20.sp
        )
    }
}

fun formatDateTime(dateTimeStr: String): String {
    return try {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val outputFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 HH:mm")
        val dateTime = LocalDateTime.parse(dateTimeStr, inputFormatter)
        dateTime.format(outputFormatter)
    } catch (e: Exception) {
        dateTimeStr // 파싱 실패 시 원본 반환
    }
}


@Preview(showBackground = true)
@Composable
fun NewsDetailScreenPreview() {
    LagoTheme {
        NewsDetailScreen()
    }
}