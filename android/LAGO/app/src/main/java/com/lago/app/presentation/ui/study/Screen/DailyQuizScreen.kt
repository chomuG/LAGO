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
import com.lago.app.presentation.viewmodel.DailyQuizSolveUiState
import com.lago.app.presentation.viewmodel.DailyQuizUiState
import com.lago.app.presentation.viewmodel.DailyQuizViewModel

@Composable
fun DailyQuizScreen(
    onBackClick: () -> Unit = {},
    viewModel: DailyQuizViewModel = hiltViewModel()
) {
    val dailyQuizState by viewModel.uiState.collectAsStateWithLifecycle()
    val solveState by viewModel.solveState.collectAsStateWithLifecycle()
    
    var showResult by remember { mutableStateOf(false) }
    var quizResult by remember { mutableStateOf<QuizResult?>(null) }
    var currentQuizItem by remember { mutableStateOf<QuizItem?>(null) }
    var dailyQuizResult by remember { mutableStateOf<com.lago.app.domain.entity.DailyQuizResult?>(null) }
    
    LaunchedEffect(Unit) {
        viewModel.loadDailyQuiz()
    }
    
    // API 응답 처리
    LaunchedEffect(solveState) {
        when (solveState) {
            is DailyQuizSolveUiState.Success -> {
                val result = (solveState as DailyQuizSolveUiState.Success).result
                dailyQuizResult = result
                quizResult = QuizResult(
                    isCorrect = result.correct,
                    rank = result.ranking,
                    reward = result.bonusAmount
                )
                showResult = true
                viewModel.resetSolveState()
            }
            is DailyQuizSolveUiState.Error -> {
                // 에러 처리 (필요시)
                viewModel.resetSolveState()
            }
            else -> {}
        }
    }

    when(dailyQuizState) {
        is DailyQuizUiState.Loading -> {
            LoadingScreen(
                title = "데일리 퀴즈",
                onBackClick = onBackClick
            )
        }
        is DailyQuizUiState.Success -> {
            val status = (dailyQuizState as DailyQuizUiState.Success).status
            if (status.quiz != null && !status.alreadySolved) {
                currentQuizItem = QuizItem(
                    status.quiz.question,
                    status.quiz.answer
                )
                
                BaseQuizScreen(
                    title = "데일리 퀴즈",
                    quizType = QuizType.DAILY,
                    currentQuiz = currentQuizItem,
                    onBackClick = onBackClick,
                    onAnswerSelected = { userAnswer ->
                        // API 호출
                        viewModel.solveQuiz(userAnswer = userAnswer)
                    },
                    onQuizResult = { result ->
                        quizResult = result
                        showResult = true
                    }
                )
            } else if (status.alreadySolved) {
                // 이미 풀었을 때는 완료 화면 표시
                CompletedQuizScreen(
                    title = "데일리 퀴즈",
                    ranking = status.ranking ?: 0,
                    onBackClick = onBackClick
                )
            }
        }
        is DailyQuizUiState.Error -> {
            BaseQuizScreen(
                title = "데일리 퀴즈",
                quizType = QuizType.DAILY,
                currentQuiz = currentQuizItem,
                onBackClick = onBackClick,
                onAnswerSelected = { userAnswer ->
                    // API 호출
                    viewModel.solveQuiz(userAnswer = userAnswer)
                },
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
                explanation = dailyQuizResult?.explanation ?: "",
                onDismiss = { showResult = false },
                onReceiveReward = { 
                    // 보상 수령 후 뒤로가기
                    showResult = false 
                    onBackClick()
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