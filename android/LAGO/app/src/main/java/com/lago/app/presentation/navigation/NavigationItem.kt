package com.lago.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.lago.app.R

sealed class NavigationItem(
    val route: String,
    val title: String,
    val iconRes: Int
) {
    object Home : NavigationItem(
        route = "home",
        title = "홈",
        iconRes = R.drawable.home
    )
    
    object Investment : NavigationItem(
        route = "investment",
        title = "투자",
        iconRes = R.drawable.stock
    )

    object Learn : NavigationItem(
        route = "learn",
        title = "학습",
        iconRes = R.drawable.scholar
    )
    
    object News : NavigationItem(
        route = "news",
        title = "뉴스",
        iconRes = R.drawable.news
    )
    
    object MyPage : NavigationItem(
        route = "mypage",
        title = "마이",
        iconRes = R.drawable.my
    )

    object AIDialog : NavigationItem(
        route = "ai_dialog",
        title = "AI 차트 분석",
        iconRes = R.drawable.my
    )

    object OrderHistory : NavigationItem(
        route = "order_history",
        title = "나의 주문 내역",
        iconRes = R.drawable.setting
    )

}

val bottomNavigationItems = listOf(
    NavigationItem.Home,
    NavigationItem.Investment,
    NavigationItem.Learn,
    NavigationItem.News,
    NavigationItem.MyPage
)