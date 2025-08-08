package com.lago.app.presentation.ui.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.lago.app.presentation.theme.*
import com.lago.app.presentation.ui.components.CommonTopAppBar
import com.lago.app.R


// 랭킹 데이터 클래스
data class RankingUser(
    val rank: Int,
    val name: String,
    val amount: String,
    val profit: String,
    val profitPercent: String,
    val isCurrentUser: Boolean = false
)

// 포디움 데이터 클래스 (1, 2, 3등)
data class PodiumUser(
    val rank: Int,
    val name: String,
    val amount: String,
    val medalResource: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(
    onBackClick: () -> Unit = {}
) {
    val currentUser = RankingUser(
        rank = 17,
        name = "박두철철철",
        amount = "12,482,000원",
        profit = "+612,000원",
        profitPercent = "(33.03%)",
        isCurrentUser = true
    )

    val podiumUsers = listOf(
        PodiumUser(1, "박두일", "12,482,000원", R.drawable.gold),
        PodiumUser(2, "박두이", "12,482,000원", R.drawable.silver),
        PodiumUser(3, "박두삼", "12,482,000원", R.drawable.bronze)
    )

    val otherUsers = listOf(
        RankingUser(4, "워렌버핏", "12,482,000원", "+612,000원", "(33.03%)"),
        RankingUser(5, "박두팔", "12,482,000원", "+612,000원", "(33.03%)"),
        RankingUser(6, "박두구", "12,482,000원", "+612,000원", "(33.03%)")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // 상단 앱바
        CommonTopAppBar(
            title = "랭킹",
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            // 상단 여백
            Spacer(modifier = Modifier.height(Spacing.sm))

            // 현재 사용자 랭킹 카드
            RankingCard(
                user = currentUser,
                isCurrentUser = true
            )

            // 포디움 섹션
            PodiumSection(podiumUsers)

            // 다른 사용자들
            otherUsers.forEach { user ->
                RankingCard(user = user)
            }

            // 하단 여백
            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

@Composable
fun RankingCard(
    user: RankingUser,
    isCurrentUser: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(Radius.lg),
                ambientColor = ShadowColor,
                spotColor = ShadowColor
            ),
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 2.dp,
                    color = Gray100,
                    shape = RoundedCornerShape(Radius.lg)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 왼쪽 랭킹 번호
                Text(
                    text = user.rank.toString(),
                    style = SubtitleSb20,
                    color = Black,
                    modifier = Modifier.padding(end = 8.dp)
                )

                // 하늘색 동그라미
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDEEFFE))
                )

                // 사용자 이름
                Text(
                    text = user.name,
                    style = SubtitleSb16,
                    color = Black,
                    modifier = Modifier.padding(start = 11.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                // 오른쪽 금액 정보
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(vertical = Spacing.md)
                ) {
                    Text(
                        text = user.amount,
                        style = TitleB16,
                        color = Black
                    )
                    Text(
                        text = "${user.profit}${user.profitPercent}",
                        style = BodyR12,
                        color = MainPink
                    )
                }
            }
        }
    }
}

@Composable
fun PodiumSection(podiumUsers: List<PodiumUser>) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            // 포디움 배경 이미지
            Image(
                painter = painterResource(id = R.drawable.top3),
                contentDescription = "Top 3 Podium",
                modifier = Modifier
                    .size(width = 316.dp, height = 186.dp)
                    .align(Alignment.BottomCenter)
            )

            // 사용자 원과 메달들
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // 2등 (왼쪽)
                PodiumUser(
                    user = podiumUsers[1],
                    circleSize = 70.dp,
                    nameStyle = TitleB16,
                    amountStyle = SubtitleSb14,
                    bottomPadding = 154.dp
                )

                // 1등 (가운데)
                PodiumUser(
                    user = podiumUsers[0],
                    circleSize = 90.dp,
                    nameStyle = TitleB18,
                    amountStyle = HeadEb18,
                    amountColor = MainBlue,
                    bottomPadding = 190.dp
                )

                // 3등 (오른쪽)
                PodiumUser(
                    user = podiumUsers[2],
                    circleSize = 70.dp,
                    nameStyle = TitleB16,
                    amountStyle = SubtitleSb14,
                    bottomPadding = 128.dp
                )
            }
        }
}

@Composable
fun PodiumUser(
    user: PodiumUser,
    circleSize: androidx.compose.ui.unit.Dp,
    nameStyle: androidx.compose.ui.text.TextStyle,
    amountStyle: androidx.compose.ui.text.TextStyle,
    amountColor: Color = Black,
    bottomPadding: androidx.compose.ui.unit.Dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = bottomPadding)
    ) {
        // 사용자 원과 메달
        Box(
            contentAlignment = Alignment.Center
        ) {
            // 회색 원
            Box(
                modifier = Modifier
                    .size(circleSize)
                    .clip(CircleShape)
                    .background(Gray400)
            )

            // 메달
            Icon(
                painter = painterResource(id = user.medalResource),
                contentDescription = "${user.rank}등 메달",
                modifier = Modifier
                    .size(37.dp)
                    .align(Alignment.BottomEnd)
                    .offset(y = 8.dp, x = 5.dp)
                    .zIndex(1f),
                tint = Color.Unspecified
            )

        }

        Spacer(modifier = Modifier.height(Spacing.sm))

        // 사용자 이름
        Text(
            text = user.name,
            style = nameStyle,
            color = Black
        )

        // 금액
        Text(
            text = user.amount,
            style = amountStyle,
            color = amountColor
        )
    }
}


@Preview(showBackground = true)
@Composable
fun RankingScreenPreview() {
    MaterialTheme {
        RankingScreen()
    }
}