package com.lago.app.presentation.ui.study.Dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lago.app.R
import com.lago.app.presentation.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DailyQuizResultDialog(
    isCorrect: Boolean,
    rank: Int,
    reward: Int,
    explanation: String = "",
    onDismiss: () -> Unit = {},
    onReceiveReward: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = ShadowColor,
                    ambientColor = ShadowColor
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // 보상 금액 표시 (정답일 때만)
                if (isCorrect && reward > 0) {
                    Card(
                        modifier = Modifier.padding(bottom = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp, 
                            MainBlue
                        )
                    ) {
                        Text(
                            text = "+${NumberFormat.getNumberInstance(Locale.KOREA).format(reward)}원",
                            style = SubtitleSb16,
                            color = MainBlue,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
                
                // 등수 표시
                Text(
                    text = if (isCorrect) "${rank}등으로\n정답을 맞췄어요." else "아쉽게도\n틀렸어요.",
                    style = HeadEb28,
                    color = if (isCorrect) MainBlue else Color(0xFFFF6669),
                    textAlign = TextAlign.Left,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // 메달 아이콘
                if (isCorrect) {
                    val medalIcon = when (rank) {
                        1 -> R.drawable.first_place_medal
                        2 -> R.drawable.second_place_medal
                        3 -> R.drawable.third_place_medal
                        else -> R.drawable.troph // 4등부터는 모두 트로피
                    }
                    
                    Image(
                        painter = painterResource(id = medalIcon),
                        contentDescription = "${rank}등 메달",
                        modifier = Modifier
                            .size(132.dp)
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 32.dp)
                    )
                } else {
                    // 틀렸을 때 슬픈 표정 아이콘
                    Image(
                        painter = painterResource(id = R.drawable.sad_face),
                        contentDescription = "틀림",
                        modifier = Modifier
                            .size(132.dp)
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 32.dp)
                    )
                }
                
                // 설명 텍스트 (API에서 받은 설명이 있을 때만 표시)
                if (explanation.isNotEmpty()) {
                    Text(
                        text = explanation,
                        style = BodyR14,
                        color = Gray700,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Left,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                }
                
                // 보상 받기 버튼
                Button(
                    onClick = onReceiveReward,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCorrect) MainBlue else Color(0xFFFF6669)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isCorrect) "${NumberFormat.getNumberInstance(Locale.KOREA).format(reward)}원 받기" else "확인",
                        style = SubtitleSb16,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DailyQuizResultDialogFirstPreview() {
    LagoTheme {
        DailyQuizResultDialog(
            isCorrect = true,
            rank = 1,
            reward = 100000
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DailyQuizResultDialogSecondPreview() {
    LagoTheme {
        DailyQuizResultDialog(
            isCorrect = true,
            rank = 2,
            reward = 50000
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DailyQuizResultDialogOutOfRankPreview() {
    LagoTheme {
        DailyQuizResultDialog(
            isCorrect = true,
            rank = 341,
            reward = 2000
        )
    }
}