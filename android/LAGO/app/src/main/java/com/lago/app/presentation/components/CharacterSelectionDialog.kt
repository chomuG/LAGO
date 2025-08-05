package com.lago.app.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.lago.app.R
import com.lago.app.presentation.theme.*
import kotlinx.coroutines.launch

// Character data model
data class CharacterInfo(
    val id: String,
    val name: String,
    val subtitle: String,
    val hashtag: String,
    val drawableRes: Int,
    val textColor: Color,
    val tagColor: Color,
    val cardColor: Color
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CharacterSelectionDialog(
    onDismiss: () -> Unit,
    onConfirm: (CharacterInfo) -> Unit
) {
    // Character data
    val characters = remember {
        listOf(
            CharacterInfo(
                id = "화끈이",
                name = "화끈이",
                subtitle = "위험? 수익?\n짜릿해!!",
                hashtag = "#하이리스크하이리턴",
                drawableRes = R.drawable.character_red,
                textColor = Color(0xFFF94E2C),
                tagColor = Color(0xFFDF2323),
                cardColor = Color(0xFFFCEDEB)
            ),
            CharacterInfo(
                id = "적극이",
                name = "적극이",
                subtitle = "위험도 수익도\n모두 챙겨요!",
                hashtag = "#적극투자 #리스크관리",
                drawableRes = R.drawable.character_yellow,
                textColor = Color(0xFFF8AE54),
                tagColor = Color(0xFFD87607),
                cardColor = Color(0xFFFFFDF6)
            ),
            CharacterInfo(
                id = "균형이",
                name = "균형이",
                subtitle = "수익도 중요,\n안정성도 중요!",
                hashtag = "#분산투자 #중장기관점",
                drawableRes = R.drawable.character_blue,
                textColor = Color(0xFF78B6FD),
                tagColor = BlueDark,
                cardColor = Color(0xFFF4FAFF)
            ),
            CharacterInfo(
                id = "조심이",
                name = "조심이",
                subtitle = "안전한 자산으로,\n착실하게!",
                hashtag = "#예금우선 #리스크회피",
                drawableRes = R.drawable.character_green,
                textColor = Color(0xFF8EC53C),
                tagColor = Color(0xFF49A10F),
                cardColor = Color(0xFFF7FFF9)
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { characters.size })
    var selectedCharacterIndex by remember { mutableStateOf(-1) } // -1은 아무것도 선택되지 않음
    val coroutineScope = rememberCoroutineScope()
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "누구의 기록을 확인할까요?",
                    style = TitleB24,
                    color = Gray900,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Character Pager
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(158.dp)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        pageSpacing = 0.dp
                    ) { page ->
                        val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                        val alpha by animateFloatAsState(
                            targetValue = if (pageOffset == 0f) 1f else 0.3f,
                            label = "alpha"
                        )
                        
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            // Card without check icon
                            CharacterCardOnly(
                                character = characters[page],
                                isChecked = selectedCharacterIndex == page,
                                modifier = Modifier
                                    .width(263.dp)
                                    .height(158.dp)
                                    .alpha(alpha)
                            )
                            
                            // Check Icon positioned at card's top-right corner
                            // Card is 263x158, centered in the Box
                            // So card's right edge is at center + 131.5dp from center
                            // Icon center should be at card's right edge, so offset from center alignment
                            IconButton(
                                onClick = { 
                                    selectedCharacterIndex = if (selectedCharacterIndex == page) -1 else page
                                },
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .offset(x = 131.5.dp, y = (-79).dp ) // x: 카드 우측 끝, y: 카드 상단 + 아이콘 반지름
                                    .size(41.dp)
                                    .zIndex(1f)
                                    .alpha(alpha)
                            ) {
                                Image(
                                    painter = painterResource(
                                        id = if (selectedCharacterIndex == page) R.drawable.ic_check_circle_dark 
                                        else R.drawable.ic_check_circle_light
                                    ),
                                    contentDescription = "Check",
                                    modifier = Modifier.size(41.dp)
                                )
                            }
                        }
                    }

                    // Left Arrow
                    if (pagerState.currentPage > 0) {
                        IconButton(
                            onClick = { 
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .size(27.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_chevron_left),
                                contentDescription = "Previous",
                                modifier = Modifier.size(27.dp)
                            )
                        }
                    }

                    // Right Arrow
                    if (pagerState.currentPage < characters.size - 1) {
                        IconButton(
                            onClick = { 
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(27.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_chevron_right),
                                contentDescription = "Next",
                                modifier = Modifier.size(27.dp)
                            )
                        }
                    }
                }

                // Page Indicator
                Row(
                    modifier = Modifier.padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(characters.size) { index ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == pagerState.currentPage) Gray600
                                    else Gray300
                                )
                        )
                    }
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Gray100
                        )
                    ) {
                        Text(
                            text = "취소",
                            style = SubtitleSb16,
                            color = Gray600
                        )
                    }

                    // Apply Button
                    Button(
                        onClick = { 
                            if (selectedCharacterIndex >= 0) {
                                onConfirm(characters[selectedCharacterIndex])
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MainBlue
                        )
                    ) {
                        Text(
                            text = "적용",
                            style = SubtitleSb16,
                            color = Color(0xFFFFFFFF)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CharacterCard(
    character: CharacterInfo,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Card (메인 카드)
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = character.cardColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Character Image (좌측)
                Image(
                    painter = painterResource(id = character.drawableRes),
                    contentDescription = character.name,
                    modifier = Modifier.size(100.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Text Content (우측)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Character Name
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = character.textColor)) {
                                append(character.name)
                            }
                        },
                        style = HeadEb18
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Subtitle
                    Text(
                        text = character.subtitle,
                        style = TitleB18,
                        color = Gray900
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Hashtag
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = character.tagColor)) {
                                append(character.hashtag)
                            }
                        },
                        style = BodyR14
                    )
                }
            }
        }

        // Check Icon (카드 위에 z축으로 올라가서 우측 상단 끝점에 중심 맞춤)
        IconButton(
            onClick = { onCheckedChange(!isChecked) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 20.5.dp, y = (-20.5).dp)
                .size(41.dp)
                .zIndex(1f)
        ) {
            Image(
                painter = painterResource(
                    id = if (isChecked) R.drawable.ic_check_circle_dark 
                    else R.drawable.ic_check_circle_light
                ),
                contentDescription = "Check",
                modifier = Modifier.size(41.dp)
            )
        }
    }
}

@Composable
fun CharacterCardOnly(
    character: CharacterInfo,
    isChecked: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = character.cardColor),
        border = if (isChecked) BorderStroke(2.dp, Color.Black) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Character Image (좌측)
            Image(
                painter = painterResource(id = character.drawableRes),
                contentDescription = character.name,
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Text Content (우측)
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                // Character Name
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = character.textColor)) {
                            append(character.name)
                        }
                    },
                    style = HeadEb18
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Subtitle
                Text(
                    text = character.subtitle,
                    style = TitleB18,
                    color = Gray900
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Hashtag
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = character.tagColor)) {
                            append(character.hashtag)
                        }
                    },
                    style = BodyR14
                )
            }
        }
    }
}


// Usage example
@Composable
fun CharacterSelectionScreen() {
    var showDialog by remember { mutableStateOf(false) }
    
    if (showDialog) {
        CharacterSelectionDialog(
            onDismiss = { showDialog = false },
            onConfirm = { character ->
                // Handle character selection
                println("Selected character: ${character.name}")
                showDialog = false
            }
        )
    }
    
    // Button to show dialog
    Button(
        onClick = { showDialog = true },
        modifier = Modifier.padding(16.dp)
    ) {
        Text("Select Character")
    }
}