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
import java.text.NumberFormat
import java.util.Locale
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
fun AssetStatusSection(
    isLoggedIn: Boolean = true,
    accountBalance: com.lago.app.domain.entity.AccountBalance? = null,
    totalProfitLoss: Long = 0L,
    totalProfitLossRate: Double = 0.0,
    isLoading: Boolean = false
) {
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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MainBlue)
            }
        } else {
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
                        text = if (isLoggedIn && accountBalance != null) {
                            NumberFormat.getNumberInstance(Locale.KOREA).format(accountBalance.totalAsset)
                        } else "?",
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
                AssetInfoRow(
                    "보유현금", 
                    if (isLoggedIn && accountBalance != null) {
                        NumberFormat.getNumberInstance(Locale.KOREA).format(accountBalance.balance)
                    } else "?"
                )
                AssetInfoRow(
                    "총매수", 
                    if (isLoggedIn && accountBalance != null) {
                        val totalInvestment = accountBalance.totalAsset - accountBalance.balance - accountBalance.profit
                        NumberFormat.getNumberInstance(Locale.KOREA).format(totalInvestment)
                    } else "?"
                )
                AssetInfoRow(
                    "총평가", 
                    if (isLoggedIn && accountBalance != null) {
                        NumberFormat.getNumberInstance(Locale.KOREA).format(accountBalance.totalStockValue)
                    } else "?"
                )

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
                            text = if (isLoggedIn) {
                                val sign = if (totalProfitLossRate > 0) "+" else ""
                                "${sign}${String.format("%.2f", totalProfitLossRate)}%"
                            } else "?",
                            style = TitleB14,
                            color = if (totalProfitLossRate > 0) MainPink else if (totalProfitLossRate < 0) Color.Blue else Gray600
                        )
                    }
                    Text(
                        text = if (isLoggedIn) {
                            val sign = if (totalProfitLoss > 0) "+" else ""
                            "${sign}${NumberFormat.getNumberInstance(Locale.KOREA).format(totalProfitLoss)}"
                        } else "?",
                        style = TitleB14,
                        color = if (totalProfitLoss > 0) MainPink else if (totalProfitLoss < 0) Color.Blue else Gray600
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
    onStockClick: (String, String) -> Unit = { _, _ ->},
    isLoggedIn: Boolean = true,
    portfolioSummary: com.lago.app.data.remote.dto.MyPagePortfolioSummary? = null
) {
    val hasStocks = stockList.isNotEmpty() && stockList.any { it.name != "기타" || (it.name == "기타" && it.percentage.removeSuffix("%").toFloatOrNull() != 0f) }
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
            // 차트 섹션 (보유종목이 있을 때만 표시)
            if (hasStocks) {
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
                                text = if (isLoggedIn) {
                                    portfolioSummary?.let { 
                                        android.util.Log.d("MyPageScreen", "📊 UI에서 수익률 표시: ${it.profitRate}%")
                                        val sign = if (it.profitRate > 0) "+" else ""
                                        "${sign}${String.format("%.1f", it.profitRate)}%"
                                    } ?: "+23.4%"
                                } else "?",
                                style = TitleB24,
                                color = if (portfolioSummary?.profitRate?.let { it > 0 } == true) MainPink else if (portfolioSummary?.profitRate?.let { it < 0 } == true) Color.Blue else MainPink
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // 주식 리스트 또는 빈 상태 메시지
            if (hasStocks) {
                stockList.forEach { stock ->
                    StockListItemInCard(
                        stock = stock,
                        onStockClick = onStockClick,
                        isLoggedIn = isLoggedIn
                    )
                    if (stock != stockList.last()) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            } else {
                // 보유종목이 없을 때 표시할 메시지
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "보유한 종목이 없습니다",
                        style = SubtitleSb16,
                        color = Gray600
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "모의투자를 시작해보세요!",
                        style = BodyR14,
                        color = Gray500
                    )
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
    onStockClick: (String, String) -> Unit = { _, _ ->},
    isLoggedIn: Boolean = true
) {
    val isClickable = stock.name != "기타"
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isClickable) {
                    Modifier.clickable { onStockClick(stock.stockCode, stock.name) }
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
                    text = if (isLoggedIn) stock.averagePrice else "? 주 평균 ? 원",
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
                    text = if (isLoggedIn) stock.percentage else "?",
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
                    text = if (isLoggedIn) stock.percentage else "?",
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