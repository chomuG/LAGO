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
    val hideBottomBarRoutes = listOf("pattern_study", "wordbook", "random_quiz", "daily_quiz")
    val shouldLogicallyShowBottomBar = currentRoute !in hideBottomBarRoutes && currentRoute?.startsWith("news_detail") != true

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
                modifier = if (showBottomBarWithDelay) Modifier.padding(innerPadding) else Modifier
            )
        }
    }
}