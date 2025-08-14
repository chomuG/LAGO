package com.lago.app.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.lago.app.R
import com.lago.app.presentation.theme.*
import kotlinx.coroutines.launch
import kotlin.math.min

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
    selectedAI: com.lago.app.domain.entity.SignalSource? = null,
    onDismiss: () -> Unit,
    onConfirm: (CharacterInfo) -> Unit,
    onClearSelection: () -> Unit = {}
) {
    // Screen dimensions for responsive design
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    
    // Adaptive dimensions based on screen size
    val isCompactScreen = screenWidth < 400.dp || screenHeight < 700.dp
    val isLargeScreen = screenWidth > 600.dp
    
    // Dynamic sizing
    val dialogPadding = if (isCompactScreen) Spacing.md else Spacing.lg
    val cardPadding = if (isCompactScreen) Spacing.md else Spacing.lg
    val cardHeight = with(density) { 
        min(screenHeight.toPx() * 0.2f, 158.dp.toPx()).toDp()
    }
    val cardWidth = with(density) {
        min(screenWidth.toPx() * 0.8f, 263.dp.toPx()).toDp()
    }
    val imageSize = if (isCompactScreen) 80.dp else if (isLargeScreen) 120.dp else 100.dp
    val buttonHeight = if (isCompactScreen) 44.dp else 50.dp
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
                .padding(horizontal = dialogPadding),
            shape = RoundedCornerShape(Radius.lg),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(cardPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "누구의 기록을 확인할까요?",
                    style = if (isCompactScreen) TitleB18 else TitleB24,
                    color = Gray900,
                    modifier = Modifier.padding(bottom = if (isCompactScreen) Spacing.lg else Spacing.xl),
                    textAlign = TextAlign.Center
                )

                // Character Pager
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cardHeight)
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
                            // Card with click functionality
                            CharacterCardOnly(
                                character = characters[page],
                                isChecked = selectedCharacterIndex == page,
                                imageSize = imageSize,
                                isCompact = isCompactScreen,
                                onClick = { 
                                    selectedCharacterIndex = if (selectedCharacterIndex == page) -1 else page
                                },
                                modifier = Modifier
                                    .width(cardWidth)
                                    .height(cardHeight)
                                    .alpha(alpha)
                            )
                            
                            // Check Icon positioned at card's top-right corner
                            // Card is 263x158, centered in the Box
                            // So card's right edge is at center + 131.5dp from center
                            // Icon center should be at card's right edge, so offset from center alignment
                            val checkIconSize = if (isCompactScreen) 32.dp else 41.dp
                            val offsetX = cardWidth / 2
                            val offsetY = -cardHeight / 2
                            
                            IconButton(
                                onClick = { 
                                    selectedCharacterIndex = if (selectedCharacterIndex == page) -1 else page
                                },
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .offset(x = offsetX, y = offsetY)
                                    .size(checkIconSize)
                                    .zIndex(1f)
                                    .alpha(alpha)
                            ) {
                                Image(
                                    painter = painterResource(
                                        id = if (selectedCharacterIndex == page) R.drawable.ic_check_circle_dark 
                                        else R.drawable.ic_check_circle_light
                                    ),
                                    contentDescription = "Check",
                                    modifier = Modifier.size(checkIconSize)
                                )
                            }
                        }
                    }

                    // Chevron navigation removed - users can swipe to navigate between characters
                }

                // Page Indicator
                Row(
                    modifier = Modifier.padding(vertical = if (isCompactScreen) Spacing.md else Spacing.lg),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(characters.size) { index ->
                        val indicatorSize = if (isCompactScreen) 6.dp else 8.dp
                        Box(
                            modifier = Modifier
                                .padding(horizontal = Spacing.xs)
                                .size(indicatorSize)
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
                    horizontalArrangement = Arrangement.spacedBy(if (isCompactScreen) Spacing.sm else Spacing.md)
                ) {
                    // Cancel Button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(buttonHeight),
                        shape = RoundedCornerShape(Radius.sm),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Gray100
                        )
                    ) {
                        Text(
                            text = "취소",
                            style = if (isCompactScreen) BodyR14 else SubtitleSb16,
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
                            .height(buttonHeight),
                        shape = RoundedCornerShape(Radius.sm),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MainBlue
                        )
                    ) {
                        Text(
                            text = "적용",
                            style = if (isCompactScreen) BodyR14 else SubtitleSb16,
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

                    // Subtitle
                    Text(
                        text = character.subtitle,
                        style = TitleB18,
                        color = Gray900,
                        lineHeight = 16.sp
                    )

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
    imageSize: androidx.compose.ui.unit.Dp = 100.dp,
    isCompact: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = character.cardColor),
        border = if (isChecked) BorderStroke(2.dp, Color.Black) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isCompact) Spacing.sm else Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Character Image (좌측 영역)
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = character.drawableRes),
                    contentDescription = character.name,
                    modifier = Modifier.size(imageSize)
                )
            }

            // Text Content (우측 영역)
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceBetween
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


                // Subtitle
                Text(
                    text = character.subtitle,
                    style = TitleB18,
                    color = Gray900
                )

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


@Preview(showBackground = true)
@Composable
fun CharacterSelectionDialogPreview() {
    CharacterSelectionDialog(
        onDismiss = { },
        onConfirm = { }
    )
}

@Preview(showBackground = true)
@Composable
fun CharacterCardOnlyPreview() {
    val sampleCharacter = CharacterInfo(
        id = "화끈이",
        name = "화끈이",
        subtitle = "위험? 수익?\n짜릿해!!",
        hashtag = "#하이리스크하이리턴",
        drawableRes = R.drawable.character_red,
        textColor = Color(0xFFF94E2C),
        tagColor = Color(0xFFDF2323),
        cardColor = Color(0xFFFCEDEB)
    )
    
    CharacterCardOnly(
        character = sampleCharacter,
        isChecked = false,
        imageSize = 100.dp,
        isCompact = false,
        onClick = { }
    )
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