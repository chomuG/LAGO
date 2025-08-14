package com.lago.app.presentation.ui.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.lago.app.presentation.theme.*
import com.lago.app.presentation.ui.components.CommonTopAppBar
import com.lago.app.presentation.viewmodel.RankingViewModel
import com.lago.app.data.remote.dto.CalculatedRankingUser
import com.lago.app.R

// ================================
// Ranking Screen
// ================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(
    userPreferences: com.lago.app.data.local.prefs.UserPreferences,
    onBackClick: () -> Unit = {},
    onUserClick: () -> Unit = {},
    onAiPortfolioClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    viewModel: RankingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoggedIn = userPreferences.getAuthToken() != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
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
            Spacer(modifier = Modifier.height(Spacing.sm))

            // 현재 사용자 랭킹 카드
            when {
                isLoggedIn && uiState.currentUser != null -> {
                    ApiRankingCard(
                        user = uiState.currentUser!!,
                        viewModel = viewModel,
                        onUserClick = onUserClick
                    )
                }
                isLoggedIn -> {
                    Text(
                        text = "아직 랭킹에 등록되지 않았습니다.",
                        style = TitleB16,
                        color = Gray600,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                else -> {
                    LoginPromptCard(onLoginClick = onLoginClick)
                }
            }

            // 포디움 섹션 (Top3)
            if (uiState.top3Users.isNotEmpty()) {
                ApiPodiumSection(
                    top3Users = uiState.top3Users,
                    viewModel = viewModel,
                    onUserClick = onUserClick
                )
            }

            // 4등 이하 리스트
            uiState.otherUsers.forEach { user ->
                ApiRankingCard(
                    user = user,
                    viewModel = viewModel,
                    onUserClick = if (user.isAi) onAiPortfolioClick else onUserClick
                )
            }

            // 로딩 상태
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MainBlue)
                }
            }

            // 에러 메시지
            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = Color.Red,
                    style = BodyR14,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

// ================================
// Cards & Rows
// ================================
@Composable
fun ApiRankingCard(
    user: CalculatedRankingUser,
    viewModel: RankingViewModel,
    onUserClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clickable { onUserClick() }
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(Radius.lg),
                ambientColor = ShadowColor,
                spotColor = ShadowColor
            ),
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                // 랭킹 번호
                Text(
                    text = user.rank.toString(),
                    style = SubtitleSb20,
                    color = Black,
                    modifier = Modifier.padding(end = 8.dp)
                )

                // 프로필 점 (AI면 파랑)
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (user.isAi) MainBlue else Color(0xFFDEEFFE))
                )

                // 사용자명 + AI 아이콘
                Row(
                    modifier = Modifier.padding(start = 11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = user.username, style = SubtitleSb16, color = Black)
                    if (user.isAi) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.robot_icon),
                            contentDescription = "AI 봇",
                            modifier = Modifier.size(16.dp),
                            tint = MainBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(vertical = Spacing.md)
                ) {
                    Text(
                        text = viewModel.formatAmount(user.totalAsset),
                        style = TitleB16,
                        color = Black
                    )
                    Text(
                        text = viewModel.formatProfitWithRate(
                            user.calculatedProfit,
                            user.calculatedProfitRate
                        ),
                        style = BodyR12,
                        color = viewModel.getProfitColor(user.calculatedProfitRate)
                    )
                }
            }
        }
    }
}

// ================================
// Podium (Top 3)
// ================================
@Composable
fun ApiPodiumSection(
    top3Users: List<CalculatedRankingUser>,
    viewModel: RankingViewModel,
    onUserClick: () -> Unit = {}
) {
    if (top3Users.size < 3) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // 포디움 배경
        Image(
            painter = painterResource(id = R.drawable.top3),
            contentDescription = "Top 3 Podium",
            modifier = Modifier
                .size(width = 316.dp, height = 186.dp)
                .align(Alignment.BottomCenter)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            val first = top3Users.find { it.rank == 1 }
            val second = top3Users.find { it.rank == 2 }
            val third = top3Users.find { it.rank == 3 }

            second?.let {
                ApiPodiumUser(
                    user = it,
                    viewModel = viewModel,
                    circleSize = 70.dp,
                    nameStyle = TitleB16,
                    amountStyle = SubtitleSb14,
                    bottomPadding = 154.dp,
                    medalResource = R.drawable.silver,
                    onUserClick = onUserClick
                )
            }

            first?.let {
                ApiPodiumUser(
                    user = it,
                    viewModel = viewModel,
                    circleSize = 90.dp,
                    nameStyle = TitleB18,
                    amountStyle = HeadEb18,
                    amountColor = MainBlue,
                    bottomPadding = 190.dp,
                    medalResource = R.drawable.gold,
                    onUserClick = onUserClick
                )
            }

            third?.let {
                ApiPodiumUser(
                    user = it,
                    viewModel = viewModel,
                    circleSize = 70.dp,
                    nameStyle = TitleB16,
                    amountStyle = SubtitleSb14,
                    bottomPadding = 128.dp,
                    medalResource = R.drawable.bronze,
                    onUserClick = onUserClick
                )
            }
        }
    }
}

@Composable
fun ApiPodiumUser(
    user: CalculatedRankingUser,
    viewModel: RankingViewModel,
    circleSize: Dp,
    nameStyle: androidx.compose.ui.text.TextStyle,
    amountStyle: androidx.compose.ui.text.TextStyle,
    amountColor: Color = Black,
    bottomPadding: Dp,
    medalResource: Int,
    onUserClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(bottom = bottomPadding)
            .clickable { onUserClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(circleSize)
                    .clip(CircleShape)
                    .background(Gray400)
            )
            Icon(
                painter = painterResource(id = medalResource),
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

        Text(text = user.username, style = nameStyle, color = Black)
        Text(
            text = viewModel.formatAmount(user.totalAsset),
            style = amountStyle,
            color = amountColor
        )
    }
}

// ================================
// Misc
// ================================
@Composable
fun LoginPromptCard(onLoginClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clickable { onLoginClick() }
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(Radius.lg),
                ambientColor = ShadowColor,
                spotColor = ShadowColor
            ),
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 2.dp,
                    color = Gray100,
                    shape = RoundedCornerShape(Radius.lg)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "로그인 해주세요.", style = TitleB18, color = Gray600)
        }
    }
}

// ================================
// Preview
// ================================
@Preview(showBackground = true)
@Composable
fun RankingScreenPreview() {
    val mockSharedPrefs = object : android.content.SharedPreferences {
        override fun getAll(): MutableMap<String, *> = mutableMapOf<String, Any?>()
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
        RankingScreen(userPreferences = com.lago.app.data.local.prefs.UserPreferences(mockSharedPrefs))
    }
}
