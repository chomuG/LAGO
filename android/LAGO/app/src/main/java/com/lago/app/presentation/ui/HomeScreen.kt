package com.lago.app.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.runtime.remember
import com.lago.app.presentation.theme.LagoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onPersonalityTestClick: () -> Unit = {}
) {
    // 사용자 경험 개선을 위한 메모이제이션
    val homeItems = remember {
        (1..5).map { index ->
            HomeItem(
                id = index,
                title = "학습 콘텐츠 $index",
                description = when (index) {
                    1 -> "차트 분석 기초를 학습하세요"
                    2 -> "투자 전략과 리스크 관리"
                    3 -> "연습 문제와 모의 투자"
                    4 -> "시장 동향 및 뉴스 분석"
                    else -> "커뮤니티와 전문가 의견"
                }
            )
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .semantics {
                contentDescription = "LAGO 홈 화면"
            }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .semantics {
                    contentDescription = "LAGO 서비스 소개 카드"
                }
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "안녕하세요!",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.semantics {
                        contentDescription = "LAGO 서비스 환영 메시지"
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "LAGO에 오신 것을 환영합니다. 투자 학습과 실전 경험을 시작해보세요.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.semantics {
                        contentDescription = "LAGO 서비스 소개 문구"
                    }
                )
            }
        }
        
        // 성향 테스트 시작 버튼
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable { onPersonalityTestClick() },
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF4285F4).copy(alpha = 0.1f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                Color(0xFF4285F4)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🧐 투자 성향 테스트",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF4285F4)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "나만의 투자 성향을 알아보고 맞춤 전략을 찾아보세요",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF666666)
                    )
                }
                Text(
                    text = "시작하기 →",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF4285F4)
                )
            }
        }
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .semantics {
                    contentDescription = "학습 콘텐츠 목록"
                }
        ) {
            items(
                items = homeItems,
                key = { it.id } // 성능 개선을 위한 key 사용
            ) { item ->
                HomeItemCard(
                    item = item,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// 데이터 클래스 (성능 최적화를 위해 외부로 이동 가능)
data class HomeItem(
    val id: Int,
    val title: String,
    val description: String
)

@Composable
private fun HomeItemCard(
    item: HomeItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.semantics {
            contentDescription = "${item.title}: ${item.description}"
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    LagoTheme {
        HomeScreen()
    }
}