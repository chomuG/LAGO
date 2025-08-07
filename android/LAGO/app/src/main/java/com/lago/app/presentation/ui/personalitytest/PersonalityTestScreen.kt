package com.lago.app.presentation.ui.personalitytest

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lago.app.R
import com.lago.app.presentation.theme.*
import com.lago.app.presentation.ui.components.CommonTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalityTestScreen(
    nickname: String = "",
    onBackClick: () -> Unit = {},
    onTestComplete: (Int) -> Unit = {}
) {
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswers by remember { mutableStateOf(mutableMapOf<Int, Int>()) }
    var selectedOptionIndex by remember { mutableStateOf(-1) }
    
    val questions = PersonalityTestData.questions
    val currentQuestion = questions[currentQuestionIndex]
    val isLastQuestion = currentQuestionIndex == questions.size - 1
    
    val progress by animateFloatAsState(
        targetValue = (currentQuestionIndex + 1) / questions.size.toFloat(),
        animationSpec = tween(300),
        label = "progress"
    )

    LaunchedEffect(currentQuestionIndex) {
        selectedOptionIndex = selectedAnswers[currentQuestion.id] ?: -1
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        CommonTopAppBar(
            title = "성향 테스트",
            onBackClick = onBackClick
        )

        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            color = MainBlue,
            trackColor = Color(0xFFE5E5E5)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${nickname}님의 투자 성향을\n알아보는 시간이에요!",
                    style = HeadEb24,
                    color = Color.Black
                )
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MainBlue.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "${currentQuestionIndex + 1}/${questions.size}",
                        style = TitleB14,
                        color = MainBlue,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF8F9FA)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = currentQuestion.question,
                    style = TitleB18,
                    color = Color.Black,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 선택지들
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                currentQuestion.options.forEachIndexed { index, option ->
                    AnswerOption(
                        text = "${index + 1}. ${option.text}",
                        isSelected = selectedOptionIndex == index,
                        onClick = {
                            selectedOptionIndex = index
                            selectedAnswers[currentQuestion.id] = option.score
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            if (selectedOptionIndex != -1) {
                Button(
                    onClick = {
                        if (isLastQuestion) {
                            val totalScore = selectedAnswers.values.sum()
                            onTestComplete(totalScore)
                        } else {
                            currentQuestionIndex++
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MainBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isLastQuestion) "결과 보기" else "다음",
                        style = TitleB16,
                        color = Color.White
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(
                            color = Gray300,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "답변을 선택해 주세요",
                        style = TitleB16,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun AnswerOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MainBlue else Gray300,
        animationSpec = tween(200),
        label = "border_color"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MainBlue.copy(alpha = 0.1f) else Color.White,
        animationSpec = tween(200),
        label = "background_color"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = borderColor
        )
    ) {
        Text(
            text = text,
            style = SubtitleSb16,
            color = Color.Black,
            modifier = Modifier.padding(20.dp)
        )
    }
}