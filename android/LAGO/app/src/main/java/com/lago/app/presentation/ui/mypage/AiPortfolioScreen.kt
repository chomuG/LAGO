package com.lago.app.presentation.ui.mypage

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lago.app.presentation.viewmodel.mypage.MyPageViewModel
import com.lago.app.presentation.viewmodel.mypage.BotPortfolioViewModel

// theme에 없는 색상들만 정의
object AiAppColors {
    val Yellow = Color(0xFFFFE28A)
    val Green = Color(0xFFC8FACC)
    val Purple = Color(0xFFC5B5F9)
}

// 데이터 클래스
data class AiStockInfo(
    val stockCode: String,
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
    onOrderHistoryClick: (Int, Int) -> Unit = { _, _ -> },
    userId: Int = 1,
    botViewModel: BotPortfolioViewModel = hiltViewModel()
) {
    val uiState by botViewModel.uiState.collectAsState()
    
    // 매매봇 이름 매핑
    val botName = when(userId) {
        1 -> "화끈이"
        2 -> "적극이"
        3 -> "균형이"
        4 -> "조심이"
        else -> "AI 매매봇"
    }
    
    // 해당 userId로 데이터 로드
    LaunchedEffect(userId) {
        android.util.Log.d("AiPortfolioScreen", "🤖 매매봇 화면 로드: userId=$userId, botName=$botName")
        runCatching {
            botViewModel.loadBotPortfolio(userId)
        }.onFailure { e ->
            android.util.Log.e("AiPortfolioScreen", "포트폴리오 로드 실패", e)
        }
    }
    // API 데이터를 기반으로 주식 리스트 생성
    val aiStockList = if (uiState.portfolioSummary != null) {
        val colors = listOf(MainBlue, MainPink, AiAppColors.Yellow, AiAppColors.Green, AiAppColors.Purple, Gray400)
        uiState.portfolioSummary!!.holdings.mapIndexed { index, holding ->
            val avgPrice = if (holding.quantity > 0) {
                holding.purchaseAmount / holding.quantity
            } else 0L
            AiStockInfo(
                stockCode = holding.stockCode,
                name = holding.stockName,
                averagePrice = "1주 평균 ${String.format("%,d", avgPrice)}원",
                percentage = "${String.format("%.1f", holding.weight)}%",
                color = colors[index % colors.size]
            )
        }
    } else {
        // 로딩 중일 때 기본 데이터
        listOf(
            AiStockInfo("", "데이터 로딩중...", "0원", "0%", Gray400)
        )
    }

    val aiPieChartData = aiStockList.map { stock ->
        AiPieChartData(stock.name, stock.percentage.removeSuffix("%").toFloatOrNull() ?: 0f, stock.color)
    }

    val safeOnOrderHistoryClick: (Int, Int) -> Unit = { uid, type ->
        runCatching { onOrderHistoryClick(uid, type) }
            .onFailure { e -> android.util.Log.e("AiPortfolioScreen", "거래내역 클릭 오류", e) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // CommonTopAppBar 추가
        CommonTopAppBar(
            title = botName,
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
            item { AiProfileSection(botName) }

            // 자산 현황 타이틀 섹션
            item {
                AiAssetTitleSection(
                    onOrderHistoryClick = onOrderHistoryClick,
                    userId = userId
                )
            }

            // 자산 현황 섹션
            item { AiAssetStatusSection(uiState.portfolioSummary, botViewModel) }

            // 포트폴리오 차트 및 주식 리스트 통합 섹션
            item { AiPortfolioSection(aiPieChartData, aiStockList, uiState.portfolioSummary, onStockClick) }


            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun AiProfileSection(botName: String) {
    // 봇 이름에 따른 캐릭터 이미지 매핑 (_circle 버전)
    val characterImage = when(botName) {
        "화끈이" -> R.drawable.character_red_circle
        "적극이" -> R.drawable.character_yellow_circle
        "균형이" -> R.drawable.character_blue_circle
        "조심이" -> R.drawable.character_green_circle
        else -> R.drawable.character_blue_circle
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 캐릭터 이미지 (배경 없이 _circle 버전 사용)
        androidx.compose.foundation.Image(
            painter = painterResource(id = characterImage),
            contentDescription = "$botName 캐릭터",
            modifier = Modifier.size(74.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 매매봇 이름
        Text(
            text = botName,
            style = TitleB24,
            color = Black
        )
    }
}

@Composable
fun AiAssetTitleSection(onOrderHistoryClick: (Int, Int) -> Unit = { _, _ -> },
                        userId: Int) {
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
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable {
                android.util.Log.d("AI_PORTFOLIO", "AiAssetTitleSection - 거래내역 클릭: userId=$userId, type=2")
                onOrderHistoryClick(userId, 2)
            }
            modifier = Modifier.clickable { onOrderHistoryClick(userId) } // userId는 봇ID(1~4), 내부에서 type=2 처리
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
fun AiAssetStatusSection(
    portfolioSummary: com.lago.app.data.remote.dto.MyPagePortfolioSummary? = null,
    botViewModel: BotPortfolioViewModel? = null
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
                    text = if (portfolioSummary != null && botViewModel != null) {
                        val totalAssets = portfolioSummary.balance + portfolioSummary.totalCurrentValue
                        botViewModel.formatAmount(totalAssets)
                    } else "0원",
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
            AiAssetInfoRow("보유현금", 
                if (portfolioSummary != null && botViewModel != null) {
                    botViewModel.formatAmount(portfolioSummary.balance)
                } else "0원"
            )
            AiAssetInfoRow("총매수", 
                if (portfolioSummary != null && botViewModel != null) {
                    botViewModel.formatAmount(portfolioSummary.totalPurchaseAmount)
                } else "0원"
            )
            AiAssetInfoRow("총평가", 
                if (portfolioSummary != null && botViewModel != null) {
                    botViewModel.formatAmount(portfolioSummary.totalCurrentValue)
                } else "0원"
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
                        text = if (portfolioSummary != null) {
                            val profitLoss = portfolioSummary.totalCurrentValue - portfolioSummary.totalPurchaseAmount
                            val profitRate = if (portfolioSummary.totalPurchaseAmount > 0) {
                                (profitLoss.toDouble() / portfolioSummary.totalPurchaseAmount) * 100
                            } else 0.0
                            val sign = if (profitLoss > 0) "+" else ""
                            "${sign}${String.format("%.2f", profitRate)}%"
                        } else "0%",
                        style = TitleB14,
                        color = if (portfolioSummary != null) {
                            val profitLoss = portfolioSummary.totalCurrentValue - portfolioSummary.totalPurchaseAmount
                            if (profitLoss > 0) MainPink else if (profitLoss < 0) MainBlue else Gray600
                        } else Gray600
                    )
                }
                Text(
                    text = if (portfolioSummary != null && botViewModel != null) {
                        val profitLoss = portfolioSummary.totalCurrentValue - portfolioSummary.totalPurchaseAmount
                        botViewModel.formatAmount(profitLoss)
                    } else "0원",
                    style = TitleB14,
                    color = if (portfolioSummary != null) {
                        val profitLoss = portfolioSummary.totalCurrentValue - portfolioSummary.totalPurchaseAmount
                        if (profitLoss > 0) MainPink else if (profitLoss < 0) MainBlue else Gray600
                    } else Gray600
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
fun AiPortfolioSection(
    pieChartData: List<AiPieChartData>, 
    stockList: List<AiStockInfo>,
    portfolioSummary: com.lago.app.data.remote.dto.MyPagePortfolioSummary? = null,
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
                            text = if (portfolioSummary != null) {
                                val profitLoss = portfolioSummary.totalCurrentValue - portfolioSummary.totalPurchaseAmount
                                val profitRate = if (portfolioSummary.totalPurchaseAmount > 0) {
                                    (profitLoss.toDouble() / portfolioSummary.totalPurchaseAmount) * 100
                                } else 0.0
                                val sign = if (profitLoss > 0) "+" else ""
                                "${sign}${String.format("%.1f", profitRate)}%"
                            } else "0%",
                            style = TitleB24,
                            color = if (portfolioSummary != null) {
                                val profitLoss = portfolioSummary.totalCurrentValue - portfolioSummary.totalPurchaseAmount
                                if (profitLoss > 0) MainPink else if (profitLoss < 0) MainBlue else Gray600
                            } else Gray600
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 주식 리스트
            stockList.forEach { stock ->
                AiStockListItemInCard(stock, onStockClick)
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
fun AiStockListItemInCard(stock: AiStockInfo, onStockClick: (String) -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable { 
                if (stock.stockCode.isNotEmpty()) {
                    onStockClick(stock.stockCode) 
                }
            },
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