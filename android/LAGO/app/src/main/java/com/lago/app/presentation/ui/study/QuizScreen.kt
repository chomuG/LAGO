package com.lago.app.presentation.ui.study

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lago.app.R
import com.lago.app.presentation.theme.*

data class QuizItem(
    val question: String,
    val correctAnswer: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    onBackClick: () -> Unit = {},
    onAnswerSelected: (Boolean) -> Unit = {}
) {
    var currentQuiz by remember { mutableStateOf(
        QuizItem(
            question = "주식의 PER이 높으면 기업의 성장 가능성이 높다?",
            correctAnswer = true
        )
    )}
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // Top App Bar
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "랜덤 퀴즈",
                    style = SubtitleSb18,
                    color = Color.Black
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = Color.Black
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = AppBackground
            ),
            modifier = Modifier.align(Alignment.TopCenter)
        )
        
        // Centered Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Question Section - Left aligned
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                // Q. label
                Text(
                    text = "Q.",
                    style = HeadEb28,
                    color = MainBlue,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Question text
                Text(
                    text = currentQuiz.question,
                    style = HeadEb28,
                    color = Color.Black,
                    lineHeight = 32.sp,
                    modifier = Modifier.padding(bottom = 48.dp)
                )
            }
            
            // Answer Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // No Button (X)
                AnswerButton(
                    iconRes = R.drawable.no,
                    text = "아니다",
                    backgroundColor = Color(0xFFFFE3E3),
                    modifier = Modifier.weight(1f),
                    onClick = { onAnswerSelected(false) }
                )
                
                // Yes Button (O)
                AnswerButton(
                    iconRes = R.drawable.yes,
                    text = "그렇다",
                    backgroundColor = BlueLightHover,
                    modifier = Modifier.weight(1f),
                    onClick = { onAnswerSelected(true) }
                )
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

@Preview(showBackground = true)
@Composable
fun QuizScreenPreview() {
    LagoTheme {
        QuizScreen()
    }
}