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
import com.lago.app.R
import com.lago.app.presentation.theme.*
import com.lago.app.presentation.viewmodel.mypage.MyPageViewModel
import com.lago.app.data.remote.dto.PieChartItem

@Composable
fun MyPageScreen(
    userPreferences: com.lago.app.data.local.prefs.UserPreferences,
    onRankingClick: () -> Unit = {},
    onStockClick: (String) -> Unit = {},
    onLoginClick: () -> Unit = {},
    onLogoutComplete: () -> Unit = {},
    viewModel: MyPageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoggedIn = userPreferences.getAuthToken() != null
    val username = userPreferences.getUsername() ?: "게스트"
    
    // 실시간 데이터 또는 기본 데이터 사용
    val portfolioSummary = uiState.portfolioSummary
    // PieChartData로 변환 (기존 UI 컴포넌트와 호환)
    val pieChartData = if (uiState.pieChartData.isNotEmpty()) {
        uiState.pieChartData.map { item ->
            PieChartData(item.name, item.percentage.toFloat(), item.color)
        }
    } else {
        // 기본 데이터 (API 응답이 없을 때)
        listOf(
            PieChartData("삼성전자", 42232f, MainBlue),
            PieChartData("한화생명", 52232f, MainPink),
            PieChartData("LG전자", 2232f, AppColors.Yellow),
            PieChartData("셀트리온", 4232f, AppColors.Green),
            PieChartData("네이버", 10232f, AppColors.Purple),
            PieChartData("기타", 1232f, AppColors.Gray)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(16.dp)) }

        // 헤더 섹션
        item { HeaderSection(isLoggedIn = isLoggedIn, username = username, onLoginClick = onLoginClick) }

        // 자산 현황 타이틀 섹션
        item { AssetTitleSectionWithRanking(onRankingClick = onRankingClick) }

        // 자산 현황 섹션 (기존 UI 유지하되 데이터만 실시간으로)
        item { 
            if (portfolioSummary != null) {
                AssetStatusSectionWithRealTimeData(portfolioSummary = portfolioSummary, viewModel = viewModel)
            } else {
                AssetStatusSection(isLoggedIn = isLoggedIn)
            }
        }

        // 포트폴리오 차트 및 주식 리스트 통합 섹션 (기존 UI 유지하되 데이터만 실시간으로)
        item { 
            val stockList = if (portfolioSummary != null) {
                android.util.Log.d("MyPageScreen", "📋 StockInfo 생성:")
                // 실시간 데이터를 기존 StockInfo 형태로 변환
                portfolioSummary.holdings.take(5).mapIndexed { index, holding ->
                    val colors = listOf(MainBlue, MainPink, AppColors.Yellow, AppColors.Green, AppColors.Purple)
                    val avgPrice = if (holding.quantity > 0) holding.purchaseAmount / holding.quantity else 0L
                    android.util.Log.d("MyPageScreen", "  - ${holding.stockName}: 1주, 평균 ${avgPrice}원, 비율 ${String.format("%.1f", holding.weight)}%")
                    StockInfo(
                        name = holding.stockName,
                        averagePrice = "1주 평균 ${viewModel.formatAmount(avgPrice)}",
                        percentage = "${String.format("%.1f", holding.weight)}%",
                        color = colors.getOrElse(index) { AppColors.Gray },
                        stockCode = holding.stockCode
                    )
                } + if (portfolioSummary.holdings.size > 5) {
                    val others = portfolioSummary.holdings.drop(5)
                    val othersWeight = others.sumOf { it.weight }
                    val othersAvg = if (others.isNotEmpty()) others.sumOf { it.purchaseAmount } / others.sumOf { it.quantity } else 0L
                    listOf(StockInfo("기타", "평균 ${viewModel.formatAmount(othersAvg)}", "${String.format("%.1f", othersWeight)}%", AppColors.Gray, "000000"))
                } else emptyList()
            } else {
                // 기본 하드코딩된 데이터
                listOf(
                    StockInfo("삼성전자", "1주 평균 42,232원", "40.7%", MainBlue, "005930"),
                    StockInfo("한화생명", "1주 평균 52,232원", "25.4%", MainPink, "088350"),
                    StockInfo("LG전자", "1주 평균 2,232원", "12.1%", AppColors.Yellow, "066570"),
                    StockInfo("셀트리온", "1주 평균 4,232원", "8.2%", AppColors.Green, "068270"),
                    StockInfo("네이버", "1주 평균 10,232원", "5.6%", AppColors.Purple, "035420"),
                    StockInfo("기타", "1주 평균 1,232원", "40.7%", AppColors.Gray, "000000")
                )
            }
            PortfolioSection(
                pieChartData, 
                stockList, 
                onStockClick, 
                isLoggedIn = isLoggedIn,
                portfolioSummary = portfolioSummary // 수익률 표시용
            )
        }

        // 로그아웃 버튼
        if (isLoggedIn) {
            item { LogoutButton(userPreferences = userPreferences, onLogoutComplete = onLogoutComplete) }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
fun HeaderSection(
    isLoggedIn: Boolean = true,
    username: String = "",
    onLoginClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 메인 카드
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isLoggedIn) { onLoginClick() }
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 왼쪽 컨텐츠
                if (isLoggedIn) {
                    Column {
                        // 위험중립형 태그
                        Box(
                            modifier = Modifier
                                .background(
                                    BlueLight,
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "위험중립형",
                                style = BodyR12,
                                color = BlueNormal
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 사용자 이름
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = username,
                                style = TitleB24,
                                color = Black
                            )
                        }
                    }
                } else {
                    // 로그인 안된 상태
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "로그인 해주세요.",
                            style = TitleB24,
                            color = Gray600
                        )
                    }
                }

                if (isLoggedIn) {
                    // 프로필 사진
                    Box(
                        modifier = Modifier
                            .size(74.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDEEFFE))
                            .align(Alignment.CenterEnd)
                    )

                    // 카드 내부 하얀 설정 아이콘 버튼 (카드 위에서 74dp, 오른쪽에서 32dp, 큰 원 아래쪽에 위치)
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .align(Alignment.BottomEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.setting),
                            contentDescription = "설정",
                            modifier = Modifier.size(12.dp),
                            tint = Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AssetTitleSectionWithRanking(
    onRankingClick: () -> Unit = {}
) {
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
            modifier = Modifier.clickable { onRankingClick() }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.rank),
                contentDescription = "랭킹",
                modifier = Modifier.size(16.dp),
                tint = Gray700
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "랭킹 보기 >",
                style = BodyR14,
                color = Gray700
            )
        }
    }
}

