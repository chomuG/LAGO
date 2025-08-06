package com.lago.app.presentation.ui.study.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lago.app.presentation.theme.AppBackground
import com.lago.app.presentation.theme.LagoTheme
import com.lago.app.presentation.theme.MainBlue
import com.lago.app.presentation.ui.study.Dialog.DailyQuizResultDialog
import com.lago.app.presentation.viewmodel.DailyQuizUiState
import com.lago.app.presentation.viewmodel.DailyQuizViewModel

@Composable
fun DailyQuizScreen(
    onBackClick: () -> Unit = {},
    viewModel: DailyQuizViewModel = hiltViewModel()
) {
    var showResult by remember { mutableStateOf(false) }
    var quizResult by remember { mutableStateOf<QuizResult?>(null) }
    
    val dailyQuizState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var currentQuizItem by remember { 
        mutableStateOf(QuizItem("주식의 PER이 높다는 것은 그 기업의 성장 가능성을 높게 본다는 뜻이다?", true)) 
    }
    
    when(dailyQuizState) {
        is DailyQuizUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppBackground),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MainBlue)
            }
        }
        is DailyQuizUiState.Success -> {
            currentQuizItem = QuizItem(
                (dailyQuizState as DailyQuizUiState.Success).quiz.question,
                (dailyQuizState as DailyQuizUiState.Success).quiz.answer
            )
            
            BaseQuizScreen(
                title = "데일리 퀴즈",
                quizType = QuizType.DAILY,
                currentQuiz = currentQuizItem,
                onBackClick = onBackClick,
                onQuizResult = { result ->
                    quizResult = result
                    showResult = true
                }
            )
        }
        is DailyQuizUiState.Error -> {
            BaseQuizScreen(
                title = "데일리 퀴즈",
                quizType = QuizType.DAILY,
                currentQuiz = currentQuizItem,
                onBackClick = onBackClick,
                onQuizResult = { result ->
                    quizResult = result
                    showResult = true
                }
            )
        }
    }
    
    quizResult?.let { result ->
        if (showResult) {
            DailyQuizResultDialog(
                isCorrect = result.isCorrect,
                rank = result.rank ?: 1,
                reward = result.reward ?: 0,
                onDismiss = { showResult = false },
                onReceiveReward = { 
                    // 보상 수령 로직
                    showResult = false 
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DailyQuizScreenPreview() {
    LagoTheme {
        DailyQuizScreen()
    }
}