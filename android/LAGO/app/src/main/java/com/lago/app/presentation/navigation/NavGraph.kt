package com.lago.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.lago.app.presentation.ui.*

@Composable
fun NavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = NavigationItem.Home.route
    ) {
        composable(NavigationItem.Home.route) {
            HomeScreen()
        }
        
        composable(NavigationItem.Investment.route) {
            InvestmentScreen()
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
    }
}