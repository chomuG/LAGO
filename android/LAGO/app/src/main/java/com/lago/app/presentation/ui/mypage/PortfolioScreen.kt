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
import com.lago.app.R
import com.lago.app.presentation.theme.*
import com.lago.app.presentation.ui.components.CommonTopAppBar

@Composable
fun PortfolioScreen(
    onRankingClick: () -> Unit = {},
    onStockClick: (String, String) -> Unit = { _, _ ->},
    onBackClick: () -> Unit = {},
    userName: String = "박두칠",
    userId: Int = 5
) {
    android.util.Log.d("PORTFOLIO_SCREEN", "포트폴리오 화면 - userId: $userId, userName: $userName")
    val stockList = listOf(
        StockInfo("삼성전자", "1주 평균 42,232원", "40.7%", MainBlue, "005930"),
        StockInfo("한화생명", "1주 평균 52,232원", "25.4%", MainPink, "088350"),
        StockInfo("LG전자", "1주 평균 2,232원", "12.1%", AppColors.Yellow, "066570"),
        StockInfo("셀트리온", "1주 평균 4,232원", "8.2%", AppColors.Green, "068270"),
        StockInfo("네이버", "1주 평균 10,232원", "5.6%", AppColors.Purple, "035420"),
        StockInfo("기타", "1주 평균 1,232원", "40.7%", AppColors.Gray, "000000")
    )

    val pieChartData = stockList.map { stock ->
        PieChartData(stock.name, stock.percentage.removeSuffix("%").toFloat(), stock.color)
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
            item { AssetStatusSection() }

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