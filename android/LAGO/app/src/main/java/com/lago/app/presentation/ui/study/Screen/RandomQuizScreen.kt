package com.lago.app.presentation.ui.study.Screen

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lago.app.domain.entity.Quiz
import com.lago.app.presentation.theme.LagoTheme
import com.lago.app.presentation.ui.study.Dialog.RandomQuizResultDialog
import com.lago.app.presentation.viewmodel.RandomQuizUiState
import com.lago.app.presentation.viewmodel.RandomQuizViewModel

@Composable
fun RandomQuizScreen(
    onBackClick: () -> Unit = {},
    onBackToLearn: () -> Unit = {},
    viewModel: RandomQuizViewModel = hiltViewModel()
) {
    val quizState by viewModel.quizState.collectAsStateWithLifecycle()
    var showResult by remember { mutableStateOf(false) }
    var quizResult by remember { mutableStateOf<QuizResult?>(null) }
    var explanation by remember { mutableStateOf<String?>(null) }
    
    // 기본 퀴즈 아이템을 미리 설정하여 화면 깜빡임 방지
    var currentQuizItem by remember { 
        mutableStateOf(
            QuizItem(
                "주식의 PER이 높다는 것은 그 기업의 성장 가능성을 높게 본다는 뜻이다?", 
                true
            )
        ) 
    }
    
    LaunchedEffect(Unit) {
        viewModel.loadRandomQuiz()
    }
    
    // API 데이터가 성공적으로 로드되면 QuizItem으로 변환
    LaunchedEffect(quizState) {
        when (quizState) {
            is RandomQuizUiState.Success -> {
                val quiz = (quizState as RandomQuizUiState.Success).quiz
                currentQuizItem = QuizItem(quiz.question, quiz.answer)
                explanation = quiz.explanation
            }
            is RandomQuizUiState.Error -> {
                // Mock data fallback (기본값과 동일하게 유지)
                explanation = "PER이 높다는 것은 주가가 주당순이익 대비 높게 형성되어 있다는 의미입니다. 이는 투자자들이 미래 성장을 기대하고 있음을 나타내지만, 동시에 주가가 과대평가되었을 가능성도 있습니다."
            }
            is RandomQuizUiState.Loading -> {
                // Loading 상태에서는 기존 퀴즈 유지 (깜빡임 방지)
            }
        }
    }
    
    // BaseQuizScreen 항상 표시 (null 체크 제거로 깜빡임 방지)
    BaseQuizScreen(
        title = "랜덤 퀴즈",
        quizType = QuizType.RANDOM,
        currentQuiz = currentQuizItem,
        onBackClick = onBackClick,
        onQuizResult = { result ->
            quizResult = result
            showResult = true
        }
    )
    
    quizResult?.let { result ->
        if (showResult) {
            RandomQuizResultDialog(
                isCorrect = result.isCorrect,
                explanation = explanation,
                onDismiss = { 
                    showResult = false
                    onBackToLearn()
                },
                onMoreQuiz = { 
                    viewModel.getNewQuiz()
                    showResult = false 
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RandomQuizScreenPreview() {
    LagoTheme {
        RandomQuizScreen()
    }
}