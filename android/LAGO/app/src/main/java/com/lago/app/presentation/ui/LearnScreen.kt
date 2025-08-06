package com.lago.app.presentation.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.lago.app.R
import com.lago.app.presentation.theme.MainBlue
import com.lago.app.presentation.theme.AppBackground
import com.lago.app.presentation.theme.ShadowColor
import com.lago.app.presentation.theme.Gray700
import com.lago.app.presentation.theme.HeadEb18
import com.lago.app.presentation.theme.HeadEb20
import com.lago.app.presentation.theme.HeadEb24
import com.lago.app.presentation.theme.HeadEb28
import com.lago.app.presentation.theme.LagoTheme
import com.lago.app.presentation.theme.SubtitleSb14
import com.lago.app.presentation.theme.SubtitleSb16

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnScreen(
    onRandomQuizClick: () -> Unit = {},
    onDailyQuizClick: () -> Unit = {},
    onPatternStudyClick: () -> Unit = {},
    onWordBookClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 26.dp)
    ) {
        // Top banner card with megaphone
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = ShadowColor,
                    ambientColor = ShadowColor
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MainBlue,
                                Color.White
                            ),
                            startY = -10f,
                            endY = 400f
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onDailyQuizClick() }
            ) {
                // Background elements - positioned first (behind content)
                
                // Left cloud
                Image(
                    painter = painterResource(id = R.drawable.cloud),
                    contentDescription = null,
                    modifier = Modifier
                        .size(97.dp)
                        .align(Alignment.TopStart)
                        .offset(x = (-5).dp, y = (-10).dp)
                )
                
                // Right cloud
                Image(
                    painter = painterResource(id = R.drawable.cloud),
                    contentDescription = null,
                    modifier = Modifier
                        .size(58.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 5.dp, y = 30.dp)
                )
                
                // School building
                Image(
                    painter = painterResource(id = R.drawable.school),
                    contentDescription = null,
                    modifier = Modifier
                        .size(180.dp)
                        .align(Alignment.TopCenter)
                        .offset(x = (-50).dp, y = 0.dp)
                )
                
                // Left tree
                Image(
                    painter = painterResource(id = R.drawable.tree),
                    contentDescription = null,
                    modifier = Modifier
                        .size(75.dp)
                        .align(Alignment.TopStart)
                        .offset(x = (-25).dp, y = 80.dp)
                )
                
                // Right tree
                Image(
                    painter = painterResource(id = R.drawable.tree),
                    contentDescription = null,
                    modifier = Modifier
                        .size(75.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 15.dp, y = 94.dp)
                )

                
                // Content - positioned on top
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    Text(
                        text = "데일리 문제 →",
                        color = Color.Black,
                        style = HeadEb20
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "오늘의 문제를 풀고 투자 상식과 함께\n투자금을 모아보아요!",
                        color = Gray700,
                        style = SubtitleSb14
                    )
                }
                
                // Glowing stars - positioned on top of everything
                Image(
                    painter = painterResource(id = R.drawable.glowing),
                    contentDescription = null,
                    modifier = Modifier
                        .size(22.dp)
                        .align(Alignment.TopCenter)
                        .offset(x = 20.dp, y = 20.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.glowing),
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.CenterStart)
                        .offset(x = 33.dp, y = (-18).dp)
                )
                
                // Megaphone (top layer)
                Image(
                    painter = painterResource(id = R.drawable.megaphone_image),
                    contentDescription = "Megaphone",
                    modifier = Modifier
                        .size(190.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 10.dp, y = (-10).dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Study streak card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(131.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = ShadowColor,
                    ambientColor = ShadowColor
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 10.dp, end = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AnimatedCalendarIcon()
                Column (
                    horizontalAlignment = Alignment.End
                ){
                    Text(
                        text = "34일째",
                        style = HeadEb24,
                        color = MainBlue
                    )
                    Text(
                        text = "연속학습중!",
                        style = HeadEb24,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "내일도 빠짐없이 함께해요",
                        style = SubtitleSb14,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section title
        Text(
            text = "투자 공부하기",
            style = HeadEb20,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Study categories row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StudyCategoryItem(
                icon = R.drawable.random_quiz_image,
                title = "랜덤퀴즈",
                modifier = Modifier.weight(1f),
                onClick = onRandomQuizClick
            )
            Spacer(modifier = Modifier.width(12.dp))
            StudyCategoryItem(
                icon = R.drawable.chart_study_image,
                title = "패턴학습",
                modifier = Modifier.weight(1f),
                onClick = onPatternStudyClick
            )
            Spacer(modifier = Modifier.width(12.dp))
            StudyCategoryItem(
                icon = R.drawable.wordbook_image,
                title = "투자단어장",
                modifier = Modifier.weight(1f),
                onClick = onWordBookClick
            )
        }

    }
}

@Composable
fun AnimatedCalendarIcon() {
    // Infinite bounce animation
    val infiniteTransition = rememberInfiniteTransition(label = "calendar_bounce")
    
    val bounceY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce_y"
    )

    Image(
        painter = painterResource(id = R.drawable.calendar),
        contentDescription = "Calendar",
        modifier = Modifier
            .size(100.dp)
            .rotate(-12.11F)
            .offset(y = bounceY.dp)
    )
}

@Composable
fun StudyCategoryItem(
    icon: Int,
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = CircleShape,
                    ambientColor = ShadowColor,
                    spotColor = ShadowColor
                )
                .clip(CircleShape)
                .background(Color.White)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = title,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center)
                    .offset(y = (-8).dp)
            )

            Image(
                painter = painterResource(id = R.drawable.ellipse_104),
                contentDescription = null,
                modifier = Modifier
                    .size(34.dp)
                    .offset(y = 26.dp)
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = title,
            style = HeadEb18,
            color = Color.Black
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LearnScreenPreview() {
    LagoTheme {
        LearnScreen()
    }
}