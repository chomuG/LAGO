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
                }
            )
        }

        composable(NavigationItem.Investment.route) {
            StockListScreen(
                onStockClick = { stockCode ->
                    navController.navigate("chart/$stockCode")
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

        // Chart Screen with stock code parameter
        composable(
            route = "chart/{stockCode}",
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
                onRankingClick = {
                    navController.navigate("ranking")
                },
                onStockClick = { stockCode ->
                    navController.navigate("chart/$stockCode")
                }
            )
        }

        composable("ranking") {
            RankingScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onUserClick = {
                    navController.navigate("portfolio")
                },
                onAiPortfolioClick = {
                    navController.navigate("ai_portfolio")
                }
            )
        }

        composable("portfolio") {
            PortfolioScreen(
                onStockClick = { stockCode ->
                    navController.navigate("chart/$stockCode")
                },
                onBackClick = {
                    navController.popBackStack()
                },
                userName = "박두칠"
            )
        }

        composable("ai_portfolio") {
            AiPortfolioScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onStockClick = { stockCode ->
                    navController.navigate("chart/$stockCode")
                },
                userName = "AI 포트폴리오"
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
    }
}