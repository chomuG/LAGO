package com.lago.app.presentation.ui.mypage

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lago.app.R
import com.lago.app.presentation.theme.*
import com.lago.app.presentation.ui.components.CommonTopAppBar

// theme에 없는 색상들만 정의
object AiAppColors {
    val Yellow = Color(0xFFFFE28A)
    val Green = Color(0xFFC8FACC)
    val Purple = Color(0xFFC5B5F9)
}

// 데이터 클래스
data class AiStockInfo(
    val name: String,
    val averagePrice: String,
    val percentage: String,
    val color: Color
)

data class AiPieChartData(
    val label: String,
    val value: Float,
    val color: Color
)

@Composable
fun AiPortfolioScreen(
    onBackClick: () -> Unit = {},
    onStockClick: (String) -> Unit = {},
    userName: String = "AI 포트폴리오"
) {
    val aiStockList = listOf(
        AiStockInfo("삼성전자", "1주 평균 42,232원", "40.7%", MainBlue),
        AiStockInfo("한화생명", "1주 평균 52,232원", "25.4%", MainPink),
        AiStockInfo("LG전자", "1주 평균 2,232원", "12.1%", AiAppColors.Yellow),
        AiStockInfo("셀트리온", "1주 평균 4,232원", "8.2%", AiAppColors.Green),
        AiStockInfo("네이버", "1주 평균 10,232원", "5.6%", AiAppColors.Purple),
        AiStockInfo("기타", "1주 평균 1,232원", "40.7%", Gray400)
    )

    val aiPieChartData = aiStockList.map { stock ->
        AiPieChartData(stock.name, stock.percentage.removeSuffix("%").toFloat(), stock.color)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // CommonTopAppBar 추가
        CommonTopAppBar(
            title = userName,
            onBackClick = onBackClick
        )
        
        // 스크롤 가능한 컨텐츠
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            // ProfileSection 추가
            item { AiProfileSection() }

            // 자산 현황 타이틀 섹션
            item { AiAssetTitleSection() }

            // 자산 현황 섹션
            item { AiAssetStatusSection() }

            // 포트폴리오 차트 및 주식 리스트 통합 섹션
            item { AiPortfolioSection(aiPieChartData, aiStockList) }


            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun AiProfileSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 프로필 사진
        Box(
            modifier = Modifier
                .size(74.dp)
                .clip(CircleShape)
                .background(Color(0xFFDEEFFE))
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 사용자 이름
        Text(
            text = "AI 포트폴리오",
            style = TitleB24,
            color = Black
        )
    }
}

@Composable
fun AiAssetTitleSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "자산 현황",
            style = HeadEb24,
            color = Black
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "거래내역 >",
                style = BodyR14,
                color = Gray700
            )
        }
    }
}

@Composable
fun AiAssetStatusSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 0.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = ShadowColor,
                spotColor = ShadowColor
            )
            .drawWithContent {
                drawContent()
                drawRoundRect(
                    color = Gray100,
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 총자산 (특별 스타일)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "총자산",
                    style = SubtitleSb18,
                    color = Black
                )
                Text(
                    text = "808,000,000",
                    style = TitleB18,
                    color = Black
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 구분선
            Box(
                modifier = Modifier
                    .width(360.dp)
                    .height(1.dp)
                    .background(Gray300)
                    .align(Alignment.CenterHorizontally)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 나머지 자산 정보
            AiAssetInfoRow("보유현금", "25,000,000")
            AiAssetInfoRow("총매수", "1,000,000")
            AiAssetInfoRow("총평가", "1,000,000")

            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "평가손익",
                        style = SubtitleSb14,
                        color = Gray600
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+24.35%",
                        style = TitleB14,
                        color = MainPink
                    )
                }
                Text(
                    text = "1,000,000",
                    style = TitleB14,
                    color = MainPink
                )
            }
        }
    }
}

@Composable
fun AiAssetInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = SubtitleSb14,
            color = Gray600
        )
        Text(
            text = value,
            style = TitleB14,
            color = Black
        )
    }
}

@Composable
fun AiPortfolioSection(pieChartData: List<AiPieChartData>, stockList: List<AiStockInfo>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 0.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = ShadowColor,
                spotColor = ShadowColor
            )
            .drawWithContent {
                drawContent()
                drawRoundRect(
                    color = Gray100,
                    cornerRadius = CornerRadius(24.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 차트 섹션
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // 도넛 차트
                    AiDonutChart(
                        data = pieChartData,
                        modifier = Modifier.size(200.dp)
                    )

                    // 중앙 텍스트
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "수익률",
                            style = SubtitleSb16,
                            color = Black
                        )
                        Text(
                            text = "+23.4%",
                            style = TitleB24,
                            color = MainPink
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 주식 리스트
            stockList.forEach { stock ->
                AiStockListItemInCard(stock)
                if (stock != stockList.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
            
            // 마지막 주식 아이템 아래 추가 패딩
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun AiDonutChart(
    data: List<AiPieChartData>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            if (data.isNotEmpty()) {
                val total = data.sumOf { it.value.toDouble() }.toFloat()
                val center = this.center
                val radius = size.minDimension / 2.2f
                val strokeWidth = radius * 0.8f
                
                var startAngle = -90f

                data.forEach { item ->
                    val sweepAngle = if (total > 0) (item.value / total) * 360f else 0f

                    drawArc(
                        color = item.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Butt
                        )
                    )

                    startAngle += sweepAngle
                }
            }
        }
    }
}

@Composable
fun AiStockListItemInCard(stock: AiStockInfo) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 색상 인디케이터
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(stock.color)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = stock.name,
                    style = SubtitleSb18,
                    color = Black
                )
                Text(
                    text = stock.averagePrice,
                    style = BodyR12,
                    color = Gray800
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stock.percentage,
                style = BodyR18,
                color = Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                painter = painterResource(id = R.drawable.bracket),
                contentDescription = "더보기",
                modifier = Modifier.size(16.dp),
                tint = Gray300
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AiPortfolioScreenPreview() {
    MaterialTheme {
        AiPortfolioScreen()
    }
}