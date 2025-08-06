package com.lago.app.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.lago.app.presentation.ui.*
import com.lago.app.presentation.ui.study.Screen.PatternStudyScreen
import com.lago.app.presentation.ui.study.Screen.WordbookScreen
import com.lago.app.presentation.ui.study.Screen.RandomQuizScreen
import com.lago.app.presentation.ui.study.Screen.DailyQuizScreen
import com.lago.app.presentation.ui.news.NewsDetailScreen

import androidx.compose.ui.Modifier

@Composable
fun NavGraph(
    navController: NavHostController,
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
            HomeScreen()
        }
        
        composable(NavigationItem.Investment.route) {
            InvestmentScreen()
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
    }
}