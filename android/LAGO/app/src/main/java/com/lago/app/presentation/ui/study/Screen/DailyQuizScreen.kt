package com.lago.app.presentation.ui.study.Screen

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.lago.app.presentation.theme.LagoTheme
import com.lago.app.presentation.ui.study.Dialog.DailyQuizResultDialog

@Composable
fun DailyQuizScreen(
    onBackClick: () -> Unit = {}
) {
    var showResult by remember { mutableStateOf(false) }
    var quizResult by remember { mutableStateOf<QuizResult?>(null) }
    
    BaseQuizScreen(
        title = "데일리 퀴즈",
        quizType = QuizType.DAILY,
        onBackClick = onBackClick,
        onQuizResult = { result ->
            quizResult = result
            showResult = true
        }
    )
    
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