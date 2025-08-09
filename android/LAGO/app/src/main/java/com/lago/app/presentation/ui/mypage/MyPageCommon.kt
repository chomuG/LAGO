package com.lago.app.presentation.ui.mypage

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lago.app.R
import com.lago.app.presentation.theme.*

// theme에 없는 색상들만 정의
object AppColors {
    val Yellow = Color(0xFFFFE28A)
    val Green = Color(0xFFC8FACC)
    val Purple = Color(0xFFC5B5F9)
    val Gray = Color(0xFFC4C4C4)
    val RiskNeutralBg = Color(0xFFECF6FF)
    val RiskNeutralText = Color(0xFF3585CC)
}

// 데이터 클래스
data class StockInfo(
    val name: String,
    val averagePrice: String,
    val percentage: String,
    val color: Color,
    val stockCode: String = "005930"
)

data class PieChartData(
    val label: String,
    val value: Float,
    val color: Color
)

@Composable
fun AssetTitleSection() {
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
    }
}

@Composable
fun AssetStatusSection() {
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
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
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
            AssetInfoRow("보유현금", "25,000,000")
            AssetInfoRow("총매수", "1,000,000")
            AssetInfoRow("총평가", "1,000,000")

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
fun AssetInfoRow(label: String, value: String) {
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
fun PortfolioSection(
    pieChartData: List<PieChartData>, 
    stockList: List<StockInfo>,
    onStockClick: (String) -> Unit = {}
) {
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
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
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
                    DonutChart(
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
                StockListItemInCard(
                    stock = stock,
                    onStockClick = onStockClick
                )
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
fun DonutChart(
    data: List<PieChartData>,
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
                val radius = size.minDimension / 2.2f  // 더 큰 반지름
                val strokeWidth = radius * 0.8f  // 적당히 두꺼운 도넛
                
                var startAngle = -90f

                data.forEach { item ->
                    val sweepAngle = if (total > 0) (item.value / total) * 360f else 0f

                    drawArc(
                        color = item.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = strokeWidth,
                            cap = androidx.compose.ui.graphics.StrokeCap.Butt
                        )
                    )

                    startAngle += sweepAngle
                }
            }
        }
    }
}

@Composable
fun StockListItemInCard(
    stock: StockInfo,
    onStockClick: (String) -> Unit = {}
) {
    val isClickable = stock.name != "기타"
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isClickable) {
                    Modifier.clickable { onStockClick(stock.stockCode) }
                } else {
                    Modifier
                }
            )
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

        if (stock.name == "기타") {
            // 기타의 경우 숫자만 중앙에 위치
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stock.percentage,
                    style = BodyR18,
                    color = Black
                )
            }
        } else {
            // 일반 주식의 경우 기존 방식
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
}