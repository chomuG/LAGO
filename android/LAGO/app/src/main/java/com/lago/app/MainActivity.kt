package com.lago.app

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import com.lago.app.presentation.navigation.BottomNavigationBar
import com.lago.app.presentation.navigation.NavGraph
import com.lago.app.presentation.theme.LagoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 다크모드 비활성화
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)


        setContent {
            LagoTheme {
                LagoApp()
            }
        }
    }
}

@Composable
fun LagoApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Routes where bottom navigation should be hidden
    val hideBottomBarRoutes = listOf(
        "pattern_study", 
        "wordbook", 
        "random_quiz", 
        "daily_quiz",
        "chart",  // 차트 탭 화면 (목 데이터)
        "chart/{stockCode}",  // 차트 화면
        "history_challenge_chart/{stockCode}",  // 역사 챌린지 차트 화면
        "stock_purchase/{stockCode}/{transactionType}"  // 구매/판매 화면
    )
    
    // Check if current route matches any of the hidden routes (including parameterized routes)
    val shouldHideBottomBar = hideBottomBarRoutes.any { route ->
        when {
            route.contains("{") -> {
                // For parameterized routes, check if current route starts with the base path
                val basePath = route.substringBefore("{")
                currentRoute?.startsWith(basePath) == true
            }
            else -> currentRoute == route
        }
    } || currentRoute?.startsWith("news_detail") == true
    
    val shouldLogicallyShowBottomBar = !shouldHideBottomBar

    // State for delayed bottom bar animation
    var showBottomBarWithDelay by remember { mutableStateOf(shouldLogicallyShowBottomBar) }

    // Add delay when showing bottom bar to improve transition smoothness
    LaunchedEffect(shouldLogicallyShowBottomBar) {
        if (shouldLogicallyShowBottomBar) {

            showBottomBarWithDelay = true
        } else {
            // Hide immediately when navigating to screens without bottom bar
            showBottomBarWithDelay = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            bottomBar = {
                if (showBottomBarWithDelay) {
                    BottomNavigationBar(navController = navController)
                }
            }
        ) { innerPadding ->
            NavGraph(
                navController = navController,
                modifier = if (showBottomBarWithDelay) {
                    Modifier.padding(innerPadding)
                } else {
                    // 네비게이션 바가 없을 때도 상태표시줄 패딩은 유지
                    Modifier.padding(
                        WindowInsets.statusBars.asPaddingValues()
                    )
                }
            )
        }
    }
}