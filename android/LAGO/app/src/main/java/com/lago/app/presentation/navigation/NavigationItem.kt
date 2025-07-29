package com.lago.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

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
        icon = Icons.Default.Share
    )
    
    object Learn : NavigationItem(
        route = "learn",
        title = "학습",
        icon = Icons.Default.Edit
    )
    
    object News : NavigationItem(
        route = "news",
        title = "뉴스",
        icon = Icons.Default.Build
    )
    
    object Portfolio : NavigationItem(
        route = "portfolio",
        title = "포트폴리오",
        icon = Icons.Default.AccountBox
    )
}

val bottomNavigationItems = listOf(
    NavigationItem.Home,
    NavigationItem.Investment,
    NavigationItem.Learn,
    NavigationItem.News,
    NavigationItem.Portfolio
)