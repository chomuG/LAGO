package com.lago.app.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.lago.app.presentation.ui.*
import com.lago.app.presentation.ui.chart.ChartScreen
import com.lago.app.presentation.ui.historychallenge.HistoryChallengeChartScreen
import com.lago.app.presentation.ui.purchase.StockPurchaseScreen
import com.lago.app.presentation.ui.chart.AIDialog
import com.lago.app.presentation.ui.study.Screen.PatternStudyScreen
import com.lago.app.presentation.ui.study.Screen.WordbookScreen
import com.lago.app.presentation.ui.study.Screen.RandomQuizScreen
import com.lago.app.presentation.ui.study.Screen.DailyQuizScreen
import com.lago.app.presentation.ui.news.NewsDetailScreen
import com.lago.app.presentation.ui.home.OrderHistoryScreen

import androidx.compose.ui.Modifier
import com.lago.app.presentation.ui.mypage.PortfolioScreen
import com.lago.app.presentation.ui.mypage.MyPageScreen
import com.lago.app.presentation.ui.mypage.RankingScreen
import com.lago.app.presentation.ui.mypage.AiPortfolioScreen
import com.lago.app.presentation.ui.stocklist.StockListScreen
import com.lago.app.data.local.prefs.UserPreferences
import com.lago.app.domain.entity.ChartStockInfo
import com.lago.app.presentation.ui.personalitytest.PersonalityTestNavigation
import com.lago.app.presentation.ui.login.LoginScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(
    navController: NavHostController,
    userPreferences: UserPreferences,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavigationItem.Home.route,
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable(NavigationItem.Home.route) {
            HomeScreen(
                userPreferences = userPreferences,
                initialType = 0, // 기본값
                onOrderHistoryClick = { type ->
                    navController.navigate("${NavigationItem.OrderHistory.route}/$type")
                },
                onLoginClick = {
                    navController.navigate("login");
                },
                onTradingBotClick = { userId ->
                    navController.navigate("ai_portfolio/$userId")
                },
                onStockClick = { stockCode, stockName ->
                    val encodedName = java.net.URLEncoder.encode(stockName, "UTF-8")
                    navController.navigate("chart/$stockCode/$encodedName")
                }
            )
        }
        
        // Type이 포함된 Home route
        composable(
            route = "${NavigationItem.Home.route}/{type}",
            arguments = listOf(navArgument("type") { type = NavType.IntType })
        ) {
            val type = it.arguments?.getInt("type") ?: 0
            HomeScreen(
                userPreferences = userPreferences,
                initialType = type,
                onOrderHistoryClick = { currentType ->
                    navController.navigate("${NavigationItem.OrderHistory.route}/$currentType")
                },
                onLoginClick = {
                    navController.navigate("login");
                },
                onTradingBotClick = { userId ->
                    navController.navigate("ai_portfolio/$userId")
                },
                onStockClick = { stockCode, stockName ->
                    val encodedName = java.net.URLEncoder.encode(stockName, "UTF-8")
                    navController.navigate("chart/$stockCode/$encodedName")
                }
            )
        }

        composable(NavigationItem.Investment.route) {
            StockListScreen(
                onStockClick = { stockCode, stockName, currentPrice, priceChange, priceChangePercent ->
                    val encodedName = java.net.URLEncoder.encode(stockName, "UTF-8")
                    navController.navigate("chart/$stockCode/$encodedName")
                },
                onHistoryChallengeStockClick = { stockCode ->
                    navController.navigate("history_challenge_chart/$stockCode")
                },
                onNewsClick = { newsId ->
                    navController.navigate("news_detail/$newsId")
                }
            )
        }



        // Chart Screen with stock parameters
        composable(
            route = "chart/{stockCode}/{stockName}",
            arguments = listOf(
                navArgument("stockCode") { type = NavType.StringType },
                navArgument("stockName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val stockCode = backStackEntry.arguments?.getString("stockCode") ?: "005930"
            val stockName = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("stockName") ?: "삼성전자", 
                "UTF-8"
            )
            android.util.Log.d("CHART_NAV", "차트 네비게이션 - stockCode: $stockCode, stockName: $stockName")

            ChartScreen(
                stockCode = stockCode,
                initialStockInfo = ChartStockInfo(
                    code = stockCode,
                    name = stockName,
                    currentPrice = 0f, // 기본값
                    priceChange = 0f, // 기본값
                    priceChangePercent = 0f, // 기본값
                    previousDay = null
                ),
                onNavigateToStockPurchase = { stockCode, action ->
                    navController.navigate("stock_purchase/$stockCode/$action")
                },
                onNavigateToAIDialog = {
                    navController.navigate(NavigationItem.AIDialog.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToStock = { selectedStockCode ->
                    navController.navigate("chart/$selectedStockCode") {
                        // 현재 차트 화면을 새로운 차트 화면으로 교체 (스택에 쌓지 않음)
                        popUpTo("chart") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        
        composable(NavigationItem.Learn.route) {
            LearnScreen(
                onRandomQuizClick = {
                    navController.navigate("random_quiz")
                },
                onDailyQuizClick = {
                    navController.navigate("daily_quiz")
                },
                onPatternStudyClick = {
                    navController.navigate("pattern_study")
                },
                onWordBookClick = {
                    navController.navigate("wordbook")
                }
            )
        }
        
        composable(NavigationItem.News.route) {
            NewsScreen(
                onNewsClick = { newsId ->
                    navController.navigate("news_detail/$newsId")
                }
            )
        }
        
        composable(NavigationItem.MyPage.route) {
            MyPageScreen(
                userPreferences = userPreferences,
                onRankingClick = {
                    navController.navigate("ranking")
                },
                onStockClick = { stockCode, stockName ->
                    val encodedName = java.net.URLEncoder.encode(stockName, "UTF-8")
                    android.util.Log.d("MYPAGE_NAV", "MyPage 주식 클릭 - stockCode: $stockCode, stockName: $stockName, encodedName: $encodedName")
                    navController.navigate("chart/$stockCode/$encodedName")
                }
            )
        }

        // Simple chart route for backward compatibility (MyPage, Portfolio, etc)
        composable(
            route = "chart_simple/{stockCode}",
            arguments = listOf(
                navArgument("stockCode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val stockCode = backStackEntry.arguments?.getString("stockCode") ?: "005930"

            ChartScreen(
                stockCode = stockCode,
                onNavigateToStockPurchase = { stockCode, action ->
                    navController.navigate("stock_purchase/$stockCode/$action")
                },
                onNavigateToAIDialog = {
                    navController.navigate(NavigationItem.AIDialog.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToStock = { selectedStockCode ->
                    navController.navigate("chart_simple/$selectedStockCode") {
                        popUpTo("chart_simple") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }


        composable("ranking") {
            RankingScreen(
                userPreferences = userPreferences,
                onBackClick = {
                    navController.popBackStack()
                },
                onUserClick = { userId, userName ->
                    android.util.Log.d("NAV_GRAPH", "일반 사용자 포트폴리오 네비게이션 - userId: $userId, userName: $userName")
                    val encodedName = java.net.URLEncoder.encode(userName, "UTF-8")
                    navController.navigate("portfolio/$userId/$encodedName")
                },
                onAiPortfolioClick = { userId ->
                    android.util.Log.d("NAV_GRAPH", "AI 포트폴리오 네비게이션 - userId: $userId")
                    navController.navigate("ai_portfolio/$userId")
                },
                onLoginClick = {
                    navController.navigate("login")
                }
            )
        }

        composable(
            route = "portfolio/{userId}/{userName}",
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType },
                navArgument("userName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 1
            val userName = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("userName") ?: "사용자", 
                "UTF-8"
            )
            PortfolioScreen(
                onStockClick = { stockCode, stockName ->
                    val encodedName = java.net.URLEncoder.encode(stockName, "UTF-8")
                    android.util.Log.d("PORTFOLIO_NAV", "Portfolio 주식 클릭 - stockCode: $stockCode, stockName: $stockName, encodedName: $encodedName")
                    navController.navigate("chart/$stockCode/$encodedName")
                },
                onBackClick = {
                    navController.popBackStack()
                },
                userName = userName,
                userId = userId
            )
        }

        // Backward compatibility for portfolio without parameters
        composable("portfolio") {
            PortfolioScreen(
                onStockClick = { stockCode, stockName ->
                    val encodedName = java.net.URLEncoder.encode(stockName, "UTF-8")
                    android.util.Log.d("PORTFOLIO_NAV", "Portfolio 주식 클릭 - stockCode: $stockCode, stockName: $stockName, encodedName: $encodedName")
                    navController.navigate("chart/$stockCode/$encodedName")
                },
                onBackClick = {
                    navController.popBackStack()
                },
                userName = "박두칠"
            )
        }

        composable(
            route = "ai_portfolio/{userId}",
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 1
            AiPortfolioScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onStockClick = { stockCode, stockName ->
                    val encodedName = java.net.URLEncoder.encode(stockName, "UTF-8")
                    android.util.Log.d("AI_PORTFOLIO_NAV", "AI Portfolio 주식 클릭 - stockCode: $stockCode, stockName: $stockName, encodedName: $encodedName")
                    navController.navigate("chart/$stockCode/$encodedName")
                },
                onOrderHistoryClick = { botUserId, type ->
                    android.util.Log.d("NAV_GRAPH", "AiPortfolio - onOrderHistoryClick: botUserId=$botUserId, type=$type")
                    navController.navigate("order_history_bot/$botUserId/$type")
                },
                userId = userId
            )
        }

        // Backward compatibility for AI Portfolio without userId
        composable("ai_portfolio") {
            AiPortfolioScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onStockClick = { stockCode, stockName ->
                    val encodedName = java.net.URLEncoder.encode(stockName, "UTF-8")
                    android.util.Log.d("AI_PORTFOLIO_NAV", "AI Portfolio 주식 클릭 - stockCode: $stockCode, stockName: $stockName, encodedName: $encodedName")
                    navController.navigate("chart/$stockCode/$encodedName")
                },
                onOrderHistoryClick = { botUserId, type ->
                    android.util.Log.d("NAV_GRAPH", "AiPortfolio - onOrderHistoryClick: botUserId=$botUserId, type=$type")
                    navController.navigate("order_history_bot/$botUserId/$type")
                },
                userId = 1 // 기본값
            )
        }

        // Stock Purchase Screen with arguments
        composable(
            route = "stock_purchase/{stockCode}/{action}",
            arguments = listOf(
                navArgument("stockCode") { type = NavType.StringType },
                navArgument("action") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val stockCode = backStackEntry.arguments?.getString("stockCode") ?: ""
            val action = backStackEntry.arguments?.getString("action") ?: "buy"

            StockPurchaseScreen(
                stockCode = stockCode,
                action = action,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onTransactionComplete = {
                    // 거래 완료 후 차트 화면으로 돌아가기
                    navController.popBackStack()
                }
            )
        }

        // AI Chart Analysis Dialog
        composable(NavigationItem.AIDialog.route) {
            AIDialog(
                onDismiss = {
                    navController.popBackStack()
                }
            )
        }

        composable("pattern_study") {
            PatternStudyScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("wordbook") {
            WordbookScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("random_quiz") {
            RandomQuizScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onBackToLearn = {
                    navController.popBackStack()
                }
            )
        }

        composable("daily_quiz") {
            DailyQuizScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("news_detail/{newsId}") { backStackEntry ->
            val newsId = backStackEntry.arguments?.getString("newsId") ?: "1"
            NewsDetailScreen(
                newsId = newsId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // History Challenge Chart Screen with stock code parameter
        composable(
            route = "history_challenge_chart/{stockCode}",
            arguments = listOf(
                navArgument("stockCode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val stockCode = backStackEntry.arguments?.getString("stockCode") ?: "005930"
            HistoryChallengeChartScreen(
                stockCode = stockCode,
                onNavigateToStockPurchase = { code, action ->
                    navController.navigate("stock_purchase/$code/$action")
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Login Screen
        composable("login") {
            LoginScreen(
                userPreferences = userPreferences,
                onKakaoLoginClick = {
                    navController.navigate("personality_test")
                }
            )
        }

        // Personality Test Flow
        composable("personality_test") {
            PersonalityTestNavigation(
                onBackToHome = {
                    navController.popBackStack()
                },
                onTestComplete = { result ->
                    // 투자성향 테스트 완료 시 임시 로그인 처리
                    userPreferences.setAuthToken("temp_token_12345")
                    userPreferences.setUserId("temp_user_001")
                    userPreferences.setUsername(result.nickname)

                    // 결과를 저장하고 홈으로 돌아가기
                    navController.navigate(NavigationItem.Home.route) {
                        popUpTo(NavigationItem.Home.route) {
                            inclusive = false
                        }
                    }
                }
            )
        }

        composable(
            route = "${NavigationItem.OrderHistory.route}/{type}",
            arguments = listOf(navArgument("type") { type = NavType.IntType })
        ) {
            val type = it.arguments?.getInt("type") ?: 0
            OrderHistoryScreen(
                type = type,
                onBackClick = {
                    navController.navigate("${NavigationItem.Home.route}/$type") {
                        popUpTo(NavigationItem.Home.route) { inclusive = true }
                    }
                }
            )
        }
        
        // 매매봇용 거래내역 (userId 파라미터 포함)
        composable(
            route = "order_history_bot/{userId}/{type}",
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType },
                navArgument("type") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 1
            val type = backStackEntry.arguments?.getInt("type") ?: 2
            android.util.Log.d("NAV_GRAPH", "OrderHistory composable - 파라미터: userId=$userId, type=$type")
            OrderHistoryScreen(
                userId = userId, // AiPortfolioScreen에서 받은 userId 전달
                type = 2, // AI 매매봇 타입
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}