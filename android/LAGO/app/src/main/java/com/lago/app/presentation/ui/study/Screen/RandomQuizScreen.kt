package com.lago.app.presentation.ui.study.Screen

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.lago.app.presentation.theme.LagoTheme
import com.lago.app.presentation.ui.study.Dialog.RandomQuizResultDialog

@Composable
fun RandomQuizScreen(
    onBackClick: () -> Unit = {}
) {
    var showResult by remember { mutableStateOf(false) }
    var quizResult by remember { mutableStateOf<QuizResult?>(null) }
    var currentQuizList by remember { mutableStateOf(getRandomQuizList()) }
    
    BaseQuizScreen(
        title = "랜덤 퀴즈",
        quizType = QuizType.RANDOM,
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
                onDismiss = { showResult = false },
                onMoreQuiz = { 
                    currentQuizList = getRandomQuizList()
                    showResult = false 
                }
            )
        }
    }
}

private fun getRandomQuizList(): List<QuizItem> {
    return listOf(
        QuizItem("주식의 PER이 높으면 기업의 성장 가능성이 높다?", true),
        QuizItem("채권의 금리가 상승하면 채권 가격은 하락한다?", true),
        QuizItem("분산투자는 위험을 증가시킨다?", false),
        QuizItem("배당수익률이 높을수록 항상 좋은 투자이다?", false),
        QuizItem("인플레이션은 현금의 가치를 감소시킨다?", true)
    )
}

@Preview(showBackground = true)
@Composable
fun RandomQuizScreenPreview() {
    LagoTheme {
        RandomQuizScreen()
    }
}