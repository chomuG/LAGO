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

@Composable
fun RandomQuizResultDialog(
    isCorrect: Boolean,
    onDismiss: () -> Unit = {},
    onMoreQuiz: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        // Result Card
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
                    if (isCorrect) {
                        // 정답인 경우
                        Text(
                            text = "정답이에요!",
                            style = HeadEb28.copy(
                                color = MainBlue
                            ),
                            textAlign = TextAlign.Left,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Hand clap icon
                        Image(
                            painter = painterResource(id = R.drawable.hand_clap),
                            contentDescription = "박수",
                            modifier = Modifier
                                .size(132.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                        
                    } else {
                        // 오답인 경우
                        Text(
                            text = "오답이에요.",
                            style = HeadEb28.copy(
                                color = Color(0xFFF45052)
                            ),
                            textAlign = TextAlign.Left,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // Sad face icon
                        Image(
                            painter = painterResource(id = R.drawable.sad_face),
                            contentDescription = "슬픈 얼굴",
                            modifier = Modifier
                                .size(132.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                    
                Spacer(modifier = Modifier.height(48.dp))
                
                // 설명 텍스트
                Text(
                    text = "PERO이 늘어날 기업의 성장 가능성이 높아요.",
                    style = TitleB20,
                    color = Color.Black,
                    textAlign = TextAlign.Left,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                HorizontalDivider(
                    thickness = 2.dp,
                    color = Color(0xFFF2F2F2),
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                )
                
                Text(
                    text = "PERO이 늘어나는 것은 주가가 주당순이익 대비 높게 형성되어 있다는 의미입니다. 이는 투자자들이 미래 성장을 기대하고 있음을 나타내지만, 동시에 주가가 과대 평가되었을 가능성도 있습니다.",
                    style = BodyR14,
                    color = Gray700,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Left,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                
                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 그만풀기 버튼
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Gray100
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "그만풀기",
                            style = SubtitleSb16,
                            color = Gray600
                        )
                    }
                    
                    // 문제 더풀기 버튼
                    Button(
                        onClick = onMoreQuiz,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MainBlue
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "문제 더풀기",
                            style = SubtitleSb16,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QuizResultDialogCorrectPreview() {
    LagoTheme {
        RandomQuizResultDialog(isCorrect = true)
    }
}

@Preview(showBackground = true)
@Composable
fun QuizResultDialogWrongPreview() {
    LagoTheme {
        RandomQuizResultDialog(isCorrect = false)
    }
}