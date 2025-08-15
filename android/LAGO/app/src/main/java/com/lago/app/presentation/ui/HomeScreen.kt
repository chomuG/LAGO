package com.lago.app.presentation.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.asImageBitmap
import com.lago.app.R
import com.lago.app.presentation.theme.*
import com.lago.app.data.local.prefs.UserPreferences
import com.lago.app.presentation.ui.components.CircularStockLogo
import androidx.compose.runtime.LaunchedEffect
import android.content.SharedPreferences

data class TradingBot(
    val id: Int,
    val name: String,
    val character: Int,
    val amount: String,
    val profit: String,
    val profitPercent: String,
    val investmentType: String
)

data class Stock(
    val name: String,
    val code: String,
    val shares: Int,
    val price: String,
    val profit: String,
    val profitPercentage: String,
    val profitColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userPreferences: UserPreferences,
    initialType: Int = 0, // 0: 모의투자, 1: 역사모드
    onOrderHistoryClick: (Int) -> Unit = {}, // type을 파라미터로 전달
    onLoginClick: () -> Unit = {},
    onTradingBotClick: (Int) -> Unit = {},
    onStockClick: (String) -> Unit = {},
    viewModel: com.lago.app.presentation.viewmodel.home.HomeViewModel = hiltViewModel()
) {
    val isLoggedIn = userPreferences.getAuthToken() != null
    val username = userPreferences.getUsername() ?: "게스트"
    val uiState by viewModel.uiState.collectAsState()
    val tradingBots = listOf(
        TradingBot(1, "화끈이", R.drawable.character_red, "12,450,000원", "+137,000원", "2.56%", "공격투자형"),
        TradingBot(2, "적극이", R.drawable.character_yellow, "8,750,000원", "+25,000원", "1.2%", "적극투자형"),
        TradingBot(3, "균형이", R.drawable.character_blue, "15,200,000원", "-45,000원", "0.8%", "위험중립형"),
        TradingBot(4, "조심이", R.drawable.character_green, "6,800,000원", "+12,000원", "0.4%", "안정추구형")
    )

    val stocks = listOf(
        Stock("삼성전자", "005930", 10, "82,000원", "-2.7%", "-2.7%", MainBlue),
        Stock("한화생명", "088350", 5, "275,000원", "+15.7%", "+15.7%", MainPink),
        Stock("삼성전자", "005930", 10, "82,000원", "-2.7%", "-2.7%", MainBlue),
        Stock("한화생명", "088350", 5, "275,000원", "+15.7%", "+15.7%", MainPink),
        Stock("삼성전자", "005930", 10, "82,000원", "-2.7%", "-2.7%", MainBlue),
        Stock("한화생명", "088350", 5, "275,000원", "+15.7%", "+15.7%", MainPink)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        item {
            // Header Section with Background and Character
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                // Background Image
                Image(
                    painter = painterResource(id = R.drawable.main_home_blue),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )

                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        // Greeting Text with background for better visibility
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                if (isLoggedIn) {
                                    Text(
                                        text = "안녕하세요 ${username}님!",
                                        style = HeadEb28
                                    )
                                    Text(
                                        text = "위험중립형에게는 중위험/중수익의" +
                                                "\n대형주를 권해요.",
                                        style = TitleB18,
                                        modifier = Modifier.padding(top = 13.dp)
                                    )
                                } else {
                                    Text(
                                        text = "로그인하고" +
                                                "\n투자성향을 파악해보세요!",
                                        style = TitleB20,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }

                            if (!isLoggedIn) {
                                Card(
                                    onClick = { onLoginClick() },
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    ),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "로그인",
                                            style = TitleB14.copy(color = Black)
                                        )

                                        Image(
                                            painter = painterResource(id = R.drawable.right_arrow),
                                            contentDescription = "로그인",
                                            modifier = Modifier
                                                .size(12.dp)
                                                .padding(start = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Character Image positioned at bottom right
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.character_blue_home),
                            contentDescription = "캐릭터",
                            modifier = Modifier
                                .size(160.dp)
                                .offset(y = 20.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 내 투자금 Section
        item {
            InvestmentSection(
                isLoggedIn = isLoggedIn,
                portfolioSummary = uiState.portfolioSummary,
                viewModel = viewModel,
                userPreferences = userPreferences,
                initialType = initialType,
                onOrderHistoryClick = onOrderHistoryClick
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 성향별 매매봇 Section
        item {
            TradingBotSection(tradingBots, onTradingBotClick)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 보유 종목 Section
        item {
            StockSection(
                isLoggedIn = isLoggedIn,
                homeStocks = uiState.stockList,
                viewModel = viewModel,
                onStockClick = onStockClick
            )
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun InvestmentSection(
    isLoggedIn: Boolean = true,
    portfolioSummary: com.lago.app.data.remote.dto.MyPagePortfolioSummary? = null,
    viewModel: com.lago.app.presentation.viewmodel.home.HomeViewModel? = null,
    userPreferences: UserPreferences,
    initialType: Int = 0,
    onOrderHistoryClick: (Int) -> Unit = {}
) {
    // UserPreferences에서 저장된 투자 모드를 가져옴
    var isHistoryMode by remember { 
        mutableStateOf(userPreferences.getInvestmentMode() == 1)
    }
    Column(
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        Text(
            text = "내 투자금",
            style = HeadEb24.copy(color = Color.Black)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = ShadowColor,
                    ambientColor = ShadowColor
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isLoggedIn) Color.White else Gray200
            )
        ) {
            if (isLoggedIn) {
                // 로그인된 상태의 기존 UI
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.money_bag),
                        contentDescription = "돈주머니",
                        modifier = Modifier
                            .size(150.dp)
                    )

                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier
                            .fillMaxHeight()
                            .height(169.dp)
                            .padding(top = 8.dp, bottom = 12.dp)
                    ) {
                        // 위쪽 그룹
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = if (isHistoryMode) "역사모드" else "모의투자",
                                    style = BodyR12.copy(color = Gray600)
                                )

                                // Material3 Switch
                                Switch(
                                    checked = isHistoryMode,
                                    onCheckedChange = { newMode ->
                                        isHistoryMode = newMode
                                        val mode = if (newMode) 1 else 0
                                        userPreferences.setInvestmentMode(mode)
                                        android.util.Log.d("HomeScreen", "Investment mode changed to: $mode")
                                    },
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .scale(0.8f),
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = MainBlue,
                                        checkedBorderColor = Color.Transparent,
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = Gray300,
                                        uncheckedBorderColor = Color.Transparent
                                    )
                                )
                            }

                            Text(
                                text = if (portfolioSummary != null && viewModel != null) {
                                    val totalAssets = portfolioSummary.balance + portfolioSummary.totalCurrentValue
                                    android.util.Log.d("HomeScreen", "🏠 내 투자금: 총자산 ${totalAssets}원")
                                    viewModel.formatAmount(totalAssets)
                                } else "13,378,095원",
                                style = HeadEb24
                            )

                            Text(
                                text = if (portfolioSummary != null && viewModel != null) {
                                    // 평가손익 = 총평가 - 총매수 (계산값)
                                    val calculatedProfitLoss = portfolioSummary.totalCurrentValue - portfolioSummary.totalPurchaseAmount
                                    val calculatedProfitRate = if (portfolioSummary.totalPurchaseAmount > 0) {
                                        (calculatedProfitLoss.toDouble() / portfolioSummary.totalPurchaseAmount) * 100
                                    } else 0.0
                                    val sign = if (calculatedProfitLoss > 0) "+" else ""
                                    "${sign}${viewModel.formatAmount(calculatedProfitLoss)} (${sign}${String.format("%.2f", calculatedProfitRate)}%)"
                                } else "+57,000원(3.33%)",
                                style = TitleB14.copy(
                                    color = if (portfolioSummary != null) {
                                        val calculatedProfitLoss = portfolioSummary.totalCurrentValue - portfolioSummary.totalPurchaseAmount
                                        if (calculatedProfitLoss > 0) Color(0xFFED5454) else if (calculatedProfitLoss < 0) Color.Blue else Color(0xFFED5454)
                                    } else Color(0xFFED5454)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // 아래쪽 주문내역
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { 
                                val type = if (isHistoryMode) 1 else 0
                                onOrderHistoryClick(type)
                            }
                        ) {
                            Text(
                                text = "거래내역",
                                style = SubtitleSb14
                            )
                            Image(
                                painter = painterResource(id = R.drawable.right_arrow),
                                contentDescription = "주문내역 보기",
                                modifier = Modifier
                                    .size(15.dp)
                                    .padding(start = 8.dp)
                            )
                        }
                    }
                }
            } else {
                // 비로그인 상태의 새로운 UI
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(169.dp)
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.Bottom)
                    ) {
                        Text(
                            text = "로그인하시고",
                            style = TitleB24
                        )
                        Text(
                            text = "투자금을 확인하세요.",
                            style = TitleB24,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Image(
                        painter = painterResource(id = R.drawable.lock_image),
                        contentDescription = "잠금",
                        modifier = Modifier
                            .size(150.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}

@Composable
private fun TradingBotSection(tradingBots: List<TradingBot>, onTradingBotClick: (Int) -> Unit = {}) {
    Column {
        Text(
            text = "성향별 매매봇",
            style = HeadEb24,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {
            items(tradingBots) { bot ->
                TradingBotCard(bot, onTradingBotClick)
            }
        }
    }
}

@Composable
private fun TradingBotCard(bot: TradingBot, onTradingBotClick: (Int) -> Unit = {}) {
    val profitColor = if (bot.profit.startsWith("-")) MainBlue else Color(0xFFFF6B6B)
    Card(
        onClick = { onTradingBotClick(bot.id) },
        modifier = Modifier
            .width(273.dp)
            .height(158.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = ShadowColor,
                ambientColor = ShadowColor
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = bot.name,
                            style = TitleB14
                        )

                        Text(
                            text = " | ",
                            style = TitleB14.copy(color = Gray500),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        Text(
                            text = bot.investmentType,
                            style = BodyR14.copy(color = Gray500)
                        )
                    }

                    Image(
                        painter = painterResource(id = R.drawable.right_arrow),
                        contentDescription = "상세보기",
                        modifier = Modifier.size(15.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = bot.amount,
                    style = TitleB24
                )

                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = bot.profit,
                        style = SubtitleSb14.copy(color = profitColor)
                    )
                    Text(
                        text = "(${bot.profitPercent})",
                        style = SubtitleSb14.copy(color = profitColor),
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }

            // Character positioned at bottom right
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Image(
                    painter = painterResource(id = bot.character),
                    contentDescription = bot.name,
                    modifier = Modifier.size(95.dp)
                )
            }
        }
    }
}

@Composable
private fun StockSection(
    isLoggedIn: Boolean = true,
    homeStocks: List<com.lago.app.presentation.viewmodel.home.HomeStock> = emptyList(),
    viewModel: com.lago.app.presentation.viewmodel.home.HomeViewModel? = null,
    onStockClick: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        Text(
            text = "보유 종목",
            style = HeadEb24
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = ShadowColor,
                    ambientColor = ShadowColor
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isLoggedIn) Color.White else Gray200
            )
        ) {
            if (isLoggedIn) {
                // 로그인된 상태의 기존 UI
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (homeStocks.isNotEmpty() && viewModel != null) {
                        homeStocks.forEachIndexed { index, homeStock ->
                            HomeStockItem(homeStock, viewModel, onStockClick)
                            if (index != homeStocks.lastIndex) {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    } else {
                        // 기본 데이터 또는 로딩 상태
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "데이터를 로드 중입니다...",
                                style = BodyR14.copy(color = Gray600)
                            )
                        }
                    }
                }
            } else {
                // 비로그인 상태의 새로운 UI
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "로그인이 필요합니다",
                        style = TitleB16.copy(color = Gray600)
                    )
                }
            }
        }
    }
}

