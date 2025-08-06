package com.lago.app.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lago.app.R
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// 색상 정의
object AppColors {
    val AppBackground = Color(0xFFF8F9FA)
    val Black = Color(0xFF000000)
    val MainBlue = Color(0xFF4A90E2)
    val MainPink = Color(0xFFFF6B9D)
    val Gray300 = Color(0xFFD1D5DB)
    val Gray700 = Color(0xFF374151)
    val Gray800 = Color(0xFF1F2937)
    val Yellow = Color(0xFFFFE28A)
    val Green = Color(0xFFC8FACC)
    val Purple = Color(0xFFC5B5F9)
    val Gray = Color(0xFFC4C4C4)
    val RiskNeutralBg = Color(0xFFECF6FF)
    val RiskNeutralText = Color(0xFF3585CC)
}

// 텍스트 스타일 정의
object AppTextStyles {
    val TitleB24 = androidx.compose.ui.text.TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
    )
    val HeadEb24 = androidx.compose.ui.text.TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.ExtraBold
    )
    val SubtitleSb16 = androidx.compose.ui.text.TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
    )
    val SubtitleSb14 = androidx.compose.ui.text.TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold
    )
    val BodyR18 = androidx.compose.ui.text.TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal
    )
    val BodyR12 = androidx.compose.ui.text.TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal
    )
}

// 데이터 클래스
data class StockInfo(
    val name: String,
    val averagePrice: String,
    val percentage: String,
    val color: Color
)

data class PieChartData(
    val label: String,
    val value: Float,
    val color: Color
)

@Composable
fun PortfolioScreen() {
    val stockList = listOf(
        StockInfo("삼성전자", "1주 평균 42,232원", "40.7%", AppColors.MainBlue),
        StockInfo("한화생명", "1주 평균 52,232원", "25.4%", AppColors.MainPink),
        StockInfo("LG전자", "1주 평균 2,232원", "12.1%", AppColors.Yellow),
        StockInfo("셀트리온", "1주 평균 4,232원", "8.2%", AppColors.Green),
        StockInfo("네이버", "1주 평균 10,232원", "5.6%", AppColors.Purple),
        StockInfo("기타", "1주 평균 1,232원", "40.7%", AppColors.Gray)
    )

    val pieChartData = stockList.map { stock ->
        PieChartData(stock.name, stock.percentage.removeSuffix("%").toFloat(), stock.color)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.AppBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(16.dp)) }

        // 헤더 섹션
        item { HeaderSection() }

        // 자산 현황 섹션
        item { AssetStatusSection() }

        // 차트 섹션
        item { ChartSection(pieChartData) }

        // 주식 리스트
        items(stockList) { stock ->
            StockListItem(stock)
        }

        // 로그아웃 버튼
        item { LogoutButton() }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            // 위험중립형 태그
            Box(
                modifier = Modifier
                    .background(
                        AppColors.RiskNeutralBg,
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "위험중립형",
                    style = AppTextStyles.BodyR12,
                    color = AppColors.RiskNeutralText
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 사용자 이름
            Text(
                text = "박두철",
                style = AppTextStyles.TitleB24,
                color = AppColors.Black
            )
        }

        // 프로필 아이콘
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.setting),
                contentDescription = "설정",
                modifier = Modifier.size(24.dp),
                tint = AppColors.Gray700
            )
        }
    }
}

@Composable
fun AssetStatusSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 자산 현황 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "자산 현황",
                    style = AppTextStyles.HeadEb24,
                    color = AppColors.Black
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.rank),
                        contentDescription = "랭킹",
                        modifier = Modifier.size(18.dp),
                        tint = AppColors.Gray700
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "랭킹 보러가기 >",
                        style = AppTextStyles.BodyR12,
                        color = AppColors.Gray700
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 자산 정보
            AssetInfoRow("총자산", "808,000,000")
            AssetInfoRow("보유현금", "25,000,000")
            AssetInfoRow("총매수", "1,000,000")
            AssetInfoRow("총평가", "1,000,000")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "평가손익",
                    style = AppTextStyles.BodyR12,
                    color = AppColors.Gray800
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "+24.35%",
                        style = AppTextStyles.BodyR12,
                        color = AppColors.MainPink
                    )
                    Text(
                        text = "1,000,000",
                        style = AppTextStyles.BodyR12,
                        color = AppColors.Black
                    )
                }
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
            style = AppTextStyles.BodyR12,
            color = AppColors.Gray800
        )
        Text(
            text = value,
            style = AppTextStyles.BodyR12,
            color = AppColors.Black
        )
    }
}

@Composable
fun ChartSection(data: List<PieChartData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                // 도넛 차트
                DonutChart(
                    data = data,
                    modifier = Modifier.size(200.dp)
                )

                // 중앙 텍스트
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "수익률",
                        style = AppTextStyles.SubtitleSb16,
                        color = AppColors.Black
                    )
                    Text(
                        text = "+23.4%",
                        style = AppTextStyles.TitleB24,
                        color = AppColors.MainPink
                    )
                }
            }
        }
    }
}

@Composable
fun DonutChart(
    data: List<PieChartData>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val total = data.sumOf { it.value.toDouble() }.toFloat()
        val center = center
        val radius = size.minDimension / 2.5f
        val innerRadius = radius * 0.6f

        var startAngle = -90f

        data.forEach { item ->
            val sweepAngle = (item.value / total) * 360f

            drawArc(
                color = item.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = radius - innerRadius
                )
            )

            startAngle += sweepAngle
        }
    }
}

@Composable
fun StockListItem(stock: StockInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 색상 인디케이터
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(stock.color)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = stock.name,
                        style = AppTextStyles.SubtitleSb14,
                        color = AppColors.Black
                    )
                    Text(
                        text = stock.averagePrice,
                        style = AppTextStyles.BodyR12,
                        color = AppColors.Gray800
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stock.percentage,
                    style = AppTextStyles.BodyR18,
                    color = AppColors.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = R.drawable.bracket),
                    contentDescription = "더보기",
                    modifier = Modifier.size(16.dp),
                    tint = AppColors.Gray300
                )
            }
        }
    }
}

@Composable
fun LogoutButton() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        TextButton(
            onClick = { /* 로그아웃 로직 */ }
        ) {
            Text(
                text = "로그아웃",
                style = AppTextStyles.BodyR12,
                color = AppColors.Gray700
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyPageScreenPreview() {
    MaterialTheme {
        PortfolioScreen()
    }
}