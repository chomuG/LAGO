package com.lago.app.presentation.ui.study.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lago.app.R
import com.lago.app.presentation.theme.*
import com.lago.app.presentation.ui.components.CommonTopAppBar

enum class QuizType {
    DAILY, RANDOM
}

data class QuizItem(
    val question: String,
    val correctAnswer: Boolean
)

data class QuizResult(
    val isCorrect: Boolean,
    val rank: Int? = null,
    val reward: Int? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseQuizScreen(
    title: String,
    quizType: QuizType,
    currentQuiz: QuizItem? = null,
    onBackClick: () -> Unit = {},
    onQuizResult: (QuizResult) -> Unit = {},
    onAnswerSelected: ((Boolean) -> Unit)? = null // 사용자가 선택한 답안 (true/false)
) {
    val defaultQuizList = listOf(
        QuizItem("주식의 PER이 높으면 기업의 성장 가능성이 높다?", true),
        QuizItem("채권의 금리가 상승하면 채권 가격은 하락한다?", true),
        QuizItem("분산투자는 위험을 증가시킨다?", false),
        QuizItem("배당수익률이 높을수록 항상 좋은 투자이다?", false),
        QuizItem("인플레이션은 현금의 가치를 감소시킨다?", true)
    )
    
    var quiz by remember { mutableStateOf(currentQuiz ?: defaultQuizList.random()) }
    
    // currentQuiz가 변경되면 업데이트
    LaunchedEffect(currentQuiz) {
        currentQuiz?.let { quiz = it }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        CommonTopAppBar(
            title = title,
            onBackClick = onBackClick
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Q.",
                    style = HeadEb28,
                    color = MainBlue,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = quiz.question,
                    style = HeadEb28,
                    color = Color.Black,
                    lineHeight = 32.sp,
                    modifier = Modifier.padding(bottom = 48.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                AnswerButton(
                    iconRes = R.drawable.no,
                    text = "아니다",
                    backgroundColor = Color(0xFFFFE3E3),
                    modifier = Modifier.weight(1f),
                    onClick = { 
                        val isCorrect = quiz.correctAnswer == false
                        val userAnswer = false
                        
                        // API 연동이 있는 경우 사용자 답안을 전달
                        if (onAnswerSelected != null) {
                            onAnswerSelected(userAnswer)
                        } else {
                            // 기존 로직 (오프라인 모드)
                            when (quizType) {
                                QuizType.DAILY -> {
                                    val rank = (1..12).random()
                                    val reward = when {
                                        rank == 1 -> 100000
                                        rank <= 3 -> 50000
                                        rank <= 10 -> 10000
                                        else -> 2000
                                    }
                                    onQuizResult(QuizResult(isCorrect, rank, reward))
                                }
                                QuizType.RANDOM -> {
                                    onQuizResult(QuizResult(isCorrect))
                                }
                            }
                        }
                    }
                )
                
                AnswerButton(
                    iconRes = R.drawable.yes,
                    text = "그렇다",
                    backgroundColor = BlueLightHover,
                    modifier = Modifier.weight(1f),
                    onClick = { 
                        val isCorrect = quiz.correctAnswer == true
                        val userAnswer = true
                        
                        // API 연동이 있는 경우 사용자 답안을 전달
                        if (onAnswerSelected != null) {
                            onAnswerSelected(userAnswer)
                        } else {
                            // 기존 로직 (오프라인 모드)
                            when (quizType) {
                                QuizType.DAILY -> {
                                    val rank = (1..12).random()
                                    val reward = when {
                                        rank == 1 -> 100000
                                        rank <= 3 -> 50000
                                        rank <= 10 -> 10000
                                        else -> 2000
                                    }
                                    onQuizResult(QuizResult(isCorrect, rank, reward))
                                }
                                QuizType.RANDOM -> {
                                    onQuizResult(QuizResult(isCorrect))
                                }
                            }
                        }
                    }
                )
                }
            }
        }
    }
}

@Composable
fun AnswerButton(
    iconRes: Int,
    text: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .height(180.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = ShadowColor,
                ambientColor = ShadowColor
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = iconRes),
                contentDescription = text,
                modifier = Modifier.size(68.dp),
                tint = Color.Unspecified
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = text,
                style = HeadEb18,
                color = if (iconRes == R.drawable.no) Color(0xFFFF6669) else MainBlue,
                textAlign = TextAlign.Center
            )
        }
    }
}