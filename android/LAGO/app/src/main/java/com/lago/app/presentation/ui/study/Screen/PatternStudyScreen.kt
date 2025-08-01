package com.lago.app.presentation.ui.study.Screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lago.app.R
import com.lago.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternStudyScreen(
    onBackClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(1) } // 선택된 상태
    val tabItems = listOf("헤드 & 숄더", "더블탑", "더블 바텀", "삼각 수렴", "웨지 패턴", "채널 패턴")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "차트 패턴 학습",
                    style = HeadEb20,
                    color = Color.Black
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.White
            )
        )


        // Fixed Tab Row
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = AppBackground
            ),
            shape = RoundedCornerShape(0.dp)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                itemsIndexed(tabItems) { index, item ->
                    TabButton(
                        text = item,
                        isSelected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
        
        // Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Pattern Title
            Text(
                text = "${tabItems[selectedTab]} 패턴",
                style = HeadEb24,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Chart Image
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AppBackground
                )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.double_top_chart),
                    contentDescription = "Double Top Chart Pattern",
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Information Cards
            InfoCard(
                number = "1",
                description = "**더블 탑**은 강저히 비슷한 두 꼭대기를 만들고 내려가는 **하락 반전 신호**에요."
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Divider Line
            HorizontalDivider(
                thickness = 1.dp,
                color = Gray300
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            InfoCard(
                number = "2",
                description = "**목선(바닥선)**을 아래로 뚫으면 더 하락할 수 있으니 **매도 시점**으로 봐요."
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Divider Line
            HorizontalDivider(
                thickness = 1.dp,
                color = Gray300
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            InfoCard(
                number = "3",
                description = "목표 가격은 꼭대기와 목선 사이 거리만큼 더 떨어질 수 있어요."
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                thickness = 1.dp,
                color = Gray300
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "차트 설명은 TradingView 공식 자료를 참고하여 작성하였습니다. " +
                        "\n © TradingView, Inc.",
                style = BodyR12,
                color = Gray400,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
            )

            // Add bottom spacing for better scrolling experience
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) BlueLight else Color.White,
            contentColor = if (isSelected) MainBlue else Gray600
        ),
        modifier = Modifier
            .height(45.dp)
            .border(
                width = 2.dp,
                color = if (isSelected) MainBlue else Gray300,
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Text(
            text = text,
            style = if (isSelected) TitleB16 else BodyR16
        )
    }
}

@Composable
fun InfoCard(
    number: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppBackground
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Number Circle
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = MainBlue,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number,
                    style = BodyR12,
                    color = Color.White
                )
            }
            
            // Text Content
            Column {
                Text(
                    text = parseStyledText(description),
                    style = BodyR16
                )
            }
        }
    }
}

@Composable
fun parseStyledText(text: String) = buildAnnotatedString {
    val parts = text.split("**")
    var isHighlight = false
    
    for (part in parts) {
        if (isHighlight) {
            withStyle(
                style = SpanStyle(
                    color = MainBlue,
                    fontWeight = TitleB16.fontWeight
                )
            ) {
                append(part)
            }
        } else {
            withStyle(
                style = SpanStyle(
                    color = Color.Black,
                    fontWeight = BodyR16.fontWeight
                )
            ) {
                append(part)
            }
        }
        isHighlight = !isHighlight
    }
}

@Preview(showBackground = true)
@Composable
fun PatternStudyScreenPreview() {
    LagoTheme {
        PatternStudyScreen()
    }
}