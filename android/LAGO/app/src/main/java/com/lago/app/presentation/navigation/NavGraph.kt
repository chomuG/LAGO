package com.lago.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.lago.app.presentation.ui.*
import com.lago.app.presentation.ui.study.PatternStudyScreen
import com.lago.app.presentation.ui.study.WordbookScreen

import androidx.compose.ui.Modifier

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
            InvestmentScreen()
        }
        
        composable(NavigationItem.Learn.route) {
            LearnScreen(
                onPatternStudyClick = {
                    navController.navigate("pattern_study")
                },
                onWordBookClick = {
                    navController.navigate("wordbook")
                }
            )
        }
        
        composable(NavigationItem.News.route) {
            NewsScreen()
        }
        
        composable(NavigationItem.Portfolio.route) {
            PortfolioScreen()
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
    }
}