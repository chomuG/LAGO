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
import androidx.compose.ui.unit.sp
import com.lago.app.R
import com.lago.app.domain.entity.ChartPattern
import com.lago.app.presentation.theme.*
import com.lago.app.presentation.ui.components.CommonTopAppBar
import com.lago.app.presentation.viewmodel.ChartPatternsUiState
import com.lago.app.presentation.viewmodel.PatternStudyViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternStudyScreen(
    onBackClick: () -> Unit = {},
    viewModel: PatternStudyViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) } // 선택된 상태
    val patternsState by viewModel.patternsState.collectAsStateWithLifecycle()
    
    // 더미 데이터
    val dummyPatterns = listOf(
        ChartPattern(1, "헤드 & 숄더", "헤드&숄더 패턴은 3개의 고점을 기준으로 하는 차트 형태로, 바깥쪽 두 봉우리는 높이가 비슷하고 가운데가 가장 높다.", "head_and_shoulder.jpg"),
        ChartPattern(2, "더블탑", "더블탑 패턴은 두 개의 유사한 고점이 형성되는 차트 패턴입니다.", "double_top.jpg"),
        ChartPattern(3, "더블 바텀", "더블 바텀 패턴은 두 개의 유사한 저점이 형성되는 차트 패턴입니다.", "double_bottom.jpg"),
        ChartPattern(4, "삼각 수렴", "삼각 수렴 패턴은 가격이 점차 좁아지는 범위를 보여주는 패턴입니다.", "triangle.jpg"),
        ChartPattern(5, "웨지 패턴", "웨지 패턴은 가격이 반대 방향으로 기울어진 두 선 사이에서 움직이는 패턴입니다.", "wedge.jpg"),
        ChartPattern(6, "채널 패턴", "채널 패턴은 가격이 두 평행선 사이에서 움직이는 패턴입니다.", "channel.jpg")
    )
    
    LaunchedEffect(Unit) {
        viewModel.loadChartPatterns()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        CommonTopAppBar(
            title = "차트 패턴 학습",
            onBackClick = onBackClick,
            backgroundColor = Color.White
        )


        // Content based on API state
        when (patternsState) {
            is ChartPatternsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MainBlue)
                }
            }
            is ChartPatternsUiState.Success -> {
                val patterns = (patternsState as ChartPatternsUiState.Success).patterns
                PatternContent(
                    patterns = patterns,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
            is ChartPatternsUiState.Error -> {
                Column {
                    // Error notice
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "⚠️ 데이터 로드 실패",
                                style = TitleB16,
                                color = Color.Red
                            )
                            Text(
                                text = "아래는 레이아웃 확인용 더미 데이터입니다",
                                style = BodyR12,
                                color = Gray600
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.loadChartPatterns() },
                                colors = ButtonDefaults.buttonColors(containerColor = MainBlue)
                            ) {
                                Text("다시 시도", color = Color.White)
                            }
                        }
                    }
                    
                    // Dummy data
                    PatternContent(
                        patterns = dummyPatterns,
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )
                }
            }
        }
    }
}

@Composable
fun PatternContent(
    patterns: List<ChartPattern>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Column {
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
                itemsIndexed(patterns) { index, pattern ->
                    TabButton(
                        text = pattern.name,
                        isSelected = selectedTab == index,
                        onClick = { onTabSelected(index) }
                    )
                }
            }
        }
        
        // Scrollable Content
        if (patterns.isNotEmpty() && selectedTab < patterns.size) {
            val currentPattern = patterns[selectedTab]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Pattern Title
                Text(
                    text = "${currentPattern.name} 패턴",
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
            
                // Pattern Description
                Text(
                    text = currentPattern.description,
                    style = BodyR16,
                    color = Color.Black,
                    lineHeight = 24.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Add bottom spacing for better scrolling experience
                Spacer(modifier = Modifier.height(32.dp))
            }
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


@Preview(showBackground = true)
@Composable
fun PatternStudyScreenPreview() {
    LagoTheme {
        PatternStudyScreen()
    }
}