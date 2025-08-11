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
                // 보상 금액 표시
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
                
                // 등수 표시
                Text(
                    text = "${rank}등으로\n정답을 맞췄어요.",
                    style = HeadEb28,
                    color = MainBlue,
                    textAlign = TextAlign.Left,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // 메달 아이콘
                val medalIcon = when {
                    rank == 1 -> R.drawable.first_place_medal
                    rank in 2..3 -> R.drawable.second_place_medal
                    rank <= 10 -> R.drawable.third_place_medal
                    else -> R.drawable.troph
                }
                
                Image(
                    painter = painterResource(id = medalIcon),
                    contentDescription = "${rank}등 메달",
                    modifier = Modifier
                        .size(132.dp)
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 32.dp)
                )
                
                // 설명 텍스트
                Text(
                    text = "PERO이 높으면 기업의 성장 가능성이 높아요.",
                    style = TitleB20,
                    color = Color.Black,
                    textAlign = TextAlign.Left,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                HorizontalDivider(
                    thickness = 2.dp,
                    color = Color(0xFFF2F2F2),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Text(
                    text = "PERO이 높다는 것은 주가가 주당순이익 대비 높게 형성되어 있다는 의미입니다. 이는 투자자들이 미래 성장을 기대하고 있음을 나타내지만, 동시에 주가가 과대 평가되었을 가능성도 있습니다.",
                    style = BodyR14,
                    color = Gray700,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Left,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                
                // 보상 받기 버튼
                Button(
                    onClick = onReceiveReward,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MainBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${NumberFormat.getNumberInstance(Locale.KOREA).format(reward)}원 받기",
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