@Composable
private fun StockItem(stock: Stock) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularStockLogo(
                stockCode = stock.code,
                stockName = stock.name,
                size = 40.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = stock.name,
                    style = TitleB16
                )
                Text(
                    text = "${stock.shares}주",
                    style = BodyR12
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = stock.price,
                style = TitleB14
            )
            Text(
                text = stock.profit,
                style = BodyR14.copy(color = stock.profitColor)
            )
        }
    }
}

/**
 * 홈 화면용 API 데이터 주식 아이템
 */
@Composable
private fun HomeStockItem(
    homeStock: com.lago.app.presentation.viewmodel.home.HomeStock,
    viewModel: com.lago.app.presentation.viewmodel.home.HomeViewModel,
    onStockClick: (String) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onStockClick(homeStock.stockCode) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 로고 이미지 (assets/logo/logo_종목코드)
            StockLogoImage(
                stockCode = homeStock.stockCode,
                stockName = homeStock.stockName,
                size = 40.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = homeStock.stockName,
                    style = TitleB16
                )
                Text(
                    text = "${homeStock.quantity}주",
                    style = BodyR12.copy(color = Gray600)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = if (homeStock.currentPrice != null) {
                    "${String.format("%,.0f", homeStock.currentPrice)}원"
                } else {
                    "현재가 없음"
                },
                style = TitleB14
            )
            Text(
                text = viewModel.formatProfitLoss(homeStock.profitLoss, homeStock.profitRate),
                style = BodyR14.copy(color = viewModel.getProfitLossColor(homeStock.profitLoss))
            )
        }
    }
}

