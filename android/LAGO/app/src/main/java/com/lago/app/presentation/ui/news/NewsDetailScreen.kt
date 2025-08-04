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
import com.lago.app.R
import com.lago.app.presentation.theme.*
import com.lago.app.presentation.ui.components.CommonTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    newsId: String = "1",
    onBackClick: () -> Unit = {}
) {
    var isAiSummaryExpanded by remember { mutableStateOf(true) }
    
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
        
        // Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // News Title
            Text(
                text = "삼성전자 신제품 발표, 반도체 부문 실적 개선 전망으로 주가 상승 기대",
                style = TitleB20,
                color = Color.Black,
                lineHeight = 24.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Date
            Text(
                text = "2025년 7월 30일 10:21",
                style = BodyR14,
                color = Gray700,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // AI Summary Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(12.dp),
                        spotColor = ShadowColor,
                        ambientColor = ShadowColor
                    ),
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
                            .clickable { isAiSummaryExpanded = !isAiSummaryExpanded },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.hand_clap),
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
                            AiSummaryItem(
                                text = "삼성전자가 새로운 22나노 반도체 AI 칩 생산 계획을 제공했습니다."
                            )
                            AiSummaryItem(
                                text = "이는 역대 최대 단일 고객 수주로, 미국 텍사스 공장에서 생산될 예정입니다."
                            )
                            AiSummaryItem(
                                text = "업계 마스크는 '초기 계약일 뿐'이라며 향후 물량 확대를 시사했습니다."
                            )
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
                text = """이재용 삼성전자 회장이 미국 워싱턴 D.C.방문차 출국길에 올랐습니다. 현재 미국에서 진행되는 관세 협상을 지원하기 위한 출장으로 보입니다.
                
이 회장은 오늘 오후 3시 50분쯤 김포공항에 도착해 미국으로 출국했습니다. 미국 증권 특례에 대한 취재진 질문에 답변을 하지 않았습니다.""",
                style = BodyR16,
                color = Color.Black,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
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
        Icon(
            painter = painterResource(id = R.drawable.news_sum_icon),
            contentDescription = "AI 요약 아이콘",
            tint = MainBlue,
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

@Preview(showBackground = true)
@Composable
fun NewsDetailScreenPreview() {
    LagoTheme {
        NewsDetailScreen()
    }
}