package com.lago.app.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        Stock("삼성전자", "005930", 10, "82,000원", "-2.7%", "-2.7%", Color(0xFFFF6B6B)),
        Stock("한화생명", "088350", 5, "275,000원", "+15.7%", "+15.7%", Color(0xFF51CF66))
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
                    shape = RoundedCornerShape(16.dp),
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
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.money_bag),
                    contentDescription = "돈가방",
                    modifier = Modifier.size(80.dp)
                )

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "역산모드",
                            style = BodyR14.copy(color = Gray600)
                        )

                        // Radio button style toggle
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(20.dp)
                                .background(
                                    color = Gray300,
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        color = Color.White,
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    )
                                    .align(Alignment.Center)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "13,378,095원",
                        style = HeadEb28.copy(color = Color.Black)
                    )

                    Text(
                        text = "57,000원(+3.33%)",
                        style = BodyR14.copy(color = Color(0xFFFF6B6B))
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "주문내역",
                            style = BodyR14.copy(color = Gray600)
                        )
                        Text(
                            text = " >",
                            style = BodyR14.copy(color = Gray600)
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
            .width(200.dp)
            .height(140.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
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
                    style = SubtitleSb16.copy(color = Color.Black)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = bot.amount,
                    style = TitleB16.copy(color = Color.Black)
                )

                Text(
                    text = bot.profit,
                    style = BodyR14.copy(color = bot.profitColor)
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
                    modifier = Modifier.size(50.dp)
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
                    shape = RoundedCornerShape(16.dp),
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Gray200)
                        )
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
                    style = SubtitleSb16.copy(color = Color.Black)
                )
                Text(
                    text = "${stock.shares}주",
                    style = BodyR12.copy(color = Gray600)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = stock.price,
                style = TitleB16.copy(color = Color.Black)
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