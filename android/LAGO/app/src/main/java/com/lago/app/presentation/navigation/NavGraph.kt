package com.lago.app.presentation.navigation

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
import androidx.compose.ui.Modifier
import com.lago.app.presentation.ui.stocklist.StockListScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavigationItem.Home.route,
        modifier = modifier
    ) {
        composable(NavigationItem.Home.route) {
            HomeScreen()
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
        
        composable(NavigationItem.Learn.route) {
            LearnScreen()
        }
        
        composable(NavigationItem.News.route) {
            NewsScreen()
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
    }
}