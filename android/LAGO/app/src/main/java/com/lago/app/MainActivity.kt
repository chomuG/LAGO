package com.lago.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.messaging.FirebaseMessaging
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
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
import com.lago.app.data.local.prefs.UserPreferences
import kotlinx.coroutines.delay
import com.lago.app.presentation.navigation.BottomNavigationBar
import com.lago.app.presentation.navigation.NavGraph
import com.lago.app.presentation.theme.LagoTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("FCM", "✅ Notification permission granted")
        } else {
            Log.w("FCM", "❌ Notification permission denied")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge 설정
        enableEdgeToEdge()
        
        // 상태표시줄과 네비게이션 바 설정
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 다크모드 비활성화
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // 알림 권한 요청 (Android 13+)
        requestNotificationPermission()
        
        // FCM 초기화 및 토픽 구독
        initializeFCM()

        setContent {
            LagoTheme {
                LagoApp(userPreferences = userPreferences)
            }
        }
    }
    
    private fun initializeFCM() {
        Log.d("FCM", "=== Initializing FCM ===")
        
        // FCM 토큰 가져오기
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "❌ Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // 새로운 FCM 등록 토큰 가져오기
            val token = task.result
            Log.d("FCM", "✅ FCM Registration Token: $token")
            Log.d("FCM", "📋 Copy this token for testing: $token")

            // TODO: 토큰을 서버에 전송 (필요시)
            // sendTokenToServer(token)
        }
        
        // 데일리 퀴즈 토픽 구독
        FirebaseMessaging.getInstance().subscribeToTopic("daily_quiz")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM", "✅ Successfully subscribed to 'daily_quiz' topic")
                } else {
                    Log.e("FCM", "❌ Failed to subscribe to 'daily_quiz' topic", task.exception)
                }
            }
            
        Log.d("FCM", "=== FCM Initialization Complete ===")
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("FCM", "✅ Notification permission already granted")
                }
                else -> {
                    Log.d("FCM", "🔔 Requesting notification permission...")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            Log.d("FCM", "✅ Notification permission not required (Android < 13)")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LagoApp(userPreferences: UserPreferences) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Routes where bottom navigation should be hidden
    val hideBottomBarRoutes = listOf(
        "pattern_study",
        "wordbook",
        "random_quiz",
        "daily_quiz",
        "pattern_study",
        "login", "personality_test",
        "order_history", "ranking",
        "order_history/{type}", // 타입이 포함된 거래내역 화면
        "order_history_bot/{userId}/{type}", // 매매봇용 거래내역 화면  
        "portfolio",
        "chart",  // 차트 탭 화면 (목 데이터)
        "chart/{stockCode}",  // 차트 화면
        "history_challenge_chart/{stockCode}",  // 역사 챌린지 차트 화면
        "stock_purchase/{stockCode}/{transactionType}", // 구매/판매 화면,
        "ai_portfolio",
        "ai_portfolio/{userId}" // userId가 포함된 AI 포트폴리오 화면
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
                    Modifier
                        .padding(innerPadding)
                        .windowInsetsPadding(WindowInsets.statusBars)
                } else {
                    Modifier
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                },
                userPreferences = userPreferences,
            )
        }
    }
}