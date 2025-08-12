package com.lago.app.presentation.ui.study.Screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.ui.draw.rotate
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.lago.app.R
import com.lago.app.domain.entity.Term
import com.lago.app.presentation.theme.*
import com.lago.app.presentation.viewmodel.TermsUiState
import com.lago.app.presentation.viewmodel.WordbookViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

enum class StudyStatus {
    CORRECT,    // 정답
    WRONG,      // 오답
    NOT_STUDIED // 미학습
}

data class WordItem(
    val word: String,
    val definition: String,
    val description: String,
    val studyStatus: StudyStatus? = null // null일 수도 있음
)

// Term을 WordItem으로 변환하는 확장 함수
fun Term.toWordItem(): WordItem {
    val status = when (knowStatus) {
        true -> StudyStatus.CORRECT
        false -> StudyStatus.WRONG
        null -> null
    }
    return WordItem(
        word = term,
        definition = definition,
        description = description,
        studyStatus = status
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordbookScreen(
    onBackClick: () -> Unit = {},
    viewModel: WordbookViewModel = hiltViewModel()
) {
    val termsState by viewModel.termsState.collectAsStateWithLifecycle()
    
    
    LaunchedEffect(Unit) {
        viewModel.loadTerms(userId = 1) // 일단 userId 1로 고정
    }
    
    var sortOrder by remember { mutableStateOf("이름순") }
    var searchText by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val sortOptions = listOf("이름순", "정답만", "오답만")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // Top App Bar with Search
        TopAppBar(
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = { 
                            Text(
                                "검색하기...",
                                color = Color.Gray,
                                style = BodyR16
                            )
                        },
                        trailingIcon = if (searchText.isNotEmpty()) {
                            {
                                IconButton(onClick = { searchText = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "검색어 삭제",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        } else null,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(24.dp),
                        textStyle = BodyR16,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Gray300,
                            unfocusedBorderColor = Gray300,
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedPlaceholderColor = Color.Gray,
                            unfocusedPlaceholderColor = Color.Gray
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Search Button
                    IconButton(onClick = { /* search action */ }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "검색",
                            tint = MainBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
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
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = AppBackground
            )
        )
        
        Column {
            // Sort Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "정렬:",
                    color = Gray700,
                    style = SubtitleSb14
                )
                Spacer(modifier = Modifier.width(8.dp))
                
                // Dropdown for sort options
                Box {
                    Row(
                        modifier = Modifier
                            .background(
                                Color.White,
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                Gray300,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { expanded = true }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sortOrder,
                            color = Gray700,
                            style = SubtitleSb14,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "정렬 옵션",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        sortOptions.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option,
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                },
                                onClick = {
                                    sortOrder = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            // Word List based on API state
            when (termsState) {
                is TermsUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MainBlue)
                    }
                }
                is TermsUiState.Success -> {
                    val words = (termsState as TermsUiState.Success).terms.map { it.toWordItem() }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(getFilteredAndSortedWords(words, searchText, sortOrder)) { word ->
                            WordCard(wordItem = word)
                        }
                    }
                }
                is TermsUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
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
                                    text = "네트워크 연결을 확인해주세요",
                                    style = BodyR12,
                                    color = Gray600
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.loadTerms(1) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MainBlue)
                                ) {
                                    Text("다시 시도", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getFilteredAndSortedWords(
    words: List<WordItem>,
    searchText: String,
    sortOrder: String
): List<WordItem> {
    val filtered = words.filter { 
        it.word.contains(searchText, ignoreCase = true) ||
        it.definition.contains(searchText, ignoreCase = true) ||
        it.description.contains(searchText, ignoreCase = true)
    }
    
    return when (sortOrder) {
        "이름순" -> filtered.sortedBy { it.word }
        "정답만" -> filtered.filter { it.studyStatus == StudyStatus.CORRECT }
        "오답만" -> filtered.filter { it.studyStatus == StudyStatus.WRONG }
        else -> filtered
    }
}

@Composable
fun WordCard(
    wordItem: WordItem
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(8.dp),
                spotColor = ShadowColor,
                ambientColor = ShadowColor
            )
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = wordItem.word,
                        style = TitleB24,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = wordItem.definition,
                        style = BodyR14,
                        color = Gray700,
                        lineHeight = 20.sp
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status indicator
                    when (wordItem.studyStatus) {
                        StudyStatus.CORRECT -> {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        color = Color(0xFF74DC89),
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.correct),
                                    contentDescription = "정답",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        StudyStatus.WRONG -> {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        color = Color(0xFFF16767),
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.wrong),
                                    contentDescription = "오답",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        StudyStatus.NOT_STUDIED -> {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        color = Color.Transparent,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                            )
                        }
                        null -> {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        color = Color.Transparent,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                            )
                        }
                    }
                }
            }
            
            // Expanded content (description) with smooth animation
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(
                    animationSpec = tween(
                        durationMillis = 350,
                        easing = FastOutSlowInEasing
                    ),
                    expandFrom = Alignment.Top
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = 350,
                        delayMillis = 50
                    )
                ),
                exit = shrinkVertically(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutLinearInEasing
                    ),
                    shrinkTowards = Alignment.Top
                ) + fadeOut(
                    animationSpec = tween(
                        durationMillis = 250
                    )
                )
            ) {
                Column(
                    modifier = Modifier.animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(
                        color = Gray200,
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = wordItem.description,
                        style = BodyR14,
                        color = Gray600,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WordbookScreenPreview() {
    LagoTheme {
        WordbookScreen()
    }
}