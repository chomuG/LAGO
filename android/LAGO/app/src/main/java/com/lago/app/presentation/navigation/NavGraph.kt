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
import androidx.hilt.navigation.compose.hiltViewModel
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
                onOrderHistoryClick = {
                    navController.navigate(NavigationItem.OrderHistory.route)
                },
                onLoginClick = {
                    navController.navigate("login");
                },
                onTradingBotClick = { userId ->
                    navController.navigate("ai_portfolio/$userId")
                },
                onStockClick = { stockCode ->
                    navController.navigate("chart_simple/$stockCode")
                }
            )
        }

        composable(NavigationItem.Investment.route) {
            StockListScreen(
                onStockClick = { stockCode, stockName, currentPrice, priceChange, priceChangePercent ->
                    val encodedName = java.net.URLEncoder.encode(stockName, "UTF-8")
                    navController.navigate("chart/$stockCode/$encodedName/$currentPrice/$priceChange/$priceChangePercent")
                },
                onHistoryChallengeStockClick = { stockCode ->
                    navController.navigate("history_challenge_chart/$stockCode")
                },
                onNewsClick = { newsId ->
                    navController.navigate("news_detail/$newsId")
                }
            )
        }

        composable(NavigationItem.Chart.route) {
            ChartScreen(
                stockCode = "005930", // 삼성전자 임시 목 데이터
                onNavigateToStockPurchase = { stockCode, action ->
                    navController.navigate("stock_purchase/$stockCode/$action")
                },
                onNavigateToAIDialog = {
                    navController.navigate(NavigationItem.AIDialog.route)
                },
                onNavigateBack = {
                    // 차트 탭에서는 뒤로가기 버튼 비활성화 (바텀네비게이션 탭이므로)
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

        // Chart Screen with stock parameters
        composable(
            route = "chart/{stockCode}/{stockName}/{currentPrice}/{priceChange}/{priceChangePercent}",
            arguments = listOf(
                navArgument("stockCode") { type = NavType.StringType },
                navArgument("stockName") { type = NavType.StringType },
                navArgument("currentPrice") { type = NavType.IntType },
                navArgument("priceChange") { type = NavType.IntType },
                navArgument("priceChangePercent") { type = NavType.FloatType }
            )
        ) { backStackEntry ->
            val stockCode = backStackEntry.arguments?.getString("stockCode") ?: "005930"
            val stockName = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("stockName") ?: "삼성전자", 
                "UTF-8"
            )
            val currentPrice = backStackEntry.arguments?.getInt("currentPrice") ?: 74200
            val priceChange = backStackEntry.arguments?.getInt("priceChange") ?: 0
            val priceChangePercent = backStackEntry.arguments?.getFloat("priceChangePercent") ?: 0f

            ChartScreen(
                stockCode = stockCode,
                initialStockInfo = ChartStockInfo(
                    code = stockCode,
                    name = stockName,
                    currentPrice = currentPrice.toFloat(),
                    priceChange = priceChange.toFloat(),
                    priceChangePercent = priceChangePercent
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
                onStockClick = { stockCode ->
                    // MyPage에서는 기본 방식으로 네비게이션 (backward compatibility)
                    navController.navigate("chart_simple/$stockCode")
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
                onUserClick = {
                    navController.navigate("portfolio")
                },
                onAiPortfolioClick = {
                    navController.navigate("ai_portfolio")
                },
                onLoginClick = {
                    navController.navigate("login")
                }
            )
        }

        composable("portfolio") {
            PortfolioScreen(
                onStockClick = { stockCode ->
                    navController.navigate("chart_simple/$stockCode")
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
                onStockClick = { stockCode ->
                    navController.navigate("chart_simple/$stockCode")
                },
                onOrderHistoryClick = { botUserId ->
                    navController.navigate("order_history/$botUserId")
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
                onStockClick = { stockCode ->
                    navController.navigate("chart_simple/$stockCode")
                },
                onOrderHistoryClick = { botUserId ->
                    navController.navigate("order_history/$botUserId")
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

        composable(NavigationItem.OrderHistory.route) {
            OrderHistoryScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // 매매봇용 거래내역 (userId 파라미터 포함)
        composable(
            route = "order_history/{userId}",
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 5
            OrderHistoryScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                // TODO: userId 파라미터를 OrderHistoryScreen에 전달하도록 향후 수정 필요
                userId = userId
            )
        }
    }
}