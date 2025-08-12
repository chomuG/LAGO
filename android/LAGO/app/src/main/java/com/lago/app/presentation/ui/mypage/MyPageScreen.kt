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

@Composable
fun MyPageScreen(
    userPreferences: com.lago.app.data.local.prefs.UserPreferences,
    onRankingClick: () -> Unit = {},
    onStockClick: (String) -> Unit = {},
    onLoginClick: () -> Unit = {},
    onLogoutComplete: () -> Unit = {}
) {
    val isLoggedIn = userPreferences.getAuthToken() != null
    val username = userPreferences.getUsername() ?: "게스트"
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

        // 자산 현황 섹션
        item { AssetStatusSection(isLoggedIn = isLoggedIn) }

        // 포트폴리오 차트 및 주식 리스트 통합 섹션
        item { PortfolioSection(pieChartData, stockList, onStockClick, isLoggedIn = isLoggedIn) }

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