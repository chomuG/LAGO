package com.lago.app.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lago.app.presentation.theme.MainBlue
import com.lago.app.presentation.theme.AppBackground
import com.lago.app.presentation.theme.ShadowColor
import com.lago.app.presentation.theme.TitleB18
import com.lago.app.presentation.theme.BodyR14
import com.lago.app.presentation.theme.TitleB24
import com.lago.app.presentation.theme.BodyR18
import com.lago.app.presentation.theme.BodyR12
import com.lago.app.presentation.theme.Gray700
import com.lago.app.presentation.theme.HeadEb24
import com.lago.app.presentation.theme.HeadEb28
import com.lago.app.presentation.theme.LagoTheme
import com.lago.app.presentation.theme.SubtitleSb16

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 24.dp, vertical = 26.dp)
    ) {
        // Top banner card with megaphone
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
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
                            startY = -300f,  // 더 위에서 시작
//                            endY = 600f
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.align(Alignment.BottomStart)
                ) {
                    Text(
                        text = "데일리 문제 →",
                        color = Color.Black,
                        style = HeadEb24
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "오늘의 문제를 풀고 투자 상식과 함께\n투자금을 모아보아요!",
                        color = Gray700,
                        style = SubtitleSb16
                    )
                }
                Image(
                    painter = painterResource(id = com.lago.app.R.drawable.megaphone_image),
                    contentDescription = "Megaphone",
                    modifier = Modifier
                        .size(190.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 10.dp, y = (-10).dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Study streak card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
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
                    .padding(start = 10.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Image(
                    painter = painterResource(id = com.lago.app.R.drawable.calendar_image),
                    contentDescription = "Calendar",
                    modifier = Modifier.size(100.dp)
                )
                Column (
                    horizontalAlignment = Alignment.End
                ){
                    Text(
                        text = "34일째",
                        style = HeadEb28,
                        color = MainBlue
                    )
                    Text(
                        text = "연속학습중!",
                        style = HeadEb28,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "내일도 빠짐없이 함께해요",
                        style = SubtitleSb16,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section title
        Text(
            text = "투자 공부하기",
            style = TitleB18,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Study categories row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StudyCategoryItem(
                icon = com.lago.app.R.drawable.random_quiz_image,
                title = "랜덤퀴즈",
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            StudyCategoryItem(
                icon = com.lago.app.R.drawable.chart_study_image,
                title = "패턴학습",
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            StudyCategoryItem(
                icon = com.lago.app.R.drawable.wordbook_image,
                title = "투자단어장",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StudyCategoryItem(
    icon: Int,
    title: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .size(80.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = ShadowColor,
                    ambientColor = ShadowColor
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = title,
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = BodyR12,
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