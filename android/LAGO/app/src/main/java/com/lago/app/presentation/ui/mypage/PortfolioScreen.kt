package com.lago.app.presentation.ui.mypage

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lago.app.R
import com.lago.app.presentation.theme.*
import com.lago.app.presentation.ui.components.CommonTopAppBar
import com.lago.app.presentation.viewmodel.portfolio.PortfolioViewModel
import com.lago.app.util.PortfolioCalculator
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PortfolioScreen(
    onRankingClick: () -> Unit = {},
    onStockClick: (String, String) -> Unit = { _, _ ->},
    onBackClick: () -> Unit = {},
    userName: String = "박두칠",
    userId: Int = 5,
    viewModel: PortfolioViewModel = hiltViewModel()
) {
    android.util.Log.d("PORTFOLIO_SCREEN", "포트폴리오 화면 - userId: $userId, userName: $userName")
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(userId) {
        // userId가 변경되면 해당 유저의 포트폴리오 로드
        viewModel.loadPortfolio(userId)
    }
    
    // 실시간 보유종목 데이터를 StockInfo 리스트로 변환
    val stockList = remember(uiState.stockHoldings) {
        uiState.stockHoldings.mapIndexed { index, holding ->
            val colors = listOf(MainBlue, MainPink, AppColors.Yellow, AppColors.Green, AppColors.Purple, AppColors.Gray)
            val percentage = viewModel.calculatePortfolioWeight(holding)
            StockInfo(
                name = holding.stockName,
                averagePrice = "${holding.quantity}주 평균 ${NumberFormat.getNumberInstance(Locale.KOREA).format(holding.avgBuyPrice)}원",
                percentage = "${String.format("%.1f", percentage)}%",
                color = colors.getOrElse(index) { AppColors.Gray },
                stockCode = holding.stockCode
            )
        }
    }

    val pieChartData = remember(stockList) {
        stockList.map { stock ->
            PieChartData(stock.name, stock.percentage.removeSuffix("%").toFloat(), stock.color)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // TopAppBar 추가
        CommonTopAppBar(
            title = "${userName}님의 프로필",
            onBackClick = onBackClick
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            // 프로필 섹션
            item { ProfileSection(userName) }

            // 자산 현황 타이틀 섹션
            item { AssetTitleSection() }

            // 자산 현황 섹션
            item { 
                AssetStatusSection(
                    isLoggedIn = true,
                    accountBalance = uiState.accountBalance,
                    totalProfitLoss = uiState.totalProfitLoss,
                    totalProfitLossRate = uiState.totalProfitLossRate,
                    isLoading = uiState.isLoading
                ) 
            }

            // 포트폴리오 차트 및 주식 리스트 통합 섹션
            item { PortfolioSection(pieChartData, stockList, onStockClick) }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun ProfileSection(userName: String = "박두칠") {
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
            text = userName,
            style = TitleB24,
            color = Black
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PortfolioScreenPreview() {
    MaterialTheme {
        PortfolioScreen()
    }
}