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
import com.lago.app.presentation.ui.purchase.StockPurchaseScreen
import com.lago.app.presentation.ui.chart.AIDialog
import com.lago.app.presentation.ui.study.Screen.PatternStudyScreen
import com.lago.app.presentation.ui.study.Screen.WordbookScreen
import com.lago.app.presentation.ui.study.Screen.RandomQuizScreen
import com.lago.app.presentation.ui.study.Screen.DailyQuizScreen
import com.lago.app.presentation.ui.news.NewsDetailScreen
import com.lago.app.presentation.ui.home.OrderHistoryScreen

import androidx.compose.ui.Modifier
import com.lago.app.presentation.ui.stocklist.StockListScreen
import com.lago.app.data.local.prefs.UserPreferences
import androidx.hilt.navigation.compose.hiltViewModel

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
                }
            )
        }

        composable(NavigationItem.Chart.route) {
            ChartScreen(
                onNavigateToStockPurchase = { stockCode, action ->
                    navController.navigate("stock_purchase/$stockCode/$action")
                },
                onNavigateToAIDialog = {
                    navController.navigate(NavigationItem.AIDialog.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
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
                }
            )
        }

        // Chart with specific stock code
        composable(
            route = "chart/{stockCode}",
            arguments = listOf(
                navArgument("stockCode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val stockCode = backStackEntry.arguments?.getString("stockCode") ?: "005930"

            ChartScreen(
                stockCode = stockCode,
                onNavigateToStockPurchase = { code, action ->
                    navController.navigate("stock_purchase/$code/$action")
                },
                onNavigateToAIDialog = {
                    navController.navigate(NavigationItem.AIDialog.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
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
        
        composable(NavigationItem.Portfolio.route) {
            PortfolioScreen()
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

        composable(NavigationItem.OrderHistory.route) {
            OrderHistoryScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}