/**
 * 주식 로고 이미지 컴포넌트 (assets/logos/에서 로드)
 */
@Composable
private fun StockLogoImage(
    stockCode: String,
    stockName: String,
    size: Dp = 40.dp
) {
    val context = LocalContext.current
    val logoBitmap = remember(stockCode) {
        try {
            // assets/logos/logo_종목코드.png 경로에서 로드
            val fileName = "logos/logo_$stockCode.png"
            val inputStream = context.assets.open(fileName)
            android.graphics.BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            android.util.Log.w("StockLogoImage", "로고 이미지 로드 실패: $stockCode", e)
            null
        }
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Gray100),
        contentAlignment = Alignment.Center
    ) {
        if (logoBitmap != null) {
            Image(
                bitmap = logoBitmap.asImageBitmap(),
                contentDescription = "$stockName 로고",
                modifier = Modifier
                    .size(size * 0.8f)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            // 로고가 없으면 이니셜 표시
            Text(
                text = stockName.take(2),
                style = TitleB14.copy(color = Gray600),
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    LagoTheme {
        val mockSharedPrefs = object : SharedPreferences {
            override fun getAll(): MutableMap<String, *> = mutableMapOf<String, Any>()
            override fun getString(key: String?, defValue: String?): String? = defValue
            override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = defValues
            override fun getInt(key: String?, defValue: Int): Int = defValue
            override fun getLong(key: String?, defValue: Long): Long = defValue
            override fun getFloat(key: String?, defValue: Float): Float = defValue
            override fun getBoolean(key: String?, defValue: Boolean): Boolean = defValue
            override fun contains(key: String?): Boolean = false
            override fun edit(): SharedPreferences.Editor = object : SharedPreferences.Editor {
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
            override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
            override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        }
        HomeScreen(userPreferences = UserPreferences(mockSharedPrefs))
    }
}