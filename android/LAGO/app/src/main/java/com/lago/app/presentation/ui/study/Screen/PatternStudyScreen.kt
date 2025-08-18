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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
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
    
    // 더미 데이터 (테스트용 이미지 URL 포함)
    val dummyPatterns = listOf(
        ChartPattern(patternId = 1, name = "헤드 & 숄더", description = "## 더블 탑은 가격이 비슷한 두 꼭대기를 만들고 내려가는 **하락 반전 신호**에요. ## **목선(바닥선)**을 아래로 뚫으면 더 하락할 수 있으니 @@매도 시점@@으로 봐요. ## 목표 가격은 꼭대기와 목선 사이 거리만큼 더 떨어질 수 있어요.", chartImg = "https://picsum.photos/400/300?random=1"),
        ChartPattern(patternId = 2, name = "더블탑", description = "## 더블 탑은 가격이 비슷한 두 꼭대기를 만들고 내려가는 **하락 반전 신호**에요. ## **목선(바닥선)**을 아래로 뚫으면 더 하락할 수 있으니 @@매도 시점@@으로 봐요. ## 목표 가격은 꼭대기와 목선 사이 거리만큼 더 떨어질 수 있어요.", chartImg = "https://picsum.photos/400/300?random=2"),
        ChartPattern(patternId = 3, name = "더블 바텀", description = "## 더블 바텀은 가격이 비슷한 두 바닥을 만들고 올라가는 **상승 반전 신호**에요. ## **목선(고점선)**을 위로 뚫으면 더 상승할 수 있으니 @@매수 시점@@으로 봐요. ## 목표 가격은 바닥과 목선 사이 거리만큼 더 올라갈 수 있어요.", chartImg = "https://picsum.photos/400/300?random=3"),
        ChartPattern(patternId = 4, name = "삼각 수렴", description = "## 삼각 수렴은 가격이 점점 좁아지는 범위에서 움직이는 패턴이에요. ## 상승 삼각형, 하락 삼각형, 대칭 삼각형으로 나뉘며 **돌파 방향**을 예측할 수 있어요. ## 거래량이 줄어들다가 돌파 시점에 급증하는 특징이 있어요.", chartImg = "https://picsum.photos/400/300?random=4"),
        ChartPattern(patternId = 5, name = "웨지 패턴", description = "## 웨지 패턴은 가격이 기울어진 두 선 사이에서 수렴하는 패턴이에요. ## **상승 웨지**는 하락 신호, **하락 웨지**는 상승 신호로 해석해요. ## 삼각 수렴과 비슷하지만 두 선이 모두 같은 방향으로 기울어진 점이 달라요.", chartImg = "https://picsum.photos/400/300?random=5"),
        ChartPattern(patternId = 6, name = "채널 패턴", description = "## 채널 패턴은 가격이 **평행선** 사이에서 규칙적으로 움직이는 패턴이에요. ## 상단선 근처에서 @@매도@@, 하단선 근처에서 @@매수@@하는 전략을 사용해요. ## 채널을 벗어나면 새로운 추세가 시작될 신호로 봐요.", chartImg = "https://picsum.photos/400/300?random=6")
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
            
            // Chart Image (API 데이터 기반 동적 로드)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AppBackground
                )
            ) {
                // 차트 이미지
                ChartPatternImage(
                    imageUrl = currentPattern.chartImg,
                    patternName = currentPattern.name,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
                // Pattern Description
                FormattedDescriptionText(
                    description = currentPattern.description,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Add bottom spacing for better scrolling experience
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun FormattedDescriptionText(
    description: String,
    modifier: Modifier = Modifier
) {
    // ## 으로 구분된 섹션들을 처리
    val sections = description.split("##").filter { it.isNotBlank() }
    
    Column(modifier = modifier) {
        sections.forEachIndexed { index, section ->
            val trimmedSection = section.trim()
            
            if (trimmedSection.isNotEmpty()) {
                // 번호 원 아이콘과 텍스트를 한 행에 배치
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // 번호 원 아이콘
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
                            text = "${index + 1}",
                            style = BodyR12,
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // 포맷된 텍스트
                    Text(
                        text = buildFormattedText(trimmedSection),
                        style = BodyR16,
                        lineHeight = 24.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // 섹션 간 구분선 (마지막 섹션 제외)
                if (index < sections.size - 1) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = Color(0xFFF2F2F2),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun buildFormattedText(text: String) = buildAnnotatedString {
    var currentIndex = 0
    
    // **text** 패턴을 MainBlue로 처리
    val boldPattern = Regex("\\*\\*(.*?)\\*\\*")
    // @@text@@ 패턴을 MainPink로 처리
    val pinkPattern = Regex("@@(.*?)@@")
    
    // 모든 패턴을 찾아서 정렬
    val allMatches = mutableListOf<MatchResult>()
    allMatches.addAll(boldPattern.findAll(text))
    allMatches.addAll(pinkPattern.findAll(text))
    allMatches.sortBy { it.range.first }
    
    allMatches.forEach { matchResult ->
        // 패턴 전 텍스트 추가
        if (matchResult.range.first > currentIndex) {
            append(text.substring(currentIndex, matchResult.range.first))
        }
        
        // 패턴에 따라 스타일 적용
        val isBoldPattern = matchResult.value.startsWith("**")
        val isPinkPattern = matchResult.value.startsWith("@@")
        
        withStyle(
            style = SpanStyle(
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = when {
                    isBoldPattern -> MainBlue
                    isPinkPattern -> MainPink
                    else -> Color.Black
                }
            )
        ) {
            append(matchResult.groupValues[1])
        }
        
        currentIndex = matchResult.range.last + 1
    }
    
    // 남은 텍스트 추가
    if (currentIndex < text.length) {
        append(text.substring(currentIndex))
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
fun ChartPatternImage(
    imageUrl: String,
    patternName: String,
    modifier: Modifier = Modifier
) {
    if (imageUrl.isNotEmpty()) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "${patternName} 차트 패턴",
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    } else {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "이미지 없음",
                style = BodyR14,
                color = Gray400,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PatternStudyScreenPreview() {
    LagoTheme {
        PatternStudyScreen()
    }
}