@Composable
fun LogoutButton(
    userPreferences: com.lago.app.data.local.prefs.UserPreferences,
    onLogoutComplete: () -> Unit = {}
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        TextButton(
            onClick = {
                showLogoutDialog = true
            }
        ) {
            Text(
                text = "로그아웃",
                style = BodyR12,
                color = Gray700
            )
        }
    }
    
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = {
                showLogoutDialog = false
            },
            title = {
                Text(
                    text = "로그아웃",
                    style = TitleB18,
                    color = Black
                )
            },
            text = {
                Text(
                    text = "로그아웃 하시겠습니까?",
                    style = BodyR14,
                    color = Black
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // 모든 사용자 데이터 삭제
                        userPreferences.clearAllData()
                        showLogoutDialog = false
                        // 홈 화면으로 이동
                        onLogoutComplete()
                    }
                ) {
                    Text(
                        text = "예",
                        style = SubtitleSb14,
                        color = MainBlue
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                    }
                ) {
                    Text(
                        text = "아니오",
                        style = SubtitleSb14,
                        color = Gray600
                    )
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MyPageScreenPreview() {
    val mockSharedPrefs = object : android.content.SharedPreferences {
        override fun getAll(): MutableMap<String, *> = mutableMapOf<String, Any>()
        override fun getString(key: String?, defValue: String?): String? = defValue
        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = defValues
        override fun getInt(key: String?, defValue: Int): Int = defValue
        override fun getLong(key: String?, defValue: Long): Long = defValue
        override fun getFloat(key: String?, defValue: Float): Float = defValue
        override fun getBoolean(key: String?, defValue: Boolean): Boolean = defValue
        override fun contains(key: String?): Boolean = false
        override fun edit(): android.content.SharedPreferences.Editor = object : android.content.SharedPreferences.Editor {
            override fun putString(key: String?, value: String?) = this
            override fun putStringSet(key: String?, values: MutableSet<String>?) = this
            override fun putInt(key: String?, value: Int) = this
            override fun putLong(key: String?, value: Long) = this
            override fun putFloat(key: String?, value: Float) = this
            override fun putBoolean(key: String?, value: Boolean) = this
            override fun remove(key: String?) = this
            override fun clear() = this
            override fun commit() = true
            override fun apply() {}
        }
        override fun registerOnSharedPreferenceChangeListener(listener: android.content.SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: android.content.SharedPreferences.OnSharedPreferenceChangeListener?) {}
    }
    MaterialTheme {
        MyPageScreen(userPreferences = com.lago.app.data.local.prefs.UserPreferences(mockSharedPrefs))
    }
}

/**
 * 기존 UI 스타일 유지하면서 실시간 데이터 표시
 */
@Composable
fun AssetStatusSectionWithRealTimeData(
    portfolioSummary: com.lago.app.data.remote.dto.MyPagePortfolioSummary,
    viewModel: MyPageViewModel
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
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 총자산 (특별 스타일) - 기존과 동일
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
                    text = run {
                        val totalAssets = portfolioSummary.balance + portfolioSummary.totalCurrentValue
                        android.util.Log.d("MyPageScreen", "🏦 총자산 계산: ${portfolioSummary.balance} + ${portfolioSummary.totalCurrentValue} = $totalAssets")
                        viewModel.formatAmount(totalAssets)
                    },
                    style = TitleB18,
                    color = Black
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 구분선 - 기존과 동일
            Box(
                modifier = Modifier
                    .width(360.dp)
                    .height(1.dp)
                    .background(Gray300)
                    .align(Alignment.CenterHorizontally)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 나머지 자산 정보 - 실시간 데이터로
            AssetInfoRow("보유현금", viewModel.formatAmount(portfolioSummary.balance))
            AssetInfoRow("총매수", viewModel.formatAmount(portfolioSummary.totalPurchaseAmount))
            AssetInfoRow("총평가", viewModel.formatAmount(portfolioSummary.totalCurrentValue))

            // 평가손익 - 기존 스타일 유지
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
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = run {
                            // 평가손익의 수익률 = (총평가 - 총매수) / 총매수 * 100
                            val calculatedProfitRate = if (portfolioSummary.totalPurchaseAmount > 0) {
                                (portfolioSummary.profitLoss.toDouble() / portfolioSummary.totalPurchaseAmount) * 100
                            } else 0.0
                            viewModel.formatProfitLoss(portfolioSummary.profitLoss, calculatedProfitRate)
                        },
                        style = TitleB14,
                        color = viewModel.getProfitLossColor(portfolioSummary.profitLoss)
                    )
                }
            }
        }
    }
}

