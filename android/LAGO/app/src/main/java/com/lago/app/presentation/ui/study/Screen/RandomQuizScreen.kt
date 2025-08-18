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
import com.lago.app.presentation.viewmodel.QuizSolveUiState

@Composable
fun RandomQuizScreen(
    onBackClick: () -> Unit = {},
    onBackToLearn: () -> Unit = {},
    viewModel: RandomQuizViewModel = hiltViewModel()
) {
    val quizState by viewModel.quizState.collectAsStateWithLifecycle()
    val solveState by viewModel.solveState.collectAsStateWithLifecycle()
    
    var showResult by remember { mutableStateOf(false) }
    var quizResult by remember { mutableStateOf<QuizResult?>(null) }
    var explanation by remember { mutableStateOf<String?>(null) }
    
    // API에서 받아온 퀴즈 데이터
    var currentQuizItem by remember { mutableStateOf<QuizItem?>(null) }
    
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
    
    // 퀴즈 풀이 결과 처리
    LaunchedEffect(solveState) {
        when (solveState) {
            is QuizSolveUiState.Success -> {
                val apiResult = (solveState as QuizSolveUiState.Success).result
                quizResult = QuizResult(isCorrect = apiResult.correct)
                explanation = apiResult.explanation
                showResult = true
            }
            is QuizSolveUiState.Error -> {
                // 오프라인 모드 - 로컬에서 정답 확인
            }
            else -> { /* Loading or Idle */ }
        }
    }
    
    // 퀴즈 화면 표시 (로딩 중에도 표시)
    when (quizState) {
        is RandomQuizUiState.Loading -> {
            LoadingScreen(
                title = "랜덤 퀴즈",
                onBackClick = onBackClick
            )
        }
        is RandomQuizUiState.Success -> {
            currentQuizItem?.let { quiz ->
                BaseQuizScreen(
                    title = "랜덤 퀴즈",
                    quizType = QuizType.RANDOM,
                    currentQuiz = quiz,
                    onBackClick = onBackClick,
                    onAnswerSelected = { userAnswer ->
                        // TODO: 실제 유저 ID 가져오기 (현재는 더미 값 1 사용)
                        viewModel.solveQuiz(userAnswer = userAnswer)
                    }
                )
            }
        }
        is RandomQuizUiState.Error -> {
            // 에러 시에도 기본 화면 표시
            BaseQuizScreen(
                title = "랜덤 퀴즈",
                quizType = QuizType.RANDOM,
                currentQuiz = QuizItem("네트워크 오류가 발생했습니다. 다시 시도해주세요.", true),
                onBackClick = onBackClick
            )
        }
    }
    
    quizResult?.let { result ->
        if (showResult) {
            RandomQuizResultDialog(
                isCorrect = result.isCorrect,
                explanation = explanation,
                onDismiss = { 
                    showResult = false
                    viewModel.resetSolveState()
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