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
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.lago.app.R
import com.lago.app.presentation.theme.*

data class TradingBot(
    val name: String,
    val character: Int,
    val amount: String,
    val profit: String,
    val profitColor: Color
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
fun HomeScreen() {
    val tradingBots = listOf(
        TradingBot("화끈이", R.drawable.character_red, "12,450,000원", "137,000원(+2.56%)", Color(0xFFFF6B6B)),
        TradingBot("적극이", R.drawable.character_yellow, "8,750,000원", "25,000원(+1.2%)", Color(0xFF51CF66)),
        TradingBot("균형이", R.drawable.character_blue, "15,200,000원", "-45,000원(-0.8%)", Color(0xFFFF6B6B)),
        TradingBot("조심이", R.drawable.character_green, "6,800,000원", "12,000원(+0.4%)", Color(0xFF51CF66))
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
                        Column(
                            modifier = Modifier.padding(top = 24.dp)
                        ) {
                            Text(
                                text = "안녕하세요 박두칠님!",
                                style = HeadEb28
                            )
                            Text(
                                text = "위험중립형에게는 중위험/중수익의" +
                                        "\n대형주를 권해요.",
                                style = TitleB18,
                                modifier = Modifier.padding(top = 13.dp)
                            )
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
            InvestmentSection()
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 성향별 매매봇 Section
        item {
            TradingBotSection(tradingBots)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 보유 종목 Section
        item {
            StockSection(stocks)
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun InvestmentSection() {
    var isHistoryMode by remember { mutableStateOf(true) }
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
                containerColor = Color.White
            )
        ) {
            Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                val composition by rememberLottieComposition(
                    LottieCompositionSpec.Asset("money_bag_animation.json")
                )
                val progress by animateLottieCompositionAsState(
                    composition,
                    iterations = LottieConstants.IterateForever
                )
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier
                        .size(150.dp)
                )

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .fillMaxHeight()
                        .height(169.dp)
                        .padding(top = 16.dp, bottom = 16.dp)
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
                                onCheckedChange = { isHistoryMode = it },
                                modifier = Modifier.padding(start = 8.dp),
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

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "13,378,095원",
                            style = HeadEb24
                        )

                        Text(
                            text = "+57,000원(3.33%)",
                            style = TitleB14.copy(color = Color(0xFFED5454))
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // 아래쪽 주문내역
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "주문내역",
                            style = SubtitleSb14
                        )
                        Text(
                            text = " >",
                            style = SubtitleSb18
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TradingBotSection(tradingBots: List<TradingBot>) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        Text(
            text = "성향별 매매봇",
            style = HeadEb24
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(tradingBots) { bot ->
                TradingBotCard(bot)
            }
        }
    }
}

@Composable
private fun TradingBotCard(bot: TradingBot) {
    Card(
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
                Text(
                    text = bot.name,
                    style = TitleB14
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = bot.amount,
                    style = TitleB24
                )

                Text(
                    text = bot.profit,
                    style = SubtitleSb14.copy(color = Color(0xFFF63232))
                )
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
private fun StockSection(stocks: List<Stock>) {
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
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                stocks.forEachIndexed { index, stock ->
                    StockItem(stock)
                    if (index != stocks.lastIndex) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                    }
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
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = when (stock.name) {
                            "삼성전자" -> BlueLight
                            else -> Color(0xFFFFE9E9)
                        },
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stock.name.take(2),
                    style = TitleB14.copy(
                        color = when (stock.name) {
                            "삼성전자" -> BlueNormal
                            else -> Color(0xFFFF6B6B)
                        }
                    )
                )
            }

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

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    LagoTheme {
        HomeScreen()
    }
}