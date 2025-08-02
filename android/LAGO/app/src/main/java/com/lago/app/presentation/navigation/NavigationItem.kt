package com.lago.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.media3.common.Timeline

sealed class NavigationItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : NavigationItem(
        route = "home",
        title = "홈",
        icon = Icons.Default.Home
    )
    
    object Investment : NavigationItem(
        route = "investment",
        title = "투자",
        icon = Icons.Default.Star
    )
    
    object Chart : NavigationItem(
        route = "chart",
        title = "차트",
        icon = Icons.Default.Build
    )
    
    object Learn : NavigationItem(
        route = "learn",
        title = "학습",
        icon = Icons.Default.Person
    )
    
    object News : NavigationItem(
        route = "news",
        title = "뉴스",
        icon = Icons.Default.Email
    )
    
    object Portfolio : NavigationItem(
        route = "portfolio",
        title = "포트폴리오",
        icon = Icons.Default.AccountBox
    )
    
    // Chart-related additional screens
    object StockPurchase : NavigationItem(
        route = "stock_purchase/{stockCode}/{action}",
        title = "주식 매매",
        icon = Icons.Default.ShoppingCart
    )
    
    object AIDialog : NavigationItem(
        route = "ai_dialog",
        title = "AI 차트 분석",
        icon = Icons.Default.Edit
    )
}

val bottomNavigationItems = listOf(
    NavigationItem.Home,
    NavigationItem.Investment,
    NavigationItem.Chart,
    NavigationItem.News,
    NavigationItem.Portfolio
)